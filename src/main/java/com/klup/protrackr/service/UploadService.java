package com.klup.protrackr.service;

import com.klup.protrackr.domain.Project;
import com.klup.protrackr.domain.ProjectMedia;
import com.klup.protrackr.domain.User;
import com.klup.protrackr.dto.upload.ProjectMediaDto;
import com.klup.protrackr.dto.user.UserDto;
import com.klup.protrackr.exception.BadRequestException;
import com.klup.protrackr.exception.NotFoundException;
import com.klup.protrackr.mapper.DtoMapper;
import com.klup.protrackr.repo.ProjectMediaRepository;
import com.klup.protrackr.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class UploadService {
    private final Path uploadDir;
    private final String publicBaseUrl;
    private final ProjectService projectService;
    private final UserService userService;
    private final PortfolioService portfolioService;
    private final ProjectMediaRepository projectMediaRepository;

    public UploadService(
            @Value("${app.upload.dir}") String uploadDir,
            @Value("${app.upload.public-base-url}") String publicBaseUrl,
            ProjectService projectService,
            UserService userService,
            PortfolioService portfolioService,
            ProjectMediaRepository projectMediaRepository
    ) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.publicBaseUrl = publicBaseUrl.startsWith("/") ? publicBaseUrl : "/" + publicBaseUrl;
        this.projectService = projectService;
        this.userService = userService;
        this.portfolioService = portfolioService;
        this.projectMediaRepository = projectMediaRepository;
    }

    @Transactional
    public UserDto uploadAvatar(UserPrincipal principal, MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BadRequestException("File is required");
        User u = userService.requireCurrentUserEntity(principal);

        String cleanName = sanitizeFilename(file.getOriginalFilename());
        String storedName = UUID.randomUUID() + "_" + cleanName;
        Path targetDir = uploadDir.resolve("avatars").resolve(String.valueOf(u.getId()));
        Path target = targetDir.resolve(storedName);

        writeFile(targetDir, target, file);

        String url = publicBaseUrl + "/avatars/" + u.getId() + "/" + storedName;
        u.setAvatarUrl(url);
        return DtoMapper.userDto(u);
    }

    @Transactional
    public java.util.Map<String, Object> uploadResume(UserPrincipal principal, MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BadRequestException("File is required");
        User u = userService.requireCurrentUserEntity(principal);

        String cleanName = sanitizeFilename(file.getOriginalFilename());
        String storedName = UUID.randomUUID() + "_" + cleanName;
        Path targetDir = uploadDir.resolve("resumes").resolve(String.valueOf(u.getId()));
        Path target = targetDir.resolve(storedName);

        writeFile(targetDir, target, file);

        String url = publicBaseUrl + "/resumes/" + u.getId() + "/" + storedName;
        portfolioService.setResumeUrl(principal, url);
        return java.util.Map.of("resumeUrl", url);
    }

    @Transactional(readOnly = true)
    public List<ProjectMediaDto> listProjectMedia(UserPrincipal principal, Long projectId) {
        Project p = projectService.getProjectForTeamAccess(principal, projectId);
        return projectMediaRepository.findByProjectIdOrderByCreatedAtDesc(p.getId())
                .stream()
                .map(DtoMapper::projectMediaDto)
                .toList();
    }

    @Transactional
    public ProjectMediaDto uploadProjectMedia(UserPrincipal principal, Long projectId, MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BadRequestException("File is required");
        Project p = projectService.getProjectForTeamAccess(principal, projectId);
        User uploader = userService.requireCurrentUserEntity(principal);

        String cleanName = sanitizeFilename(file.getOriginalFilename());
        String storedName = UUID.randomUUID() + "_" + cleanName;
        Path targetDir = uploadDir.resolve("projects").resolve(String.valueOf(p.getId()));
        Path target = targetDir.resolve(storedName);

        writeFile(targetDir, target, file);

        String url = publicBaseUrl + "/projects/" + p.getId() + "/" + storedName;

        ProjectMedia media = new ProjectMedia();
        media.setProject(p);
        media.setUploader(uploader);
        media.setFileUrl(url);
        media.setFileName(cleanName);
        media.setFileType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());

        return DtoMapper.projectMediaDto(projectMediaRepository.save(media));
    }

    @Transactional
    public void deleteMedia(UserPrincipal principal, Long mediaId) {
        ProjectMedia media = projectMediaRepository.findById(mediaId)
                .orElseThrow(() -> new NotFoundException("Media not found"));
        projectService.getProjectForTeamAccess(principal, media.getProject().getId());

        // Delete file on disk (best-effort)
        try {
            Path diskPath = toDiskPath(media.getFileUrl());
            Files.deleteIfExists(diskPath);
        } catch (Exception ignored) {}

        projectMediaRepository.delete(media);
    }

    private void writeFile(Path targetDir, Path target, MultipartFile file) {
        try {
            Files.createDirectories(targetDir);
            file.transferTo(target);
        } catch (IOException e) {
            throw new BadRequestException("Failed to store file");
        }
    }

    private Path toDiskPath(String fileUrl) {
        // fileUrl looks like: /uploads/projects/{projectId}/{name}
        String prefix = publicBaseUrl.endsWith("/") ? publicBaseUrl : publicBaseUrl + "/";
        String rel = fileUrl.startsWith(prefix) ? fileUrl.substring(prefix.length()) : fileUrl;
        return uploadDir.resolve(rel).normalize();
    }

    private static String sanitizeFilename(String original) {
        String name = (original == null || original.isBlank()) ? "file" : original;
        name = name.replace("\\", "/");
        name = name.substring(name.lastIndexOf('/') + 1);
        name = name.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (name.length() > 200) name = name.substring(name.length() - 200);
        return name;
    }
}

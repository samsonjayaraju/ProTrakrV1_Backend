package com.klup.protrackr.controller;

import com.klup.protrackr.api.ApiResponse;
import com.klup.protrackr.security.CurrentUser;
import com.klup.protrackr.service.UploadService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {
    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<?> uploadAvatar(@RequestPart("file") @NotNull MultipartFile file) {
        var principal = CurrentUser.require();
        return ApiResponse.ok(uploadService.uploadAvatar(principal, file), "Avatar updated");
    }

    @GetMapping("/projects/{projectId}")
    public ApiResponse<?> listProjectMedia(@PathVariable Long projectId) {
        var principal = CurrentUser.require();
        return ApiResponse.ok(uploadService.listProjectMedia(principal, projectId));
    }

    @PostMapping(value = "/projects/{projectId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<?> uploadProjectMedia(@PathVariable Long projectId, @RequestPart("file") @NotNull MultipartFile file) {
        var principal = CurrentUser.require();
        return ApiResponse.ok(uploadService.uploadProjectMedia(principal, projectId, file));
    }

    @DeleteMapping("/media/{id}")
    public ApiResponse<?> deleteMedia(@PathVariable Long id) {
        var principal = CurrentUser.require();
        uploadService.deleteMedia(principal, id);
        return ApiResponse.ok(null, "Deleted");
    }
}

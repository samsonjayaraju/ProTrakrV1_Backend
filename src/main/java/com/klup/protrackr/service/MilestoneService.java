package com.klup.protrackr.service;

import com.klup.protrackr.domain.Milestone;
import com.klup.protrackr.domain.Project;
import com.klup.protrackr.dto.milestone.MilestoneDto;
import com.klup.protrackr.dto.milestone.MilestoneCreateRequest;
import com.klup.protrackr.dto.milestone.MilestoneUpdateRequest;
import com.klup.protrackr.exception.NotFoundException;
import com.klup.protrackr.mapper.DtoMapper;
import com.klup.protrackr.repo.MilestoneRepository;
import com.klup.protrackr.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MilestoneService {
    private final MilestoneRepository milestoneRepository;
    private final ProjectService projectService;

    public MilestoneService(MilestoneRepository milestoneRepository, ProjectService projectService) {
        this.milestoneRepository = milestoneRepository;
        this.projectService = projectService;
    }

    @Transactional(readOnly = true)
    public List<Milestone> listByProject(UserPrincipal principal, Long projectId) {
        Project p = projectService.getProject(principal, projectId);
        return milestoneRepository.findByProjectIdOrdered(p.getId());
    }

    @Transactional(readOnly = true)
    public List<MilestoneDto> listByProjectDtos(UserPrincipal principal, Long projectId) {
        return listByProject(principal, projectId).stream().map(DtoMapper::milestoneDto).toList();
    }

    @Transactional
    public Milestone create(UserPrincipal principal, MilestoneCreateRequest req) {
        Project p = projectService.getProject(principal, req.projectId());
        Milestone m = new Milestone();
        m.setProject(p);
        m.setTitle(req.title());
        m.setCompleted(req.completed());
        m.setSortOrder(req.sortOrder());
        return milestoneRepository.save(m);
    }

    @Transactional
    public MilestoneDto createDto(UserPrincipal principal, MilestoneCreateRequest req) {
        return DtoMapper.milestoneDto(create(principal, req));
    }

    @Transactional(readOnly = true)
    public Milestone get(UserPrincipal principal, Long id) {
        Milestone m = milestoneRepository.findById(id).orElseThrow(() -> new NotFoundException("Milestone not found"));
        projectService.getProject(principal, m.getProject().getId());
        return m;
    }

    @Transactional(readOnly = true)
    public MilestoneDto getDto(UserPrincipal principal, Long id) {
        return DtoMapper.milestoneDto(get(principal, id));
    }

    @Transactional
    public Milestone update(UserPrincipal principal, Long id, MilestoneUpdateRequest req) {
        Milestone m = get(principal, id);
        if (req.title() != null) m.setTitle(req.title());
        if (req.completed() != null) m.setCompleted(req.completed());
        if (req.sortOrder() != null) m.setSortOrder(req.sortOrder());
        return milestoneRepository.save(m);
    }

    @Transactional
    public MilestoneDto updateDto(UserPrincipal principal, Long id, MilestoneUpdateRequest req) {
        return DtoMapper.milestoneDto(update(principal, id, req));
    }

    @Transactional
    public void delete(UserPrincipal principal, Long id) {
        Milestone m = get(principal, id);
        milestoneRepository.delete(m);
    }
}

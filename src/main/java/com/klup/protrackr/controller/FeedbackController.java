package com.klup.protrackr.controller;

import com.klup.protrackr.api.ApiResponse;
import com.klup.protrackr.dto.feedback.FeedbackCreateRequest;
import com.klup.protrackr.dto.feedback.FeedbackUpdateRequest;
import com.klup.protrackr.security.CurrentUser;
import com.klup.protrackr.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {
    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping
    public ApiResponse<?> list(@RequestParam Long projectId) {
        var principal = CurrentUser.require();
        return ApiResponse.ok(feedbackService.listByProjectDtos(principal, projectId));
    }

    @PostMapping
    public ApiResponse<?> create(@Valid @RequestBody FeedbackCreateRequest req) {
        var principal = CurrentUser.require();
        return ApiResponse.ok(feedbackService.createDto(principal, req));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> get(@PathVariable Long id) {
        var principal = CurrentUser.require();
        return ApiResponse.ok(feedbackService.getDto(principal, id));
    }

    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody FeedbackUpdateRequest req) {
        var principal = CurrentUser.require();
        return ApiResponse.ok(feedbackService.updateDto(principal, id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        var principal = CurrentUser.require();
        feedbackService.delete(principal, id);
        return ApiResponse.ok(null, "Deleted");
    }
}

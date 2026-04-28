package com.klup.protrackr.controller;

import com.klup.protrackr.api.ApiResponse;
import com.klup.protrackr.dto.review.ReviewCreateRequest;
import com.klup.protrackr.dto.review.ReviewUpdateRequest;
import com.klup.protrackr.security.CurrentUser;
import com.klup.protrackr.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ApiResponse<?> list(@RequestParam(required = false) Long projectId) {
        var principal = CurrentUser.require();
        return ApiResponse.ok(reviewService.listDtos(principal, projectId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    public ApiResponse<?> create(@Valid @RequestBody ReviewCreateRequest req) {
        var principal = CurrentUser.require();
        return ApiResponse.ok(reviewService.createDto(principal, req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody ReviewUpdateRequest req) {
        var principal = CurrentUser.require();
        return ApiResponse.ok(reviewService.updateDto(principal, id, req), "Review updated");
    }
}

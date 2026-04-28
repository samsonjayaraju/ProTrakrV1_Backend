package com.klup.protrackr.controller;

import com.klup.protrackr.api.ApiResponse;
import com.klup.protrackr.dto.portfolio.PortfolioUpdateRequest;
import com.klup.protrackr.security.CurrentUser;
import com.klup.protrackr.service.PortfolioService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {
    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/me")
    public ApiResponse<?> me() {
        var principal = CurrentUser.require();
        return ApiResponse.ok(portfolioService.getMyPortfolio(principal));
    }

    @PutMapping("/me")
    public ApiResponse<?> updateMe(@Valid @RequestBody PortfolioUpdateRequest req) {
        var principal = CurrentUser.require();
        return ApiResponse.ok(portfolioService.updateMyPortfolio(principal, req), "Portfolio updated");
    }

    @GetMapping("/me/resume")
    public ApiResponse<?> resume() {
        var principal = CurrentUser.require();
        var dto = portfolioService.getMyPortfolio(principal);
        return ApiResponse.ok(java.util.Map.of("resumeUrl", dto.resumeUrl()));
    }

    @GetMapping("/public/{userId}")
    public ApiResponse<?> publicByUserId(@PathVariable Long userId) {
        var principal = CurrentUser.optional();
        return ApiResponse.ok(portfolioService.getPublicPortfolioByUserId(principal, userId));
    }

    @GetMapping("/public/slug/{slug}")
    public ApiResponse<?> publicBySlug(@PathVariable String slug) {
        var principal = CurrentUser.optional();
        return ApiResponse.ok(portfolioService.getPublicPortfolioBySlug(principal, slug));
    }
}


package com.mystudentguide.controller;

import com.mystudentguide.dto.admin.AdminStatsResponse;
import com.mystudentguide.service.AdminStatsService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Statistiques du tableau de bord administrateur.
 *
 * GET /api/admin/stats (ADMIN uniquement)
 */
@RestController
@RequestMapping("/api/admin")
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    public AdminStatsController(AdminStatsService adminStatsService) {
        this.adminStatsService = adminStatsService;
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminStatsResponse> getStats(Authentication authentication) {
        return ResponseEntity.ok(
                adminStatsService.getStats(authentication.getName())
        );
    }
}

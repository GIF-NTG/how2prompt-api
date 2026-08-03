package com.example.how2prompt.modules.analytics.controller;

import com.example.how2prompt.common.response.ApiResponse;
import com.example.how2prompt.modules.analytics.dto.AnalyticsDashboardResponse;
import com.example.how2prompt.modules.analytics.service.AnalyticsAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsAdminController {

    private final AnalyticsAdminService analyticsAdminService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AnalyticsDashboardResponse>> getDashboard() {
        AnalyticsDashboardResponse response = analyticsAdminService.getDashboardMetrics();
        return ResponseEntity.ok(ApiResponse.of(response));
    }
}

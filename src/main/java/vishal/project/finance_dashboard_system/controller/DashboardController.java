package vishal.project.finance_dashboard_system.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vishal.project.finance_dashboard_system.dto.DashboardSummaryDto;
import vishal.project.finance_dashboard_system.dto.RecentActivityDto;
import vishal.project.finance_dashboard_system.dto.TrendResponseDto;
import vishal.project.finance_dashboard_system.service.DashboardService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    public ResponseEntity<DashboardSummaryDto> getSummary(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ){
        return ResponseEntity.ok(dashboardService.getSummary(startDate, endDate));
    }

    @GetMapping("/recent-activity")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    public ResponseEntity<List<RecentActivityDto>> getRecentActivity(
            @RequestParam(defaultValue = "5") int limit
    ){
        return ResponseEntity.ok(dashboardService.getRecentActivity(limit));
    }

    @GetMapping("/trends")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    public ResponseEntity<List<TrendResponseDto>> getTrends(
            @RequestParam(defaultValue = "monthly") String type,
            @RequestParam (required = false) LocalDate startDate,
            @RequestParam (required = false) LocalDate endDate
    ){
        return ResponseEntity.ok(dashboardService.getTrends(type,startDate, endDate));
    }
}


package vishal.project.finance_dashboard_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vishal.project.finance_dashboard_system.dto.CategorySummaryDto;
import vishal.project.finance_dashboard_system.dto.DashboardSummaryDto;
import vishal.project.finance_dashboard_system.dto.RecentActivityDto;
import vishal.project.finance_dashboard_system.dto.TrendResponseDto;
import vishal.project.finance_dashboard_system.entity.FinancialRecord;
import vishal.project.finance_dashboard_system.entity.User;
import vishal.project.finance_dashboard_system.entity.enums.Role;
import vishal.project.finance_dashboard_system.exception.BadRequestException;
import vishal.project.finance_dashboard_system.repository.FinancialRecordRepository;
import vishal.project.finance_dashboard_system.repository.TrendProjection;
import vishal.project.finance_dashboard_system.utils.FinancialRecordSpecification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;


import static vishal.project.finance_dashboard_system.utils.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FinancialRecordRepository financialRecordRepository;

    public DashboardSummaryDto getSummary(LocalDate startDate, LocalDate endDate) {

        User currentUser = getCurrentUser();

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        boolean isPrivileged = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.ANALYST;
        User userFilter = isPrivileged ? null : currentUser;

        BigDecimal totalIncome = financialRecordRepository.getTotalIncome(userFilter, startDate, endDate);
        BigDecimal totalExpense = financialRecordRepository.getTotalExpense(userFilter, startDate, endDate);

        BigDecimal netBalance = totalIncome.subtract(totalExpense);

        List<CategorySummaryDto> categoryBreakdown =
                financialRecordRepository.getCategorySummary(userFilter, startDate, endDate)
                        .stream()
                        .map(p -> {
                            CategorySummaryDto dto = new CategorySummaryDto();
                            dto.setCategory(p.getCategory());
                            dto.setTotal(p.getTotal());
                            return dto;
                        })
                        .toList();

        DashboardSummaryDto dto = new DashboardSummaryDto();
        dto.setTotalIncome(totalIncome);
        dto.setTotalExpense(totalExpense);
        dto.setNetBalance(netBalance);
        dto.setCategoryBreakdown(categoryBreakdown);

        return dto;
    }

    public List<RecentActivityDto> getRecentActivity(int limit) {
        User currentUser = getCurrentUser();
        boolean isPrivileged = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.ANALYST;

        int effectiveLimit = Math.max(1, Math.min(limit, 100));

        List<FinancialRecord> recentRecords = financialRecordRepository.findAll(
                FinancialRecordSpecification.filterRecords(
                        isPrivileged ? null : currentUser,
                        null, null, null, null, null, null
                ),
                PageRequest.of(0, effectiveLimit, Sort.by("createdAt").descending())
        ).getContent();

        return recentRecords.stream()
                .map(record -> {
                    RecentActivityDto dto = new RecentActivityDto();
                    dto.setId(record.getId());
                    dto.setType(record.getType());
                    dto.setCategory(record.getCategory());
                    dto.setAmount(record.getAmount());
                    dto.setCreatedAt(record.getCreatedAt());
                    if (record.getCreatedBy() != null) {
                        dto.setCreatedBy(record.getCreatedBy().getName());
                    }
                    return dto;
                })
                .toList();
    }

    public List<TrendResponseDto> getTrends(String type, LocalDate startDate, LocalDate endDate) {
        User currentUser = getCurrentUser();
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        User userFilter = isPrivileged(currentUser) ? null : currentUser;
        List<TrendProjection> projections;
        boolean weekly = "weekly".equalsIgnoreCase(type);

        if (weekly) {
            projections = financialRecordRepository.getWeeklyTrends(userFilter, startDate, endDate);
        } else {
            projections = financialRecordRepository.getMonthlyTrends(userFilter, startDate, endDate);
        }

        return projections.stream()
                .map(p -> {
                    TrendResponseDto dto = new TrendResponseDto();
                    int key = p.getPeriod() != null ? p.getPeriod() : 0;

                    if (weekly) {
                        int year = key / 100;
                        int week = key % 100;
                        dto.setLabel(year + "-W" + String.format("%02d", week));
                    } else {
                        int year = key / 100;
                        int month = key % 100;
                        dto.setLabel(year + "-" + getMonthName(month));
                    }

                    dto.setIncome(p.getIncome() != null ? p.getIncome() : BigDecimal.ZERO);
                    dto.setExpense(p.getExpense() != null ? p.getExpense() : BigDecimal.ZERO);
                    return dto;
                })
                .toList();
    }

    private boolean isPrivileged(User user) {
        return user.getRole() == Role.ADMIN || user.getRole() == Role.ANALYST;
    }

    private String getMonthName(int month) {
        if (month < 1 || month > 12) {
            return "UNK";
        }
        return Month.of(month).name().substring(0, 3);
    }
}

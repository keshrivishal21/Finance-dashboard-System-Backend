package vishal.project.finance_dashboard_system.dto;

import lombok.Data;
import vishal.project.finance_dashboard_system.entity.enums.RecordType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RecentActivityDto {
    private Long id;
    private BigDecimal amount;
    private RecordType type;
    private String category;
    private LocalDate date;
    private String createdBy;
    private LocalDateTime createdAt;
}

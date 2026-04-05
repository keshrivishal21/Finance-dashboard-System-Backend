package vishal.project.finance_dashboard_system.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TrendResponseDto {
    private String label;
    private BigDecimal income;
    private BigDecimal expense;
}

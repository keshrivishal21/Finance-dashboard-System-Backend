package vishal.project.finance_dashboard_system.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CategorySummaryDto {
    private String category;
    private BigDecimal total;
}

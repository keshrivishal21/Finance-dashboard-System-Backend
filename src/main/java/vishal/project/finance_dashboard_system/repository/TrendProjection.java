package vishal.project.finance_dashboard_system.repository;

import java.math.BigDecimal;

public interface TrendProjection {
    Integer getPeriod();
    BigDecimal getIncome();
    BigDecimal getExpense();
}

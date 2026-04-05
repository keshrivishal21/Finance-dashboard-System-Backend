package vishal.project.finance_dashboard_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vishal.project.finance_dashboard_system.entity.FinancialRecord;
import vishal.project.finance_dashboard_system.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long>, JpaSpecificationExecutor<FinancialRecord> {

    @Query("""
    SELECT
        (CAST(FUNCTION('date_part', 'year', r.date) AS integer) * 100
         + CAST(FUNCTION('date_part', 'month', r.date) AS integer)) as period,
        SUM(CASE WHEN r.type = 'INCOME' THEN r.amount ELSE 0 END) as income,
        SUM(CASE WHEN r.type = 'EXPENSE' THEN r.amount ELSE 0 END) as expense
    FROM FinancialRecord r
    WHERE (:user IS NULL OR r.createdBy = :user)
      AND (:startDate IS NULL OR r.date >= :startDate)
      AND (:endDate IS NULL OR r.date <= :endDate)
    GROUP BY (CAST(FUNCTION('date_part', 'year', r.date) AS integer) * 100
              + CAST(FUNCTION('date_part', 'month', r.date) AS integer))
    ORDER BY (CAST(FUNCTION('date_part', 'year', r.date) AS integer) * 100
              + CAST(FUNCTION('date_part', 'month', r.date) AS integer))
""")
    List<TrendProjection> getMonthlyTrends(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    @Query("""
    SELECT
        (CAST(FUNCTION('date_part', 'year', r.date) AS integer) * 100
         + CAST(FUNCTION('date_part', 'week', r.date) AS integer)) as period,
        SUM(CASE WHEN r.type = 'INCOME' THEN r.amount ELSE 0 END) as income,
        SUM(CASE WHEN r.type = 'EXPENSE' THEN r.amount ELSE 0 END) as expense
    FROM FinancialRecord r
    WHERE (:user IS NULL OR r.createdBy = :user)
      AND (:startDate IS NULL OR r.date >= :startDate)
      AND (:endDate IS NULL OR r.date <= :endDate)
    GROUP BY (CAST(FUNCTION('date_part', 'year', r.date) AS integer) * 100
              + CAST(FUNCTION('date_part', 'week', r.date) AS integer))
    ORDER BY (CAST(FUNCTION('date_part', 'year', r.date) AS integer) * 100
              + CAST(FUNCTION('date_part', 'week', r.date) AS integer))
""")
    List<TrendProjection> getWeeklyTrends(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
    SELECT COALESCE(SUM(r.amount), 0)
    FROM FinancialRecord r
    WHERE r.type = 'INCOME'
      AND (:user IS NULL OR r.createdBy = :user)
      AND (:startDate IS NULL OR r.date >= :startDate)
      AND (:endDate IS NULL OR r.date <= :endDate)
""")
    BigDecimal getTotalIncome(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
    SELECT COALESCE(SUM(r.amount), 0)
    FROM FinancialRecord r
    WHERE r.type = 'EXPENSE'
      AND (:user IS NULL OR r.createdBy = :user)
      AND (:startDate IS NULL OR r.date >= :startDate)
      AND (:endDate IS NULL OR r.date <= :endDate)
""")
    BigDecimal getTotalExpense(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
    SELECT
        r.category as category,
        SUM(r.amount) as total
    FROM FinancialRecord r
    WHERE (:user IS NULL OR r.createdBy = :user)
      AND (:startDate IS NULL OR r.date >= :startDate)
      AND (:endDate IS NULL OR r.date <= :endDate)
    GROUP BY r.category
""")
    List<CategoryProjection> getCategorySummary(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}

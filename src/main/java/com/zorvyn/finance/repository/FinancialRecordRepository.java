package com.zorvyn.finance.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zorvyn.finance.dto.projection.CategorySummaryProjection;
import com.zorvyn.finance.dto.projection.MonthlyTrendProjection;
import com.zorvyn.finance.enums.TransactionType;
import com.zorvyn.finance.model.FinancialRecord;

@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {

    // Paginated + filtered listing

    @Query("""
            SELECT r FROM FinancialRecord r
            WHERE (:type     IS NULL OR r.type = :type)
              AND (:category  IS NULL OR LOWER(r.category) = LOWER(:category))
              AND (:dateFrom  IS NULL OR r.transactionDate >= :dateFrom)
              AND (:dateTo    IS NULL OR r.transactionDate <= :dateTo)
            ORDER BY r.transactionDate DESC, r.createdAt DESC
            """)
    Page<FinancialRecord> findAllFiltered(
            @Param("type")      TransactionType type,
            @Param("category")  String          category,
            @Param("dateFrom")  LocalDate       dateFrom,
            @Param("dateTo")    LocalDate       dateTo,
            Pageable pageable
    );

    // Dashboard: totals

    @Query("""
            SELECT COALESCE(SUM(r.amount), 0)
            FROM FinancialRecord r
            WHERE r.type = com.zorvyn.finance.enums.TransactionType.INCOME
            """)
    BigDecimal sumTotalIncome();

    @Query("""
            SELECT COALESCE(SUM(r.amount), 0)
            FROM FinancialRecord r
            WHERE r.type = com.zorvyn.finance.enums.TransactionType.EXPENSE
            """)
    BigDecimal sumTotalExpenses();

    @Query("SELECT COUNT(r) FROM FinancialRecord r")
    Long countAllRecords();

    // Dashboard: category breakdown

    @Query("""
            SELECT r.category  AS category,
                   SUM(r.amount) AS total,
                   COUNT(r)      AS count
            FROM FinancialRecord r
            WHERE (:type IS NULL OR r.type = :type)
            GROUP BY r.category
            ORDER BY total DESC
            """)
    List<CategorySummaryProjection> findCategoryBreakdown(
            @Param("type") TransactionType type
    );

    // Dashboard: monthly trends

    @Query("""
            SELECT FUNCTION('YEAR',  r.transactionDate) AS year,
                   FUNCTION('MONTH', r.transactionDate) AS month,
                   r.type                               AS type,
                   SUM(r.amount)                        AS total
            FROM FinancialRecord r
            WHERE r.transactionDate >= :fromDate
            GROUP BY FUNCTION('YEAR',  r.transactionDate),
                     FUNCTION('MONTH', r.transactionDate),
                     r.type
            ORDER BY year ASC, month ASC
            """)
    List<MonthlyTrendProjection> findMonthlyTrends(
            @Param("fromDate") LocalDate fromDate
    );

    // Dashboard: recent activity

    @Query("""
            SELECT r FROM FinancialRecord r
            ORDER BY r.transactionDate DESC, r.createdAt DESC
            """)
    List<FinancialRecord> findRecentActivity(Pageable pageable);

    // Dashboard: net balance

    @Query("""
            SELECT COALESCE(SUM(
                CASE WHEN r.type = com.zorvyn.finance.enums.TransactionType.INCOME
                     THEN r.amount
                     ELSE -r.amount
                END), 0)
            FROM FinancialRecord r
            """)
    BigDecimal calculateNetBalance();
}
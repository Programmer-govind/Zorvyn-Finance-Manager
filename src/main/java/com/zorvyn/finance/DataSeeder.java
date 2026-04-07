package com.zorvyn.finance;

import com.zorvyn.finance.enums.Role;
import com.zorvyn.finance.enums.TransactionType;
import com.zorvyn.finance.model.FinancialRecord;
import com.zorvyn.finance.model.User;
import com.zorvyn.finance.repository.FinancialRecordRepository;
import com.zorvyn.finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository            userRepository;
    private final FinancialRecordRepository recordRepository;
    private final PasswordEncoder           passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {

        if (userRepository.count() > 0) {
            log.info("Database already seeded — skipping.");
            return;
        }

        log.info("Seeding database with test data...");

        // ── Users ────────────────────────────────────────────────────

        User admin = userRepository.save(User.builder()
                .fullName("Admin User")
                .email("admin@zorvyn.com")
                .password(passwordEncoder.encode("Admin@123"))
                .role(Role.ROLE_ADMIN)
                .active(true)
                .deleted(false)
                .build());

        User analyst = userRepository.save(User.builder()
                .fullName("Analyst User")
                .email("analyst@zorvyn.com")
                .password(passwordEncoder.encode("Analyst@123"))
                .role(Role.ROLE_ANALYST)
                .active(true)
                .deleted(false)
                .build());

        User viewer = userRepository.save(User.builder()
                .fullName("Viewer User")
                .email("viewer@zorvyn.com")
                .password(passwordEncoder.encode("Viewer@123"))
                .role(Role.ROLE_VIEWER)
                .active(true)
                .deleted(false)
                .build());

        log.info("Created users: admin, analyst, viewer");

        // ── Financial Records ────────────────────────────────────────

        // Current month — income
        saveRecord(admin, BigDecimal.valueOf(85000.00),
                TransactionType.INCOME, "salary",
                LocalDate.now().withDayOfMonth(1),
                "Monthly salary - April 2026");

        saveRecord(admin, BigDecimal.valueOf(12000.00),
                TransactionType.INCOME, "freelance",
                LocalDate.now().withDayOfMonth(5),
                "Freelance project payment");

        saveRecord(admin, BigDecimal.valueOf(3500.00),
                TransactionType.INCOME, "investments",
                LocalDate.now().withDayOfMonth(10),
                "Dividend income Q1 2026");

        // Current month — expenses
        saveRecord(admin, BigDecimal.valueOf(18000.00),
                TransactionType.EXPENSE, "rent",
                LocalDate.now().withDayOfMonth(2),
                "Monthly rent - April 2026");

        saveRecord(admin, BigDecimal.valueOf(4200.00),
                TransactionType.EXPENSE, "utilities",
                LocalDate.now().withDayOfMonth(5),
                "Electricity and internet bills");

        saveRecord(admin, BigDecimal.valueOf(8500.00),
                TransactionType.EXPENSE, "groceries",
                LocalDate.now().withDayOfMonth(8),
                "Monthly grocery shopping");

        saveRecord(admin, BigDecimal.valueOf(2200.00),
                TransactionType.EXPENSE, "transport",
                LocalDate.now().withDayOfMonth(12),
                "Fuel and commute expenses");

        // Previous month records — for trend data
        saveRecord(admin, BigDecimal.valueOf(85000.00),
                TransactionType.INCOME, "salary",
                LocalDate.now().minusMonths(1).withDayOfMonth(1),
                "Monthly salary - March 2026");

        saveRecord(admin, BigDecimal.valueOf(15000.00),
                TransactionType.EXPENSE, "rent",
                LocalDate.now().minusMonths(1).withDayOfMonth(2),
                "Monthly rent - March 2026");

        saveRecord(admin, BigDecimal.valueOf(6800.00),
                TransactionType.EXPENSE, "groceries",
                LocalDate.now().minusMonths(1).withDayOfMonth(10),
                "Grocery shopping - March");

        saveRecord(admin, BigDecimal.valueOf(9500.00),
                TransactionType.INCOME, "freelance",
                LocalDate.now().minusMonths(1).withDayOfMonth(15),
                "Freelance project - March");

        // Two months ago — for richer trend data
        saveRecord(admin, BigDecimal.valueOf(85000.00),
                TransactionType.INCOME, "salary",
                LocalDate.now().minusMonths(2).withDayOfMonth(1),
                "Monthly salary - February 2026");

        saveRecord(admin, BigDecimal.valueOf(14000.00),
                TransactionType.EXPENSE, "rent",
                LocalDate.now().minusMonths(2).withDayOfMonth(2),
                "Monthly rent - February 2026");

        saveRecord(admin, BigDecimal.valueOf(5200.00),
                TransactionType.EXPENSE, "utilities",
                LocalDate.now().minusMonths(2).withDayOfMonth(7),
                "Bills - February 2026");

        saveRecord(admin, BigDecimal.valueOf(4000.00),
                TransactionType.INCOME, "investments",
                LocalDate.now().minusMonths(2).withDayOfMonth(20),
                "Investment returns - February");

        log.info("Seeded {} financial records", recordRepository.count());
        log.info("─────────────────────────────────────────────");
        log.info("TEST CREDENTIALS:");
        log.info("  ADMIN    → admin@zorvyn.com    / Admin@123");
        log.info("  ANALYST  → analyst@zorvyn.com  / Analyst@123");
        log.info("  VIEWER   → viewer@zorvyn.com   / Viewer@123");
        log.info("─────────────────────────────────────────────");
    }

    private void saveRecord(User createdBy,
                             BigDecimal amount,
                             TransactionType type,
                             String category,
                             LocalDate date,
                             String notes) {
        recordRepository.save(FinancialRecord.builder()
                .amount(amount)
                .type(type)
                .category(category)
                .transactionDate(date)
                .notes(notes)
                .createdBy(createdBy)
                .deleted(false)
                .build());
    }
}
package com.zorvyn.finance.repository;

import com.zorvyn.finance.enums.Role;
import com.zorvyn.finance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""
            SELECT u FROM User u
            WHERE (:role IS NULL OR u.role = :role)
              AND (:active IS NULL OR u.active = :active)
              AND u.deleted = false
            ORDER BY u.createdAt DESC
            """)
    List<User> findAllFiltered(
            @Param("role")   Role    role,
            @Param("active") Boolean active
    );

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.deleted = true WHERE u.id = :id")
    void softDeleteById(@Param("id") Long id);

    @Query("SELECT COUNT(u) FROM User u WHERE u.active = true AND u.deleted = false")
    Long countActiveUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.deleted = false")
    Long countByRole(@Param("role") Role role);
}
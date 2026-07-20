package com.digital_banking_api.repository;

import com.digital_banking_api.entity.Loan;
import com.digital_banking_api.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByUserId(Long userId);

    List<Loan> findByStatus(LoanStatus status);

    List<Loan> findByUserIdOrderByCreatedAtDesc(Long userId);

}
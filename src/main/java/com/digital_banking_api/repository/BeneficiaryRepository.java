package com.digital_banking_api.repository;

import com.digital_banking_api.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
    List<Beneficiary> findByUserId(Long userId);
    Optional<Beneficiary> findByIdAndUserId(Long id, Long userId);
    boolean existsByAccountNumberAndUserId(String accountNumber, Long userId);
}

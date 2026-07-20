package com.digital_banking_api.repository;

import com.digital_banking_api.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
    List<Transfer> findByFromAccountId(Long accountId);
    List<Transfer> findByToAccountId(Long accountId);
    Optional<Transfer> findByReference(String reference);
    List<Transfer> findByFromAccountIdOrderByCreatedAtDesc(Long accountId);
    List<Transfer> findByToAccountIdOrderByCreatedAtDesc(Long accountId);
}

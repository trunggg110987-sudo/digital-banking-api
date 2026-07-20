package com.digital_banking_api.service;

import com.digital_banking_api.dto.request.AddBeneficiaryRequest;
import com.digital_banking_api.dto.response.BeneficiaryResponse;
import com.digital_banking_api.dto.response.TransferResponse;

import java.math.BigDecimal;
import java.util.List;

public interface TransferService {
    TransferResponse transferMoney(Long fromAccountId, Long toAccountId, BigDecimal amount, String description, Long userId);

    List<TransferResponse> getTransferHistory(Long accountId, Long userId);

    BeneficiaryResponse addBeneficiary(AddBeneficiaryRequest request, Long userId);

    List<BeneficiaryResponse> getBeneficiaries(Long userId);

    void deleteBeneficiary(Long beneficiaryId, Long userId);
}
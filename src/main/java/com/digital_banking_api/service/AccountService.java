package com.digital_banking_api.service;

import com.digital_banking_api.dto.request.CreateAccountRequest;
import com.digital_banking_api.dto.response.AccountResponse;
import com.digital_banking_api.dto.response.TransactionResponse;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    AccountResponse createAccount(CreateAccountRequest request, Long userId);

    AccountResponse getAccountById(Long accountId, Long userId);

    List<AccountResponse> getUserAccounts(Long userId);

    void withdraw(Long accountId, BigDecimal amount, Long userId);

    void deposit(Long accountId, BigDecimal amount, Long userId);

    void freezeAccount(Long accountId, Long userId);

    void unfreezeAccount(Long accountId, Long userId);

    void closeAccount(Long accountId, Long userId);
    
    List<TransactionResponse> getTransactionHistory(Long accountId, Long userId);
}

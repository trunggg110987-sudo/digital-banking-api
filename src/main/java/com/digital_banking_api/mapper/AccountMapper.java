package com.digital_banking_api.mapper;

import com.digital_banking_api.dto.response.AccountResponse;
import com.digital_banking_api.entity.BankAccount;

public final class AccountMapper {

    private AccountMapper() {
    }

    public static AccountResponse toResponse(BankAccount account) {
        if (account == null) {
            return null;
        }

        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setAccountType(account.getAccountType());
        response.setBalance(account.getBalance());
        response.setCurrency(account.getCurrency());
        response.setStatus(account.getStatus());
        response.setCreatedAt(account.getCreatedAt());
        return response;
    }
}

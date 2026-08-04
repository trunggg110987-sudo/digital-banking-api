package com.digital_banking_api.service.impl;

import com.digital_banking_api.dto.request.CreateAccountRequest;
import com.digital_banking_api.dto.response.AccountResponse;
import com.digital_banking_api.dto.response.TransactionResponse;
import com.digital_banking_api.entity.BankAccount;
import com.digital_banking_api.entity.Role;
import com.digital_banking_api.entity.Transaction;
import com.digital_banking_api.entity.User;
import com.digital_banking_api.enums.AccountStatus;
import com.digital_banking_api.enums.AccountType;
import com.digital_banking_api.enums.TransactionStatus;
import com.digital_banking_api.enums.TransactionType;
import com.digital_banking_api.exception.BadRequestException;
import com.digital_banking_api.exception.InsufficientBalanceException;
import com.digital_banking_api.exception.ResourceNotFoundException;
import com.digital_banking_api.repository.BankAccountRepository;
import com.digital_banking_api.repository.TransactionRepository;
import com.digital_banking_api.repository.UserRepository;
import com.digital_banking_api.util.AccountNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock BankAccountRepository accountRepository;
    @Mock AccountNumberGenerator accountNumberGenerator;
    @Mock TransactionRepository transactionRepository;
    @Mock UserRepository userRepository;
    @InjectMocks AccountServiceImpl accountService;

    private User customer;
    private User admin;
    private BankAccount account;

    @BeforeEach
    void setUp() {
        Role customerRole = new Role();
        customerRole.setName("CUSTOMER");
        Role adminRole = new Role();
        adminRole.setName("ADMIN");

        customer = new User();
        customer.setId(1L);
        customer.setEmail("customer@test.com");
        customer.setFullName("Customer");
        customer.setRole(customerRole);

        admin = new User();
        admin.setId(2L);
        admin.setEmail("admin@test.com");
        admin.setFullName("Admin");
        admin.setRole(adminRole);

        account = new BankAccount();
        account.setId(1L);
        account.setUser(customer);
        account.setAccountNumber("1234567890");
        account.setAccountType(AccountType.SAVINGS);
        account.setBalance(new BigDecimal("5000"));
        account.setCurrency("VND");
        account.setStatus(AccountStatus.ACTIVE);
    }

    @Test
    void createAccount_success() {
        CreateAccountRequest request = new CreateAccountRequest(AccountType.SAVINGS, "VND");
        when(userRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(accountNumberGenerator.generateAccountNumber()).thenReturn("1234567890");
        when(accountRepository.save(any(BankAccount.class))).thenReturn(account);

        AccountResponse response = accountService.createAccount(request, customer.getId());

        assertEquals("1234567890", response.getAccountNumber());
    }

    @Test
    void getAccountById_notFound() {
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> accountService.getAccountById(1L, 1L));
    }

    @Test
    void getUserAccounts_success() {
        when(accountRepository.findByUserId(1L)).thenReturn(List.of(account));
        List<AccountResponse> responses = accountService.getUserAccounts(1L);
        assertEquals(1, responses.size());
    }

    @Test
    void withdraw_success() {
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(BankAccount.class))).thenReturn(account);

        accountService.withdraw(1L, new BigDecimal("1000"), 1L);

        assertEquals(new BigDecimal("4000"), account.getBalance());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void withdraw_insufficientBalance() {
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(account));
        assertThrows(InsufficientBalanceException.class, () -> accountService.withdraw(1L, new BigDecimal("10000"), 1L));
    }

    @Test
    void deposit_success() {
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(BankAccount.class))).thenReturn(account);

        accountService.deposit(1L, new BigDecimal("500"), 1L);

        assertEquals(new BigDecimal("5500"), account.getBalance());
    }

    @Test
    void freezeAccount_adminSuccess() {
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(BankAccount.class))).thenReturn(account);

        accountService.freezeAccount(1L, admin.getId());

        assertEquals(AccountStatus.FROZEN, account.getStatus());
    }

    @Test
    void unfreezeAccount_adminSuccess() {
        account.setStatus(AccountStatus.FROZEN);
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(BankAccount.class))).thenReturn(account);

        accountService.unfreezeAccount(1L, admin.getId());

        assertEquals(AccountStatus.ACTIVE, account.getStatus());
    }

    @Test
    void closeAccount_success() {
        account.setBalance(BigDecimal.ZERO);
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(BankAccount.class))).thenReturn(account);

        accountService.closeAccount(1L, 1L);

        assertEquals(AccountStatus.CLOSED, account.getStatus());
    }

    @Test
    void getTransactionHistory_success() {
        Transaction tx = new Transaction();
        tx.setId(1L);
        tx.setType(TransactionType.DEPOSIT);
        tx.setAmount(new BigDecimal("100"));
        tx.setStatus(TransactionStatus.SUCCESS);
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(account));
        when(transactionRepository.findByAccountIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(tx));

        List<TransactionResponse> responses = accountService.getTransactionHistory(1L, 1L);

        assertEquals(1, responses.size());
    }
}

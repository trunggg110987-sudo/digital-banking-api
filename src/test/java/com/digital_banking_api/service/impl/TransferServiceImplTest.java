package com.digital_banking_api.service.impl;

import com.digital_banking_api.dto.request.AddBeneficiaryRequest;
import com.digital_banking_api.dto.response.BeneficiaryResponse;
import com.digital_banking_api.dto.response.TransferResponse;
import com.digital_banking_api.entity.BankAccount;
import com.digital_banking_api.entity.Beneficiary;
import com.digital_banking_api.entity.Role;
import com.digital_banking_api.entity.Transaction;
import com.digital_banking_api.entity.Transfer;
import com.digital_banking_api.entity.User;
import com.digital_banking_api.enums.AccountStatus;
import com.digital_banking_api.enums.BeneficiaryStatus;
import com.digital_banking_api.enums.TransactionStatus;
import com.digital_banking_api.enums.TransactionType;
import com.digital_banking_api.enums.TransferStatus;
import com.digital_banking_api.exception.BadRequestException;
import com.digital_banking_api.exception.InsufficientBalanceException;
import com.digital_banking_api.exception.ResourceNotFoundException;
import com.digital_banking_api.repository.BankAccountRepository;
import com.digital_banking_api.repository.BeneficiaryRepository;
import com.digital_banking_api.repository.TransactionRepository;
import com.digital_banking_api.repository.TransferRepository;
import com.digital_banking_api.repository.UserRepository;
import com.digital_banking_api.util.TransferReferenceGenerator;
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
class TransferServiceImplTest {

    @Mock TransferRepository transferRepository;
    @Mock BeneficiaryRepository beneficiaryRepository;
    @Mock UserRepository userRepository;
    @Mock BankAccountRepository accountRepository;
    @Mock TransactionRepository transactionRepository;
    @Mock TransferReferenceGenerator transferReferenceGenerator;
    @InjectMocks TransferServiceImpl transferService;

    private User user;
    private BankAccount fromAccount;
    private BankAccount toAccount;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setName("CUSTOMER");

        user = new User();
        user.setId(1L);
        user.setFullName("Customer");
        user.setRole(role);

        fromAccount = new BankAccount();
        fromAccount.setId(1L);
        fromAccount.setUser(user);
        fromAccount.setAccountNumber("1111111111");
        fromAccount.setBalance(new BigDecimal("5000"));
        fromAccount.setStatus(AccountStatus.ACTIVE);

        toAccount = new BankAccount();
        toAccount.setId(2L);
        toAccount.setUser(user);
        toAccount.setAccountNumber("2222222222");
        toAccount.setBalance(new BigDecimal("2000"));
        toAccount.setStatus(AccountStatus.ACTIVE);
    }

    @Test
    void transferMoney_success() {
        when(accountRepository.findByIdAndUserIdWithLock(1L, 1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByIdWithLock(2L)).thenReturn(Optional.of(toAccount));
        when(transferReferenceGenerator.generateReferenceNumber()).thenReturn("REF001");
        when(accountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransferResponse response = transferService.transferMoney(1L, 2L, new BigDecimal("1000"), "Rent", 1L);

        assertEquals("REF001", response.getReference());
        assertEquals(new BigDecimal("4000"), fromAccount.getBalance());
        assertEquals(new BigDecimal("3000"), toAccount.getBalance());
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    void transferMoney_sameAccount() {
        assertThrows(BadRequestException.class,
                () -> transferService.transferMoney(1L, 1L, new BigDecimal("1000"), "Rent", 1L));
    }

    @Test
    void transferMoney_insufficientBalance() {
        when(accountRepository.findByIdAndUserIdWithLock(1L, 1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByIdWithLock(2L)).thenReturn(Optional.of(toAccount));
        assertThrows(InsufficientBalanceException.class,
                () -> transferService.transferMoney(1L, 2L, new BigDecimal("99999"), "Rent", 1L));
    }

    @Test
    void getTransferHistory_success() {
        Transfer t = new Transfer();
        t.setId(1L);
        t.setFromAccount(fromAccount);
        t.setToAccount(toAccount);
        t.setAmount(new BigDecimal("100"));
        t.setStatus(TransferStatus.SUCCESS);
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(fromAccount));
        when(transferRepository.findByFromAccountIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(t));
        when(transferRepository.findByToAccountIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

        List<TransferResponse> responses = transferService.getTransferHistory(1L, 1L);

        assertEquals(1, responses.size());
    }

    @Test
    void addBeneficiary_success() {
        AddBeneficiaryRequest request = new AddBeneficiaryRequest("2222222222", "Bank", "Holder", "Nick");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountRepository.findByAccountNumber("2222222222")).thenReturn(Optional.of(toAccount));
        when(beneficiaryRepository.existsByAccountNumberAndUserId("2222222222", 1L)).thenReturn(false);
        when(beneficiaryRepository.save(any(Beneficiary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BeneficiaryResponse response = transferService.addBeneficiary(request, 1L);

        assertEquals("2222222222", response.getAccountNumber());
        assertEquals(BeneficiaryStatus.ACTIVE, response.getStatus());
    }
}

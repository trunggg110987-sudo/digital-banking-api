package com.digital_banking_api.service.impl;

import com.digital_banking_api.dto.request.CreateAccountRequest;
import com.digital_banking_api.dto.response.AccountResponse;
import com.digital_banking_api.dto.response.TransactionResponse;
import com.digital_banking_api.entity.BankAccount;
import com.digital_banking_api.entity.Transaction;
import com.digital_banking_api.entity.User;
import com.digital_banking_api.enums.AccountStatus;
import com.digital_banking_api.enums.TransactionStatus;
import com.digital_banking_api.enums.TransactionType;
import com.digital_banking_api.exception.BadRequestException;
import com.digital_banking_api.exception.InsufficientBalanceException;
import com.digital_banking_api.exception.ResourceNotFoundException;
import com.digital_banking_api.repository.BankAccountRepository;
import com.digital_banking_api.repository.TransactionRepository;
import com.digital_banking_api.repository.UserRepository;
import com.digital_banking_api.service.AccountService;
import com.digital_banking_api.util.AccountNumberGenerator;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final BankAccountRepository accountRepository;

    private final AccountNumberGenerator accountNumberGenerator;

    private final TransactionRepository transactionRepository;

    private final UserRepository userRepository;

    private AccountResponse convertToAccountResponse(BankAccount bankAccount) {
        AccountResponse response = new AccountResponse();
        response.setId(bankAccount.getId());
        response.setAccountNumber(bankAccount.getAccountNumber());
        response.setAccountType(bankAccount.getAccountType());
        response.setBalance(bankAccount.getBalance());
        response.setCurrency(bankAccount.getCurrency());
        response.setStatus(bankAccount.getStatus());
        response.setCreatedAt(bankAccount.getCreatedAt());

        return response;
    }

    private TransactionResponse convertToTransactionResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();

        response.setId(transaction.getId());
        response.setType(transaction.getType());
        response.setAmount(transaction.getAmount());
        response.setDescription(transaction.getDescription());
        response.setBalanceAfter(transaction.getBalanceAfter());
        response.setStatus(transaction.getStatus());
        response.setCreatedAt(transaction.getCreatedAt());

        return response;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }
    }

    private BankAccount getActiveAccount(Long accountId, Long userId) {

        BankAccount account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Account is not active");
        }

        return account;
    }

    private void createTransaction(BankAccount account, BigDecimal amount, BigDecimal balanceAfter,
                                   TransactionType type, String description) {

        Transaction transaction = new Transaction();

        transaction.setAccount(account);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setStatus(TransactionStatus.SUCCESS);

        transactionRepository.save(transaction);
    }

    @Override
    public AccountResponse createAccount(CreateAccountRequest request, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Email not found"));

        BankAccount bankAccount = new BankAccount();

        bankAccount.setAccountNumber(accountNumberGenerator.generateAccountNumber());
        bankAccount.setUser(user);
        bankAccount.setBalance(BigDecimal.ZERO);
        bankAccount.setStatus(AccountStatus.ACTIVE);
        bankAccount.setCurrency("VND");
        bankAccount.setAccountType(request.getAccountType());

        BankAccount savedAccount = accountRepository.save(bankAccount);
        return convertToAccountResponse(savedAccount);
    }

    @Override
    public AccountResponse getAccountById(Long accountId, Long userId) {
        BankAccount account = accountRepository.findByIdAndUserId(accountId, userId).orElseThrow(()
                -> new ResourceNotFoundException("Account not found"));
        return convertToAccountResponse(account);
    }

    @Override
    public List<AccountResponse> getUserAccounts(Long userId) {

        List<BankAccount> accounts = accountRepository.findByUserId(userId);

        List<AccountResponse> responses = new ArrayList<>();

        for (BankAccount account : accounts) {
            responses.add(convertToAccountResponse(account));
        }

        return responses;
    }

    @Override
    @Transactional
    public void withdraw(Long accountId, BigDecimal amount, Long userId) {

        validateAmount(amount);

        BankAccount account = getActiveAccount(accountId, userId);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance. Current: "
                            + account.getBalance()
                            + ", Required: "
                            + amount);
        }

        BigDecimal newBalance = account.getBalance().subtract(amount);

        account.setBalance(newBalance);

        accountRepository.save(account);

        createTransaction(account, amount, newBalance, TransactionType.WITHDRAW, "Withdrawal");
    }

    @Override
    public void deposit(Long accountId, BigDecimal amount, Long userId) {

        validateAmount(amount);

        BankAccount account = getActiveAccount(accountId, userId);

        BigDecimal newBalance = account.getBalance().add(amount);

        account.setBalance(newBalance);

        accountRepository.save(account);

        createTransaction(account, amount, newBalance, TransactionType.DEPOSIT, "Deposit");
    }

    @Override
    public void freezeAccount(Long accountId, Long userId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!"ADMIN".equalsIgnoreCase(requester.getRole().getName())) {
            throw new AccessDeniedException("Only admin can freeze accounts");
        }
        BankAccount account = accountRepository.findById(accountId).orElseThrow(()
                -> new ResourceNotFoundException("Account not found"));

        if(account.getStatus().equals(AccountStatus.FROZEN)) {
            throw new BadRequestException("Account already frozen");
        }

        account.setStatus(AccountStatus.FROZEN);
        accountRepository.save(account);
    }

    @Override
    public void unfreezeAccount(Long accountId, Long userId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!"ADMIN".equalsIgnoreCase(requester.getRole().getName())) {
            throw new AccessDeniedException("Only admin can unfreeze accounts");
        }
        BankAccount account = accountRepository.findById(accountId).orElseThrow(()
                -> new ResourceNotFoundException("Account not found"));

        if(!account.getStatus().equals(AccountStatus.FROZEN)) {
            throw new BadRequestException("Account is not frozen");
        }

        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);
    }

    @Override
    public void closeAccount(Long accountId, Long userId) {
        BankAccount account = accountRepository.findByIdAndUserId(accountId, userId).orElseThrow(()
                -> new ResourceNotFoundException("Account not found"));

        if(account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new BadRequestException(
                    "Cannot close account with remaining balance: " + account.getBalance());
        }

        account.setStatus(AccountStatus.CLOSED);
        accountRepository.save(account);
    }

    @Override
    public List<TransactionResponse> getTransactionHistory(Long accountId, Long userId) {
        accountRepository.findByIdAndUserId(accountId, userId).orElseThrow(()
                -> new ResourceNotFoundException("Account not found"));

        List<Transaction> transactions = transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId);

        List<TransactionResponse> responses = new ArrayList<>();

        for(Transaction transaction : transactions) {
            responses.add(convertToTransactionResponse(transaction));
        }
        return responses;
    }
}

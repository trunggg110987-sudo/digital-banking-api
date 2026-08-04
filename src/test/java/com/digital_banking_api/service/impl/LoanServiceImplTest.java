package com.digital_banking_api.service.impl;

import com.digital_banking_api.dto.request.ApplyLoanRequest;
import com.digital_banking_api.dto.response.LoanResponse;
import com.digital_banking_api.entity.*;
import com.digital_banking_api.enums.*;
import com.digital_banking_api.exception.BadRequestException;
import com.digital_banking_api.exception.InsufficientBalanceException;
import com.digital_banking_api.exception.ResourceNotFoundException;
import com.digital_banking_api.repository.*;
import com.digital_banking_api.util.LoanCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceImplTest {
    @Mock LoanRepository loanRepository;
    @Mock LoanRepaymentRepository repaymentRepository;
    @Mock BankAccountRepository accountRepository;
    @Mock UserRepository userRepository;
    @Mock TransactionRepository transactionRepository;
    @Mock LoanCalculator loanCalculator;
    @Mock CardRepository cardRepository;
    @InjectMocks LoanServiceImpl loanService;

    private User user;
    private BankAccount account;
    private Loan loan;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setName("CUSTOMER");
        user = new User();
        user.setId(1L);
        user.setFullName("User");
        user.setRole(role);

        account = new BankAccount();
        account.setId(1L);
        account.setUser(user);
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(new BigDecimal("10000"));

        loan = new Loan();
        loan.setId(1L);
        loan.setUser(user);
        loan.setAccount(account);
        loan.setLoanType(LoanType.PERSONAL);
        loan.setPrincipalAmount(new BigDecimal("5000"));
        loan.setInterestRate(new BigDecimal("12"));
        loan.setTermMonths(12);
        loan.setMonthlyPayment(new BigDecimal("444.44"));
        loan.setRemainingBalance(new BigDecimal("5000"));
        loan.setStatus(LoanStatus.PENDING);
        loan.setStartDate(LocalDate.now());
        loan.setEndDate(LocalDate.now().plusMonths(12));
    }

    @Test
    void applyLoan_success() {
        ApplyLoanRequest request = new ApplyLoanRequest(1L, LoanType.PERSONAL, new BigDecimal("5000"), 12, new BigDecimal("12"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(loanCalculator.calculateMonthlyPayment(any(), any(), any())).thenReturn(new BigDecimal("444.44"));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoanResponse response = loanService.applyLoan(request, 1L);

        assertEquals(LoanStatus.PENDING, response.getStatus());
    }

    @Test
    void applyLoan_accountNotOwned() {
        User other = new User();
        other.setId(2L);
        account.setUser(other);
        ApplyLoanRequest request = new ApplyLoanRequest(1L, LoanType.PERSONAL, new BigDecimal("5000"), 12, new BigDecimal("12"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        assertThrows(ResourceNotFoundException.class, () -> loanService.applyLoan(request, 1L));
    }

    @Test
    void approveLoan_success() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        assertEquals(LoanStatus.APPROVED, loanService.approveLoan(1L).getStatus());
    }

    @Test
    void rejectLoan_success() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        assertEquals(LoanStatus.REJECTED, loanService.rejectLoan(1L).getStatus());
    }

    @Test
    void disburseLoan_success() {
        loan.setStatus(LoanStatus.APPROVED);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(accountRepository.findByIdWithLock(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(loanCalculator.generateRepaymentSchedule(any(Loan.class))).thenReturn(List.of());
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoanResponse response = loanService.disburseLoan(1L);

        assertEquals(LoanStatus.ACTIVE, response.getStatus());
        assertEquals(new BigDecimal("15000"), account.getBalance());
    }

    @Test
    void repayLoan_insufficientBalance() {
        loan.setStatus(LoanStatus.ACTIVE);
        LoanRepayment repayment = new LoanRepayment();
        repayment.setInstallmentNumber(1);
        repayment.setStatus(RepaymentStatus.PENDING);
        repayment.setTotalAmount(new BigDecimal("20000"));
        repayment.setRemainingBalance(BigDecimal.ZERO);

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(repaymentRepository.findByLoanIdOrderByInstallmentNumber(1L)).thenReturn(List.of(repayment));
        when(accountRepository.findByIdWithLock(1L)).thenReturn(Optional.of(account));

        assertThrows(InsufficientBalanceException.class, () -> loanService.repayLoan(1L, 1L));
    }

    @Test
    void getMyLoans_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(loanRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(loan));
        assertEquals(1, loanService.getMyLoans(1L).size());
    }

    @Test
    void getLoanById_notBelongToUser() {
        User other = new User();
        other.setId(2L);
        loan.setUser(other);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        assertThrows(ResourceNotFoundException.class, () -> loanService.getLoanById(1L, 1L));
    }
}

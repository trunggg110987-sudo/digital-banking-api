package com.digital_banking_api.service.impl;

import com.digital_banking_api.dto.request.ApplyLoanRequest;
import com.digital_banking_api.dto.response.LoanRepaymentResponse;
import com.digital_banking_api.dto.response.LoanResponse;
import com.digital_banking_api.entity.BankAccount;
import com.digital_banking_api.entity.Card;
import com.digital_banking_api.entity.Loan;
import com.digital_banking_api.entity.LoanRepayment;
import com.digital_banking_api.entity.Transaction;
import com.digital_banking_api.entity.User;
import com.digital_banking_api.enums.AccountStatus;
import com.digital_banking_api.enums.CardStatus;
import com.digital_banking_api.enums.CardType;
import com.digital_banking_api.enums.LoanStatus;
import com.digital_banking_api.enums.LoanType;
import com.digital_banking_api.enums.RepaymentStatus;
import com.digital_banking_api.enums.TransactionStatus;
import com.digital_banking_api.enums.TransactionType;
import com.digital_banking_api.exception.BadRequestException;
import com.digital_banking_api.exception.InsufficientBalanceException;
import com.digital_banking_api.exception.ResourceNotFoundException;
import com.digital_banking_api.repository.BankAccountRepository;
import com.digital_banking_api.repository.CardRepository;
import com.digital_banking_api.repository.LoanRepository;
import com.digital_banking_api.repository.LoanRepaymentRepository;
import com.digital_banking_api.repository.TransactionRepository;
import com.digital_banking_api.repository.UserRepository;
import com.digital_banking_api.service.LoanService;
import com.digital_banking_api.util.LoanCalculator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final LoanRepaymentRepository repaymentRepository;
    private final BankAccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final LoanCalculator loanCalculator;
    private final CardRepository cardRepository;

    private LoanResponse mapLoanToResponse(Loan loan) {
        LoanResponse response = new LoanResponse();
        response.setId(loan.getId());
        response.setLoanType(loan.getLoanType());
        response.setPrincipalAmount(loan.getPrincipalAmount());
        response.setMonthlyPayment(loan.getMonthlyPayment());
        response.setRemainingBalance(loan.getRemainingBalance());
        response.setTermMonths(loan.getTermMonths());
        response.setInterestRate(loan.getInterestRate());
        response.setStatus(loan.getStatus());
        response.setStartDate(loan.getStartDate());
        response.setEndDate(loan.getEndDate());
        return response;
    }

    private LoanRepaymentResponse mapRepaymentToResponse(LoanRepayment repayment) {
        LoanRepaymentResponse response = new LoanRepaymentResponse();
        response.setInstallmentNumber(repayment.getInstallmentNumber());
        response.setDueDate(repayment.getDueDate());
        response.setPaidDate(repayment.getPaidDate());
        response.setPrincipalAmount(repayment.getPrincipalAmount());
        response.setInterestAmount(repayment.getInterestAmount());
        response.setTotalAmount(repayment.getTotalAmount());
        response.setRemainingBalance(repayment.getRemainingBalance());
        response.setStatus(repayment.getStatus());
        return response;
    }

    private void validateLoanRequest(ApplyLoanRequest request) {
        if (request == null) {
            throw new BadRequestException("Request cannot be null");
        }
        if (request.getPrincipalAmount() == null || request.getPrincipalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Principal amount must be greater than zero");
        }
        if (request.getInterestRate() == null || request.getInterestRate().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Interest rate is invalid");
        }
        if (request.getTermMonths() == null || request.getTermMonths() <= 0) {
            throw new BadRequestException("Loan term is invalid");
        }
    }

    private void createTransaction(BankAccount account, TransactionType type, BigDecimal amount, String description) {
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setBalanceAfter(account.getBalance());
        transaction.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(transaction);
    }

    private void issueLoanCard(BankAccount account) {
        Card card = new Card();
        card.setAccount(account);
        String cardNumber = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        card.setCardNumber(cardNumber);
        card.setExpiryDate(LocalDate.now().plusYears(5));
        card.setHolderName(account.getUser().getFullName());
        card.setCardType(CardType.DEBIT);
        card.setStatus(CardStatus.ACTIVE);
        card.setContactlessEnabled(true);
        cardRepository.save(card);
    }

    @Override
    public LoanResponse applyLoan(ApplyLoanRequest request, Long userId) {
        validateLoanRequest(request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        BankAccount account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (!account.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Account does not belong to this user");
        }

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Account is not active");
        }

        Loan loan = new Loan();
        loan.setUser(user);
        loan.setAccount(account);
        loan.setLoanType(request.getLoanType());
        loan.setPrincipalAmount(request.getPrincipalAmount());
        loan.setInterestRate(request.getInterestRate());
        loan.setTermMonths(request.getTermMonths());
        loan.setStatus(LoanStatus.PENDING);
        loan.setRemainingBalance(request.getPrincipalAmount());
        loan.setStartDate(LocalDate.now());
        loan.setEndDate(LocalDate.now().plusMonths(request.getTermMonths()));

        BigDecimal monthlyPayment = loanCalculator.calculateMonthlyPayment(
                request.getPrincipalAmount(),
                request.getInterestRate(),
                request.getTermMonths());
        loan.setMonthlyPayment(monthlyPayment);

        return mapLoanToResponse(loanRepository.save(loan));
    }

    @Override
    public List<LoanResponse> getMyLoans(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<LoanResponse> responses = new ArrayList<>();
        for (Loan loan : loanRepository.findByUserIdOrderByCreatedAtDesc(userId)) {
            responses.add(mapLoanToResponse(loan));
        }
        return responses;
    }

    @Override
    public LoanResponse getLoanById(Long loanId, Long userId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (!loan.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Loan does not belong to this user");
        }

        return mapLoanToResponse(loan);
    }

    @Override
    public List<LoanRepaymentResponse> getRepaymentSchedule(Long loanId, Long userId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (!loan.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Loan does not belong to this user");
        }

        List<LoanRepaymentResponse> responses = new ArrayList<>();
        for (LoanRepayment repayment : repaymentRepository.findByLoanIdOrderByInstallmentNumber(loanId)) {
            responses.add(mapRepaymentToResponse(repayment));
        }
        return responses;
    }

    @Override
    public LoanResponse approveLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new BadRequestException("Only pending loans can be approved");
        }

        loan.setStatus(LoanStatus.APPROVED);
        return mapLoanToResponse(loanRepository.save(loan));
    }

    @Override
    public LoanResponse rejectLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new BadRequestException("Only pending loans can be rejected");
        }

        loan.setStatus(LoanStatus.REJECTED);
        return mapLoanToResponse(loanRepository.save(loan));
    }

    @Override
    public LoanResponse disburseLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new BadRequestException("Loan must be APPROVED");
        }

        BankAccount account = accountRepository.findByIdWithLock(loan.getAccount().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Account is not active, cannot disburse loan");
        }

        account.setBalance(account.getBalance().add(loan.getPrincipalAmount()));
        accountRepository.save(account);
        createTransaction(account, TransactionType.LOAN_DISBURSEMENT, loan.getPrincipalAmount(), "Loan Disbursement");

        loan.setStatus(LoanStatus.ACTIVE);
        loan.setStartDate(LocalDate.now());
        loan.setEndDate(LocalDate.now().plusMonths(loan.getTermMonths()));

        Loan savedLoan = loanRepository.save(loan);
        issueLoanCard(account);
        repaymentRepository.saveAll(loanCalculator.generateRepaymentSchedule(savedLoan));

        return mapLoanToResponse(savedLoan);
    }

    @Override
    public void repayLoan(Long loanId, Long userId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (!loan.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Loan not found");
        }

        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new BadRequestException("Loan is not active");
        }

        List<LoanRepayment> repayments = repaymentRepository.findByLoanIdOrderByInstallmentNumber(loanId);
        LoanRepayment nextRepayment = null;
        for (LoanRepayment repayment : repayments) {
            if (repayment.getStatus() == RepaymentStatus.PENDING) {
                nextRepayment = repayment;
                break;
            }
        }

        if (nextRepayment == null) {
            throw new BadRequestException("Loan has been fully repaid");
        }

        BankAccount account = accountRepository.findByIdWithLock(loan.getAccount().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (account.getBalance().compareTo(nextRepayment.getTotalAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(nextRepayment.getTotalAmount()));
        accountRepository.save(account);
        createTransaction(account, TransactionType.LOAN_REPAYMENT, nextRepayment.getTotalAmount(),
                "Loan repayment installment #" + nextRepayment.getInstallmentNumber());

        nextRepayment.setStatus(RepaymentStatus.PAID);
        nextRepayment.setPaidDate(LocalDate.now());
        repaymentRepository.save(nextRepayment);

        loan.setRemainingBalance(nextRepayment.getRemainingBalance());
        boolean completed = true;
        for (LoanRepayment repayment : repayments) {
            if (repayment.getStatus() == RepaymentStatus.PENDING) {
                completed = false;
                break;
            }
        }
        if (completed) {
            loan.setStatus(LoanStatus.COMPLETED);
        }
        loanRepository.save(loan);
    }
}

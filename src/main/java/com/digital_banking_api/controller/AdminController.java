package com.digital_banking_api.controller;

import com.digital_banking_api.dto.request.UserStatusUpdateRequest;
import com.digital_banking_api.dto.response.AccountResponse;
import com.digital_banking_api.dto.response.LoanResponse;
import com.digital_banking_api.dto.response.UserResponse;
import com.digital_banking_api.entity.BankAccount;
import com.digital_banking_api.entity.Loan;
import com.digital_banking_api.entity.User;
import com.digital_banking_api.enums.LoanStatus;
import com.digital_banking_api.enums.UserStatus;
import com.digital_banking_api.mapper.AccountMapper;
import com.digital_banking_api.mapper.UserMapper;
import com.digital_banking_api.response.ApiResponse;
import com.digital_banking_api.service.AccountService;
import com.digital_banking_api.repository.BankAccountRepository;
import com.digital_banking_api.repository.LoanRepository;
import com.digital_banking_api.repository.UserRepository;
import com.digital_banking_api.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Admin management APIs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;
    private final LoanRepository loanRepository;
    private final AccountService accountService;

    public AdminController(UserRepository userRepository,
                           BankAccountRepository bankAccountRepository,
                           LoanRepository loanRepository,
                           AccountService accountService) {
        this.userRepository = userRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.loanRepository = loanRepository;
        this.accountService = accountService;
    }

    @GetMapping("/users")
    @Operation(summary = "List all users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsers() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }

    @PatchMapping("/users/{id}/status")
    @Operation(summary = "Update user status")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusUpdateRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserStatus status = UserStatus.valueOf(request.getStatus().toUpperCase());
        user.setStatus(status);
        User saved = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success(UserMapper.toResponse(saved), "User status updated successfully"));
    }

    @GetMapping("/accounts")
    @Operation(summary = "List all accounts")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccounts() {
        List<AccountResponse> accounts = bankAccountRepository.findAll().stream()
                .map(AccountMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(accounts, "Accounts retrieved successfully"));
    }

    @GetMapping("/loans/pending")
    @Operation(summary = "List pending loans")
    public ResponseEntity<ApiResponse<List<LoanResponse>>> getPendingLoans() {
        List<LoanResponse> loans = loanRepository.findByStatus(LoanStatus.PENDING).stream()
                .map(this::toLoanResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(loans, "Pending loans retrieved successfully"));
    }

    @PatchMapping("/accounts/{id}/freeze")
    @Operation(summary = "Freeze account")
    public ResponseEntity<ApiResponse<Void>> freezeAccount(@PathVariable Long id) {
        accountService.freezeAccount(id, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "Account frozen successfully"));
    }

    @PatchMapping("/accounts/{id}/unfreeze")
    @Operation(summary = "Unfreeze account")
    public ResponseEntity<ApiResponse<Void>> unfreezeAccount(@PathVariable Long id) {
        accountService.unfreezeAccount(id, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "Account unfrozen successfully"));
    }

    private LoanResponse toLoanResponse(Loan loan) {
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

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser().getId();
    }
}

package com.digital_banking_api.controller;

import com.digital_banking_api.dto.request.CreateAccountRequest;
import com.digital_banking_api.dto.request.DepositRequest;
import com.digital_banking_api.dto.request.WithdrawRequest;
import com.digital_banking_api.dto.response.AccountResponse;
import com.digital_banking_api.dto.response.TransactionResponse;
import com.digital_banking_api.response.ApiResponse;
import com.digital_banking_api.security.CustomUserDetails;
import com.digital_banking_api.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Accounts", description = "Bank account management APIs")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @Operation(summary = "Create account")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        Long userId = getCurrentUserId();
        AccountResponse response = accountService.createAccount(request, userId);
        return ResponseEntity.status(201)
                .body(ApiResponse.success(response, "Account created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account by id")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        AccountResponse response = accountService.getAccountById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get my accounts")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getUserAccounts() {
        Long userId = getCurrentUserId();
        List<AccountResponse> response = accountService.getUserAccounts(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/withdraw")
    @Operation(summary = "Withdraw money")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @PathVariable Long id,
            @Valid @RequestBody WithdrawRequest request) {
        Long userId = getCurrentUserId();
        accountService.withdraw(id, request.getAmount(), userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Withdrawal successful"));
    }

    @PostMapping("/{id}/deposit")
    @Operation(summary = "Deposit money")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> deposit(
            @PathVariable Long id,
            @Valid @RequestBody DepositRequest request) {
        Long userId = getCurrentUserId();
        accountService.deposit(id, request.getAmount(), userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Deposit successful"));
    }

    @PatchMapping("/{id}/freeze")
    @Operation(summary = "Freeze account")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> freezeAccount(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        accountService.freezeAccount(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Account frozen"));
    }

    @PatchMapping("/{id}/unfreeze")
    @Operation(summary = "Unfreeze account")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> unfreezeAccount(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        accountService.unfreezeAccount(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Account unfrozen"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Close account")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> closeAccount(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        accountService.closeAccount(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Account closed"));
    }

    @GetMapping("/{id}/transactions")
    @Operation(summary = "Get transaction history")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactions(
            @PathVariable Long id) {
        Long userId = getCurrentUserId();
        List<TransactionResponse> response = accountService.getTransactionHistory(id, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        return userDetails.getUser().getId();
    }
}

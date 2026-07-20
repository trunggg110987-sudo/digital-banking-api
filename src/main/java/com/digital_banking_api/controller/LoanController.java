package com.digital_banking_api.controller;

import com.digital_banking_api.dto.request.ApplyLoanRequest;
import com.digital_banking_api.dto.response.LoanRepaymentResponse;
import com.digital_banking_api.dto.response.LoanResponse;
import com.digital_banking_api.response.ApiResponse;
import com.digital_banking_api.security.CustomUserDetails;
import com.digital_banking_api.service.LoanService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@AllArgsConstructor
public class LoanController {

    private final LoanService loanService;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser().getId();
    }

    @PostMapping("/apply")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<LoanResponse>> applyLoan(
            @RequestBody ApplyLoanRequest request) {

        Long userId = getCurrentUserId();
        LoanResponse response = loanService.applyLoan(request, userId);

        return ResponseEntity.status(201)
                .body(ApiResponse.success(response, "Loan application submitted successfully"));
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<LoanResponse>>> getMyLoans() {

        Long userId = getCurrentUserId();
        List<LoanResponse> response = loanService.getMyLoans(userId);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Loans retrieved successfully")
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<LoanResponse>> getLoanById(
            @PathVariable Long id) {

        Long userId = getCurrentUserId();
        LoanResponse response = loanService.getLoanById(id, userId);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Loan details retrieved successfully")
        );
    }

    @GetMapping("/{id}/schedule")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<LoanRepaymentResponse>>> getRepaymentSchedule(
            @PathVariable Long id) {

        Long userId = getCurrentUserId();
        List<LoanRepaymentResponse> response = loanService.getRepaymentSchedule(id, userId);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Repayment schedule retrieved successfully")
        );
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LoanResponse>> approveLoan(
            @PathVariable Long id) {

        LoanResponse response = loanService.approveLoan(id);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Loan approved successfully")
        );
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LoanResponse>> rejectLoan(
            @PathVariable Long id) {

        LoanResponse response = loanService.rejectLoan(id);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Loan rejected successfully")
        );
    }

    @PatchMapping("/{id}/disburse")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LoanResponse>> disburseLoan(
            @PathVariable Long id) {

        LoanResponse response = loanService.disburseLoan(id);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Loan disbursed successfully")
        );
    }

    @PatchMapping("/{id}/repay")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> repayLoan(
            @PathVariable Long id) {

        Long userId = getCurrentUserId();
        
        loanService.repayLoan(id, userId);

        return ResponseEntity.ok(
                ApiResponse.success(null, "Loan repayment processed successfully")
        );
    }

}

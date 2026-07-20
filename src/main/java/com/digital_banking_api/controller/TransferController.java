package com.digital_banking_api.controller;

import com.digital_banking_api.dto.request.AddBeneficiaryRequest;
import com.digital_banking_api.dto.request.TransferRequest;
import com.digital_banking_api.dto.response.BeneficiaryResponse;
import com.digital_banking_api.dto.response.TransferResponse;
import com.digital_banking_api.response.ApiResponse;
import com.digital_banking_api.security.CustomUserDetails;
import com.digital_banking_api.service.TransferService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transfers")
@AllArgsConstructor
public class TransferController {

    private final TransferService transferService;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        return customUserDetails.getUser().getId();
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TransferResponse>> transfer(@RequestBody TransferRequest request) {
        Long userId = getCurrentUserId();
        TransferResponse response = transferService.transferMoney(
                request.getFromAccountId(),
                request.getToAccountId(),
                request.getAmount(),
                request.getDescription(),
                userId);

        return ResponseEntity.status(200).body(ApiResponse.success(response, "Transfer successful"));
    }

    @GetMapping("{accountId}/history")
    public ResponseEntity<ApiResponse<List<TransferResponse>>> getHistory(@PathVariable("accountId") Long accountId) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(transferService.getTransferHistory(accountId, userId)));
    }

    @PostMapping("/beneficiaries")
    public ResponseEntity<ApiResponse<BeneficiaryResponse>> addBeneficiaryRequest(@RequestBody AddBeneficiaryRequest request) {
        Long userId = getCurrentUserId();
        BeneficiaryResponse response = transferService.addBeneficiary(request, userId);
        return ResponseEntity.status(201).body(ApiResponse.success(response, "Add beneficiary successful"));
    }

    @GetMapping("/beneficiaries")
    public ResponseEntity<ApiResponse<List<BeneficiaryResponse>>> getBeneficiaries() {
        Long userId = getCurrentUserId();
        return ResponseEntity.status(200).body(ApiResponse.success(transferService.getBeneficiaries(userId), "get Beneficiaries successful"));
    }

    @DeleteMapping("/beneficiaries/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBeneficiary(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        transferService.deleteBeneficiary(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Delete Beneficiary successful"));
    }
}

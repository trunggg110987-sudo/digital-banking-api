package com.digital_banking_api.dto.request;

import com.digital_banking_api.enums.LoanType;
import lombok.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplyLoanRequest {

    @NotNull(message = "Account id is required")
    private Long accountId;

    @NotNull(message = "Loan type is required")
    private LoanType loanType;

    @NotNull(message = "Principal amount is required")
    @Positive(message = "Principal amount must be greater than zero")
    private BigDecimal principalAmount;

    @NotNull(message = "Term months is required")
    @Positive(message = "Term months must be greater than zero")
    private Integer termMonths;

    @NotNull(message = "Interest rate is required")
    @Positive(message = "Interest rate must be greater than zero")
    private BigDecimal interestRate;

}
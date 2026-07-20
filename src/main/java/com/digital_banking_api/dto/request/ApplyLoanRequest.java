package com.digital_banking_api.dto.request;

import com.digital_banking_api.enums.LoanType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplyLoanRequest {

    private Long accountId;

    private LoanType loanType;

    private BigDecimal principalAmount;

    private Integer termMonths;

    private BigDecimal interestRate;

}
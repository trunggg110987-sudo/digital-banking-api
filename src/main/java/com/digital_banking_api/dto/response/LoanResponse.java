package com.digital_banking_api.dto.response;

import com.digital_banking_api.enums.LoanStatus;
import com.digital_banking_api.enums.LoanType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanResponse {

    private Long id;

    private LoanType loanType;

    private BigDecimal principalAmount;

    private BigDecimal monthlyPayment;

    private BigDecimal remainingBalance;

    private Integer termMonths;

    private BigDecimal interestRate;

    private LoanStatus status;

    private LocalDate startDate;

    private LocalDate endDate;

}
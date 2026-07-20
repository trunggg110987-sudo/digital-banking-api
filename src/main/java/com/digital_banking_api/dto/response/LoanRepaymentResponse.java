package com.digital_banking_api.dto.response;

import com.digital_banking_api.enums.RepaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanRepaymentResponse {

    private Integer installmentNumber;

    private LocalDate dueDate;

    private LocalDate paidDate;

    private BigDecimal principalAmount;

    private BigDecimal interestAmount;

    private BigDecimal totalAmount;

    private BigDecimal remainingBalance;

    private RepaymentStatus status;

}
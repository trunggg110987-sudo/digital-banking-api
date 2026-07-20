package com.digital_banking_api.util;

import com.digital_banking_api.entity.Loan;
import com.digital_banking_api.entity.LoanRepayment;
import com.digital_banking_api.enums.RepaymentStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class LoanCalculator {

    private static final MathContext MC = new MathContext(20);

    public BigDecimal calculateMonthlyPayment(
            BigDecimal principal,
            BigDecimal annualInterestRate,
            Integer months) {

        BigDecimal monthlyRate = annualInterestRate
                .divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);

        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(
                    BigDecimal.valueOf(months),
                    2,
                    RoundingMode.HALF_UP);
        }

        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);

        BigDecimal numerator =
                principal.multiply(monthlyRate)
                        .multiply(onePlusR.pow(months, MC));

        BigDecimal denominator =
                onePlusR.pow(months, MC)
                        .subtract(BigDecimal.ONE);

        return numerator.divide(
                denominator,
                2,
                RoundingMode.HALF_UP);
    }

    public List<LoanRepayment> generateRepaymentSchedule(Loan loan) {

        List<LoanRepayment> schedules = new ArrayList<>();

        BigDecimal remaining = loan.getPrincipalAmount();

        BigDecimal monthlyRate =
                loan.getInterestRate()
                        .divide(BigDecimal.valueOf(1200),10,RoundingMode.HALF_UP);

        BigDecimal monthlyPayment = loan.getMonthlyPayment();

        LocalDate dueDate = loan.getStartDate();

        for(int i=1;i<=loan.getTermMonths();i++){

            BigDecimal interest =
                    remaining.multiply(monthlyRate)
                            .setScale(2,RoundingMode.HALF_UP);

            BigDecimal principal;
            BigDecimal totalAmount;

            // Last installment: adjust to avoid rounding drift
            if(i == loan.getTermMonths()){
                principal = remaining;
                totalAmount = principal.add(interest);
            } else {
                principal =
                        monthlyPayment.subtract(interest);

                if(principal.compareTo(BigDecimal.ZERO) < 0){
                    principal = BigDecimal.ZERO;
                }
                totalAmount = monthlyPayment;
            }

            remaining =
                    remaining.subtract(principal);

            if(remaining.compareTo(BigDecimal.ZERO)<0){
                remaining = BigDecimal.ZERO;
            }

            LoanRepayment repayment = new LoanRepayment();

            repayment.setLoan(loan);

            repayment.setInstallmentNumber(i);

            repayment.setDueDate(dueDate.plusMonths(i));

            repayment.setPrincipalAmount(principal);

            repayment.setInterestAmount(interest);

            repayment.setTotalAmount(totalAmount);

            repayment.setRemainingBalance(remaining);

            repayment.setStatus(RepaymentStatus.PENDING);

            schedules.add(repayment);
        }

        return schedules;
    }

}
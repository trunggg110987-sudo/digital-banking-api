package com.digital_banking_api.service;

import com.digital_banking_api.dto.request.ApplyLoanRequest;
import com.digital_banking_api.dto.response.LoanRepaymentResponse;
import com.digital_banking_api.dto.response.LoanResponse;

import java.util.List;

public interface LoanService {

    LoanResponse applyLoan(ApplyLoanRequest request, Long userId);

    List<LoanResponse> getMyLoans(Long userId);

    LoanResponse getLoanById(Long loanId, Long userId);

    List<LoanRepaymentResponse> getRepaymentSchedule(Long loanId, Long userId);

    LoanResponse approveLoan(Long loanId);

    LoanResponse rejectLoan(Long loanId);

    LoanResponse disburseLoan(Long loanId);

    void repayLoan(Long loanId, Long userId);

}
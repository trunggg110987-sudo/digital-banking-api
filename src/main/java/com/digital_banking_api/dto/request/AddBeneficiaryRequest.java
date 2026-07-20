package com.digital_banking_api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddBeneficiaryRequest {
    private String accountNumber;
    private String bankName;
    private String accountHolderName;
    private String nickname;
}
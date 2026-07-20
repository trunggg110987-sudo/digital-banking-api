package com.digital_banking_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BeneficiaryResponse {
    private Long id;
    private String accountNumber;
    private String bankName;
    private String accountHolderName;
    private String nickname;
    private String status;
}
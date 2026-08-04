package com.digital_banking_api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddBeneficiaryRequest {
    @NotBlank(message = "Account number is required")
    private String accountNumber;
    @NotBlank(message = "Bank name is required")
    private String bankName;
    @NotBlank(message = "Account holder name is required")
    private String accountHolderName;
    private String nickname;
}

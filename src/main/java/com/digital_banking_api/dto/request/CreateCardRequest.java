package com.digital_banking_api.dto.request;

import com.digital_banking_api.enums.CardType;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class CreateCardRequest {

    @NotNull(message = "Account id is required")
    private Long accountId;

    @NotNull(message = "Card type is required")
    private CardType cardType;

}

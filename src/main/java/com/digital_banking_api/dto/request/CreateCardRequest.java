package com.digital_banking_api.dto.request;

import com.digital_banking_api.enums.CardType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCardRequest {

    private Long accountId;

    private CardType cardType;

}
package com.digital_banking_api.dto.response;

import com.digital_banking_api.enums.CardStatus;
import com.digital_banking_api.enums.CardType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class CardResponse {

    private Long id;

    private String cardNumber;

    private String holderName;

    private LocalDate expiryDate;

    private CardType cardType;

    private CardStatus status;

    private LocalDateTime createdAt;

}
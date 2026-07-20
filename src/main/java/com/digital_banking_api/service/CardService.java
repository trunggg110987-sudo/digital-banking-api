package com.digital_banking_api.service;

import com.digital_banking_api.dto.request.CreateCardRequest;
import com.digital_banking_api.dto.response.CardResponse;

import java.util.List;

public interface CardService {

    CardResponse issueCard(CreateCardRequest request, Long userId);

    List<CardResponse> getUserCards(Long userId);

    void blockCard(Long cardId, Long userId);

    void unblockCard(Long cardId, Long userId);

}
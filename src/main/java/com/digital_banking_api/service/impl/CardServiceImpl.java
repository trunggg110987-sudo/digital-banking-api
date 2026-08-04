package com.digital_banking_api.service.impl;

import com.digital_banking_api.dto.request.CreateCardRequest;
import com.digital_banking_api.dto.response.CardResponse;
import com.digital_banking_api.entity.BankAccount;
import com.digital_banking_api.entity.Card;
import com.digital_banking_api.enums.AccountStatus;
import com.digital_banking_api.enums.CardStatus;
import com.digital_banking_api.exception.BadRequestException;
import com.digital_banking_api.exception.ResourceNotFoundException;
import com.digital_banking_api.exception.UnauthorizedException;
import com.digital_banking_api.repository.BankAccountRepository;
import com.digital_banking_api.repository.CardRepository;
import com.digital_banking_api.service.CardService;
import com.digital_banking_api.util.CardNumberGenerator;
import com.digital_banking_api.util.CvvGenerator;
import com.digital_banking_api.util.CvvHasher;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import java.time.LocalDate;

@Service
@Transactional
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final BankAccountRepository accountRepository;
    private final CardNumberGenerator cardNumberGenerator;
    private final CvvGenerator cvvGenerator;

    public CardServiceImpl(CardRepository cardRepository,
                           BankAccountRepository accountRepository,
                           CardNumberGenerator cardNumberGenerator,
                           CvvGenerator cvvGenerator) {
        this.cardRepository = cardRepository;
        this.accountRepository = accountRepository;
        this.cardNumberGenerator = cardNumberGenerator;
        this.cvvGenerator = cvvGenerator;
    }

    private CardResponse convertToCardResponse(Card card) {

        CardResponse response = new CardResponse();

        response.setId(card.getId());
        response.setCardNumber(maskCardNumber(card.getCardNumber()));
        response.setHolderName(card.getHolderName());
        response.setExpiryDate(card.getExpiryDate());
        response.setCardType(card.getCardType());
        response.setStatus(card.getStatus());
        response.setCreatedAt(card.getCreatedAt());

        return response;
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + lastFour;
    }

    @Override
    public CardResponse issueCard(CreateCardRequest request, Long userId) {

        if(request == null){
            throw new BadRequestException("Request cannot be null");
        }

        BankAccount account = accountRepository
                .findByIdAndUserId(request.getAccountId(), userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Account not found"));

        if(account.getStatus() != AccountStatus.ACTIVE){
            throw new BadRequestException("Account must be ACTIVE");
        }

        Card card = new Card();

        card.setAccount(account);
        card.setHolderName(account.getUser().getFullName());
        card.setCardNumber(cardNumberGenerator.generateCardNumber());
        String cvv = cvvGenerator.generateCVV();
        card.setCvv(CvvHasher.hashCVV(cvv));
        card.setExpiryDate(LocalDate.now().plusYears(5));
        card.setCardType(request.getCardType());
        card.setStatus(CardStatus.ACTIVE);

        Card saved = cardRepository.save(card);

        return convertToCardResponse(saved);
    }

    @Override
    public List<CardResponse> getUserCards(Long userId) {

        List<Card> cards = cardRepository.findByAccountUserId(userId);

        List<CardResponse> responses = new ArrayList<>();

        for(Card card : cards){
            responses.add(convertToCardResponse(card));
        }

        return responses;
    }

    @Override
    public void blockCard(Long cardId, Long userId) {

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Card not found"));

        if(!card.getAccount().getUser().getId().equals(userId)){
            throw new UnauthorizedException("Cannot block another user's card");
        }

        if(card.getStatus() == CardStatus.BLOCKED){
            throw new BadRequestException("Card already blocked");
        }

        card.setStatus(CardStatus.BLOCKED);

        cardRepository.save(card);
    }

    @Override
    public void unblockCard(Long cardId, Long userId) {

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Card not found"));

        if(!card.getAccount().getUser().getId().equals(userId)){
            throw new UnauthorizedException("Cannot unblock another user's card");
        }

        if(card.getStatus() != CardStatus.BLOCKED){
            throw new BadRequestException("Card is not blocked");
        }

        card.setStatus(CardStatus.ACTIVE);

        cardRepository.save(card);
    }

}
package com.digital_banking_api.service.impl;

import com.digital_banking_api.dto.request.CreateCardRequest;
import com.digital_banking_api.dto.response.CardResponse;
import com.digital_banking_api.entity.BankAccount;
import com.digital_banking_api.entity.Card;
import com.digital_banking_api.entity.Role;
import com.digital_banking_api.entity.User;
import com.digital_banking_api.enums.AccountStatus;
import com.digital_banking_api.enums.CardStatus;
import com.digital_banking_api.enums.CardType;
import com.digital_banking_api.exception.BadRequestException;
import com.digital_banking_api.repository.BankAccountRepository;
import com.digital_banking_api.repository.CardRepository;
import com.digital_banking_api.util.CardNumberGenerator;
import com.digital_banking_api.util.CvvGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {
    @Mock CardRepository cardRepository;
    @Mock BankAccountRepository accountRepository;
    @Mock CardNumberGenerator cardNumberGenerator;
    @Mock CvvGenerator cvvGenerator;
    @InjectMocks CardServiceImpl cardService;

    private User user;
    private BankAccount account;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setName("CUSTOMER");
        user = new User();
        user.setId(1L);
        user.setFullName("User");
        user.setRole(role);
        account = new BankAccount();
        account.setId(1L);
        account.setUser(user);
        account.setStatus(AccountStatus.ACTIVE);
    }

    @Test
    void issueCard_success() {
        CreateCardRequest request = new CreateCardRequest();
        request.setAccountId(1L);
        request.setCardType(CardType.DEBIT);
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(account));
        when(cardNumberGenerator.generateCardNumber()).thenReturn("1234567812345678");
        when(cvvGenerator.generateCVV()).thenReturn("123");
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CardResponse response = cardService.issueCard(request, 1L);

        assertTrue(response.getCardNumber().endsWith("5678"));
    }

    @Test
    void issueCard_inactiveAccount() {
        account.setStatus(AccountStatus.CLOSED);
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(account));
        CreateCardRequest request = new CreateCardRequest();
        request.setAccountId(1L);
        request.setCardType(CardType.DEBIT);
        assertThrows(BadRequestException.class, () -> cardService.issueCard(request, 1L));
    }

    @Test
    void getUserCards_success() {
        Card card = new Card();
        card.setAccount(account);
        card.setCardNumber("1234567812345678");
        card.setStatus(CardStatus.ACTIVE);
        when(cardRepository.findByAccountUserId(1L)).thenReturn(List.of(card));

        assertEquals(1, cardService.getUserCards(1L).size());
    }
}

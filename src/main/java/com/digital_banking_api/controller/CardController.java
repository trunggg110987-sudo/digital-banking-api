package com.digital_banking_api.controller;

import com.digital_banking_api.dto.request.CreateCardRequest;
import com.digital_banking_api.dto.response.CardResponse;
import com.digital_banking_api.response.ApiResponse;
import com.digital_banking_api.security.CustomUserDetails;
import com.digital_banking_api.service.CardService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@AllArgsConstructor
public class CardController {

    private final CardService cardService;

    private Long getCurrentUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return userDetails.getUser().getId();
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CardResponse>> issueCard(
            @RequestBody CreateCardRequest request){

        Long userId = getCurrentUserId();

        CardResponse response =
                cardService.issueCard(request,userId);

        return ResponseEntity.status(201)
                .body(ApiResponse.success(response,
                        "Card issued successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CardResponse>>> getCards(){

        Long userId = getCurrentUserId();

        return ResponseEntity.ok(
                ApiResponse.success(cardService.getUserCards(userId)));
    }

    @PatchMapping("/{id}/block")
    public ResponseEntity<ApiResponse<Void>> blockCard(
            @PathVariable Long id){

        Long userId = getCurrentUserId();

        cardService.blockCard(id,userId);

        return ResponseEntity.ok(
                ApiResponse.success(null, "Card blocked successfully")
        );
    }

    @PatchMapping("/{id}/unblock")
    public ResponseEntity<ApiResponse<Void>> unblockCard(
            @PathVariable Long id){

        Long userId = getCurrentUserId();

        cardService.unblockCard(id,userId);

        return ResponseEntity.ok(
                ApiResponse.success(null, "Card unblocked successfully")
        );
    }

}
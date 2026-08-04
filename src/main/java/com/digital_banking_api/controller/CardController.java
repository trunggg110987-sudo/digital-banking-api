package com.digital_banking_api.controller;

import com.digital_banking_api.dto.request.CreateCardRequest;
import com.digital_banking_api.dto.response.CardResponse;
import com.digital_banking_api.response.ApiResponse;
import com.digital_banking_api.security.CustomUserDetails;
import com.digital_banking_api.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@AllArgsConstructor
@Tag(name = "Cards", description = "Card management APIs")
public class CardController {

    private final CardService cardService;

    private Long getCurrentUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return userDetails.getUser().getId();
    }

    @PostMapping
    @Operation(summary = "Issue card")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<CardResponse>> issueCard(
            @Valid @RequestBody CreateCardRequest request){

        Long userId = getCurrentUserId();

        CardResponse response =
                cardService.issueCard(request,userId);

        return ResponseEntity.status(201)
                .body(ApiResponse.success(response,
                        "Card issued successfully"));
    }

    @GetMapping
    @Operation(summary = "Get my cards")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<List<CardResponse>>> getCards(){

        Long userId = getCurrentUserId();

        return ResponseEntity.ok(
                ApiResponse.success(cardService.getUserCards(userId)));
    }

    @PatchMapping("/{id}/block")
    @Operation(summary = "Block card")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> blockCard(
            @PathVariable Long id){

        Long userId = getCurrentUserId();

        cardService.blockCard(id,userId);

        return ResponseEntity.ok(
                ApiResponse.success(null, "Card blocked successfully")
        );
    }

    @PatchMapping("/{id}/unblock")
    @Operation(summary = "Unblock card")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> unblockCard(
            @PathVariable Long id){

        Long userId = getCurrentUserId();

        cardService.unblockCard(id,userId);

        return ResponseEntity.ok(
                ApiResponse.success(null, "Card unblocked successfully")
        );
    }

}

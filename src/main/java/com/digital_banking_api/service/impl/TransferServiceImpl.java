package com.digital_banking_api.service.impl;

import com.digital_banking_api.dto.request.AddBeneficiaryRequest;
import com.digital_banking_api.dto.response.BeneficiaryResponse;
import com.digital_banking_api.dto.response.TransferResponse;
import com.digital_banking_api.entity.*;
import com.digital_banking_api.enums.AccountStatus;
import com.digital_banking_api.enums.TransactionStatus;
import com.digital_banking_api.enums.TransactionType;
import com.digital_banking_api.enums.TransferStatus;
import com.digital_banking_api.exception.*;
import com.digital_banking_api.repository.*;
import com.digital_banking_api.service.TransferService;
import com.digital_banking_api.util.TransferReferenceGenerator;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class TransferServiceImpl implements TransferService {

    private final TransferRepository transferRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;
    private final BankAccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransferReferenceGenerator transferReferenceGenerator;

    private TransferResponse convertToTransferResponse(Transfer transfer) {
        TransferResponse response = new TransferResponse();

        response.setId(transfer.getId());
        response.setFromAccountNumber(transfer.getFromAccount().getAccountNumber());
        response.setToAccountNumber(transfer.getToAccount().getAccountNumber());
        response.setAmount(transfer.getAmount());
        response.setDescription(transfer.getDescription());
        response.setStatus(transfer.getStatus().name());
        response.setReference(transfer.getReference());
        response.setCreatedAt(transfer.getCreatedAt());

        return response;
    }

    private BeneficiaryResponse convertToBeneficiaryResponse(Beneficiary beneficiary) {
        BeneficiaryResponse response = new BeneficiaryResponse();

        response.setId(beneficiary.getId());
        response.setAccountNumber(beneficiary.getAccountNumber());
        response.setNickname(beneficiary.getNickname());
        response.setBankName(beneficiary.getBankName());
        response.setAccountHolderName(beneficiary.getAccountHolderName());
        response.setStatus(beneficiary.getStatus());

        return response;
    }

    @Override
    @Transactional
    public TransferResponse transferMoney(Long fromAccountId, Long toAccountId, BigDecimal amount, String description, Long userId) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }

        if (fromAccountId == null || toAccountId == null) {
            throw new BadRequestException("fromAccountId or toAccountId must not be null");
        }

        if (fromAccountId.equals(toAccountId)) {
            throw new BadRequestException("không thể chuyển tiền vào cùng 1 tài khoản!");
        }

        if (description == null || description.isBlank()) {
            throw new BadRequestException("Description must not be blank");
        }

        BankAccount firstLock;
        BankAccount secondLock;

        if (fromAccountId < toAccountId) {

            firstLock = accountRepository.findByIdAndUserIdWithLock(fromAccountId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("From account not found"));

            secondLock = accountRepository.findByIdWithLock(toAccountId)
                    .orElseThrow(() -> new ResourceNotFoundException("To account not found"));

        } else {
            firstLock = accountRepository.findByIdWithLock(toAccountId)
                    .orElseThrow(() -> new ResourceNotFoundException("To account not found"));

            secondLock = accountRepository.findByIdAndUserIdWithLock(fromAccountId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("From account not found"));
        }

        BankAccount fromAccount;
        BankAccount toAccount;

        if (fromAccountId < toAccountId) {
            fromAccount = firstLock;
            toAccount = secondLock;
        } else {
            fromAccount = secondLock;
            toAccount = firstLock;
        }

        System.out.println("From Status = " + fromAccount.getStatus());
        System.out.println("To Status = " + toAccount.getStatus());

        if (fromAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("From account status must be ACTIVE");
        }

        if (toAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("To account status must be ACTIVE");
        }

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Số dư không đủ để thực hiện giao dịch!");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        accountRepository.save(fromAccount);
        toAccount.setBalance(toAccount.getBalance().add(amount));
        accountRepository.save(toAccount);

        Transfer transfer = new Transfer();
        transfer.setReference(transferReferenceGenerator.generateReferenceNumber());
        transfer.setFromAccount(fromAccount);
        transfer.setToAccount(toAccount);
        transfer.setDescription(description);
        transfer.setAmount(amount);
        transfer.setStatus(TransferStatus.SUCCESS);

        Transaction transferOut = new Transaction();
        transferOut.setAccount(fromAccount);
        transferOut.setType(TransactionType.TRANSFER_OUT);
        transferOut.setAmount(amount);
        transferOut.setDescription("Transfer to " + toAccount.getAccountNumber());
        transferOut.setBalanceAfter(fromAccount.getBalance());
        transferOut.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(transferOut);

        Transaction transferIn = new Transaction();
        transferIn.setType(TransactionType.TRANSFER_IN);
        transferIn.setAccount(toAccount);
        transferIn.setDescription("Received from  " + fromAccount.getAccountNumber());
        transferIn.setAmount(amount);
        transferIn.setBalanceAfter(toAccount.getBalance());
        transferIn.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(transferIn);

        Transfer savedTransfer = transferRepository.save(transfer);
        return convertToTransferResponse(savedTransfer);
    }

    @Override
    public List<TransferResponse> getTransferHistory(Long accountId, Long userId) {
        accountRepository.findByIdAndUserId(accountId, userId).orElseThrow(()
                -> new ResourceNotFoundException("Account not found"));

        List<Transfer> outgoing = transferRepository.findByFromAccountIdOrderByCreatedAtDesc(accountId);

        List<Transfer> incoming = transferRepository.findByToAccountIdOrderByCreatedAtDesc(accountId);

        List<Transfer> transfers = new ArrayList<>();

        transfers.addAll(outgoing);
        transfers.addAll(incoming);

        transfers.sort(Comparator.comparing(Transfer::getCreatedAt).reversed());

        List<TransferResponse> responses = new ArrayList<>();

        for (Transfer transfer : transfers) {
            responses.add(convertToTransferResponse(transfer));
        }

        return responses;
    }

    @Override
    public BeneficiaryResponse addBeneficiary(AddBeneficiaryRequest request, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(()
                -> new ResourceNotFoundException("Account not found"));

        if (request == null) {
            throw new BadRequestException("Request cannot be null");
        }

        if (request.getAccountNumber() == null || request.getAccountNumber().isBlank()) {
            throw new BadRequestException("Account number must not be blank");
        }

        if (request.getBankName() == null || request.getBankName().isBlank()) {
            throw new BadRequestException("Bank name must not be blank");
        }

        if (request.getAccountHolderName() == null || request.getAccountHolderName().isBlank()) {
            throw new BadRequestException("Account holder name must not be blank");
        }

        BankAccount bankAccount = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found"));

        if (beneficiaryRepository.existsByAccountNumberAndUserId(request.getAccountNumber(), userId)) {
            throw new BadRequestException("Beneficiary already exists");
        }

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setAccountNumber(bankAccount.getAccountNumber());
        beneficiary.setBankName(request.getBankName());
        beneficiary.setAccountHolderName(bankAccount.getUser().getFullName());
        beneficiary.setNickname(request.getNickname());
        beneficiary.setUser(user);
        beneficiary.setStatus(beneficiary.getStatus());

        Beneficiary saved = beneficiaryRepository.save(beneficiary);
        return convertToBeneficiaryResponse(saved);
    }

    @Override
    public List<BeneficiaryResponse> getBeneficiaries(Long userId) {
        userRepository.findById(userId).orElseThrow(()
                -> new ResourceNotFoundException("User not found"));

        List<Beneficiary> beneficiaries = beneficiaryRepository.findByUserId(userId);

        List<BeneficiaryResponse> responses = new ArrayList<>();

        for (Beneficiary beneficiary : beneficiaries) {
            responses.add(convertToBeneficiaryResponse(beneficiary));
        }

        if(beneficiaries.isEmpty()){
            throw new ResourceNotFoundException("cannot find beneficiary");
        }

        return responses;

    }

    @Override
    public void deleteBeneficiary(Long beneficiaryId, Long userId) {

        Beneficiary beneficiary = beneficiaryRepository.findByIdAndUserId(beneficiaryId, userId).orElseThrow(()
                -> new ResourceNotFoundException("Beneficiary not found or access denied"));

        beneficiaryRepository.delete(beneficiary);
    }
}

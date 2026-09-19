package com.appfinanceiro.service;

import com.appfinanceiro.domain.Account;
import com.appfinanceiro.domain.User;
import com.appfinanceiro.dto.request.AccountRequestDTO;
import com.appfinanceiro.dto.request.TransferRequestDTO;
import com.appfinanceiro.dto.response.AccountResponseDTO;
import com.appfinanceiro.exception.ResourceNotFoundException;
import com.appfinanceiro.repository.AccountRepository;
import com.appfinanceiro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AccountResponseDTO> findAllByUser(UUID userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponseDTO findByIdAndUser(UUID id, UUID userId) {
        Account account = accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta", "id", id));
        return mapToResponse(account);
    }

    @Transactional
    public AccountResponseDTO create(UUID userId, AccountRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", userId));

        Account account = Account.builder()
                .user(user)
                .name(request.name())
                .type(request.type())
                .initialBalance(request.initialBalance())
                .currentBalance(request.initialBalance())
                .bankName(request.bankName())
                .color(request.color() != null ? request.color() : "#10B981")
                .icon(request.icon() != null ? request.icon() : "Landmark")
                .build();

        account = accountRepository.save(account);
        return mapToResponse(account);
    }

    @Transactional
    public AccountResponseDTO update(UUID id, UUID userId, AccountRequestDTO request) {
        Account account = accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta", "id", id));

        account.setName(request.name());
        account.setType(request.type());
        account.setBankName(request.bankName());
        if (request.color() != null) account.setColor(request.color());
        if (request.icon() != null) account.setIcon(request.icon());

        account = accountRepository.save(account);
        return mapToResponse(account);
    }

    @Transactional
    public void delete(UUID id, UUID userId) {
        Account account = accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta", "id", id));
        accountRepository.delete(account);
    }

    @Transactional
    public void transfer(UUID userId, TransferRequestDTO request) {
        if (request.sourceAccountId().equals(request.destinationAccountId())) {
            throw new IllegalArgumentException("As contas de origem e destino devem ser diferentes.");
        }

        Account source = accountRepository.findByIdAndUserId(request.sourceAccountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta de origem", "id", request.sourceAccountId()));

        Account destination = accountRepository.findByIdAndUserId(request.destinationAccountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta de destino", "id", request.destinationAccountId()));

        source.setCurrentBalance(source.getCurrentBalance().subtract(request.amount()));
        destination.setCurrentBalance(destination.getCurrentBalance().add(request.amount()));

        accountRepository.save(source);
        accountRepository.save(destination);
    }

    public AccountResponseDTO mapToResponse(Account account) {
        return new AccountResponseDTO(
                account.getId(),
                account.getName(),
                account.getType(),
                account.getInitialBalance(),
                account.getCurrentBalance(),
                account.getBankName(),
                account.getColor(),
                account.getIcon()
        );
    }
}

package com.appfinanceiro.service;

import com.appfinanceiro.domain.*;
import com.appfinanceiro.domain.enums.PaymentMethod;
import com.appfinanceiro.domain.enums.TransactionStatus;
import com.appfinanceiro.domain.enums.TransactionType;
import com.appfinanceiro.dto.request.AccountRequestDTO;
import com.appfinanceiro.dto.request.TransferRequestDTO;
import com.appfinanceiro.dto.response.AccountResponseDTO;
import com.appfinanceiro.exception.ResourceNotFoundException;
import com.appfinanceiro.repository.AccountRepository;
import com.appfinanceiro.repository.CategoryRepository;
import com.appfinanceiro.repository.TransactionRepository;
import com.appfinanceiro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final AccountBalanceService accountBalanceService;

    private static final UUID TRANSFER_CATEGORY_ID = UUID.fromString("00000000-0000-0000-0000-00000000000C");

    @Transactional(readOnly = true)
    public List<AccountResponseDTO> findAllByUser(UUID userId) {
        return accountRepository.findByUserIdAndArchivedAtIsNull(userId).stream()
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
                .bankName(request.bankName())
                .color(request.color() != null ? request.color() : "#10B981")
                .icon(request.icon() != null ? request.icon() : "Landmark")
                .build();

        account = accountRepository.save(account);
        return mapToResponse(account);
    }

    @Transactional
    public AccountResponseDTO update(UUID id, UUID userId, AccountRequestDTO request) {
        Account account = accountRepository.findByIdAndUserIdAndArchivedAtIsNull(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta", "id", id));

        account.setName(request.name());
        account.setType(request.type());
        account.setBankName(request.bankName());
        if (request.color() != null) account.setColor(request.color());
        if (request.icon() != null) account.setIcon(request.icon());

        account = accountRepository.save(account);
        return mapToResponse(account);
    }

    /**
     * Exclui conta se não tem transações. Se tem histórico, arquiva (soft-delete).
     * @return true se foi arquivada, false se foi excluída fisicamente
     */
    @Transactional
    public boolean delete(UUID id, UUID userId) {
        Account account = accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta", "id", id));

        if (transactionRepository.existsByAccountId(account.getId())) {
            account.setArchivedAt(LocalDateTime.now());
            accountRepository.save(account);
            return true;
        } else {
            accountRepository.delete(account);
            return false;
        }
    }

    /**
     * Transferência entre contas: cria duas Transaction linkadas por transfer_group_id.
     * A conta origem recebe EXPENSE (OUT) e a destino recebe INCOME (IN).
     * O saldo é derivado automaticamente.
     */
    @Transactional
    public void transfer(UUID userId, TransferRequestDTO request) {
        if (request.sourceAccountId().equals(request.destinationAccountId())) {
            throw new IllegalArgumentException("As contas de origem e destino devem ser diferentes.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", userId));

        Account source = accountRepository.findByIdAndUserIdAndArchivedAtIsNull(request.sourceAccountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta de origem", "id", request.sourceAccountId()));

        Account destination = accountRepository.findByIdAndUserIdAndArchivedAtIsNull(request.destinationAccountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta de destino", "id", request.destinationAccountId()));

        Category transferCategory = categoryRepository.findById(TRANSFER_CATEGORY_ID)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria de transferência", "id", TRANSFER_CATEGORY_ID));

        UUID groupId = UUID.randomUUID();
        String desc = request.description() != null && !request.description().isBlank()
                ? request.description()
                : "Transferência: " + source.getName() + " → " + destination.getName();

        // Transação de saída (conta origem)
        Transaction outgoing = Transaction.builder()
                .user(user)
                .account(source)
                .category(transferCategory)
                .description(desc)
                .amount(request.amount())
                .date(request.date())
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.COMPLETED)
                .paymentMethod(PaymentMethod.TRANSFER)
                .transferGroupId(groupId)
                .transferDirection("OUT")
                .notes(request.notes())
                .build();

        // Transação de entrada (conta destino)
        Transaction incoming = Transaction.builder()
                .user(user)
                .account(destination)
                .category(transferCategory)
                .description(desc)
                .amount(request.amount())
                .date(request.date())
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.COMPLETED)
                .paymentMethod(PaymentMethod.TRANSFER)
                .transferGroupId(groupId)
                .transferDirection("IN")
                .notes(request.notes())
                .build();

        transactionRepository.save(outgoing);
        transactionRepository.save(incoming);
    }

    public AccountResponseDTO mapToResponse(Account account) {
        BigDecimal currentBalance = accountBalanceService.getBalance(account.getId());
        return new AccountResponseDTO(
                account.getId(),
                account.getName(),
                account.getType(),
                account.getInitialBalance(),
                currentBalance,
                account.getBankName(),
                account.getColor(),
                account.getIcon(),
                account.isArchived()
        );
    }
}

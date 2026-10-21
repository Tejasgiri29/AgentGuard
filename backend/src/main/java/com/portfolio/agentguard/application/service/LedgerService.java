package com.portfolio.agentguard.application.service;

import com.portfolio.agentguard.domain.exception.LedgerImbalanceException;
import com.portfolio.agentguard.domain.exception.ResourceNotFoundException;
import com.portfolio.agentguard.domain.model.JournalEntry;
import com.portfolio.agentguard.domain.model.JournalLine;
import com.portfolio.agentguard.domain.model.LedgerAccount;
import com.portfolio.agentguard.domain.repository.JournalEntryRepository;
import com.portfolio.agentguard.domain.repository.LedgerAccountRepository;
import com.portfolio.agentguard.domain.valueobject.AccountType;
import com.portfolio.agentguard.domain.valueobject.EntryType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerService {

    private final LedgerAccountRepository accountRepository;
    private final JournalEntryRepository journalEntryRepository;

    public record LineItem(String accountId, EntryType entryType, BigDecimal amount) {}

    public record TrialBalance(BigDecimal totalDebits, BigDecimal totalCredits, boolean isBalanced) {}

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public JournalEntry postJournalEntry(String tenantId, String referenceId, String description, List<LineItem> lineItems) {
        log.info("Posting double-entry journal for ref: {} in tenant: {}", referenceId, tenantId);

        BigDecimal sumDebits = BigDecimal.ZERO;
        BigDecimal sumCredits = BigDecimal.ZERO;

        for (LineItem item : lineItems) {
            if (item.amount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Line amount must be positive: " + item.amount());
            }
            if (item.entryType() == EntryType.DEBIT) {
                sumDebits = sumDebits.add(item.amount());
            } else {
                sumCredits = sumCredits.add(item.amount());
            }
        }

        // Invariant check: Debits must strictly equal Credits
        if (sumDebits.compareTo(sumCredits) != 0) {
            throw new LedgerImbalanceException(
                "Double-entry invariant violated! Debits ($" + sumDebits + ") do not balance Credits ($" + sumCredits + ")"
            );
        }

        JournalEntry entry = JournalEntry.builder()
            .id("je-" + UUID.randomUUID())
            .tenantId(tenantId)
            .referenceId(referenceId)
            .description(description)
            .reversal(false)
            .build();

        for (LineItem item : lineItems) {
            LedgerAccount account = accountRepository.findWithLockByIdAndTenantId(item.accountId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ledger account not found: " + item.accountId()));

            // Update balance according to accounting equation
            // Normal balance: ASSET / EXPENSE increase with DEBIT; LIABILITY / EQUITY / REVENUE increase with CREDIT
            BigDecimal newBalance = calculateNewBalance(account.getType(), account.getBalance(), item.entryType(), item.amount());
            account.setBalance(newBalance);
            accountRepository.save(account);

            JournalLine line = JournalLine.builder()
                .id("jl-" + UUID.randomUUID())
                .accountId(account.getId())
                .entryType(item.entryType())
                .amount(item.amount())
                .build();

            entry.addLine(line);
        }

        return journalEntryRepository.save(entry);
    }

    public TrialBalance computeTrialBalance(String tenantId) {
        List<LedgerAccount> accounts = accountRepository.findByTenantId(tenantId);
        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        for (LedgerAccount acc : accounts) {
            if (acc.getType() == AccountType.ASSET || acc.getType() == AccountType.EXPENSE) {
                totalDebits = totalDebits.add(acc.getBalance());
            } else {
                totalCredits = totalCredits.add(acc.getBalance());
            }
        }

        boolean balanced = totalDebits.compareTo(totalCredits) == 0;
        return new TrialBalance(totalDebits, totalCredits, balanced);
    }

    private BigDecimal calculateNewBalance(AccountType type, BigDecimal currentBalance, EntryType entryType, BigDecimal amount) {
        boolean isDebitNormal = (type == AccountType.ASSET || type == AccountType.EXPENSE);
        if (isDebitNormal) {
            return entryType == EntryType.DEBIT ? currentBalance.add(amount) : currentBalance.subtract(amount);
        } else {
            return entryType == EntryType.CREDIT ? currentBalance.add(amount) : currentBalance.subtract(amount);
        }
    }
}

package com.portfolio.agentguard.application.service;

import com.portfolio.agentguard.domain.exception.LedgerImbalanceException;
import com.portfolio.agentguard.domain.model.JournalEntry;
import com.portfolio.agentguard.domain.model.LedgerAccount;
import com.portfolio.agentguard.domain.repository.JournalEntryRepository;
import com.portfolio.agentguard.domain.repository.LedgerAccountRepository;
import com.portfolio.agentguard.domain.valueobject.AccountType;
import com.portfolio.agentguard.domain.valueobject.EntryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    @Mock
    private LedgerAccountRepository accountRepository;

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @InjectMocks
    private LedgerService ledgerService;

    private LedgerAccount assetAccount;
    private LedgerAccount liabilityAccount;

    @BeforeEach
    void setUp() {
        assetAccount = LedgerAccount.builder()
            .id("acc-asset-1")
            .tenantId("tenant-corp-alpha")
            .accountNumber("1000-CASH")
            .name("Cash Vault")
            .type(AccountType.ASSET)
            .balance(new BigDecimal("1000.00"))
            .active(true)
            .build();

        liabilityAccount = LedgerAccount.builder()
            .id("acc-liab-1")
            .tenantId("tenant-corp-alpha")
            .accountNumber("2000-PAYABLE")
            .name("Merchant Accounts Payable")
            .type(AccountType.LIABILITY)
            .balance(BigDecimal.ZERO)
            .active(true)
            .build();
    }

    @Test
    @DisplayName("Should successfully post balanced journal entry")
    void testBalancedJournalEntryPosting() {
        when(accountRepository.findWithLockByIdAndTenantId("acc-asset-1", "tenant-corp-alpha"))
            .thenReturn(Optional.of(assetAccount));
        when(accountRepository.findWithLockByIdAndTenantId("acc-liab-1", "tenant-corp-alpha"))
            .thenReturn(Optional.of(liabilityAccount));
        when(journalEntryRepository.save(any(JournalEntry.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        List<LedgerService.LineItem> items = List.of(
            new LedgerService.LineItem("acc-asset-1", EntryType.DEBIT, new BigDecimal("50.00")),
            new LedgerService.LineItem("acc-liab-1", EntryType.CREDIT, new BigDecimal("50.00"))
        );

        JournalEntry entry = ledgerService.postJournalEntry("tenant-corp-alpha", "ref-tx-01", "Test settlement", items);

        assertNotNull(entry);
        assertEquals(2, entry.getLines().size());
        verify(accountRepository, times(2)).save(any(LedgerAccount.class));
    }

    @Test
    @DisplayName("Should throw LedgerImbalanceException when debits do not equal credits")
    void testImbalancedJournalEntryRejected() {
        List<LedgerService.LineItem> items = List.of(
            new LedgerService.LineItem("acc-asset-1", EntryType.DEBIT, new BigDecimal("50.00")),
            new LedgerService.LineItem("acc-liab-1", EntryType.CREDIT, new BigDecimal("49.00"))
        );

        assertThrows(
            LedgerImbalanceException.class,
            () -> ledgerService.postJournalEntry("tenant-corp-alpha", "ref-tx-02", "Imbalanced entry", items)
        );

        verifyNoInteractions(journalEntryRepository);
    }
}

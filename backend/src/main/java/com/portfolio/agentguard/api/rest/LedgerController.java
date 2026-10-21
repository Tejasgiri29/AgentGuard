package com.portfolio.agentguard.api.rest;

import com.portfolio.agentguard.application.service.LedgerService;
import com.portfolio.agentguard.config.TenantContext;
import com.portfolio.agentguard.domain.model.JournalEntry;
import com.portfolio.agentguard.domain.model.LedgerAccount;
import com.portfolio.agentguard.domain.repository.JournalEntryRepository;
import com.portfolio.agentguard.domain.repository.LedgerAccountRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ledger")
@RequiredArgsConstructor
@Tag(name = "Double-Entry Ledger", description = "Immutable balanced financial ledger inspection and trial balance")
public class LedgerController {

    private final LedgerAccountRepository ledgerAccountRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final LedgerService ledgerService;

    @GetMapping("/accounts")
    @Operation(summary = "List chart of accounts")
    public ResponseEntity<List<LedgerAccount>> listAccounts() {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(ledgerAccountRepository.findByTenantId(tenantId));
    }

    @GetMapping("/journal-entries")
    @Operation(summary = "List immutable journal entries and lines")
    public ResponseEntity<List<JournalEntry>> listJournalEntries() {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(journalEntryRepository.findByTenantIdOrderByPostedAtDesc(tenantId));
    }

    @GetMapping("/trial-balance")
    @Operation(summary = "Generate live trial balance confirming debit/credit invariant")
    public ResponseEntity<LedgerService.TrialBalance> getTrialBalance() {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(ledgerService.computeTrialBalance(tenantId));
    }
}

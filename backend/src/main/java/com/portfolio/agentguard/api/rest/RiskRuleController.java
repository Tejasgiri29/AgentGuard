package com.portfolio.agentguard.api.rest;

import com.portfolio.agentguard.config.TenantContext;
import com.portfolio.agentguard.domain.model.RiskRule;
import com.portfolio.agentguard.domain.repository.RiskRuleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/risk/rules")
@RequiredArgsConstructor
@Tag(name = "Risk & Policy Rules", description = "Explainable fraud rule inspection and simulation")
public class RiskRuleController {

    private final RiskRuleRepository riskRuleRepository;

    @GetMapping
    @Operation(summary = "List active risk evaluation rules")
    public ResponseEntity<List<RiskRule>> listRules() {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(riskRuleRepository.findByTenantId(tenantId));
    }
}

package com.example.lending.controller;

import com.example.lending.domain.DecisionTrace;
import com.example.lending.domain.LoanApplication;
import com.example.lending.domain.NomineeRequest;
import com.example.lending.service.LoanApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loan-applications")
@CrossOrigin(origins = "*")
@Tag(name = "Loan Applications", description = "Endpoints for managing loan applications and viewing decision trails")
public class LoanApplicationController {

    private final LoanApplicationService service;

    public LoanApplicationController(LoanApplicationService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get all loan applications (optionally filtered by borrowerId)")
    public List<LoanApplication> getAllApplications(@RequestParam(required = false) String borrowerId) {
        if (borrowerId != null && !borrowerId.isBlank()) {
            return service.getApplicationsByBorrowerId(borrowerId);
        }
        return service.getAllApplications();
    }

    @PostMapping
    @Operation(summary = "Submit a new loan application")
    public LoanApplication submitApplication(@RequestBody LoanApplication application) {
        return service.processLoanApplication(application);
    }

    @GetMapping("/{id}/decision-trail")
    @Operation(summary = "Get the explainable decision trail for a specific loan application")
    public ResponseEntity<DecisionTrace> getDecisionTrail(@PathVariable Long id) {
        return service.getDecisionTrace(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/collateral")
    @Operation(summary = "Submit collateral for an application in COLLATERAL_REQUIRED state")
    public ResponseEntity<LoanApplication> submitCollateral(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        String type = (String) payload.get("collateralType");
        BigDecimal value = new BigDecimal(payload.get("collateralValue").toString());
        return ResponseEntity.ok(service.submitCollateral(id, type, value));
    }

    @PostMapping("/{id}/nominee")
    @Operation(summary = "Request a nominee for a loan application")
    public ResponseEntity<NomineeRequest> requestNominee(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String nomineeId = payload.get("nomineeId");
        return ResponseEntity.ok(service.requestNominee(id, nomineeId));
    }

    @PostMapping("/nominee-requests/{requestId}/accept")
    @Operation(summary = "Accept a nominee request")
    public ResponseEntity<LoanApplication> acceptNominee(@PathVariable Long requestId) {
        return ResponseEntity.ok(service.acceptNomineeRequest(requestId));
    }
}

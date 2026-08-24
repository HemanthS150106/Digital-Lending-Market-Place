package com.example.lending.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "decision_trace")
public class DecisionTrace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;

    @Column(name = "overall_outcome", nullable = false)
    private String overallOutcome;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "decisionTrace", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<RuleTraceEntry> traceEntries = new ArrayList<>();

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LoanApplication getLoanApplication() { return loanApplication; }
    public void setLoanApplication(LoanApplication loanApplication) { this.loanApplication = loanApplication; }
    public String getOverallOutcome() { return overallOutcome; }
    public void setOverallOutcome(String overallOutcome) { this.overallOutcome = overallOutcome; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<RuleTraceEntry> getTraceEntries() { return traceEntries; }
    public void setTraceEntries(List<RuleTraceEntry> traceEntries) { this.traceEntries = traceEntries; }
    
    public void addTraceEntry(RuleTraceEntry entry) {
        traceEntries.add(entry);
        entry.setDecisionTrace(this);
    }
}

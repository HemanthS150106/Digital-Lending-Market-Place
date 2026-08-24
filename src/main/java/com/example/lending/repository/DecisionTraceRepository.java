package com.example.lending.repository;

import com.example.lending.domain.DecisionTrace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DecisionTraceRepository extends JpaRepository<DecisionTrace, Long> {
    Optional<DecisionTrace> findByLoanApplicationId(Long loanApplicationId);
}

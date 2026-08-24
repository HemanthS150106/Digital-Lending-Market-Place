package com.example.lending.repository;

import com.example.lending.domain.LoanOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanOfferRepository extends JpaRepository<LoanOffer, Long> {
    List<LoanOffer> findByLoanApplicationId(Long loanApplicationId);
}

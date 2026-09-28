package com.example.lending.repository;

import com.example.lending.domain.NomineeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NomineeRequestRepository extends JpaRepository<NomineeRequest, Long> {
    List<NomineeRequest> findByNomineeIdAndStatus(String nomineeId, String status);
    List<NomineeRequest> findByLoanApplicationId(Long loanApplicationId);
}

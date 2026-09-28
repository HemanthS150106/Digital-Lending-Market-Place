package com.example.lending.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class CreditBureauService {

    /**
     * Mocks a credit bureau call returning the total current monthly EMI obligations for a borrower.
     */
    public BigDecimal getExistingMonthlyEmis(String borrowerId) {
        // Mock logic: B-1002 has high existing debt, B-1001 has low
        if ("B-1002".equals(borrowerId)) {
            return new BigDecimal("1200.00");
        }
        return new BigDecimal("200.00");
    }
}

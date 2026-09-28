package com.example.lending.rulesengine.rules;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class EmiCalculator {

    /**
     * Calculates Monthly EMI using standard PMT formula: P * r * (1 + r)^n / ((1 + r)^n - 1)
     * Assuming standard 12.5% annual rate for affordability check.
     */
    public static BigDecimal calculateEmi(BigDecimal principal, int tenureMonths) {
        if (principal == null || tenureMonths <= 0) return BigDecimal.ZERO;
        
        // Use 12.5% annual interest for simulation
        BigDecimal annualRate = new BigDecimal("0.125");
        BigDecimal monthlyRate = annualRate.divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
        
        // (1 + r)^n
        BigDecimal onePlusRToN = monthlyRate.add(BigDecimal.ONE).pow(tenureMonths, MathContext.DECIMAL64);
        
        // P * r * (1 + r)^n
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRToN);
        
        // (1 + r)^n - 1
        BigDecimal denominator = onePlusRToN.subtract(BigDecimal.ONE);
        
        if (denominator.compareTo(BigDecimal.ZERO) == 0) return principal.divide(new BigDecimal(tenureMonths), 2, RoundingMode.HALF_UP);
        
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }
}

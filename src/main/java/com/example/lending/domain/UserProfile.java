package com.example.lending.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profile")
public class UserProfile {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "monthly_income", nullable = false)
    private BigDecimal monthlyIncome = BigDecimal.ZERO;
    
    @Column(name = "property_value", nullable = false)
    private BigDecimal propertyValue = BigDecimal.ZERO;
    
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }
    public BigDecimal getPropertyValue() { return propertyValue; }
    public void setPropertyValue(BigDecimal propertyValue) { this.propertyValue = propertyValue; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

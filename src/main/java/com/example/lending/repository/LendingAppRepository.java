package com.example.lending.repository;

import com.example.lending.domain.LendingApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LendingAppRepository extends JpaRepository<LendingApp, Long> {
}

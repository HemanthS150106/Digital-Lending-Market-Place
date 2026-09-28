package com.example.lending.controller;

import com.example.lending.domain.NomineeRequest;
import com.example.lending.domain.UserProfile;
import com.example.lending.repository.NomineeRequestRepository;
import com.example.lending.repository.UserProfileRepository;
import com.example.lending.service.LoanApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserProfileRepository userProfileRepository;
    private final NomineeRequestRepository nomineeRequestRepository;
    private final LoanApplicationService loanApplicationService;

    public UserController(UserProfileRepository userProfileRepository, 
                          NomineeRequestRepository nomineeRequestRepository,
                          LoanApplicationService loanApplicationService) {
        this.userProfileRepository = userProfileRepository;
        this.nomineeRequestRepository = nomineeRequestRepository;
        this.loanApplicationService = loanApplicationService;
    }

    @PostMapping("/login")
    public ResponseEntity<UserProfile> login(@RequestBody UserProfile credentials) {
        Optional<UserProfile> profile = userProfileRepository.findById(credentials.getId());
        if (profile.isPresent() && profile.get().getPassword().equals(credentials.getPassword())) {
            return ResponseEntity.ok(profile.get());
        }
        return ResponseEntity.status(401).build();
    }

    @PostMapping("/register")
    public ResponseEntity<UserProfile> register(@RequestBody UserProfile profile) {
        if (userProfileRepository.existsById(profile.getId())) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(userProfileRepository.save(profile));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserProfile> updateProfile(@PathVariable String id, @RequestBody UserProfile updates) {
        return userProfileRepository.findById(id).map(profile -> {
            profile.setMonthlyIncome(updates.getMonthlyIncome());
            profile.setPropertyValue(updates.getPropertyValue());
            return ResponseEntity.ok(userProfileRepository.save(profile));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/nominee-requests")
    public ResponseEntity<List<NomineeRequest>> getNomineeRequests(@PathVariable String id) {
        return ResponseEntity.ok(nomineeRequestRepository.findByNomineeIdAndStatus(id, "PENDING"));
    }
}

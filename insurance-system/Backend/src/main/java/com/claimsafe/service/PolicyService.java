package com.claimsafe.service;

import com.claimsafe.model.Policy;
import com.claimsafe.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository policyRepository;

    public List<Policy> getActivePolicies() {
        return policyRepository.findByIsActiveTrue();
    }

    public Policy getPolicy(String policyNumber) {
        return policyRepository.findById(policyNumber)
                .orElseThrow(() -> new RuntimeException("Policy not found: " + policyNumber));
    }
}

package com.claimsafe.controller;

import com.claimsafe.dto.ApiResponse;
import com.claimsafe.model.Policy;
import com.claimsafe.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Exposes active policies so the frontend can load them dynamically.
 * GET /api/policies
 * GET /api/policies/{policyNumber}
 */
@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Policy>>> getActivePolicies() {
        return ResponseEntity.ok(ApiResponse.ok(policyService.getActivePolicies()));
    }

    @GetMapping("/{policyNumber}")
    public ResponseEntity<ApiResponse<Policy>> getPolicy(@PathVariable String policyNumber) {
        return ResponseEntity.ok(ApiResponse.ok(policyService.getPolicy(policyNumber)));
    }
}

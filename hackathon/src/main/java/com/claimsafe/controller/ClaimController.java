package com.claimsafe.controller;

import com.claimsafe.dto.ApiResponse;
import com.claimsafe.dto.ClaimRequest;
import com.claimsafe.dto.ClaimResponse;
import com.claimsafe.dto.StatsResponse;
import com.claimsafe.service.ClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * REST controller for the ClaimSafe insurance portal.
 *
 * Base URL: /api/claims
 *
 * Endpoints consumed by the frontend HTML:
 *   GET    /api/claims              – list all / filtered
 *   POST   /api/claims              – submit new claim
 *   GET    /api/claims/{id}         – claim details
 *   PATCH  /api/claims/{id}/status  – update status
 *   POST   /api/claims/{id}/docs    – upload documents
 *   GET    /api/claims/stats        – dashboard stats
 */
@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    // ── List / Search ──────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<ClaimResponse>>> listClaims(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {

        List<ClaimResponse> claims = claimService.getAllClaims(status, search);
        return ResponseEntity.ok(ApiResponse.ok(claims));
    }

    // ── Submit new claim ───────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<ClaimResponse>> submitClaim(
            @Valid @RequestBody ClaimRequest req) {

        ClaimResponse created = claimService.submitClaim(req);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Claim submitted successfully", created));
    }

    // ── Claim details ──────────────────────────────────────────────────────

    @GetMapping("/{claimId}")
    public ResponseEntity<ApiResponse<ClaimResponse>> getClaim(
            @PathVariable String claimId) {

        return ResponseEntity.ok(ApiResponse.ok(claimService.getClaim(claimId)));
    }

    // ── Update status ──────────────────────────────────────────────────────

    @PatchMapping("/{claimId}/status")
    public ResponseEntity<ApiResponse<ClaimResponse>> updateStatus(
            @PathVariable String claimId,
            @RequestBody Map<String, String> body) {

        String newStatus = body.get("status");
        if (newStatus == null || newStatus.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("'status' field is required"));
        }
        ClaimResponse updated = claimService.updateStatus(claimId, newStatus);
        return ResponseEntity.ok(ApiResponse.ok("Status updated", updated));
    }

    // ── Upload documents ───────────────────────────────────────────────────

    @PostMapping(value = "/{claimId}/docs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<ClaimResponse.DocumentInfo>>> uploadDocs(
            @PathVariable String claimId,
            @RequestParam("files") List<MultipartFile> files) throws IOException {

        List<ClaimResponse.DocumentInfo> docs = claimService.uploadDocuments(claimId, files);
        return ResponseEntity.ok(ApiResponse.ok("Documents uploaded", docs));
    }

    // ── Dashboard stats ────────────────────────────────────────────────────

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<StatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.ok(claimService.getStats()));
    }
}

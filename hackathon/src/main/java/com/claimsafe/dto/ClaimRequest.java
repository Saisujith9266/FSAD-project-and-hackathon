package com.claimsafe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

// ─── Submit Claim Request ───────────────────────────────────────────────────

@Data
public class ClaimRequest {

    @NotBlank(message = "Policy number is required")
    private String policyNumber;

    @NotBlank(message = "Claim type is required")
    private String claimType;

    @NotNull(message = "Incident date is required")
    private LocalDate incidentDate;

    @NotNull(message = "Claim amount is required")
    @Positive(message = "Claim amount must be positive")
    private Long claimAmount;

    @NotBlank(message = "Description is required")
    private String description;

    private String location;
    private String additionalNotes;
}

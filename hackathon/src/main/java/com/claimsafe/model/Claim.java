package com.claimsafe.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "claims")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Claim {

    @Id
    @Column(name = "claim_id", length = 30)
    private String claimId;                 // e.g. CLM-2025-001

    @NotBlank
    @Column(name = "policy_number", length = 20)
    private String policyNumber;            // e.g. POL-H-44821

    @NotBlank
    @Column(name = "policy_type", length = 20)
    private String policyType;             // Health | Auto | Home | Life

    @NotBlank
    @Column(name = "claim_type", length = 60)
    private String claimType;              // Hospitalization, Surgery, …

    @NotNull
    @Column(name = "incident_date")
    private LocalDate incidentDate;

    @NotNull
    @Positive
    @Column(name = "claim_amount")
    private Long claimAmount;              // in INR (paise-free)

    @NotBlank
    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "location", length = 300)
    private String location;

    @Column(name = "additional_notes", length = 1000)
    private String additionalNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private ClaimStatus status = ClaimStatus.PENDING;

    @Column(name = "filed_at")
    @Builder.Default
    private LocalDateTime filedAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum ClaimStatus {
        PENDING, UNDER_REVIEW, APPROVED, REJECTED
    }
}

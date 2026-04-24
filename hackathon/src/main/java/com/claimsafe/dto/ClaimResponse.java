package com.claimsafe.dto;

import com.claimsafe.model.Claim;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ClaimResponse {

    private String claimId;
    private String policyNumber;
    private String policyType;
    private String claimType;
    private LocalDate incidentDate;
    private Long claimAmount;
    private String description;
    private String location;
    private String additionalNotes;
    private String status;          // frontend-friendly string: "Pending", "Under Review" etc.
    private LocalDateTime filedAt;
    private LocalDateTime updatedAt;
    private List<DocumentInfo> documents;

    @Data
    @Builder
    public static class DocumentInfo {
        private Long id;
        private String fileName;
        private String contentType;
        private Long fileSize;
        private LocalDateTime uploadedAt;
    }

    /** Convert Claim entity → ClaimResponse */
    public static ClaimResponse from(Claim c, List<DocumentInfo> docs) {
        return ClaimResponse.builder()
                .claimId(c.getClaimId())
                .policyNumber(c.getPolicyNumber())
                .policyType(c.getPolicyType())
                .claimType(c.getClaimType())
                .incidentDate(c.getIncidentDate())
                .claimAmount(c.getClaimAmount())
                .description(c.getDescription())
                .location(c.getLocation())
                .additionalNotes(c.getAdditionalNotes())
                .status(toFrontendStatus(c.getStatus()))
                .filedAt(c.getFiledAt())
                .updatedAt(c.getUpdatedAt())
                .documents(docs)
                .build();
    }

    private static String toFrontendStatus(Claim.ClaimStatus status) {
        return switch (status) {
            case PENDING      -> "Pending";
            case UNDER_REVIEW -> "Under Review";
            case APPROVED     -> "Approved";
            case REJECTED     -> "Rejected";
        };
    }
}

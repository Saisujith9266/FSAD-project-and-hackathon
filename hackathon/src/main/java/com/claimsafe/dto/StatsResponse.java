package com.claimsafe.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatsResponse {
    private long totalClaims;
    private long pendingClaims;        // Pending + Under Review
    private long approvedClaims;
    private long totalSettledAmount;   // sum of approved claim amounts (INR)
}

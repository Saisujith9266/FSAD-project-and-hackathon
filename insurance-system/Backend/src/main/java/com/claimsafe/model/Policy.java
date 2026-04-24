package com.claimsafe.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "policies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Policy {

    @Id
    @Column(name = "policy_number", length = 20)
    private String policyNumber;

    @Column(name = "policy_type", length = 20, nullable = false)
    private String policyType;   // Health | Auto | Home | Life

    @Column(name = "holder_name", length = 100)
    private String holderName;

    @Column(name = "coverage_amount", nullable = false)
    private Long coverageAmount; // in INR

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}

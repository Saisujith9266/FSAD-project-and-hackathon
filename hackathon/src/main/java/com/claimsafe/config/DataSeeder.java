package com.claimsafe.config;

import com.claimsafe.model.Claim;
import com.claimsafe.model.Policy;
import com.claimsafe.repository.ClaimRepository;
import com.claimsafe.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds the H2 database with the same sample data shown in the frontend HTML,
 * so the UI works out-of-the-box on first boot.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;

    @Override
    public void run(String... args) {
        seedPolicies();
        seedClaims();
        log.info("✅ ClaimSafe: sample data seeded successfully.");
    }

    private void seedPolicies() {
        if (policyRepository.count() > 0) return;

        policyRepository.saveAll(List.of(
            Policy.builder()
                .policyNumber("POL-H-44821")
                .policyType("Health")
                .holderName("Suvin Sai")
                .coverageAmount(500000L)
                .build(),
            Policy.builder()
                .policyNumber("POL-A-39204")
                .policyType("Auto")
                .holderName("Suvin Sai")
                .coverageAmount(300000L)
                .build(),
            Policy.builder()
                .policyNumber("POL-HM-17651")
                .policyType("Home")
                .holderName("Suvin Sai")
                .coverageAmount(1500000L)
                .build(),
            Policy.builder()
                .policyNumber("POL-L-29083")
                .policyType("Life")
                .holderName("Suvin Sai")
                .coverageAmount(5000000L)
                .build()
        ));
    }

    private void seedClaims() {
        if (claimRepository.count() > 0) return;

        claimRepository.saveAll(List.of(
            Claim.builder()
                .claimId("CLM-2024-006")
                .policyNumber("POL-H-44821")
                .policyType("Health")
                .claimType("Hospitalization")
                .incidentDate(LocalDate.of(2024, 11, 18))
                .claimAmount(45000L)
                .status(Claim.ClaimStatus.UNDER_REVIEW)
                .description("Patient was admitted for appendicitis surgery at Apollo Hospitals Hyderabad on 15th November 2024. Treatment lasted 3 days including post-operative care.")
                .location("Apollo Hospitals, Hyderabad")
                .filedAt(LocalDateTime.of(2024, 11, 18, 10, 0))
                .build(),
            Claim.builder()
                .claimId("CLM-2024-005")
                .policyNumber("POL-A-39204")
                .policyType("Auto")
                .claimType("Accident / Injury")
                .incidentDate(LocalDate.of(2024, 10, 2))
                .claimAmount(32000L)
                .status(Claim.ClaimStatus.APPROVED)
                .description("Vehicle involved in a rear-end collision at Hitech City signal. Front bumper and hood damaged. Police FIR filed the same day.")
                .location("Hitech City, Hyderabad")
                .filedAt(LocalDateTime.of(2024, 10, 2, 14, 30))
                .build(),
            Claim.builder()
                .claimId("CLM-2024-004")
                .policyNumber("POL-HM-17651")
                .policyType("Home")
                .claimType("Property Damage")
                .incidentDate(LocalDate.of(2024, 8, 21))
                .claimAmount(78000L)
                .status(Claim.ClaimStatus.APPROVED)
                .description("Roof damage caused by heavy rainfall during monsoon. Water seepage led to damage of two rooms including furniture.")
                .location("Madhapur, Hyderabad")
                .filedAt(LocalDateTime.of(2024, 8, 21, 9, 0))
                .build(),
            Claim.builder()
                .claimId("CLM-2024-003")
                .policyNumber("POL-H-44821")
                .policyType("Health")
                .claimType("Surgery")
                .incidentDate(LocalDate.of(2024, 7, 5))
                .claimAmount(95000L)
                .status(Claim.ClaimStatus.APPROVED)
                .description("Knee replacement surgery performed at Yashoda Hospital. Pre-authorization obtained prior to procedure.")
                .location("Yashoda Hospital, Secunderabad")
                .filedAt(LocalDateTime.of(2024, 7, 5, 11, 0))
                .build(),
            Claim.builder()
                .claimId("CLM-2024-002")
                .policyNumber("POL-A-39204")
                .policyType("Auto")
                .claimType("Theft / Burglary")
                .incidentDate(LocalDate.of(2024, 5, 12))
                .claimAmount(50000L)
                .status(Claim.ClaimStatus.REJECTED)
                .description("Claim for theft of in-car entertainment system. Rejected as the item is not covered under the existing auto policy terms.")
                .location("Jubilee Hills, Hyderabad")
                .filedAt(LocalDateTime.of(2024, 5, 12, 16, 0))
                .build(),
            Claim.builder()
                .claimId("CLM-2024-001")
                .policyNumber("POL-HM-17651")
                .policyType("Home")
                .claimType("Natural Disaster")
                .incidentDate(LocalDate.of(2024, 3, 28))
                .claimAmount(12000L)
                .status(Claim.ClaimStatus.PENDING)
                .description("Minor flood damage in the basement due to unexpected urban flooding. Repair estimate attached with photographs.")
                .location("Banjara Hills, Hyderabad")
                .filedAt(LocalDateTime.of(2024, 3, 28, 8, 0))
                .build()
        ));
    }
}

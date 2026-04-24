package com.claimsafe.service;

import com.claimsafe.dto.ClaimRequest;
import com.claimsafe.dto.ClaimResponse;
import com.claimsafe.dto.StatsResponse;
import com.claimsafe.exception.ClaimNotFoundException;
import com.claimsafe.exception.PolicyNotFoundException;
import com.claimsafe.model.Claim;
import com.claimsafe.model.ClaimDocument;
import com.claimsafe.model.Policy;
import com.claimsafe.repository.ClaimDocumentRepository;
import com.claimsafe.repository.ClaimRepository;
import com.claimsafe.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final ClaimDocumentRepository documentRepository;
    private final PolicyRepository policyRepository;

    @Value("${claimsafe.upload.dir:uploads}")
    private String uploadDir;

    // ─── Submit a new claim ────────────────────────────────────────────────

    @Transactional
    public ClaimResponse submitClaim(ClaimRequest req) {
        // Validate policy exists
        Policy policy = policyRepository.findById(req.getPolicyNumber())
                .orElseThrow(() -> new PolicyNotFoundException("Policy not found: " + req.getPolicyNumber()));

        String claimId = generateClaimId();

        Claim claim = Claim.builder()
                .claimId(claimId)
                .policyNumber(req.getPolicyNumber())
                .policyType(policy.getPolicyType())
                .claimType(req.getClaimType())
                .incidentDate(req.getIncidentDate())
                .claimAmount(req.getClaimAmount())
                .description(req.getDescription())
                .location(req.getLocation())
                .additionalNotes(req.getAdditionalNotes())
                .status(Claim.ClaimStatus.PENDING)
                .filedAt(LocalDateTime.now())
                .build();

        claimRepository.save(claim);
        log.info("Claim submitted: {}", claimId);
        return toResponse(claim);
    }

    // ─── Get all claims (optionally filtered) ─────────────────────────────

    public List<ClaimResponse> getAllClaims(String status, String search) {
        List<Claim> claims;

        if (search != null && !search.isBlank()) {
            claims = claimRepository.search(search.trim());
        } else if (status != null && !status.equalsIgnoreCase("All")) {
            Claim.ClaimStatus s = fromFrontendStatus(status);
            claims = claimRepository.findByStatusOrderByFiledAtDesc(s);
        } else {
            claims = claimRepository.findAllByOrderByFiledAtDesc();
        }

        return claims.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ─── Get single claim ──────────────────────────────────────────────────

    public ClaimResponse getClaim(String claimId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ClaimNotFoundException("Claim not found: " + claimId));
        return toResponseWithDocs(claim);
    }

    // ─── Update claim status (admin) ───────────────────────────────────────

    @Transactional
    public ClaimResponse updateStatus(String claimId, String newStatus) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ClaimNotFoundException("Claim not found: " + claimId));
        claim.setStatus(fromFrontendStatus(newStatus));
        claimRepository.save(claim);
        log.info("Claim {} status updated to {}", claimId, newStatus);
        return toResponse(claim);
    }

    // ─── Upload documents ──────────────────────────────────────────────────

    @Transactional
    public List<ClaimResponse.DocumentInfo> uploadDocuments(String claimId, List<MultipartFile> files) throws IOException {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ClaimNotFoundException("Claim not found: " + claimId));

        Path claimDir = Paths.get(uploadDir, claimId);
        Files.createDirectories(claimDir);

        List<ClaimDocument> saved = files.stream().map(file -> {
            String fileName = file.getOriginalFilename();
            Path dest = claimDir.resolve(fileName);
            try {
                Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store file: " + fileName, e);
            }
            return documentRepository.save(ClaimDocument.builder()
                    .claim(claim)
                    .fileName(fileName)
                    .filePath(dest.toString())
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .build());
        }).collect(Collectors.toList());

        log.info("Uploaded {} documents for claim {}", saved.size(), claimId);
        return saved.stream().map(this::toDocInfo).collect(Collectors.toList());
    }

    // ─── Stats ─────────────────────────────────────────────────────────────

    public StatsResponse getStats() {
        long total    = claimRepository.count();
        long pending  = claimRepository.countByStatus(Claim.ClaimStatus.PENDING)
                      + claimRepository.countByStatus(Claim.ClaimStatus.UNDER_REVIEW);
        long approved = claimRepository.countByStatus(Claim.ClaimStatus.APPROVED);
        long settled  = claimRepository.sumApprovedAmounts();

        return StatsResponse.builder()
                .totalClaims(total)
                .pendingClaims(pending)
                .approvedClaims(approved)
                .totalSettledAmount(settled)
                .build();
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    private String generateClaimId() {
        String year = String.valueOf(java.time.Year.now().getValue());
        String ts   = String.valueOf(System.currentTimeMillis()).substring(7); // last 6 digits
        return String.format("CLM-%s-%s", year, ts);
    }

    private ClaimResponse toResponse(Claim claim) {
        return ClaimResponse.from(claim, List.of());
    }

    private ClaimResponse toResponseWithDocs(Claim claim) {
        List<ClaimResponse.DocumentInfo> docs = documentRepository
                .findByClaimClaimId(claim.getClaimId())
                .stream().map(this::toDocInfo).collect(Collectors.toList());
        return ClaimResponse.from(claim, docs);
    }

    private ClaimResponse.DocumentInfo toDocInfo(ClaimDocument d) {
        return ClaimResponse.DocumentInfo.builder()
                .id(d.getId())
                .fileName(d.getFileName())
                .contentType(d.getContentType())
                .fileSize(d.getFileSize())
                .uploadedAt(d.getUploadedAt())
                .build();
    }

    private Claim.ClaimStatus fromFrontendStatus(String s) {
        return switch (s) {
            case "Pending"      -> Claim.ClaimStatus.PENDING;
            case "Under Review" -> Claim.ClaimStatus.UNDER_REVIEW;
            case "Approved"     -> Claim.ClaimStatus.APPROVED;
            case "Rejected"     -> Claim.ClaimStatus.REJECTED;
            default -> throw new IllegalArgumentException("Unknown status: " + s);
        };
    }
}

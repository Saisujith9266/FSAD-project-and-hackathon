package com.claimsafe.repository;

import com.claimsafe.model.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, String> {

    List<Claim> findByStatusOrderByFiledAtDesc(Claim.ClaimStatus status);

    List<Claim> findAllByOrderByFiledAtDesc();

    @Query("SELECT c FROM Claim c WHERE " +
           "LOWER(c.claimId) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(c.claimType) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(c.policyNumber) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "ORDER BY c.filedAt DESC")
    List<Claim> search(@Param("q") String q);

    @Query("SELECT COALESCE(SUM(c.claimAmount), 0) FROM Claim c WHERE c.status = com.claimsafe.model.Claim.ClaimStatus.APPROVED")
    Long sumApprovedAmounts();

    long countByStatus(Claim.ClaimStatus status);
}

package com.subash.brochure_sync_service.dto;

import com.subash.brochure_sync_service.model.BrochureRequest;
import com.subash.brochure_sync_service.model.SyncStatus;

import java.time.Instant;

/**
 * Outbound representation of a brochure request. Built from the entity so the
 * JPA model is never serialized directly over the API.
 */
public record BrochureRequestResponse(
        Long id,
        String name,
        String email,
        String company,
        String productInterest,
        SyncStatus status,
        String failureReason,
        Instant createdAt,
        Instant updatedAt
) {
    public static BrochureRequestResponse from(BrochureRequest entity) {
        return new BrochureRequestResponse(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getCompany(),
                entity.getProductInterest(),
                entity.getStatus(),
                entity.getFailureReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

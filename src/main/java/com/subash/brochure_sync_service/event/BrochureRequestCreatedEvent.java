package com.subash.brochure_sync_service.event;

import java.time.Instant;

/**
 * Published to Kafka after a brochure request is committed to the database.
 * Serialized as JSON; consumed by the Salesforce sync listener.
 */
public record BrochureRequestCreatedEvent(
        Long requestId,
        String name,
        String email,
        String company,
        String productInterest,
        Instant occurredAt
) {
}

package com.subash.brochure_sync_service.salesforce;

import com.subash.brochure_sync_service.event.BrochureRequestCreatedEvent;

import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Stand-in for a real Salesforce integration. Succeeds ~70% of the time and
 * throws otherwise, so both the SYNCED and FAILED paths are exercisable end to end.
 */
@Component
public class FakeSalesforceClient implements SalesforceClient {

    private static final double SUCCESS_RATE = 0.7;

    @Override
    public String createLead(BrochureRequestCreatedEvent event) {
        if (ThreadLocalRandom.current().nextDouble() > SUCCESS_RATE) {
            throw new SalesforceException("Salesforce API returned 503 Service Unavailable for " + event.email());
        }
        return "SF-" + UUID.randomUUID();
    }
}

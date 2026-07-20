package com.subash.brochure_sync_service.kafka;

import com.subash.brochure_sync_service.event.BrochureRequestCreatedEvent;
import com.subash.brochure_sync_service.salesforce.SalesforceClient;
import com.subash.brochure_sync_service.salesforce.SalesforceException;
import com.subash.brochure_sync_service.service.BrochureRequestService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

/**
 * Consumes {@link BrochureRequestCreatedEvent}s and pushes each request to
 * Salesforce via the (stubbed) {@link SalesforceClient}, recording the outcome
 * as SYNCED or FAILED on the request.
 */
@Component
public class SalesforceSyncListener {

    private static final Logger log = LoggerFactory.getLogger(SalesforceSyncListener.class);

    private final BrochureRequestService service;
    private final SalesforceClient salesforceClient;
    private final ObjectMapper objectMapper;

    public SalesforceSyncListener(BrochureRequestService service,
                                  SalesforceClient salesforceClient,
                                  ObjectMapper objectMapper) {
        this.service = service;
        this.salesforceClient = salesforceClient;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = KafkaTopics.BROCHURE_REQUEST_CREATED, groupId = KafkaTopics.BROCHURE_SYNC_GROUP)
    public void onBrochureRequestCreated(String message) {
        BrochureRequestCreatedEvent event = objectMapper.readValue(message, BrochureRequestCreatedEvent.class);
        log.info("Received brochure request created event for id={}", event.requestId());
        try {
            String salesforceId = salesforceClient.createLead(event);
            service.markSynced(event.requestId());
            log.info("Synced brochure request id={} to Salesforce lead {}", event.requestId(), salesforceId);
        } catch (SalesforceException ex) {
            service.markFailed(event.requestId(), ex.getMessage());
            log.error("Failed to sync brochure request id={} to Salesforce: {}", event.requestId(), ex.getMessage());
        }
    }
}

package com.subash.brochure_sync_service.service;

import com.subash.brochure_sync_service.dto.BrochureRequestRequest;
import com.subash.brochure_sync_service.dto.BrochureRequestResponse;
import com.subash.brochure_sync_service.event.BrochureRequestCreatedEvent;
import com.subash.brochure_sync_service.kafka.KafkaTopics;
import com.subash.brochure_sync_service.model.BrochureRequest;
import com.subash.brochure_sync_service.model.SyncStatus;
import com.subash.brochure_sync_service.repository.BrochureRequestRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Optional;

@Service
public class BrochureRequestService {

    private final BrochureRequestRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public BrochureRequestService(BrochureRequestRepository repository,
                                  KafkaTemplate<String, String> kafkaTemplate,
                                  ObjectMapper objectMapper) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public BrochureRequestResponse create(BrochureRequestRequest request) {
        BrochureRequest entity = new BrochureRequest();
        entity.setName(request.name());
        entity.setEmail(request.email());
        entity.setCompany(request.company());
        entity.setProductInterest(request.productInterest());
        entity.setStatus(SyncStatus.PENDING);

        BrochureRequest saved = repository.save(entity);
        publishAfterCommit(toEvent(saved));

        return BrochureRequestResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Optional<BrochureRequestResponse> getById(Long id) {
        return repository.findById(id).map(BrochureRequestResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<BrochureRequestResponse> list(SyncStatus status, Pageable pageable) {
        Page<BrochureRequest> page = (status == null)
                ? repository.findAll(pageable)
                : repository.findByStatus(status, pageable);
        return page.map(BrochureRequestResponse::from);
    }

    /**
     * Re-drive a request through the sync pipeline (typically after a FAILED
     * attempt): reset it to PENDING and republish the created event.
     */
    @Transactional
    public Optional<BrochureRequestResponse> retrySync(Long id) {
        return repository.findById(id).map(request -> {
            request.setStatus(SyncStatus.PENDING);
            request.setFailureReason(null);
            BrochureRequest saved = repository.save(request);
            publishAfterCommit(toEvent(saved));
            return BrochureRequestResponse.from(saved);
        });
    }

    @Transactional
    public void markSynced(Long id) {
        BrochureRequest request = findOrThrow(id);
        request.setStatus(SyncStatus.SYNCED);
        request.setFailureReason(null);
        repository.save(request);
    }

    @Transactional
    public void markFailed(Long id, String reason) {
        BrochureRequest request = findOrThrow(id);
        request.setStatus(SyncStatus.FAILED);
        request.setFailureReason(reason);
        repository.save(request);
    }

    private BrochureRequest findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Brochure request not found: " + id));
    }

    private BrochureRequestCreatedEvent toEvent(BrochureRequest request) {
        return new BrochureRequestCreatedEvent(
                request.getId(),
                request.getName(),
                request.getEmail(),
                request.getCompany(),
                request.getProductInterest(),
                Instant.now()
        );
    }

    /**
     * Publish only once the surrounding DB transaction has committed, so a
     * rolled-back request never leaks a "created" event to Kafka. When there is
     * no active transaction (e.g. plain unit tests) we publish immediately.
     */
    private void publishAfterCommit(BrochureRequestCreatedEvent event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(event);
                }
            });
        } else {
            send(event);
        }
    }

    private void send(BrochureRequestCreatedEvent event) {
        String payload = objectMapper.writeValueAsString(event);
        kafkaTemplate.send(KafkaTopics.BROCHURE_REQUEST_CREATED, String.valueOf(event.requestId()), payload);
    }
}

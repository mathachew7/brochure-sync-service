package com.subash.brochure_sync_service.service;

import com.subash.brochure_sync_service.model.BrochureRequest;
import com.subash.brochure_sync_service.repository.BrochureRequestRepository;

import com.subash.brochure_sync_service.kafka.KafkaTopics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;



@Service
public class BrochureRequestService {
    private final BrochureRequestRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public BrochureRequestService(BrochureRequestRepository repository, KafkaTemplate<String, String> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public BrochureRequest createRequest(BrochureRequest request) {
        request.setStatus("PENDING");
        request.setCreatedAt(LocalDateTime.now().toString());

        BrochureRequest savedRequest = repository.save(request);
        kafkaTemplate.send(KafkaTopics.BROCHURE_REQUEST_CREATED, savedRequest.getId().toString());

        return savedRequest;
    }

    public Optional<BrochureRequest> getRequestById(Long id) {
        return repository.findById(id);
    }

    public List<BrochureRequest> getAllRequests() {
        return repository.findAll();
    }

    public List<BrochureRequest> getRequestsByStatus(String status) {
        return repository.findByStatus(status);
    }


}
package com.subash.brochure_sync_service.service;

import com.subash.brochure_sync_service.model.BrochureRequest;
import com.subash.brochure_sync_service.repository.BrochureRequestRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;



@Service
public class BrochureRequestService {
    private final BrochureRequestRepository repository;

    public BrochureRequestService(BrochureRequestRepository repository) {
        this.repository = repository;
    }

    public BrochureRequest createRequest(BrochureRequest request) {
        request.setStatus("PENDING");
        request.setCreatedAt(LocalDateTime.now().toString());
        return repository.save(request);
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
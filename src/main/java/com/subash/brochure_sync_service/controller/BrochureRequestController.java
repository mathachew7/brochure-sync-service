package com.subash.brochure_sync_service.controller;

import com.subash.brochure_sync_service.model.BrochureRequest;
import com.subash.brochure_sync_service.service.BrochureRequestService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/brochure-requests")
public class BrochureRequestController {

    private final BrochureRequestService service;

    public BrochureRequestController(BrochureRequestService service) {
        this.service = service;
    }
    
    @PostMapping
    public BrochureRequest createBrochureRequest(@Valid @RequestBody BrochureRequest request) {
        return service.createRequest(request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrochureRequest> getRequestById(@PathVariable Long id) {
        Optional<BrochureRequest> request = service.getRequestById(id);
        if (request.isPresent()) {
            return ResponseEntity.ok(request.get());
        } else {
            return ResponseEntity.notFound().build();
        }
        
    }

    @GetMapping
    public List<BrochureRequest> getAllRequests() {
        return service.getAllRequests();
    }


    @GetMapping(params = "status")
    public List<BrochureRequest>  getRequestsByStatus(@RequestParam String status) {
        return service.getRequestsByStatus(status);
    }

}
package com.subash.brochure_sync_service.controller;

import com.subash.brochure_sync_service.dto.BrochureRequestRequest;
import com.subash.brochure_sync_service.dto.BrochureRequestResponse;
import com.subash.brochure_sync_service.model.SyncStatus;
import com.subash.brochure_sync_service.service.BrochureRequestService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/brochure-requests")
public class BrochureRequestController {

    private final BrochureRequestService service;

    public BrochureRequestController(BrochureRequestService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<BrochureRequestResponse> create(@Valid @RequestBody BrochureRequestRequest request) {
        BrochureRequestResponse created = service.create(request);
        return ResponseEntity
                .created(URI.create("/brochure-requests/" + created.id()))
                .body(created);
    }

    @GetMapping
    public Page<BrochureRequestResponse> list(
            @RequestParam(required = false) SyncStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.list(status, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrochureRequestResponse> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<BrochureRequestResponse> retry(@PathVariable Long id) {
        return service.retrySync(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

package com.subash.brochure_sync_service.repository;

import com.subash.brochure_sync_service.model.BrochureRequest;
import com.subash.brochure_sync_service.model.SyncStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrochureRequestRepository extends JpaRepository<BrochureRequest, Long> {

    Page<BrochureRequest> findByStatus(SyncStatus status, Pageable pageable);
}

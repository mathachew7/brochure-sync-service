package com.subash.brochure_sync_service.repository;

import com.subash.brochure_sync_service.model.BrochureRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface BrochureRequestRepository extends JpaRepository<BrochureRequest, Long> {

   List<BrochureRequest> findByStatus(String status);

}
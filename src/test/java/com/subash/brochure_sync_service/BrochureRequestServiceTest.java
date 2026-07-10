package com.subash.brochure_sync_service.service;

import com.subash.brochure_sync_service.model.BrochureRequest;
import com.subash.brochure_sync_service.repository.BrochureRequestRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;


@ExtendWith(MockitoExtension.class)
public class BrochureRequestServiceTest {
    
    @Mock
    private BrochureRequestRepository repository;

    @Test
    void createRequest_shouldSetDefaultStatusAndAssignCreatedAt() {
        // Create a sample BrochureRequest object
        BrochureRequest request = new BrochureRequest();
        request.setName("John Doe");
        request.setEmail("john.doe@example.com");
        request.setCompany("Globex Company");
        request.setProductInterest("ERP Suite");

        when(repository.save(request)).thenReturn(request);

        BrochureRequestService service = new BrochureRequestService(repository);

        BrochureRequest result = service.createRequest(request);

        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getCreatedAt()).isNotNull();
    }


    @Test
    void getRequestById_shouldReturnRequest_whenfound() {
        BrochureRequest existing = new BrochureRequest();
        existing.setId(1L);
        existing.setName("Jane Doe");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        BrochureRequestService service = new BrochureRequestService(repository);

        Optional<BrochureRequest> result = service.getRequestById(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    void getRequestById_shouldReturnEmptyOptional_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        BrochureRequestService service = new BrochureRequestService(repository);
        Optional<BrochureRequest> result = service.getRequestById(99L);
        assertThat(result).isEmpty();

    }

    @Test
    void getRequestsByStatus_shouldReturnMatchingRequests() {
       BrochureRequest failedRequest = new BrochureRequest();
       failedRequest.setId(2L);
       failedRequest.setStatus("FAILED");

       when(repository.findByStatus("FAILED")).thenReturn(List.of(failedRequest));
       
       BrochureRequestService service = new BrochureRequestService(repository);

       List<BrochureRequest> result = service.getRequestsByStatus("FAILED");

       assertThat(result).hasSize(1);

       assertThat(result.get(0).getStatus()).isEqualTo("FAILED");


    }
}





package com.subash.brochure_sync_service.service;

import com.subash.brochure_sync_service.model.BrochureRequest;
import com.subash.brochure_sync_service.repository.BrochureRequestRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class BrochureRequestServiceTest {
    
    @Mock
    private BrochureRequestRepository repository;

    @Test
    public void createRequest_shouldSetDefaultStatusAndAssignCreatedAt() {
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
}





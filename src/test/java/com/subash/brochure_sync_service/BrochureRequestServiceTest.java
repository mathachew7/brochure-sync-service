package com.subash.brochure_sync_service.service;

import com.subash.brochure_sync_service.dto.BrochureRequestRequest;
import com.subash.brochure_sync_service.dto.BrochureRequestResponse;
import com.subash.brochure_sync_service.model.BrochureRequest;
import com.subash.brochure_sync_service.model.SyncStatus;
import com.subash.brochure_sync_service.repository.BrochureRequestRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BrochureRequestServiceTest {

    @Mock
    private BrochureRequestRepository repository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private BrochureRequestService service;

    @Captor
    private ArgumentCaptor<BrochureRequest> requestCaptor;

    @BeforeEach
    void setUp() {
        service = new BrochureRequestService(repository, kafkaTemplate, JsonMapper.builder().build());
    }

    @Test
    void create_savesRequestAsPendingAndPublishesEvent() {
        BrochureRequestRequest request = new BrochureRequestRequest(
                "John Doe", "john.doe@example.com", "Globex", "ERP Suite");

        when(repository.save(any(BrochureRequest.class))).thenAnswer(invocation -> {
            BrochureRequest entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        BrochureRequestResponse response = service.create(request);

        verify(repository).save(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getStatus()).isEqualTo(SyncStatus.PENDING);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo(SyncStatus.PENDING);

        // With no active transaction the event is published immediately (as a JSON string).
        verify(kafkaTemplate).send(eq("brochure-requests-created"), eq("1"), anyString());
    }

    @Test
    void getById_returnsMappedResponse_whenFound() {
        BrochureRequest existing = new BrochureRequest();
        existing.setId(1L);
        existing.setName("Jane Doe");
        existing.setStatus(SyncStatus.PENDING);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        Optional<BrochureRequestResponse> result = service.getById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(1L);
        assertThat(result.get().name()).isEqualTo("Jane Doe");
    }

    @Test
    void getById_returnsEmpty_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThat(service.getById(99L)).isEmpty();
    }

    @Test
    void markSynced_setsStatusAndClearsFailureReason() {
        BrochureRequest existing = new BrochureRequest();
        existing.setId(3L);
        existing.setStatus(SyncStatus.PENDING);
        existing.setFailureReason("previous error");
        when(repository.findById(3L)).thenReturn(Optional.of(existing));

        service.markSynced(3L);

        verify(repository).save(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getStatus()).isEqualTo(SyncStatus.SYNCED);
        assertThat(requestCaptor.getValue().getFailureReason()).isNull();
    }

    @Test
    void markFailed_setsStatusAndReason() {
        BrochureRequest existing = new BrochureRequest();
        existing.setId(5L);
        existing.setStatus(SyncStatus.PENDING);
        when(repository.findById(5L)).thenReturn(Optional.of(existing));

        service.markFailed(5L, "Salesforce down");

        verify(repository).save(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getStatus()).isEqualTo(SyncStatus.FAILED);
        assertThat(requestCaptor.getValue().getFailureReason()).isEqualTo("Salesforce down");
    }
}

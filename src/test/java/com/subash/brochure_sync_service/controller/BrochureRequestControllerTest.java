package com.subash.brochure_sync_service.controller;

import com.subash.brochure_sync_service.dto.BrochureRequestRequest;
import com.subash.brochure_sync_service.dto.BrochureRequestResponse;
import com.subash.brochure_sync_service.model.SyncStatus;
import com.subash.brochure_sync_service.service.BrochureRequestService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BrochureRequestController.class)
class BrochureRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BrochureRequestService service;

    @Test
    void create_returns201_whenRequestValid() throws Exception {
        BrochureRequestRequest request = new BrochureRequestRequest(
                "John Doe", "john@example.com", "Globex", "ERP Suite");
        BrochureRequestResponse response = new BrochureRequestResponse(
                1L, "John Doe", "john@example.com", "Globex", "ERP Suite",
                SyncStatus.PENDING, null, Instant.now(), Instant.now());

        when(service.create(any(BrochureRequestRequest.class))).thenReturn(response);

        mockMvc.perform(post("/brochure-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/brochure-requests/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void create_returns400_withFieldErrors_whenInvalid() throws Exception {
        // blank name + malformed email
        String payload = "{\"name\":\"\",\"email\":\"not-an-email\",\"company\":\"Globex\",\"productInterest\":\"ERP\"}";

        mockMvc.perform(post("/brochure-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void getById_returns200_whenFound() throws Exception {
        BrochureRequestResponse response = new BrochureRequestResponse(
                7L, "Jane", "jane@example.com", "Initech", "CRM",
                SyncStatus.SYNCED, null, Instant.now(), Instant.now());
        when(service.getById(7L)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/brochure-requests/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.status").value("SYNCED"));
    }

    @Test
    void getById_returns404_whenMissing() throws Exception {
        when(service.getById(404L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/brochure-requests/404"))
                .andExpect(status().isNotFound());
    }
}

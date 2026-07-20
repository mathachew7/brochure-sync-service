package com.subash.brochure_sync_service.steps;

import com.subash.brochure_sync_service.dto.BrochureRequestRequest;
import com.subash.brochure_sync_service.dto.BrochureRequestResponse;
import com.subash.brochure_sync_service.model.BrochureRequest;
import com.subash.brochure_sync_service.model.SyncStatus;
import com.subash.brochure_sync_service.repository.BrochureRequestRepository;
import com.subash.brochure_sync_service.service.BrochureRequestService;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import org.springframework.kafka.core.KafkaTemplate;

import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BrochureRequestSteps {

    private String productInterest;
    private BrochureRequestResponse createdRequest;

    // Same mocking approach as the JUnit tests — no real Postgres or Kafka broker needed here either
    private final BrochureRequestRepository repository = mock(BrochureRequestRepository.class);
    @SuppressWarnings("unchecked")
    private final KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
    private final BrochureRequestService service =
            new BrochureRequestService(repository, kafkaTemplate, JsonMapper.builder().build());

    @Given("a prospect wants a brochure for {string}")
    public void a_prospect_wants_a_brochure_for(String product) {
        this.productInterest = product;
    }

    @When("they submit a request with name {string}, email {string}, and company {string}")
    public void they_submit_a_request(String name, String email, String company) {
        // Mock save() to hand back the request with a generated id — the service
        // publishes that id to Kafka, so it must be present to avoid an NPE
        when(repository.save(any(BrochureRequest.class))).thenAnswer(invocation -> {
            BrochureRequest entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        BrochureRequestRequest request = new BrochureRequestRequest(name, email, company, productInterest);

        // Call the real service, not a faked outcome
        createdRequest = service.create(request);
    }

    @Then("the request should be created with status {string}")
    public void the_request_should_be_created_with_status(String expectedStatus) {
        assertThat(createdRequest.status()).isEqualTo(SyncStatus.valueOf(expectedStatus));
    }
}

package com.subash.brochure_sync_service.steps;

import com.subash.brochure_sync_service.model.BrochureRequest;
import com.subash.brochure_sync_service.repository.BrochureRequestRepository;
import com.subash.brochure_sync_service.service.BrochureRequestService;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BrochureRequestSteps {

    private String productInterest;
    private BrochureRequest createdRequest;

    // Same mocking approach as your JUnit tests — no real Postgres needed here either
    private final BrochureRequestRepository repository = mock(BrochureRequestRepository.class);
    private final BrochureRequestService service = new BrochureRequestService(repository);

    @Given("a prospect wants a brochure for {string}")
    public void a_prospect_wants_a_brochure_for(String product) {
        this.productInterest = product;
    }

    @When("they submit a request with name {string}, email {string}, and company {string}")
    public void they_submit_a_request(String name, String email, String company) {
        BrochureRequest request = new BrochureRequest();
        request.setName(name);
        request.setEmail(email);
        request.setCompany(company);
        request.setProductInterest(productInterest);

        // Mock save() to just hand back whatever it's given — same trick as before
        when(repository.save(request)).thenReturn(request);

        // THIS is the real difference — now we're actually calling your real Service,
        // not faking the outcome ourselves
        createdRequest = service.createRequest(request);
    }

    @Then("the request should be created with status {string}")
    public void the_request_should_be_created_with_status(String expectedStatus) {
        assertThat(createdRequest.getStatus()).isEqualTo(expectedStatus);
    }
}
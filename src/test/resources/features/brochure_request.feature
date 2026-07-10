Feature: Brochure Request Submission

  Scenario: Submitting a valid brochure request
    Given a prospect wants a brochure for "CRM Suite"
    When they submit a request with name "Jane Doe", email "jane@example.com", and company "Globex"
    Then the request should be created with status "PENDING"
package com.subash.brochure_sync_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Inbound payload for creating a brochure request. This is the public API
 * contract, kept deliberately separate from the {@code BrochureRequest} JPA entity.
 */
public record BrochureRequestRequest(

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "Company is required")
        String company,

        @NotBlank(message = "Product interest is required")
        String productInterest
) {
}

package com.subash.brochure_sync_service.salesforce;

import com.subash.brochure_sync_service.event.BrochureRequestCreatedEvent;

/**
 * Abstraction over the Salesforce integration so the sync flow can be wired to a
 * real client or a stub without touching the listener.
 */
public interface SalesforceClient {

    /**
     * Push a brochure request to Salesforce as a lead.
     *
     * @return the created Salesforce lead id
     * @throws SalesforceException if the push fails
     */
    String createLead(BrochureRequestCreatedEvent event) throws SalesforceException;
}

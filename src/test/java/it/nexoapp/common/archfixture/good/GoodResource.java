package it.nexoapp.common.archfixture.good;

import it.nexoapp.common.archfixture.good.dto.GoodDto;
import it.nexoapp.common.archfixture.good.service.GoodService;

/**
 * Clean fixture: a resource that talks to a service interface only.
 */
class GoodResource {

    /* - dependencies - */

    /** The service the resource delegates to. */
    private GoodService service;

    // ------------------------------------------------------------------------------------------

    /**
     * Returns the DTO of the service.
     *
     * @return the DTO
     */
    GoodDto get() {
        return service.get();
    }

}

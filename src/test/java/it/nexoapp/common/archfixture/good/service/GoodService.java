package it.nexoapp.common.archfixture.good.service;

import it.nexoapp.common.archfixture.good.dto.GoodDto;

/**
 * Clean fixture: the public service interface.
 */
public interface GoodService {

    /**
     * Returns a DTO.
     *
     * @return the DTO
     */
    GoodDto get();

}

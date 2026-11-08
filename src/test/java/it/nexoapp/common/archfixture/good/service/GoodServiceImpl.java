package it.nexoapp.common.archfixture.good.service;

import it.nexoapp.common.archfixture.good.dto.GoodDto;

/**
 * Clean fixture: a package-private implementation using a package-private repository.
 */
class GoodServiceImpl implements GoodService {

    /* - dependencies - */

    /** The repository of the slice. */
    private GoodRepository repository;

    // ------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public GoodDto get() {
        return new GoodDto(String.valueOf(repository));
    }

}

/**
 * Clean fixture: a package-private repository in the service package.
 */
class GoodRepository {

}

package it.nexoapp.common.archfixture.bad;

import it.nexoapp.common.archfixture.bad.dto.BadDto;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Deliberately bad fixture: a public repository outside any service package, depending on a DTO and calling native SQL.
 * <p>
 * Plain class on purpose: no bean-defining, JAX-RS or JPA annotation, so the common test application ignores it.
 */
public class BadRepository {

    /* - dependencies - */

    /** A DTO a repository must not depend on. */
    private BadDto dto;

    // ------------------------------------------------------------------------------------------

    /**
     * Runs a native query, which bypasses the tenant filter.
     *
     * @param entityManager the entity manager
     * @return the result list
     */
    List<?> run(final EntityManager entityManager) {
        return entityManager.createNativeQuery("select 1").getResultList();
    }

}

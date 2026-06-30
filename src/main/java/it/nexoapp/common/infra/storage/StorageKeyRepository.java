package it.nexoapp.common.infra.storage;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import it.nexoapp.common.infra.storage.entity.StorageKeyEntity;
import it.nexoapp.common.infra.storage.entity.StorageKeyPK;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Repository class for managing {@link StorageKeyEntity} instances.
 * Provides database access operations for the storage key entity.
 */

@ApplicationScoped
class StorageKeyRepository implements PanacheRepositoryBase<StorageKeyEntity, StorageKeyPK> {

    /* - logger - */
    private static final Logger logger = LoggerFactory.getLogger(StorageKeyRepository.class);

    /**
     * Retrieves a StorageKeyEntity based on its type and alternative key.
     *
     * @param type the type of the storage
     * @param key the alternative key of the storage
     * @return the StorageKeyEntity corresponding to the provided type and key, or null if not found
     */
    public StorageKeyEntity findByAltKey(final String type, final String key) {
        logger.trace("Searching the object with type='{}', key='{}'", type, key);

        /* build the id */
        final StorageKeyPK id = StorageKeyPK.builder()
                .type(type)
                .altKey(key)
                .build();

        /* retrieve entity */
        return findById(id, LockModeType.PESSIMISTIC_READ);
    }

}
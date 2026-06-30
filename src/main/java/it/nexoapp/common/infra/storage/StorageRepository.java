package it.nexoapp.common.infra.storage;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import it.nexoapp.common.infra.storage.entity.StorageEntity;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Repository class for managing {@link StorageEntity} instances.
 * Provides database access operations for the storage entity.
 */
@ApplicationScoped
@RequiredArgsConstructor
class StorageRepository implements PanacheRepositoryBase<StorageEntity, UUID> {

    /* - logger - */
    private static final Logger logger = LoggerFactory.getLogger(StorageRepository.class);

    /**
     * Retrieves a StorageEntity by its type and key.
     *
     * @param type the type of the storage entity to retrieve
     * @param key the key of the storage entity to retrieve
     * @return the matching StorageEntity, or null if no entity is found
     */
    public StorageEntity getByTypeAndKey(final String type, final String key) {
        final StorageEntity storageEntity = find("type = ?1 and \"key\" = ?2", type, key)
                .firstResult();
        logger.trace("found entity? {}. by type = '{}' and key = '{}'", storageEntity != null, type, key);
        return storageEntity;
    }

    /**
     * Deletes entities by their type and key.
     *
     * @param type the type of entities to delete
     * @param key the key of entities to delete
     * @return the number of entities deleted
     */
    public long deleteByKey(final String type, final String key) {
        return delete("type = ?1 and \"key\" = ?2", type, key);
    }

}
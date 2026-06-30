package it.nexoapp.common.infra.storage;

import it.nexoapp.common.infra.storage.dto.ManagedContentType;
import it.nexoapp.common.infra.storage.entity.StorageEntity;
import it.nexoapp.common.infra.storage.entity.StorageKeyEntity;
import it.nexoapp.common.infra.storage.entity.StorageKeyPK;
import it.nexoapp.common.infra.storage.exception.StorageNotFoundException;
import it.nexoapp.common.infra.storage.exception.StorageUnexpectedDeletionCountException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
public class StorageFacadeImpl implements StorageFacade {

    /* - logger - */
    private static final Logger logger = LoggerFactory.getLogger(StorageFacadeImpl.class);

    /* - datasource - */
    private final StorageRepository repository;
    private final StorageKeyRepository keyRepository;

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * @param id {@inheritDoc}
     * @param clazz {@inheritDoc}
     * @return {@inheritDoc}
     * @param <T> {@inheritDoc}
     */
    @Override
    public <T extends Serializable> Optional<T> findById(final UUID id, final Class<T> clazz) {
        logger.trace("Searching the object with id='{}'", id);

        /* retrieve entity */
        final StorageEntity entity = repository.findById(id);

        /* checking if was found */
        if(entity == null) {
            logger.trace("The object with id {} was not found", id);
            return Optional.empty();
        }

        /* convert to object */
        return Optional.ofNullable(
                ManagedContentType.of(entity.getContentType())
                        .mapFromString(entity.getPayload(), clazz)
        );
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * @param id {@inheritDoc}
     * @param clazz {@inheritDoc}
     * @return {@inheritDoc}
     * @param <T> {@inheritDoc}
     */
    @Override
    public <T extends Serializable> T getById(final UUID id, final Class<T> clazz) {
        return findById(id, clazz)
                .orElseThrow(
                        () -> new StorageNotFoundException(id)
                );
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * @param type {@inheritDoc}
     * @param key {@inheritDoc}
     * @param clazz {@inheritDoc}
     * @return {@inheritDoc}
     * @param <T> {@inheritDoc}
     */
    @Override
    public <T extends Serializable> Optional<T> findByKey(final String type, final String key, Class<T> clazz) {
        logger.trace("Searching the object with type='{}', key='{}'", type, key);

        /* retrieve entity */
        StorageEntity entity = repository.getByTypeAndKey(type, key);

        /* checking if was found */
        if(entity == null) {
            logger.trace("No main storage found with type = '{}', key = '{}'", type, key);

            /* try to find the alt key */
            logger.debug("Searching as alt key the combo: type = '{}', key = '{}'", type, key);
            final StorageKeyEntity altKeyEntity = keyRepository.findByAltKey(type, key);

            /* checking if was found */
            if(altKeyEntity == null) {
                logger.trace("No alt storage found with type = '{}', key = '{}'", type, key);
                return Optional.empty();
            }

            /* proceed with entity */
            logger.trace("Found storage with altKey, proceding...");
            entity = repository.findById(altKeyEntity.getId());
        }

        /* convert to object */
        return Optional.ofNullable(
                ManagedContentType.of(entity.getContentType())
                        .mapFromString(entity.getPayload(), clazz)
        );
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * @param type {@inheritDoc}
     * @param key {@inheritDoc}
     * @param clazz {@inheritDoc}
     * @return {@inheritDoc}
     * @param <T> {@inheritDoc}
     */
    @Override
    public <T extends Serializable> T getByKey(final String type, final String key, final Class<T> clazz) {
        return findByKey(type, key, clazz)
                .orElseThrow(
                        () -> new StorageNotFoundException(type, key)
                );
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * @param type {@inheritDoc}
     * @param key {@inheritDoc}
     * @param altKeyList {@inheritDoc}
     * @param value {@inheritDoc}
     * @param contentType {@inheritDoc}
     * @return {@inheritDoc}
     * @param <T> {@inheritDoc}
     */
    @Override
    public <T extends Serializable> UUID put(
            final String type,
            final String key,
            final List<String> altKeyList,
            final T value,
            final MediaType contentType
    ) {
        logger.trace("Saving the object with type = '{}', key = '{}'", type, key);

        /* convert to string */
        logger.trace("Converting the object to string");
        final String strPayload = ManagedContentType.of(contentType)
                .mapToString(value);

        logger.trace("Building storage object with type = '{}', key = '{}', contentType = '{}'", type, key, contentType);
        final StorageEntity entity = StorageEntity.builder()
                .type(type)
                .key(key)
                .contentType(contentType.toString())
                .payload(strPayload)
                .build();

        /* persist entity */
        logger.trace("Persisting entity");
        repository.persist(entity);

        /* persist alt keys */
        if(altKeyList != null && !altKeyList.isEmpty()) {
            logger.trace("Persisting alt keys");

            /* persisting each alt key */
            for (final String altKey : altKeyList) {
                /* checking if the alt key is blank */
                if(StringUtils.isBlank(altKey)) {
                    logger.warn("The alt key is blank, skipping...");
                    continue;
                }

                logger.trace("Persisting altKey = '{}'", altKey);
                keyRepository.persist(
                        /* build the StorageKey entity */
                        StorageKeyEntity.builder()
                                .primaryKey(
                                        /* build the id */
                                        StorageKeyPK.builder()
                                                .type(type)
                                                .altKey(altKey)
                                                .build()
                                )
                                .id(entity.getId())
                                .build()
                );
            }
        }

        logger.trace("Object saved with id = '{}'", entity.getId());
        return entity.getId();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * @param type {@inheritDoc}
     * @param key {@inheritDoc}
     * @param altKey {@inheritDoc}
     * @param value {@inheritDoc}
     * @param contentType {@inheritDoc}
     * @return {@inheritDoc}
     * @param <T> {@inheritDoc}
     */
    @Override
    public <T extends Serializable> UUID put(
            final String type,
            final String key,
            final String altKey,
            final T value,
            final MediaType contentType
    ) {
        return put(
                type,
                key,
                List.of(altKey),
                value,
                contentType
        );
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * @param type {@inheritDoc}
     * @param key {@inheritDoc}
     * @param altKeyList {@inheritDoc}
     * @param value {@inheritDoc}
     * @return {@inheritDoc}
     * @param <T> {@inheritDoc}
     */
    @Override
    public <T extends Serializable> UUID put(
            final String type,
            final String key,
            final List<String> altKeyList,
            final T value
    ) {
        /* determine the content type */
        final MediaType contentType;
        if(value instanceof String) {
            logger.trace("The value is a String, setting contentType = '{}'", MediaType.TEXT_PLAIN_TYPE);
            contentType = MediaType.TEXT_PLAIN_TYPE;
        } else {
            logger.trace("The value is not a String, setting contentType = '{}'", MediaType.APPLICATION_JSON_TYPE);
            contentType = MediaType.APPLICATION_JSON_TYPE;
        }
        logger.trace("Determined contentType = '{}'", contentType);

        /* proceed with the put */
        return put(type, key, altKeyList, value, contentType);
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * @param type {@inheritDoc}
     * @param key {@inheritDoc}
     * @param altKey {@inheritDoc}
     * @param value {@inheritDoc}
     * @return
     * @param <T>
     */
    @Override
    public <T extends Serializable> UUID put(
            final String type,
            final String key,
            final String altKey,
            final T value
    ) {
        return put(
                type,
                key,
                List.of(altKey),
                value
        );
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * @param type {@inheritDoc}
     * @param key {@inheritDoc}
     * @param value {@inheritDoc}
     * @param contentType {@inheritDoc}
     * @return {@inheritDoc}
     * @param <T> {@inheritDoc}
     */
    @Override
    public <T extends Serializable> UUID put(
            final String type,
            final String key,
            final T value,
            final MediaType contentType
    ) {
        return put(
                type,
                key,
                List.of(),
                value,
                contentType
        );
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * @param type {@inheritDoc}
     * @param key {@inheritDoc}
     * @param value {@inheritDoc}
     * @return {@inheritDoc}
     * @param <T> {@inheritDoc}
     */
    @Override
    public <T extends Serializable> UUID put(
            final String type,
            final String key,
            final T value
    ) {
        return put(
                type,
                key,
                List.of(),
                value
        );
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * @param id {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean remove(final UUID id) {
        return repository.deleteById(id);
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * @param type {@inheritDoc}
     * @param key {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean remove(final String type, final String key) {
        long rowDeleted = repository.deleteByKey(type, key);

        if(rowDeleted > 1) {
            logger.warn("More than one row was deleted. Could not be possibile. type = '{}', key = '{}'", type, key);
            throw new StorageUnexpectedDeletionCountException();
        }

        return rowDeleted == 1;
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * @param id {@inheritDoc}
     * @return {@inheritDoc}
     * @throws StorageNotFoundException {@inheritDoc}
     */
    @Override
    public void delete(final UUID id) {
        /* try to delete the entity */
        boolean deletionSuccess = remove(id);

        /* checking if was found */
        if(!deletionSuccess) {
            logger.warn("The storage was not found. id = '{}'", id);
            throw new StorageNotFoundException(id);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * @param type {@inheritDoc}
     * @param key {@inheritDoc}
     * @return {@inheritDoc}
     * @throws StorageNotFoundException {@inheritDoc}
     */
    @Override
    public void delete(final String type, final String key) {
        /* try to delete the entity */
        boolean deletionSuccess = remove(type, key);

        /* checking if was found */
        if(!deletionSuccess) {
            logger.warn("The storage was not found. type = '{}', key = '{}'", type, key);
            throw new StorageNotFoundException(type, key);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

}
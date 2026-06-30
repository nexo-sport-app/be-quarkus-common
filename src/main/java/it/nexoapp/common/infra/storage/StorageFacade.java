package it.nexoapp.common.infra.storage;

import it.nexoapp.common.infra.storage.exception.StorageNotFoundException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.MediaType;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Storage facade interface for managing temporary, schemaless object storage in the database.
 * <p>
 * This facade provides support for storing objects without a predefined schema in the database.
 * The storage should be considered temporary in nature, typically used as an intermediate step
 * in data processing workflows.
 * </p>
 * <p>
 * A common use case is as an intermediate stage in a processing pipeline, where raw data is
 * initially saved to the storage table and subsequently processed by another API endpoint or
 * a batch poller. This pattern enables decoupling of data ingestion from data processing,
 * supporting asynchronous and distributed processing architectures.
 * </p>
 * <p>
 * The facade supports multiple lookup mechanisms including unique identifiers (UUID), custom keys,
 * and alternative keys, providing flexibility in how stored objects can be retrieved.
 * </p>
 *
 * @see Serializable
 */
public interface StorageFacade {

    /**
     * Retrieve the storage object by its ID.
     * @param id the ID of the storage object
     * @param clazz the class of the storage object
     * @return the storage object
     * @param <T> the type of the storage object
     * @throws StorageNotFoundException if the storage object is not found
     */
    <T extends Serializable> T getById(
            @NotNull final UUID id,
            @NotNull final Class<T> clazz
    );

    /**
     * Search for the storage object by its ID.
     * @param id the ID of the storage object
     * @param clazz the class of the storage object
     * @return the optional storage object
     * @param <T> the type of the storage object
     */
    <T extends Serializable> Optional<T> findById(
            @NotNull final UUID id,
            @NotNull final Class<T> clazz
    );

    /**
     * Retrieve the storage object by its key.
     * @param type the type of the storage object
     * @param key the key of the storage object
     * @param clazz the class of the storage object
     * @return the storage object
     * @param <T> the type of the storage object
     * @throws StorageNotFoundException if the storage object is not found
     */
    <T extends Serializable> T getByKey(
            @NotBlank final String type,
            @NotBlank final String key,
            @NotNull final Class<T> clazz
    );

    /**
     * Search for the storage object by its key.
     * @param type the type of the storage object
     * @param key the key of the storage object
     * @param clazz the class of the storage object
     * @return the optional storage object
     * @param <T> the type of the storage object
     */
    <T extends Serializable> Optional<T> findByKey(
            @NotBlank final String type,
            @NotBlank final String key,
            @NotNull final Class<T> clazz
    );

    /**
     * Save or update the storage object.
     * @param type the type of the storage object
     * @param key the key of the storage object
     * @param altKeyList a list of possible alternative keys for the storage object
     * @param value the content of the storage object
     * @param contentType the content type of the storage object
     * @return the ID of the storage object
     * @param <T> the type of the storage object
     */
    <T extends Serializable> UUID put(
            @NotBlank final String type,
            @NotBlank final String key,
            final List<String> altKeyList,
            @NotBlank final T value,
            final MediaType contentType
    );

    /**
     * Save or update the storage object.
     * @param type the type of the storage object
     * @param key the key of the storage object
     * @param altKey an alternative keys for the storage object
     * @param value the content of the storage object
     * @param contentType the content type of the storage object
     * @return the ID of the storage object
     * @param <T> the type of the storage object
     */
    <T extends Serializable> UUID put(
            @NotBlank final String type,
            @NotBlank final String key,
            final String altKey,
            @NotBlank final T value,
            final MediaType contentType
    );

    /**
     * Save or update the storage object.
     * @param type the type of the storage object
     * @param key the key of the storage object
     * @param altKeyList a list of possible alternative keys for the storage object
     * @param value the content of the storage object
     * @return the ID of the storage object
     * @param <T> the type of the storage object
     */
    <T extends Serializable> UUID put(
            @NotBlank final String type,
            @NotBlank final String key,
            final List<String> altKeyList,
            @NotNull final T value
    );

    /**
     * Save or update the storage object.
     * @param type the type of the storage object
     * @param key the key of the storage object
     * @param altKey an alternative keys for the storage object
     * @param value the content of the storage object
     * @return the ID of the storage object
     * @param <T> the type of the storage object
     */
    <T extends Serializable> UUID put(
            @NotBlank final String type,
            @NotBlank final String key,
            final String altKey,
            @NotNull final T value);

    /**
     * Save or update the storage object.
     * @param type the type of the storage object
     * @param key the key of the storage object
     * @param value the content of the storage object
     * @param contentType the content type of the storage object
     * @return the ID of the storage object
     * @param <T> the type of the storage object
     */
    <T extends Serializable> UUID put(
            @NotBlank final String type,
            @NotBlank final String key,
            @NotNull final T value,
            final MediaType contentType
    );

    /**
     * Save or update the storage object.
     * @param type the type of the storage object
     * @param key the key of the storage object
     * @param value the content of the storage object
     * @return the ID of the storage object
     * @param <T> the type of the storage object
     */
    <T extends Serializable> UUID put(
            @NotBlank final String type,
            @NotBlank final String key,
            @NotNull final T value
    );

    /**
     * Delete the storage object.
     * @param id the ID of the storage object
     * @throws StorageNotFoundException if the storage object is not found
     */
    void delete(
            @NotNull final UUID id
    );

    /**
     * Delete the storage object.
     * @param type the type of the storage object
     * @param key the key of the storage object
     * @throws StorageNotFoundException if the storage object is not found
     */
    void delete(
            @NotBlank final String type,
            @NotBlank final String key
    );

    /**
     * Remove the storage object if it exists.
     * @param id the ID of the storage object
     */
    boolean remove(
            @NotNull final UUID id
    );

    /**
     * Remove the storage object if it exists.
     * @param type the type of the storage object
     * @param key the key of the storage object
     */
    boolean remove(
            @NotBlank final String type,
            @NotBlank final String key
    );

}
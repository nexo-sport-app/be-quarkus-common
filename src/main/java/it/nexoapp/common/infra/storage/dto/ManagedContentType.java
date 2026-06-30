package it.nexoapp.common.infra.storage.dto;

import it.nexoapp.common.infra.storage.mapper.ApplicationJsonContentMapper;
import it.nexoapp.common.infra.storage.mapper.ContentMapper;
import it.nexoapp.common.infra.storage.mapper.TextPlainContentMapper;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Optional;

/**
 * Enum representing managed content types with associated content mappers.
 * Provides utility methods for serializing and deserializing objects to
 * and from their string representations based on the associated content type.
 */
@RequiredArgsConstructor
public enum ManagedContentType {

    APPLICATION_JSON(MediaType.APPLICATION_JSON, new ApplicationJsonContentMapper()),
    TEXT_PLAIN(MediaType.TEXT_PLAIN, new TextPlainContentMapper()),
    ;

    /* - constants - */
    public static final ManagedContentType DEFAULT = APPLICATION_JSON;
    private static final Logger logger = LoggerFactory.getLogger(ManagedContentType.class);

    /* - properties - */
    private final String contentType;
    private final ContentMapper mapper;

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Converts the given serializable object to its string representation.
     *
     * @param <T> the type of the object, which must implement {@link Serializable}
     * @param object the object to be converted to a string
     * @return the string representation of the given object
     */
    public <T extends Serializable> String mapToString(final T object) {
        /* convert the object */
        logger.trace("Mapping from {} to string", object.getClass().getSimpleName());
        return mapper.toString(object);
    }

    /**
     * Maps a string representation of an object to its corresponding object of the specified class.
     *
     * @param <T> the type of the object, which must implement {@link Serializable}
     * @param object the string representation of the object
     * @param clazz the {@link Class} instance of the target object type
     * @return the deserialized object of the specified type
     */
    public <T extends Serializable> T mapFromString(final String object, final Class<T> clazz) {
        /* managed if empty string */
        if(StringUtils.isBlank(object)) {
            logger.warn("The string is empty, returning null");
            return null;
        }

        /* serialize the strings */
        logger.trace("Mapping from string to {}", clazz.getSimpleName());
        return mapper.fromString(object, clazz);
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Retrieves the corresponding {@link ManagedContentType} for the specified content type string.
     * If no matching content type is found, the default {@link ManagedContentType} is returned.
     *
     * @param contentType the string representation of the content type to look up
     * @return the corresponding {@link ManagedContentType}, or the default content type if not found
     */
    public static ManagedContentType of(final String contentType) {
        /* managed if no content type */
        if (StringUtils.isBlank(contentType)) {
            logger.debug("No content type found for {}", contentType);
            return DEFAULT;
        }

        /* try to find the content type */
        final Optional<ManagedContentType> optionalContentType = Arrays.stream(ManagedContentType.values())
                .filter(mct -> Strings.CS.equals(mct.contentType, contentType))
                .findFirst();

        /* managed if type not found */
        if(optionalContentType.isEmpty()) {
            logger.debug("No content type found for {}, fallback to default", contentType);
            return DEFAULT;
        }

        /* return the found content type */
        logger.trace("Content type found for {}", contentType);
        return optionalContentType.get();
    }

    /**
     * Retrieves the corresponding {@link ManagedContentType} for the specified {@link MediaType}.
     * If no matching content type is found, the default {@link ManagedContentType} is returned.
     *
     * @param mediaType the {@link MediaType} to convert to a {@link ManagedContentType}
     * @return the corresponding {@link ManagedContentType}, or the default content type if not found
     */
    public static ManagedContentType of(final MediaType mediaType) {
        return of(mediaType.toString());
    }

    // -----------------------------------------------------------------------------------------------------------------

}
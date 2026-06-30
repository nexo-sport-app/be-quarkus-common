package it.nexoapp.common.infra.storage.mapper;

import it.nexoapp.common.infra.storage.exception.IllegalRequestedStorageContentException;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * {@link ContentMapper} implementation that uses the text/plain content type.
 */
public class TextPlainContentMapper implements ContentMapper {

    public static final Logger logger = LoggerFactory.getLogger(TextPlainContentMapper.class);

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * @param object {@inheritDoc}
     * @return {@inheritDoc}
     * @param <T> {@inheritDoc}
     */
    @Override
    public <T extends Serializable> String toString(final T object) {
        if (object instanceof String s) {
            logger.trace("Mapping from {} to string", object.getClass().getSimpleName());
            return s;
        }

        logger.trace("Mapping from {} to string", object.getClass().getSimpleName());
        return String.valueOf(object);
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * @param string {@inheritDoc}
     * @param clazz {@inheritDoc}
     * @return {@inheritDoc}
     * @param <T> {@inheritDoc}
     */
    @Override
    public <T extends Serializable> T fromString(final String string, final Class<T> clazz) {
        if(!clazz.equals(String.class)) {
            logger.warn("The class {} is not a String, expected only String in TextPlainContentMapper", clazz.getSimpleName());
            throw new IllegalRequestedStorageContentException(MediaType.TEXT_PLAIN, clazz);
        }

        logger.trace("Returning the object value");
        return clazz.cast(string);
    }

    // -----------------------------------------------------------------------------------------------------------------

}

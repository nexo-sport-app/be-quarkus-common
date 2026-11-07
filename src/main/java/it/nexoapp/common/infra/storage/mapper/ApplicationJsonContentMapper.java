package it.nexoapp.common.infra.storage.mapper;

import it.nexoapp.common.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * {@link ContentMapper} implementation that uses the application/json content type.
 */
public class ApplicationJsonContentMapper implements ContentMapper {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationJsonContentMapper.class);

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * @param object {@inheritDoc}
     * @return {@inheritDoc}
     * @param <T> {@inheritDoc}
     */
    @Override
    public <T extends Serializable> String toString(final T object) {
        logger.trace("Mapping from {} to string", object.getClass().getSimpleName());
        return JSONUtils.toJson(object);
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
        logger.trace("Mapping from string to {}", clazz.getSimpleName());
        return JSONUtils.fromJson(string, clazz);
    }

    // -----------------------------------------------------------------------------------------------------------------

}
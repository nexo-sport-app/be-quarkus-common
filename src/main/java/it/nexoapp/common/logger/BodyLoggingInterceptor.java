package it.nexoapp.common.logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.ws.rs.ConstrainedTo;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.ReaderInterceptorContext;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;

/**
 * Logs request and response bodies at DEBUG, with every {@link Sensitive} member masked (D-18).
 * <p>
 * The interceptor does nothing unless its logger is DEBUG-enabled, and then checks that before serializing anything.
 * Only JSON bodies whose Java type is a record (or a collection of records) are written, through a private mapper
 * that masks the {@link Sensitive} members. Every other body (multipart, binary, text, streams, maps, trees) is
 * replaced by a placeholder: the raw entity stream is never read here.
 * <p>
 * Requests with a non-JSON body (for example multipart forms, which are parsed without a body reader) get their
 * placeholder from the request filter; the reader interceptor only handles JSON bodies.
 */
@Provider
@ConstrainedTo(RuntimeType.SERVER)
class BodyLoggingInterceptor implements ContainerRequestFilter, ReaderInterceptor, WriterInterceptor {

    /* - constants - */
    /** Longest body written to the log, in characters; the rest is cut. */
    private static final int MAX_BODY_LENGTH = 10_000;

    /** Subtype suffix of the structured JSON media types, for example {@code application/problem+json}. */
    private static final String JSON_SUFFIX = "+json";

    /* - logger - */
    /** The logger of the interceptor. */
    private static final Logger LOG = LoggerFactory.getLogger(BodyLoggingInterceptor.class);

    /* - mapper - */
    /** Mapper that renders the {@link Sensitive} members as {@value SensitiveMaskingIntrospector#MASK}. */
    private static final ObjectMapper MASKING_MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setAnnotationIntrospector(new SensitiveMaskingIntrospector());

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(final ContainerRequestContext requestContext) {
        if (LOG.isDebugEnabled() && requestContext.hasEntity() && !isJson(requestContext.getMediaType())) {
            LOG.debug("request body={}", placeholder(requestContext.getMediaType()));
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Object aroundReadFrom(final ReaderInterceptorContext context) throws IOException, WebApplicationException {
        final Object entity = context.proceed();
        if (LOG.isDebugEnabled() && isJson(context.getMediaType())) {
            LOG.debug("request body={}", render(context.getMediaType(), entity));
        }
        return entity;
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public void aroundWriteTo(final WriterInterceptorContext context) throws IOException, WebApplicationException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("response body={}", render(context.getMediaType(), context.getEntity()));
        }
        context.proceed();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Renders a body for the log: the masked JSON for records, else a placeholder naming the media type.
     *
     * @param mediaType media type of the body, may be null
     * @param entity    the typed body, may be null
     * @return the text to log, never null
     */
    private static String render(final MediaType mediaType, final Object entity) {
        if (!isJson(mediaType) || !isRecordBody(entity)) {
            return placeholder(mediaType);
        }
        try {
            final String json = MASKING_MAPPER.writeValueAsString(entity);
            return json.length() > MAX_BODY_LENGTH ? json.substring(0, MAX_BODY_LENGTH) + "..." : json;
        } catch (final IOException | RuntimeException e) {
            // logging must never break the request; the cause is not written because it could quote the value
            return "[body not loggable: " + entity.getClass().getSimpleName() + "]";
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Tells whether the media type is JSON.
     *
     * @param mediaType the media type, may be null
     * @return true for {@code application/json} and the {@code +json} subtypes
     */
    private static boolean isJson(final MediaType mediaType) {
        return mediaType != null
                && (MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType)
                || mediaType.getSubtype().toLowerCase().endsWith(JSON_SUFFIX));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Tells whether the body is a record or a collection of records only.
     *
     * @param entity the typed body, may be null
     * @return true when the body can be rendered safely
     */
    private static boolean isRecordBody(final Object entity) {
        if (entity == null) {
            return false;
        }
        if (entity instanceof final Collection<?> collection) {
            return collection.stream().allMatch(element -> element != null && element.getClass().isRecord());
        }
        return entity.getClass().isRecord();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Builds the placeholder that replaces a body that is not logged.
     *
     * @param mediaType media type of the body, may be null
     * @return the placeholder, without media type parameters
     */
    private static String placeholder(final MediaType mediaType) {
        return "[body omitted: " + (mediaType == null ? "unknown" : mediaType.getType() + "/" + mediaType.getSubtype()) + "]";
    }

}

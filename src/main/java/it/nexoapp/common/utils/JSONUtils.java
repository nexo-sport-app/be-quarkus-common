package it.nexoapp.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.nexoapp.common.exception.IllegalInstantiationException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for JSON-related operations that provides methods for serializing objects
 * to JSON, deserializing JSON to objects, validating JSON strings, and cleaning JSON structure.
 * The class uses Jackson's {@code JsonMapper} for JSON processing.
 */
public class JSONUtils {

    /* - LOGGER - */
    private static final Logger logger = LoggerFactory.getLogger(JSONUtils.class);
    private static final String GENERIC_ERROR_MESSAGE = "Caught exception for JSON: {}";
    private static final String VALIDATION_ERROR_MESSAGE = "Validation failed for the provided JSON string: \"{}\", with error {}";
    private static final String BLANK_WARNING = "The provided JSON string is blank.";
    private static final String NULL_WARNING = "The provided object is null. Returning null.";

    // -----------------------------------------------------------------------------------------------------------------

    private JSONUtils() {
        throw new IllegalInstantiationException();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Creates and returns a configured instance of {@code JsonMapper}.
     *
     * @param useAnnotation Indicates whether Jackson annotations should be enabled during
     *                      serialization and deserialization.
     * @return A {@code JsonMapper} instance configured with necessary modules and features
     *         for JSON processing.
     */
    public static JsonMapper getMapper(final boolean useAnnotation) {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .configure(MapperFeature.USE_ANNOTATIONS, useAnnotation)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .build();
    }

    /**
     * Returns a pre-configured instance of {@code JsonMapper} with default settings.
     *
     * @return A {@code JsonMapper} instance configured with the default parameters,
     *         including Jackson annotations enabled during serialization and deserialization.
     */
    public static JsonMapper getMapper() {
        return getMapper(true);
    }

    /**
     * Validates whether the provided string is a well-formed JSON.
     *
     * @param json The string to be validated as JSON.
     * @return {@code true} if the string is a valid JSON, {@code false} otherwise.
     */
    public static boolean isValidJson(final String json) {
        if(StringUtils.isBlank(json)){
            logger.warn(BLANK_WARNING);
            return false;
        }

        try{
            final ObjectMapper objectMapper = JsonMapper.builder()
                    .build();
            objectMapper.readTree(json);
            return true;
        } catch (final Exception e) {
            logger.warn(VALIDATION_ERROR_MESSAGE, json, e.getMessage());
            return false;
        }
    }

    /**
     * Serializes the given object into a JSON-formatted string. The serialization
     * process can optionally consider Jackson annotations based on the provided
     * flag. If an error occurs during the serialization process, the method logs
     * the exception and returns null.
     *
     * @param obj           The object to be serialized into JSON. It can be any Java
     *                      object that is serializable by Jackson.
     * @param useAnnotation A boolean flag indicating whether Jackson annotations
     *                      should be considered during serialization. If true,
     *                      annotations will influence the serialization process;
     *                      otherwise, they will be ignored.
     * @return A JSON-formatted string representation of the provided object.
     *         Returns null if an exception occurs during the serialization process.
     */
    public static String toJson(final Object obj, final boolean useAnnotation) {
        final JsonMapper jsonMapper = getMapper(useAnnotation);
        String json = null;

        /* obj received is null, return null */
        if (obj == null) {
            logger.warn(NULL_WARNING);
            return json;
        }

        try {
            json = jsonMapper.writeValueAsString(obj);
        } catch (final JsonProcessingException e) {
            logger.error(GENERIC_ERROR_MESSAGE, json, e);
        }
        return json;
    }

    /**
     * Serializes the given object into a JSON-formatted string using default settings.
     * This method uses Jackson's default annotation handling for the serialization process.
     *
     * @param obj The object to be serialized into JSON. It can be any Java object
     *            that is serializable by Jackson. If null, the method will return null.
     * @return A JSON-formatted string representation of the provided object.
     *         Returns null if an exception occurs during the serialization process
     *         or if the input object is null.
     */
    public static String toJson(final Object obj) {
        return toJson(obj, true);
    }

    /**
     * Deserializes the provided JSON string into an instance of the specified class type.
     *
     * @param <T>           The type of the object to be returned.
     * @param json          The JSON string to be deserialized. Must not be null or empty
     *                      if deserialization is expected to succeed.
     * @param clazz         The {@code Class} object representing the type of the object
     *                      to be returned. This determines the target of deserialization.
     * @param useAnnotation A boolean flag indicating whether Jackson annotations should be
     *                      enabled during deserialization. If {@code true}, annotations will
     *                      influence the deserialization behavior; otherwise, they will be ignored.
     * @return An instance of type {@code T} populated with data deserialized from the JSON string,
     *         or {@code null} if the deserialization fails or if the input JSON string is blank.
     */
    public static <T> T fromJson(final String json, final Class<T> clazz, final boolean useAnnotation) {
        if (StringUtils.isBlank(json)) {
            logger.warn(BLANK_WARNING);
            return null;
        }

        if (clazz.equals(String.class)) {
            return clazz.cast(json);
        }

        final JsonMapper jsonMapper = getMapper(useAnnotation);
        try {
            if (StringUtils.isNotBlank(json)) {
                return jsonMapper.readValue(json, clazz);
            }
        } catch (final Exception e) {
            logger.error(GENERIC_ERROR_MESSAGE, json, e);
        }
        return null;
    }

    /**
     * Deserializes the provided JSON string into an instance of the specified class type
     * using default annotation settings.
     *
     * @param <T>   The type of the object to be returned.
     * @param json  The JSON string to be deserialized. Must not be null or empty
     *              if deserialization is expected to succeed.
     * @param clazz The {@code Class} object representing the type of the object
     *              to be returned. This determines the target of deserialization.
     * @return An instance of type {@code T} populated with data deserialized from the JSON string,
     *         or {@code null} if the deserialization fails or if the input JSON string is blank.
     */
    public static <T> T fromJson(final String json, final Class<T> clazz) {
        return fromJson(json, clazz, true);
    }

    /**
     * Cleans up and formats a JSON string by converting it into a normalized and compact structure.
     * The method uses a {@code JsonMapper} instance to parse the input JSON and re-serialize it,
     * ensuring that the output has consistent formatting. If an exception occurs during processing,
     * the original input JSON string is returned.
     *
     * @param json The JSON string to be cleaned up and formatted. It must be well-formed for successful processing.
     * @return A cleaned-up and compacted JSON string. Returns the original JSON string in case of processing errors.
     */
    public static String cleanupJson(final String json) {
        if(StringUtils.isBlank(json)){
            logger.warn(BLANK_WARNING);
            return json;
        }

        try {
            final JsonMapper mapper = getMapper();

            /* converting it into a normalized and compact structure */
            final JsonNode jsonNode = getMapper().readTree(json);

            /* re-serialize it */
            return mapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            logger.error(GENERIC_ERROR_MESSAGE, json, e);
            return json;
        }
    }

    /**
     * Serializes the given object into a human-readable, indented JSON string. The serialization
     * process can optionally consider Jackson annotations based on the provided flag. If an error
     * occurs during the serialization process, the method logs the exception and returns null.
     *
     * @param obj           The object to be serialized into pretty-printed JSON. It can be any Java
     *                      object that is serializable by Jackson.
     * @param useAnnotation A boolean flag indicating whether Jackson annotations
     *                      should be considered during serialization. If true,
     *                      annotations will influence the serialization process;
     *                      otherwise, they will be ignored.
     * @return An indented, human-readable JSON-formatted string representation of the provided object.
     *         Returns null if an exception occurs during the serialization process or if the input object is null.
     */
    public static String prettyPrint(final Object obj, final boolean useAnnotation) {
        final JsonMapper jsonMapper = getMapper(useAnnotation);
        String json = null;

        /* obj received is null, return null */
        if (obj == null) {
            logger.warn(NULL_WARNING);
            return json;
        }

        try {
            json = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (final JsonProcessingException e) {
            logger.error(GENERIC_ERROR_MESSAGE, json, e);
        }
        return json;
    }

    /**
     * Serializes the given object into a human-readable, indented JSON string using default settings.
     * This method uses Jackson's default annotation handling for the serialization process.
     *
     * @param obj The object to be serialized into pretty-printed JSON. It can be any Java object
     *            that is serializable by Jackson. If null, the method will return null.
     * @return An indented, human-readable JSON-formatted string representation of the provided object.
     *         Returns null if an exception occurs during the serialization process or if the input object is null.
     */
    public static String prettyPrint(final Object obj) {
        return prettyPrint(obj, true);
    }

    /**
     * Reformats the provided JSON string into a human-readable, indented JSON string. The string
     * is first deserialized into a generic {@code Object} and then re-serialized using the
     * pretty-printing writer, so it must be well-formed for successful processing.
     *
     * @param json          The JSON string to be pretty-printed. Must not be null or blank
     *                      if the operation is expected to succeed.
     * @param useAnnotation A boolean flag indicating whether Jackson annotations should be
     *                      enabled during the intermediate deserialization/serialization steps.
     *                      If {@code true}, annotations will influence the behavior; otherwise, they will be ignored.
     * @return An indented, human-readable JSON-formatted string.
     *         Returns null if the input JSON string is blank or malformed.
     */
    public static String prettyPrint(final String json, final boolean useAnnotation) {
        final Object object = fromJson(json, Object.class, useAnnotation);
        return prettyPrint(object, useAnnotation);
    }

    /**
     * Reformats the provided JSON string into a human-readable, indented JSON string using
     * default annotation settings.
     *
     * @param json The JSON string to be pretty-printed. Must not be null or blank
     *             if the operation is expected to succeed.
     * @return An indented, human-readable JSON-formatted string.
     *         Returns null if the input JSON string is blank or malformed.
     */
    public static String prettyPrint(final String json) {
        return prettyPrint(json, true);
    }

}
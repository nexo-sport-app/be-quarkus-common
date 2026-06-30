package it.nexoapp.common.logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.nexoapp.common.utils.JSONUtils;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractLoggingFilter {

    protected static final Logger logger = LoggerFactory.getLogger("CORE");

    protected static final Set<MediaType> READABLE_MEDIA_TYPES = Set.of(
            MediaType.APPLICATION_JSON_TYPE,
            MediaType.TEXT_PLAIN_TYPE,
            MediaType.APPLICATION_FORM_URLENCODED_TYPE,
            MediaType.APPLICATION_XML_TYPE,
            MediaType.APPLICATION_ATOM_XML_TYPE,
            MediaType.APPLICATION_SVG_XML_TYPE,
            MediaType.APPLICATION_XHTML_XML_TYPE
    );

    protected static final String REQUEST_TYPE = "Request";
    protected static final String RESPONSE_TYPE = "Response";
    protected static final String FAULT_TYPE = "Fault";
    protected static final String MASKED_VALUE = "##";

    @ConfigProperty(name = "athlon.logging.fields.to.mask", defaultValue = "Authorization,")
    List<String> fieldToMask;

    @ConfigProperty(name = "athlon.logging.fields.max.length", defaultValue = "100000")
    int maxFieldLength;

    /**
     * Gets the type level for logging.
     *
     * @return the type level string
     */
    protected abstract Log.Type getLogType();

    /**
     * Formats the headers map into a JSON string, blurring sensitive header values.
     *
     * @param headers the headers map to format
     * @return JSON string representation of headers
     */
    protected String formatHeaders(final MultivaluedMap<String, ?> headers) {
        Map<String, Object> headerMap = new HashMap<>();
        for(Map.Entry<String, ? extends List<?>> entry : headers.entrySet()){
            if(fieldToMask.contains(entry.getKey())){
                headerMap.put(entry.getKey(), List.of("*******"));
            } else {
                headerMap.put(entry.getKey(), entry.getValue());
            }
        }
        return JSONUtils.toJson(headerMap);
    }

    /**
     * Logs the inbound entity stream and returns it for further processing.
     *
     * @param sb      StringBuilder to append the entity content
     * @param stream  input stream to read
     * @param charset character set to use for reading
     * @return the reset input stream
     * @throws IOException if an I/O error occurs
     */
    protected InputStream logInboundEntity(final StringBuilder sb, InputStream stream, final Charset charset) throws IOException {
        if(!stream.markSupported()) {
            stream = new BufferedInputStream(stream);
        }
        stream.mark(Integer.MAX_VALUE);
        final byte[] entity = stream.readAllBytes();
        String body = new String(entity, charset);
        if(entity.length > 0 && !JSONUtils.isValidJson(body)) {
            body = JSONUtils.toJson(body);
        }
        sb.append(checkBody(body));
        stream.reset();
        return stream;
    }

    /**
     * Checks if the given media type is readable.
     *
     * @param mediaType the media type to check
     * @return true if the media type is readable, false otherwise
     */
    protected boolean isReadable(final MediaType mediaType) {
        if (mediaType == null){
            return true;
        } else {
            for (MediaType supportedMediaType : READABLE_MEDIA_TYPES) {
                if (supportedMediaType.isCompatible(mediaType)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the charset from the media type or returns UTF-8 as default.
     *
     * @param mediaType the media type to extract charset from
     * @return the charset specified in the media type or UTF-8
     */
    protected Charset getCharset(final MediaType mediaType) {
        final String charset = mediaType == null ? null : mediaType.getParameters().get(MediaType.CHARSET_PARAMETER);
        return charset == null ? StandardCharsets.UTF_8 : Charset.forName(charset);
    }

    /**
     * Processes the given JSON data, masking sensitive fields and ensuring proper formatting.
     * The method checks the fields in the JSON data based on predefined rules and returns
     * a properly formatted JSON string.
     *
     * @param data the input JSON data as a string
     * @return the modified and formatted JSON string, or the original input if an error occurs
     */
    protected String checkBody(final String data) {
        try {
            final JsonMapper mapper = JSONUtils.getMapper();
            final JsonNode root = mapper.readTree(data);
            checkNode(root, fieldToMask, maxFieldLength);

            return mapper.writeValueAsString(root);
        } catch (final Exception e) {
            logger.error("Error checking JSON data, caught: ", e);
            return data;
        }
    }

    /**
     * Recursively checks the given JSON node and modifies its contents based on
     * specified keys and maximum text length. If a key matches or the length of
     * the textual value exceeds the specified maximum length, the value is masked.
     *
     * @param node the root JSON node to be checked
     * @param keysToCheck a list of keys to be checked for masking requirements
     * @param maxLength the maximum allowed length for textual values before masking
     */
    protected void checkNode(JsonNode node, List<String> keysToCheck, int maxLength) {
        if (node.isObject()) {
            final ObjectNode obj = (ObjectNode) node;
            for (Map.Entry<String, JsonNode> entry : obj.properties()) {
                final String key = entry.getKey();
                final JsonNode child = entry.getValue();

                checkNode(child, keysToCheck, maxLength);

                if (child.isTextual() && (keysToCheck.contains(key) || child.asText().length() > maxLength)) {
                    obj.put(key, maskField(child.asText()));
                }

            }
        } else if (node.isArray()) {
            final ArrayNode arr = (ArrayNode) node;
            for (int i = 0; i < arr.size(); i++) {
                final JsonNode child = arr.get(i);
                checkNode(child, keysToCheck, maxLength);

                if(child.isTextual() && child.asText().length() > maxLength) {
                    arr.set(i, maskField(child.asText()));
                }
            }
        }
    }

    /**
     * Masks a given string by partially replacing its content with hash ("#") symbols.
     * If the input string has more than 4 characters, it returns a string where only
     * the first two and the last two characters remain unchanged, with "##" inserted
     * in between. For strings with 4 or fewer characters, it returns "##".
     *
     * @param field the input string to be masked
     * @return the masked string, or the original string if it is blank
     */
    protected String maskField(final String field){
        if(StringUtils.isBlank(field)) return field;

        if(field.length() > 4){
            return field.substring(0, 2) + MASKED_VALUE + field.substring(field.length() - 2);
        } else {
            return MASKED_VALUE;
        }
    }

    protected Log.Phase getResponsePhase(int status){
        if(status >= 200 && status < 300){
            return Log.Phase.RESPONSE;
        } else {
            return Log.Phase.FAULT;
        }
    }

    public void log(final Log log) {
        try {
            final StringBuilder sb = new StringBuilder();

            if(StringUtils.isNotBlank(log.getClientCallId())){
                sb.append("clientCallId=").append(log.getClientCallId()).append("  ");
            }

            sb.append("type").append("=").append(getLogType()).append("  ");

            if(log.getPhase() != null){
                sb.append("phase").append("=").append(log.getPhase()).append("  ");
            }

            if(StringUtils.isNotBlank(log.getMethod())) {
                sb.append("method").append("=").append(log.getMethod()).append("  ");
            }

            if(StringUtils.isNotBlank(log.getEndpoint())) {
                sb.append("\nurl").append("=").append(log.getEndpoint()).append("  ");
            }

            if(log.getStatus() != null) {
                sb.append("httpStatus").append("=").append(log.getStatus()).append("  ");
            }

            if(log.getElapsed() != null){
                sb.append("elapsed").append("=").append(log.getElapsed()).append("  ");
            }

            if(StringUtils.isNotBlank(log.getHeaders())) {
                sb.append("\nheaders").append("=").append(log.getHeaders()).append("  ");
            }

            if(StringUtils.isNotBlank(log.getBody())) {
                sb.append("\npayload").append("=").append(checkBody(log.getBody()));
            }

            if(StringUtils.isNotBlank(log.getSql())) {
                sb.append("\nsql").append("=").append(log.getSql());
            }

            logger.atLevel(log.getLevel()).log(sb.toString().trim());
        } catch (final Exception e) {
            logger.error("Error logging request or response, caught: ", e);
        }
    }

}

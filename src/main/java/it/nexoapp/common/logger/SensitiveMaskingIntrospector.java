package it.nexoapp.common.logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Jackson introspector that serializes every member annotated {@link Sensitive} as {@value #MASK}, whatever its type.
 * <p>
 * Used only by the dedicated body-logging mapper, never by the mappers that produce the HTTP payloads.
 */
class SensitiveMaskingIntrospector extends JacksonAnnotationIntrospector {

    /* - constants - */
    /** Text written in place of a sensitive value. */
    static final String MASK = "##";

    /** Serializer shared by every masked member. */
    private static final MaskSerializer MASK_SERIALIZER = new MaskSerializer();

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Object findSerializer(final Annotated annotated) {
        if (annotated.hasAnnotation(Sensitive.class)) {
            return MASK_SERIALIZER;
        }
        return super.findSerializer(annotated);
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Writes the mask in place of any value.
     */
    private static final class MaskSerializer extends StdSerializer<Object> {

        /**
         * Creates the serializer.
         */
        MaskSerializer() {
            super(Object.class);
        }

        // -------------------------------------------------------------------------------------------------------------

        /**
         * {@inheritDoc}
         */
        @Override
        public void serialize(final Object value, final JsonGenerator generator, final SerializerProvider provider)
                throws IOException {
            generator.writeString(MASK);
        }

    }

}

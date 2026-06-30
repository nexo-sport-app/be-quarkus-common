package it.nexoapp.common.domain;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents a key-value pair.
 * @param key the reference key
 * @param value the value associated with the key
 */
public record KeyValue(
        String key,
        Object value
) implements Serializable {

    @Serial
    private static final long serialVersionUID = -2726533000786862133L;

    /**
     * Creates a new KeyValue instance using the provided key and value.
     *
     * @param key the key corresponding to the KeyValue pair
     * @param value the value associated with the key
     * @return a new instance of KeyValue containing the provided key and value
     */
    public static KeyValue of(final String key, final Object value) {
        return new KeyValue(key, value);
    }

}
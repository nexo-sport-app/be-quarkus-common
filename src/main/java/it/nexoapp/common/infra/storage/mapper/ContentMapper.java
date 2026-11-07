package it.nexoapp.common.infra.storage.mapper;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * Manage the conversion of an object to a string and vice versa.
 */
public interface ContentMapper {


    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Convert an object to a string.
     * @param object the object to convert
     * @return the string representation of the object
     * @param <T> the type of the object
     */
    <T extends Serializable> String toString(@NotNull final T object);

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Convert a string to an object.
     * @param string the string to convert
     * @param clazz serialization class
     * @return the object represented by the string
     * @param <T> the type of the object
     */
    <T extends Serializable> T fromString(@NotEmpty final String string, @NotNull final Class<T> clazz);

    // -----------------------------------------------------------------------------------------------------------------

}
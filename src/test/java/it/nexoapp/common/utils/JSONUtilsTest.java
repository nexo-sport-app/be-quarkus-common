package it.nexoapp.common.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.io.Serial;
import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

class JSONUtilsTest {

    private static final Person VALID_PERSON = new Person("John", 30, "New York");
    private static final String VALID_JSON = "{\"name\":\"John\",\"age\":30,\"city\":\"New York\"}";
    private static final String INVALID_JSON = "{\"name\":\"John\",\"age\":30,\"city\":\"New York\"";

    /**
     * A simple test fixture class used exclusively for testing JSONUtils functionality.
     * <p>
     * This class represents a basic Person object with common properties (name, age, city)
     * and is used to verify serialization, deserialization, and JSON conversion operations
     * in the JSONUtils test suite.
     * </p>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Person implements Serializable {

        @Serial
        private static final long serialVersionUID = -8219516770938062523L;

        private String name;
        private int age;
        private String city;

    }

    // --
    // JSON Validation Tests
    // -----------------------------------------------------------------------------------------------------------------

    @Test
    void whenValidJsonIsProvided_thenIsValidJsonReturnsTrue() {
        // Act
        boolean result = JSONUtils.isValidJson(VALID_JSON);

        // Assert
        assertTrue(result, "Valid JSON should return true");
    }

    @Test
    void whenInvalidJsonIsProvided_thenIsValidJsonReturnsFalse() {
        // Act
        boolean result = JSONUtils.isValidJson(INVALID_JSON);

        // Assert
        assertFalse(result, "Invalid JSON should return false");
    }

    @Test
    void whenEmptyStringIsProvided_thenIsValidJsonReturnsFalse() {
        // Act
        boolean result = JSONUtils.isValidJson("");

        // Assert
        assertFalse(result, "Empty string should return false");
    }

    @Test
    void whenNullIsProvided_thenIsValidJsonReturnsFalse() {
        // Act
        boolean result = JSONUtils.isValidJson(null);

        // Assert
        assertFalse(result, "Null string should return false");
    }

    @Test
    void whenWhitespaceStringIsProvided_thenIsValidJsonReturnsFalse() {
        // Act
        boolean result = JSONUtils.isValidJson("    ");

        // Assert
        assertFalse(result, "Whitespace string should return false");
    }

    // --
    // Object to JSON Serialization Tests
    // -----------------------------------------------------------------------------------------------------------------

    @Test
    void whenValidObjectIsProvided_thenToJsonReturnsCorrectJson() {
        // Act
        String json = JSONUtils.toJson(VALID_PERSON);

        // Assert
        assertEquals(VALID_JSON, json, "Object should be converted to valid JSON");
    }

    @Test
    void whenNullObjectIsProvided_thenToJsonReturnsNull() {
        // Act
        String json = JSONUtils.toJson(null);

        // Assert
        assertNull(json, "Null object should return null");
    }

    @Test
    void whenExceptionalObjectIsProvided_thenToJsonHandlesGracefully() {
        // Arrange
        Object exceptionalObject = new Object() {
            @Override
            public String toString() {
                throw new RuntimeException("Serialization error");
            }
        };

        // Act
        String json = JSONUtils.toJson(exceptionalObject);

        // Assert
        assertEquals("{}", json, "Exceptional object should return empty JSON");
    }

    // --
    // JSON to Object Deserialization Tests
    // -----------------------------------------------------------------------------------------------------------------

    @Test
    void whenValidJsonStringProvided_thenFromJsonReturnsObject() {
        // Act
        Person result = JSONUtils.fromJson(VALID_JSON, Person.class);

        // Assert
        assertNotNull(result, "Valid JSON should return non-null object");
        assertEquals("John", result.getName(), "Name should be 'John'");
        assertEquals(30, result.getAge(), "Age should be 30");
    }

    @Test
    void whenInvalidJsonStringProvided_thenFromJsonReturnsNull() {
        // Act
        Person result = JSONUtils.fromJson(INVALID_JSON, Person.class);

        // Assert
        assertNull(result, "Invalid JSON should return null");
    }

    @Test
    void whenNullStringProvided_thenFromJsonReturnsNull() {
        // Act
        Person result = JSONUtils.fromJson(null, Person.class);

        // Assert
        assertNull(result, "Null JSON string should return null");
    }

    @Test
    void whenJsonStringAndWrongClassProvided_thenFromJsonReturnsNull() {
        // Act
        String result = JSONUtils.fromJson(VALID_JSON, String.class);

        // Assert
        assertEquals(VALID_JSON, result, "JSON string cast to String should match original");
    }

    // --
    // JSON Clean up Tests
    // -----------------------------------------------------------------------------------------------------------------

    @Test
    void whenValidJsonIsProvided_thenCleanupJsonReturnsFormattedJson() {
        // Arrange
        String messyJson = " { \"name\" : \"John\" , \"age\": 30 , \"city\":\"New York\" } ";

        // Act
        String cleanedJson = JSONUtils.cleanupJson(messyJson);

        // Assert
        assertEquals(VALID_JSON, cleanedJson, "Cleanup method should return correctly formatted JSON.");
    }

    @Test
    void whenInvalidJsonIsProvided_thenCleanupJsonReturnsOriginalString() {
        // Act
        String result = JSONUtils.cleanupJson(INVALID_JSON);

        // Assert
        assertEquals(INVALID_JSON, result, "Cleanup method should return the original string if the JSON is invalid.");
    }

    @Test
    void whenNullIsProvided_thenCleanupJsonReturnsNull() {
        // Act
        String result = JSONUtils.cleanupJson(null);

        // Assert
        assertNull(result, "Cleanup method should return null when input is null.");
    }

    @Test
    void whenEmptyStringIsProvided_thenCleanupJsonReturnsEmptyString() {
        // Act
        String result = JSONUtils.cleanupJson("");

        // Assert
        assertEquals("", result, "Cleanup method should return an empty string when input is an empty string.");
    }

    @Test
    void whenJsonWithWhitespaceIsProvided_thenCleanupJsonRemovesWhitespace() {
        // Arrange
        String jsonWithWhitespace = "\t{\n  \"name\": \"John\",\n  \"age\": 30,\n\n  \"city\": \"New York\"  }\n";

        // Act
        String cleanedJson = JSONUtils.cleanupJson(jsonWithWhitespace);

        // Assert
        assertEquals(VALID_JSON, cleanedJson, "Cleanup method should remove unnecessary whitespace from a JSON string.");
    }

}
package it.nexoapp.common.constant;

/**
 * Enumeration that centralizes the possible values for Quarkus profiles and environment types.
 * <p>
 * This enum defines the standard environments used across the application to ensure consistency
 * in environment configuration and profile management throughout the Quarkus-based system.
 */
public enum Environment {

    /**
     * Test environment.
     * Represents the environment used when running automated tests (unit and integration),
     * typically activated through the Quarkus {@code test} profile. It relies on in-memory
     * or mocked resources to keep test executions fast, isolated and reproducible.
     */
    TEST,

    /**
     * Dev environment.
     * Represents the development environment typically used for personal or small-scale testing.
     * This environment allows developers to iterate and debug application features
     * with minimal external dependencies.
     */
    DEV,

    /**
     * System Integration Testing (SIT) environment.
     * This environment is designed for testing the integration of various system components
     * to ensure they work together as expected. It validates the interaction between modules
     * and external systems, focusing on data flow and communication.
     */
    SIT,

    /**
     * User Acceptance Testing (UAT) environment.
     * This environment is intended for end-users to validate the application with real-world scenarios
     * before it is promoted to the production environment. It closely mirrors the production environment
     * to ensure accurate testing outcomes.
     */
    UAT,

    /**
     * Production environment.
     * Represents the live environment where the application is deployed for end-users.
     * This environment is configured to handle real-world traffic and ensures high availability,
     * reliability, and optimal performance.
     */
    PROD

}

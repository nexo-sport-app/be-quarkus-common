package it.nexoapp.common.security;

/**
 * Provider-agnostic identity of a caller: the issuer and the subject of the validated bearer token and nothing
 * else (D-01). It is the only token-derived data the platform keeps about a caller.
 *
 * @param issuer  the {@code iss} claim of the token, never blank
 * @param subject the {@code sub} claim of the token, never blank
 */
public record IdentityKey(String issuer, String subject) {

    /**
     * Validates that both parts are present.
     *
     * @throws IllegalArgumentException when the issuer or the subject is null or blank
     */
    public IdentityKey {
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("issuer must not be blank");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("subject must not be blank");
        }
    }

}

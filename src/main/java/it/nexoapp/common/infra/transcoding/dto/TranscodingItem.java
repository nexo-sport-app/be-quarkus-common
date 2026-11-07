package it.nexoapp.common.infra.transcoding.dto;

import jakarta.validation.constraints.NotEmpty;

/**
 * Represents an item used in transcoding processes, containing
 * domain, language, and value details.
 *
 * @param domain   The domain associated with the transcoding item.
 * @param language The language associated with the transcoding item.
 * @param value    The value to be transcoded.
 */
public record TranscodingItem(
        @NotEmpty
        String domain,
        @NotEmpty
        String language,
        @NotEmpty
        String value
) {}
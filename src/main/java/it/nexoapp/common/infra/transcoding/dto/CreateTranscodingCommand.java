package it.nexoapp.common.infra.transcoding.dto;

import it.nexoapp.common.domain.Date;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Represents a command to create a transcoding operation.
 *
 * @param list      Identifier for the transcoding list.
 * @param item      List of transcoding items to be processed.
 * @param validFrom Start time indicating the validity of the transcoding command.
 * @param validTo   End time indicating when the transcoding command is no longer valid.
 */
public record CreateTranscodingCommand(
        @NotEmpty
        String list,
        @NotEmpty
        List<@Valid TranscodingItem> item,
        @NotNull
        Date validFrom,
        @NotNull
        Date validTo
) {}
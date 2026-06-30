package it.nexoapp.common.infra.transcoding;

import it.nexoapp.common.domain.Date;
import it.nexoapp.common.infra.transcoding.dto.CreateTranscodingCommand;
import it.nexoapp.common.infra.transcoding.dto.ListHandler;
import it.nexoapp.common.infra.transcoding.dto.TranscodeCommand;
import it.nexoapp.common.infra.transcoding.exception.FromTranscodeNotFoundException;
import it.nexoapp.common.infra.transcoding.exception.ToTranscodeNotFoundException;
import it.nexoapp.common.infra.transcoding.exception.TranscodingException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public interface TranscodingFacade {

    Logger logger = LoggerFactory.getLogger(TranscodingFacade.class);

    /**
     * Default domain used when no domain is specified.
     */
    String DEFAULT_DOMAIN = "nexo";

    /**
     * Default language used when no language is specified.
     */
    String DEFAULT_LANGUAGE = "xx";

    // -----------------------------------------------------------------------------------------------------------------


    /**
     * Retrieves a list of elements of type T from the specified list name using the given handler to handle the data.
     *
     * @param <T> The type of elements in the list to be retrieved.
     * @param list The name of the list to retrieve data from.
     * @param handler The handler to process and retrieve the elements of type T from the list.
     * @param referenceDate The date used as a reference for the list retrieval.
     * @return A list of elements of type T retrieved and processed using the provided handler.
     */
    <T> List<T> getList(
            @NotEmpty
            final String list,
            @NotNull
            final ListHandler<T> handler,
            @NotNull
            final Date referenceDate
    );

    /**
     * Retrieves a list of elements of type T from the specified list name using the given handler to handle the data.
     *
     * @param <T> The type of elements in the list to be retrieved.
     * @param list The name of the list to retrieve data from.
     * @param handler The handler to process and retrieve the elements of type T from the list.
     * @return A list of elements of type T retrieved and processed using the provided handler.
     */
    <T> List<T> getList(
            @NotEmpty
            final String list,
            @NotNull
            final ListHandler<T> handler
    );

    /**
     * Transcode a value from one domain and language to another within a specified list category.
     *
     * @param command The TranscodeCommand containing the source value, target parameters, and additional context for the transcoding operation.
     * @return A string representing the transcoded value.
     * @throws FromTranscodeNotFoundException If no matching transcoding is found for the source value.
     * @throws ToTranscodeNotFoundException If no matching transcoding is found for the target value.
     */
    String transcode(@Valid @NotNull final TranscodeCommand command) throws TranscodingException;

    /**
     * Saves a list of transcoding commands for processing.
     *
     * @param commands List of CreateTranscodingCommand objects to be processed and stored.
     */
    void save(@NotNull final List<@Valid CreateTranscodingCommand> commands);

    /**
     * Saves a single transcoding command for processing and storage.
     *
     * @param commands The CreateTranscodingCommand object to be saved.
     */
    void save(@NotNull final CreateTranscodingCommand commands);

}
package it.nexoapp.common.infra.transcoding.exception;

import it.nexoapp.common.domain.Date;
import jakarta.ws.rs.core.Response;

import java.io.Serial;

/**
 * Exception thrown when a match for the specified "From" values could not be found during
 * the transcoding process.
 */
public class FromTranscodeNotFoundException extends TranscodingException {

    @Serial
    private static final long serialVersionUID = 3215664802059618303L;

    /* - constant - */
    private static final String CODE = TranscodingException.DEFAULT_CODE + ".missing.from";
    private static final String MESSAGE = "Could not find a match with From values. list='%s' domain='%s', language='%s', value='%s', referenceDate='%s'";

    /**
     * Constructor for FromTranscodeNotFoundException
     * @param list the requested list
     * @param fromDomain the searched domain
     * @param fromLanguage the searched language
     * @param value the searched language
     * @param referenceDate the reference date of the search
     */
    public FromTranscodeNotFoundException(
            final String list,
            final String fromDomain,
            final String fromLanguage,
            final String value,
            final Date referenceDate
    ) {
        super(Response.Status.NOT_FOUND, CODE,  String.format(MESSAGE, list, fromDomain, fromLanguage, value, referenceDate));
    }

}
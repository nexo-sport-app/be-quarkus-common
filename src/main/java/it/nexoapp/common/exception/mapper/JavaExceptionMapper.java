package it.nexoapp.common.exception.mapper;

import it.nexoapp.common.constant.LoggerName;
import it.nexoapp.common.constant.MDCConstant;
import it.nexoapp.common.exception.UnexpectedException;
import it.nexoapp.common.exception.dto.Fault;
import it.nexoapp.common.utils.MDCUtils;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class JavaExceptionMapper implements ExceptionMapper<Exception> {

    /* - logger - */
    private static final Logger logger = LoggerFactory.getLogger(LoggerName.EXCEPTION);

    @Override
    public Response toResponse(final Exception exception) {
        final UnexpectedException ex = new UnexpectedException(exception);
        logger.error("Caught a runtime exception: ", exception);
        return Response
                .status(ex.getStatus())
                .entity(
                        Fault.builder()
                                .trace(MDCUtils.get(MDCConstant.CALL_ID))
                                .origin(MDCUtils.get(MDCConstant.APP_CODE))
                                .timestamp(ex.getTimestamp())
                                .code(ex.getCode())
                                .message(exception.getMessage())
                                .build()
                )
                .build();
    }

}

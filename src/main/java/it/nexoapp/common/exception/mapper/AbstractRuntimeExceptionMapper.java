package it.nexoapp.common.exception.mapper;

import it.nexoapp.common.constant.LoggerName;
import it.nexoapp.common.constant.MDCConstant;
import it.nexoapp.common.exception.AbstractRuntimeException;
import it.nexoapp.common.exception.dto.Fault;
import it.nexoapp.common.utils.MDCUtils;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class AbstractRuntimeExceptionMapper implements ExceptionMapper<AbstractRuntimeException> {

    private static final Logger logger = LoggerFactory.getLogger(LoggerName.EXCEPTION);

    @Override
    public Response toResponse(final AbstractRuntimeException exception) {
        logger.error("Caught a runtime exception: ", exception);
        return Response
                .status(exception.getStatus())
                .entity(
                        Fault.builder()
                                .trace(MDCUtils.get(MDCConstant.CALL_ID))
                                .origin(MDCUtils.get(MDCConstant.APP_CODE))
                                .timestamp(exception.getTimestamp())
                                .code(exception.getCode())
                                .message(exception.getMessage())
                                .build()
                )
                .build();
    }

}

package it.nexoapp.common.logger.server;

import it.nexoapp.common.logger.AbstractLoggingFilter;
import it.nexoapp.common.logger.Log;
import jakarta.annotation.Priority;
import jakarta.interceptor.Interceptor;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
@Priority(Interceptor.Priority.APPLICATION - 100)
public class ServerLoggingInFilter extends AbstractLoggingFilter implements ContainerRequestFilter {

    @Override
    protected Log.Type getLogType() {
        return Log.Type.SERVER;
    }

    @Override
    public void filter(final ContainerRequestContext rcx) throws IOException {
        rcx.setProperty("elapsed", System.currentTimeMillis());

        final StringBuilder body = new StringBuilder();
        if(rcx.hasEntity() && isReadable(rcx.getMediaType())) {
            rcx.setEntityStream(
                    logInboundEntity(body, rcx.getEntityStream(), getCharset(rcx.getMediaType()))
            );
        }



        log(
                Log.builder()
                        .phase(Log.Phase.REQUEST)
                        .method(rcx.getMethod())
                        .endpoint(rcx.getUriInfo().getAbsolutePath().toString())
                        .headers(formatHeaders(rcx.getHeaders()))
                        .body(body.toString())
                        .build()
        );

    }

}
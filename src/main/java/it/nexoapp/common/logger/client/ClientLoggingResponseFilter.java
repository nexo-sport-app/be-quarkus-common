package it.nexoapp.common.logger.client;

import it.nexoapp.common.logger.AbstractLoggingFilter;
import it.nexoapp.common.logger.Log;
import it.nexoapp.common.utils.JSONUtils;
import jakarta.annotation.Priority;
import jakarta.interceptor.Interceptor;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
@Priority(Interceptor.Priority.LIBRARY_AFTER)
public class ClientLoggingResponseFilter extends AbstractLoggingFilter implements ClientResponseFilter {

    @Override
    protected Log.Type getLogType() {
        return Log.Type.CLIENT;
    }

    @Override
    public void filter(final ClientRequestContext rqx, final ClientResponseContext rsx) throws IOException {
        final StringBuilder body = new StringBuilder();
        if(rsx.hasEntity() && isReadable(rsx.getMediaType())) {
            rsx.setEntityStream(
                    logInboundEntity(body, rsx.getEntityStream(), getCharset(rsx.getMediaType()))
            );
        }

        log(
                Log.builder()
                        .phase(getResponsePhase(rsx.getStatus()))
                        .method(rqx.getMethod())
                        .endpoint(rqx.getUri().toString())
                        .clientCallId((String) rqx.getProperty("clientCallId"))
                        .status(rsx.getStatus())
                        .elapsed(System.currentTimeMillis() - (Long) rqx.getProperty("elapsed"))
                        .headers(JSONUtils.toJson(rsx.getHeaders()))
                        .body(body.toString())
                        .build()
        );
    }

}
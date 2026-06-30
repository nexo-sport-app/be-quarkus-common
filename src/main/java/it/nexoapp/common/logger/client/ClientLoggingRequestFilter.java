package it.nexoapp.common.logger.client;

import it.nexoapp.common.logger.AbstractLoggingFilter;
import it.nexoapp.common.logger.Log;
import it.nexoapp.common.utils.JSONUtils;
import jakarta.annotation.Priority;
import jakarta.interceptor.Interceptor;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.ext.Provider;

import java.util.UUID;

@Provider
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
public class ClientLoggingRequestFilter extends AbstractLoggingFilter implements ClientRequestFilter {

    @Override
    protected Log.Type getLogType() {
        return Log.Type.CLIENT;
    }

    @Override
    public void filter(final ClientRequestContext rqx) {
        final String clientCallId = UUID.randomUUID().toString();
        rqx.setProperty("clientCallId", clientCallId);
        rqx.setProperty("elapsed", System.currentTimeMillis());
        String body = "";
        if(rqx.hasEntity()) {
            body = JSONUtils.toJson(rqx.getEntity());
        }

        log(
                Log.builder()
                        .phase(Log.Phase.RESPONSE)
                        .method(rqx.getMethod())
                        .endpoint(rqx.getUri().toString())
                        .clientCallId(clientCallId)
                        .headers(formatHeaders(rqx.getHeaders()))
                        .body(body)
                        .build()
        );
    }

}
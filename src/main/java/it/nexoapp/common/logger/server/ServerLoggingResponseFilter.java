package it.nexoapp.common.logger.server;

import it.nexoapp.common.logger.AbstractLoggingFilter;
import it.nexoapp.common.logger.Log;
import it.nexoapp.common.utils.JSONUtils;
import jakarta.annotation.Priority;
import jakarta.interceptor.Interceptor;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Interceptor.Priority.APPLICATION + 100)
public class ServerLoggingResponseFilter extends AbstractLoggingFilter implements ContainerResponseFilter {

    @Override
    protected Log.Type getLogType() {
        return Log.Type.SERVER;
    }

    @Override
    public void filter(final ContainerRequestContext rqx, final ContainerResponseContext rsx) {
        String body = "";
        if(rsx.hasEntity()) {
            body = JSONUtils.toJson(rsx.getEntity());
        }

        Long elapsed = null;
        if(rqx.hasProperty("elapsed")){
            elapsed = System.currentTimeMillis() - (Long) rqx.getProperty("elapsed");
        }

        log(      Log.builder()
                .phase(getResponsePhase(rsx.getStatus()))
                .method(rqx.getMethod())
                .endpoint(rqx.getUriInfo().getAbsolutePath().toString())
                .status(rsx.getStatus())
                .elapsed(elapsed)
                .headers(JSONUtils.toJson(rsx.getHeaders()))
                .body(body)
                .build()
        );
    }

}
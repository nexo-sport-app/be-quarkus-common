package it.nexoapp.common.context;

import it.nexoapp.common.constant.Headers;
import it.nexoapp.common.constant.MDCConstant;
import it.nexoapp.common.utils.MDCUtils;
import jakarta.annotation.Priority;
import jakarta.interceptor.Interceptor;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Interceptor.Priority.PLATFORM_AFTER)
public class RequestContextOutFilter implements ContainerResponseFilter {

    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) {
        responseContext.getHeaders().add(Headers.CALL_ID, MDCUtils.get(MDCConstant.CALL_ID));
    }

}
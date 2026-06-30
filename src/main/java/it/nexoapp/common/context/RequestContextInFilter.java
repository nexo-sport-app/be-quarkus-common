package it.nexoapp.common.context;

import io.vertx.ext.web.RoutingContext;
import it.nexoapp.common.configuration.AppInfo;
import it.nexoapp.common.constant.Headers;
import it.nexoapp.common.constant.Language;
import it.nexoapp.common.constant.MDCConstant;
import it.nexoapp.common.utils.MDCUtils;
import jakarta.annotation.Priority;
import jakarta.interceptor.Interceptor;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.UUID;

@Provider
@Priority(Interceptor.Priority.PLATFORM_BEFORE)
@RequiredArgsConstructor
public class RequestContextInFilter implements ContainerRequestFilter {

    final AppInfo appInfo;
    final RequestContext requestContext;
    final RoutingContext routingContext;
    final JsonWebToken jwt;

    @Override
    public void filter(final ContainerRequestContext rc) {
        // SERVICE INFO
        // Set app code
        MDCUtils.put(MDCConstant.APP_CODE, appInfo.code());

        // Set app node
        MDCUtils.put(MDCConstant.APP_NODE, appInfo.hostname());

        // REQUEST INFO
        // Get all Headers
        requestContext.setHeaders(rc.getHeaders());

        // Set up sessionId
        String sessionId = jwt.getClaim("sid");
        MDCUtils.put(MDCConstant.SESSION_ID, sessionId);
        requestContext.setSessionId(sessionId);

        // Set up callId
        String callId = StringUtils.defaultIfEmpty(rc.getHeaders().getFirst(Headers.CALL_ID), UUID.randomUUID().toString());
        MDCUtils.put(MDCConstant.CALL_ID, callId);
        requestContext.setCallId(callId);

        //Set up ip
        String clientIpAddress = routingContext.request().remoteAddress().host();
        MDCUtils.put(MDCConstant.IP, clientIpAddress);
        requestContext.setIp(clientIpAddress);

        //Set up language
        requestContext.setLanguage(Language.fromLocale(rc.getLanguage()));

        // USER INFO
        // Set up userId
        String userId = jwt.getClaim("sub");
        MDCUtils.put(MDCConstant.USER_ID, userId);
        requestContext.setUserId(userId);

        // Set up the username
        String username = jwt.getClaim("preferred_username");
        MDCUtils.put(MDCConstant.USERNAME, username);
        requestContext.setUsername(username);

        // Set up FirstName
        String firstName = jwt.getClaim("given_name");
        requestContext.setFirstName(firstName);

        // Set up LastName
        String lastName = jwt.getClaim("family_name");
        requestContext.setLastName(lastName);

        // Set up the username
        String email = jwt.getClaim("email");
        MDCUtils.put(MDCConstant.EMAIL, email);
        requestContext.setEmail(email);

    }

}
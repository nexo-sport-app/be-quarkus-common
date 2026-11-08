package it.nexoapp.common.context;

import io.quarkus.arc.Arc;
import it.nexoapp.common.configuration.AppInfo;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

/**
 * Default {@link ActorContext}: the explicit {@link ExecutionScope} actor first, then the member or
 * identity of the current request, then the system actor.
 * <p>
 * An active request context does not imply an HTTP request (tenant scopes activate one on bare threads), so
 * the request branch only counts when the {@link RequestContext} actually holds a member or a subject. Bare
 * threads without a request context never touch the request-scoped bean.
 */
@ApplicationScoped
@RequiredArgsConstructor
class ActorContextImpl implements ActorContext {

    /* - configuration - */
    private final AppInfo appInfo;

    /* - request - */
    /** Request-scoped context, read only while the request context is active. */
    private final RequestContext requestContext;

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public String currentActor() {
        return ExecutionScope.currentActor()
                .or(this::requestActor)
                .orElseGet(() -> ExecutionScope.systemActor(appInfo.code()));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Derives the actor from the current request: the member when the caller is linked, else the identity
     * provider subject.
     *
     * @return the member or idp actor, empty without an active request context or caller identifiers
     */
    private Optional<String> requestActor() {
        if (!Arc.container().requestContext().isActive()) {
            return Optional.empty();
        }
        final UUID memberId = requestContext.getMemberId();
        if (memberId != null) {
            return Optional.of(ExecutionScope.memberActor(memberId));
        }
        final String subject = requestContext.getSubject();
        if (subject != null && !subject.isBlank()) {
            return Optional.of(ExecutionScope.idpActor(subject));
        }
        return Optional.empty();
    }

}

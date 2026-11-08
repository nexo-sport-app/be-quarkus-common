package it.nexoapp.common.context;

import it.nexoapp.common.configuration.AppInfo;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

/**
 * Default {@link ActorContext}: explicit {@link ExecutionScope} actor first, then the system
 * actor. It has no request-scoped dependency, so it works on bare threads.
 */
@ApplicationScoped
@RequiredArgsConstructor
class ActorContextImpl implements ActorContext {

    /* - configuration - */
    private final AppInfo appInfo;

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public String currentActor() {
        return ExecutionScope.currentActor()
                .orElseGet(() -> ExecutionScope.systemActor(appInfo.code()));
    }

}

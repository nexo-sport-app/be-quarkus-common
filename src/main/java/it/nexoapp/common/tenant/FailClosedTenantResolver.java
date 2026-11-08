package it.nexoapp.common.tenant;

import io.quarkus.arc.Arc;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;
import it.nexoapp.common.context.RequestContext;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

/**
 * Hibernate tenant resolver that fails closed: the tenant comes from the explicit {@link TenantContext}
 * scope, else from the association of the current HTTP request when its caller is linked, and when there is
 * neither the session does not open. There is no default tenant (decision D-02).
 * The resolver is ignored by services that do not enable discriminator multi-tenancy.
 */
@PersistenceUnitExtension
@ApplicationScoped
@RequiredArgsConstructor
class FailClosedTenantResolver implements TenantResolver {

    /* - request - */
    /** Request-scoped context, read only while the request context is active. */
    private final RequestContext requestContext;

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * @throws TenantNotResolvedException always: the platform has no default tenant
     */
    @Override
    public String getDefaultTenantId() {
        throw new TenantNotResolvedException();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * <p>
     * An active request context does not imply an HTTP request (tenant scopes on bare threads activate one),
     * so the request branch only counts when the request context actually holds an association.
     *
     * @throws TenantNotResolvedException when neither an explicit tenant scope nor a linked request association exists
     */
    @Override
    public String resolveTenantId() {
        return TenantContext.current()
                .or(this::requestAssociation)
                .orElseThrow(TenantNotResolvedException::new);
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Reads the association of the current request.
     *
     * @return the association of a linked caller, empty without an active request context or association
     */
    private Optional<String> requestAssociation() {
        if (!Arc.container().requestContext().isActive()) {
            return Optional.empty();
        }
        final UUID association = requestContext.getAssociationId();
        return Optional.ofNullable(association).map(UUID::toString);
    }

}

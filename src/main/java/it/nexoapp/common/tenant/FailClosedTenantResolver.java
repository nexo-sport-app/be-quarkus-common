package it.nexoapp.common.tenant;

import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Hibernate tenant resolver that fails closed: the tenant comes from the explicit {@link TenantContext}
 * scope, and when there is none the session does not open. There is no default tenant (decision D-02).
 * The resolver is ignored by services that do not enable discriminator multi-tenancy.
 */
@PersistenceUnitExtension
@ApplicationScoped
class FailClosedTenantResolver implements TenantResolver {

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
     *
     * @throws TenantNotResolvedException when no explicit tenant scope is active
     */
    @Override
    public String resolveTenantId() {
        return TenantContext.current().orElseThrow(TenantNotResolvedException::new);
    }

}

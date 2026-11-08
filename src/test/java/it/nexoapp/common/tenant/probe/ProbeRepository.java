package it.nexoapp.common.tenant.probe;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

/**
 * Repository of the probe rows. Test bean of the common test application.
 */
@ApplicationScoped
public class ProbeRepository implements PanacheRepositoryBase<ProbeEntity, UUID> {

}

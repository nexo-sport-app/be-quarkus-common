package it.nexoapp.common.hibernate.listener;

import it.nexoapp.common.context.ActorContext;
import it.nexoapp.common.hibernate.entity.AuditEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;

/**
 * JPA listener that fills the audit columns of every {@link AuditEntity}. The actor comes from
 * {@link ActorContext}, which does not depend on an HTTP request, so jobs and consumers are
 * attributed too.
 */
@ApplicationScoped
@RequiredArgsConstructor
public class AuditEntityListener {

    /* - logger - */
    private static final Logger logger = LoggerFactory.getLogger(AuditEntityListener.class);

    /* - actor - */
    private final ActorContext actorContext;

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Sets created and updated actor and timestamp before an entity is inserted.
     *
     * @param entity entity about to be persisted
     */
    @PrePersist
    public void onPrePersist(final AuditEntity entity) {
        logger.trace("PrePersist - updating audit fields before persist");

        final String actor = actorContext.currentActor();
        final OffsetDateTime now = OffsetDateTime.now();

        entity.setCreatedBy(actor);
        entity.setCreatedAt(now);
        entity.setUpdatedBy(actor);
        entity.setUpdatedAt(now);
        logger.debug("PrePersist - audit fields updated. Actor: {}", actor);
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Sets updated actor and timestamp before an entity is updated.
     *
     * @param entity entity about to be updated
     */
    @PreUpdate
    public void onPreUpdate(final AuditEntity entity) {
        logger.trace("PreUpdate - updating audit fields before update");

        final String actor = actorContext.currentActor();
        final OffsetDateTime now = OffsetDateTime.now();

        entity.setUpdatedBy(actor);
        entity.setUpdatedAt(now);
        logger.debug("PreUpdate - audit fields updated. Actor: {}", actor);
    }

}

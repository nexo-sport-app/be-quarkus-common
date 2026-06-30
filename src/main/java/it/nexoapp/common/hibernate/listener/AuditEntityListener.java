package it.nexoapp.common.hibernate.listener;

import it.nexoapp.common.context.RequestContext;
import it.nexoapp.common.hibernate.entity.AuditEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;

@ApplicationScoped
@RequiredArgsConstructor
public class AuditEntityListener {

    private static final Logger logger = LoggerFactory.getLogger(AuditEntityListener.class);

    final RequestContext context;

    @PrePersist
    public void onPrePersist(final AuditEntity entity) {
        logger.trace("PrePersist - updating audit fields before persist");

        final String user = context.getUserId();
        logger.trace("PrePersist - user: {}", user);
        final OffsetDateTime now = OffsetDateTime.now();
        logger.trace("PrePersist - now: {}", now);

        entity.setCreatedBy(user);
        entity.setCreatedAt(now);
        entity.setUpdatedBy(user);
        entity.setUpdatedAt(now);
        logger.debug("PrePersist - audit fields updated. User: {}", user);
    }

    @PreUpdate
    public void onPreUpdate(final AuditEntity entity) {
        logger.trace("PreUpdate - updating audit fields before update");

        final String user = context.getUserId();
        logger.trace("PreUpdate - user: {}", user);
        final OffsetDateTime now = OffsetDateTime.now();
        logger.trace("PreUpdate - now: {}", now);

        entity.setUpdatedBy(user);
        entity.setUpdatedAt(now);
        logger.debug("PreUpdate - audit fields updated. User: {}", context.getUserId());
    }

}
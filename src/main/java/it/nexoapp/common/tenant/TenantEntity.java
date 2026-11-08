package it.nexoapp.common.tenant;

import it.nexoapp.common.hibernate.entity.AuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.TenantId;

import java.io.Serial;

/**
 * Base class of every tenant-owned entity. Hibernate fills the tenant column on writes and adds the
 * tenant filter to every read, update and delete. The attribute is a {@code String} on a {@code UUID}
 * column: services must set {@code quarkus.datasource.jdbc.additional-jdbc-properties.stringtype=unspecified}.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class TenantEntity extends AuditEntity {

    @Serial
    private static final long serialVersionUID = 7728514190263950442L;

    /* tenancy */
    /** Identifier of the owning association, assigned by Hibernate from the resolved tenant and never updated. */
    @TenantId
    @Column(
            name = "tenant_id",
            columnDefinition = "UUID",
            nullable = false,
            updatable = false
    )
    private String tenantId;

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Rejects writes of tenant rows inside an unscoped block: they would belong to no association.
     *
     * @throws IllegalStateException when called inside {@link TenantContext#runUnscoped(Runnable)}
     */
    @PrePersist
    void rejectUnscopedWrite() {
        if (TenantContext.isUnscoped()) {
            throw new IllegalStateException("Tenant rows cannot be written outside a tenant scope");
        }
    }

}

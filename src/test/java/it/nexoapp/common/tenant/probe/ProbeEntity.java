package it.nexoapp.common.tenant.probe;

import it.nexoapp.common.tenant.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.io.Serial;
import java.util.UUID;

/**
 * Probe entity of the common test application: a minimal audited table used to prove audit
 * attribution and tenant isolation on a real database. Test sources only.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "probe")
public class ProbeEntity extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 3318470182645930111L;

    /* identifiers */
    /** Unique identifier of the probe row. */
    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(
            name = "id",
            columnDefinition = "UUID",
            nullable = false,
            unique = true
    )
    private UUID id;

    /* probe data */
    /** Tax code of the probe subject. */
    @Column(
            name = "tax_code",
            columnDefinition = "VARCHAR(16)",
            nullable = false
    )
    private String taxCode;

    /** Last name of the probe subject. */
    @Column(
            name = "last_name",
            columnDefinition = "VARCHAR(40)",
            nullable = false
    )
    private String lastName;

    /* concurrency */
    /** Optimistic locking version. */
    @Version
    @Column(
            name = "version",
            columnDefinition = "BIGINT",
            nullable = false
    )
    private Long version;

}

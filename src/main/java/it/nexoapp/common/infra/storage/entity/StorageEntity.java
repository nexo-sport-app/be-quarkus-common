package it.nexoapp.common.infra.storage.entity;

import it.nexoapp.common.hibernate.entity.AuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Represents a storage entity mapped to the database table "storage".
 * This entity stores generic key-value pair data with a specified content type
 * and supports unique constraints across the combination of type and key.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "storage",
        uniqueConstraints = @UniqueConstraint(
                name = "storage_uq",
                columnNames = {"type", "key"}
        )
)
public class StorageEntity extends AuditEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 7307697878984321280L;

    /* identifiers */
    /**
     * Identifies the storage entity uniquely.
     */
    @Id
    @Column(
            name = "id",
            columnDefinition = "UUID",
            nullable = false,
            unique = true
    )
    private UUID id;

    /**
     * Identifies the storage entity type.
     */
    @Column(
            name = "type",
            columnDefinition = "TEXT",
            nullable = false
    )
    private String type;

    /**
     * Represent a unique entry for the type.
     */
    @Column(
            name = "key",
            columnDefinition = "TEXT",
            nullable = false
    )
    private String key;

    /* data */
    /**
     * Describe the content type of the payload.
     */
    @Column(
            name = "content_type",
            columnDefinition = "TEXT",
            nullable = false
    )
    private String contentType;

    /**
     * It contains the serialized object in the given content type.
     */
    @Column(
            name = "payload",
            columnDefinition = "TEXT",
            nullable = false
    )
    private String payload;

}

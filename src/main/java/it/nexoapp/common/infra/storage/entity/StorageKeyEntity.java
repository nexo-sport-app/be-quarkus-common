package it.nexoapp.common.infra.storage.entity;

import it.nexoapp.common.hibernate.entity.AuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "storage_key")
public class StorageKeyEntity extends AuditEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 3052847946838078118L;

    /**
     * The composite primary key of the storage-key.
     */
    @EmbeddedId
    private StorageKeyPK primaryKey;

    /**
     * The unique identifier of the storage.
     */
    @Column(name = "id", columnDefinition = "UUID", nullable = false)
    private UUID id;

}

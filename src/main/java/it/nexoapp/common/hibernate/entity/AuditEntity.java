package it.nexoapp.common.hibernate.entity;

import it.nexoapp.common.hibernate.listener.AuditEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditEntityListener.class)
public class AuditEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 4744142846061786685L;

    @Column(
            name = "created_by",
            columnDefinition = "TEXT",
            nullable = false
    )
    private String createdBy;

    @Column(
            name = "created_at",
            columnDefinition = "TIMESTAMPTZ",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    @Column(
            name = "updated_by",
            columnDefinition = "TEXT",
            nullable = false
    )
    private String updatedBy;

    @Column(
            name = "updated_at",
            columnDefinition = "TIMESTAMPTZ",
            nullable = false
    )
    private OffsetDateTime updatedAt;

}
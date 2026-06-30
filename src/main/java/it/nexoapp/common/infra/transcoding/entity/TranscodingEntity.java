package it.nexoapp.common.infra.transcoding.entity;

import it.nexoapp.common.hibernate.entity.AuditEntity;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.io.Serial;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "transcoding")
public class TranscodingEntity extends AuditEntity {

    @Serial
    private static final long serialVersionUID = 7707472501210383832L;

    @EmbeddedId
    private TranscodingPK primaryKey;

}
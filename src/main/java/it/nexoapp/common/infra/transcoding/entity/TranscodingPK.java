package it.nexoapp.common.infra.transcoding.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class TranscodingPK implements Serializable {

    @Serial
    private static final long serialVersionUID = -3373548717953279498L;

    @Column(
            name = "id",
            columnDefinition = "UUID",
            nullable = false
    )
    private UUID id;

    @Column(
            name = "list",
            columnDefinition = "TEXT",
            nullable = false
    )
    private String list;

    @Column(
            name = "domain",
            columnDefinition = "TEXT",
            nullable = false
    )
    private String domain;

    @Column(
            name = "language",
            columnDefinition = "TEXT",
            nullable = false
    )
    private String language;

    @Column(
            name = "value",
            columnDefinition = "TEXT",
            nullable = false
    )
    private String value;

    @Column(
            name = "valid_from",
            columnDefinition = "DATE",
            nullable = false
    )
    private LocalDate validFrom;

    @Column(
            name = "valid_to",
            columnDefinition = "DATE",
            nullable = false
    )
    private LocalDate validTo;

}

package it.nexoapp.common.infra.storage.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import org.apache.commons.lang3.Strings;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class StorageKeyPK implements Serializable {

    @Serial
    private static final long serialVersionUID = 1285350993549174770L;

    /**
     * The type of the storage.
     */
    @Column(
            name = "type",
            columnDefinition = "TEXT",
            nullable = false
    )
    private String type;

    /**
     * An alternative key for the storage.
     */
    @Column(
            name = "alt_key",
            columnDefinition = "TEXT",
            nullable = false
    )
    private String altKey;

   @Override
   public boolean equals(final Object obj) {
       if(obj == null) {
           return false;
       }

       if (!(obj instanceof StorageKeyPK key)) {
           return false;
       }

       return Strings.CS.equals(altKey, key.altKey)
               && Strings.CS.equals(type, key.type);
   }

    @Override
    public int hashCode() {
        return Objects.hash(type, altKey);
    }

}

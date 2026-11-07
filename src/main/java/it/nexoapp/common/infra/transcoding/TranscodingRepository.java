package it.nexoapp.common.infra.transcoding;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import it.nexoapp.common.infra.transcoding.entity.TranscodingEntity;
import it.nexoapp.common.infra.transcoding.entity.TranscodingPK;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
class TranscodingRepository implements PanacheRepositoryBase<TranscodingEntity, TranscodingPK> {

}
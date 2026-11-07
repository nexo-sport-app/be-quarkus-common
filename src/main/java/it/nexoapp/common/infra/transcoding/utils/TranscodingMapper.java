package it.nexoapp.common.infra.transcoding.utils;

import it.nexoapp.common.infra.transcoding.dto.CreateTranscodingCommand;
import it.nexoapp.common.infra.transcoding.dto.TranscodingItem;
import it.nexoapp.common.infra.transcoding.entity.TranscodingEntity;
import it.nexoapp.common.infra.transcoding.entity.TranscodingPK;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "cdi")
public interface TranscodingMapper {

    default List<TranscodingEntity> decouple(final CreateTranscodingCommand command) {
        // 1. create a parent uuid
        final UUID parentId = UUID.randomUUID();

        return command.item()
                .stream()
                .map(item ->
                    TranscodingEntity.builder()
                            .primaryKey(
                                    TranscodingPK.builder()
                                            .id(parentId)
                                            .list(command.list())
                                            .domain(item.domain())
                                            .language(item.language())
                                            .value(item.value())
                                            .validFrom(command.validFrom().toLocalDate())
                                            .validTo(command.validTo().toLocalDate())
                                            .build()
                            )
                            .build()
                ).collect(Collectors.toCollection(ArrayList::new));
    }

    @Mapping(target = ".", source = "primaryKey")
    TranscodingItem decouple(final TranscodingEntity entity);

}
package it.nexoapp.common.archfixture.mapperimpl;

import org.mapstruct.Mapper;

/**
 * Clean fixture: a MapStruct mapper interface. The processor generates a public {@code GoodMapperImpl} for it, which
 * the shared rules must allow.
 */
@Mapper
public interface GoodMapper {

    /**
     * Maps a source to a target.
     *
     * @param source the source
     * @return the target
     */
    GoodTarget map(GoodSource source);

}

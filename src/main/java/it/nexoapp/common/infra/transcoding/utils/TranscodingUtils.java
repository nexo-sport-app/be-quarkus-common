package it.nexoapp.common.infra.transcoding.utils;

import it.nexoapp.common.exception.IllegalInstantiationException;
import it.nexoapp.common.infra.transcoding.dto.TranscodingItem;
import org.apache.commons.lang3.Strings;

public class TranscodingUtils {

    private TranscodingUtils() {
        throw new IllegalInstantiationException();
    }


    public static boolean checkItem(
            final TranscodingItem item,
            final String domainToCheck,
            final String languageToCheck
    ) {
        if(item == null) {
            return false;
        }

        return Strings.CS.equals(item.domain(), domainToCheck) &&
                Strings.CS.equals(item.language(), languageToCheck);
    }

}
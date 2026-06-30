package it.nexoapp.common.infra.transcoding.dto;

import java.util.List;

/**
 * Handles conversion of related TranscodingItems into a single object.
 * <p>
 * Groups multiple transcoding items that belong together into one unified object.
 * For example, a municipality has separate items for ISTAT code, Belfiore code,
 * Italian name, and English name, which are combined into:
 * {@code Comune(id=ISTAT, belfiore=BELFIORE, nomeIt=NOME_IT, nomeEn=NOME_EN)}
 *
 * @param <T> The target object type
 */
public interface ListHandler<T> {

    /**
     * Processes a list of related TranscodingItem objects and converts them into a single
     * object of type T. This method is intended to unify multiple related
     * TranscodingItems into one cohesive representation.
     *
     * @param itemList the list of TranscodingItem objects to process. Each item
     *                 should contain domain, language, and value information
     *                 relevant to the transcoding operation.
     * @return an object of type T representing the unified result of processing
     *         the given list of TranscodingItem objects.
     */
    T handle(final List<TranscodingItem> itemList);

}

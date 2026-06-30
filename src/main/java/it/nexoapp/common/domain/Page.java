package it.nexoapp.common.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Represents a paginated structure for managing a subset of elements within a larger dataset.
 * @param <T> The type of elements contained in the page.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Page<T extends Serializable> implements Serializable {

    @Serial
    private static final long serialVersionUID = -1546430987544322414L;

    /**
     * Represents the content of the current page. The type of the elements in the list
     * is generic and varies depending on the layer using the {@code Page} class.
     */
    private List<T> content;

    /**
     * Represents the current page number in the pagination context.
     * This value is zero-based, meaning the first page is represented by 0.
     */
    private int pageNumber;
    /**
     * Represents the number of elements to be displayed on each page.
     */
    private int pageSize;
    /**
     * Represents the total number of elements in the entire dataset.
     */
    private long totalElements;
    /**
     * Represents the total number of pages available in the pagination context.
     */
    private int totalPages;

    // --
    // STATIC METHOD
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new instance of {@code Page} with the given content and metadata.
     * @param <T>          The type of elements in the content of the page.
     * @param content      The list of elements representing the content of the page. Cannot be null.
     * @param pageNumber   The current page number (zero-based index).
     * @param pageSize     The number of items per page. A value of 0 indicates no paging.
     * @param totalElements The total number of elements across all pages.
     * @return A new {@code Page} instance containing the specified content and metadata.
     */
    public static <T extends Serializable> Page<T> of(List<T> content, int pageNumber, int pageSize, long totalElements) {
        int totalPages = pageSize == 0 ? 0 : (int) Math.ceil((double) totalElements / pageSize);
        return Page.<T>builder()
                .content(content)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }

    // --
    // UTILITY METHODS
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Checks if the current page is the first page in the pagination sequence.
     * @return true if the current page is the first page (page number is 0), false otherwise.
     */
    public boolean isFirst() { return pageNumber == 0; }

    /**
     * Checks if the current page is the last page in the pagination sequence.
     * @return true if the current page is the last page (page number is greater than or equal to totalPages - 1),
     *         false otherwise.
     */
    public boolean isLast()  { return pageNumber >= totalPages - 1; }

    /**
     * Checks if the content of the current page is empty.
     * @return true if the content is null or contains no elements, false otherwise.
     */
    public boolean isEmpty() { return content == null || content.isEmpty(); }

    /**
     * Transforms the content of the current {@code Page} to a new type using the provided mapping function.
     * This method applies the given {@code mapper} function to each element in the content of the page
     * and returns a new {@code Page} instance containing the transformed content.
     *
     * @param <R> The type of elements in the resulting page after the transformation.
     * @param mapper A {@code Function} that takes an element of type {@code T} and returns
     *               an element of type {@code R}. This function is applied to each element
     *               in the current page's content.
     * @return A new {@code Page} instance containing the transformed elements, with the
     *         same page metadata (page number, page size, total elements) as the current page.
     */
    public <R extends Serializable> Page<R> map(java.util.function.Function<T, R> mapper) {
        return Page.of(
                content.stream().map(mapper).toList(),
                pageNumber,
                pageSize,
                totalElements
        );
    }

}
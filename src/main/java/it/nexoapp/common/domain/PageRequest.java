package it.nexoapp.common.domain;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.EnumUtils;

/**
 * Represents a request for paginated data, encapsulating details about the
 * desired page number, page size, sorting field, and sort direction.
 * This class is immutable and allows for the generation of a properly
 * validated and formatted page request.
 */
@Getter
@Builder
public class PageRequest {

    /**
     * The default size of a page when no specific size is provided or a given size is invalid.
     * It must be greater than zero and is generally capped by the maximum allowable size.
     */
    private static final int DEFAULT_SIZE = 20;

    /**
     * Represents the maximum allowable size for a page request.
     * This constant is used as an upper boundary to enforce size constraints,
     * preventing excessive or invalid page sizes.
     */
    private static final int MAX_SIZE = 1000;

    /**
     * Represents the page number for a paginated request.
     * Must be non-negative, with a default value of 0 if not specified.
     */
    private final int page;

    /**
     * Represents the number of items to be retrieved in a single page request.
     * This field is immutable and must be a positive integer.
     */
    private final int size;

    /**
     * Specifies the field by which the results should be sorted in a paginated request.
     * This determines the property or column that will be used as the basis for ordering the data.
     * The value of this variable is case-sensitive and must match the naming convention of the data source.
     * If not specified, a default value (e.g., "id") may be applied.
     */
    private final String sortBy;

    /**
     * Represents the direction of sorting to be applied when retrieving paginated results.
     * It determines whether the sorting should be in ascending or descending order.
     */
    private final SortDirection direction;

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Class that represents the direction of sorting for a page request.
     */
    public enum SortDirection { ASC, DESC }

    // --
    // STATIC METHOD
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new {@code PageRequest} object with the specified parameters, applying default values and validations
     * to ensure that the arguments are within acceptable ranges and formats.
     * If a parameter is null or invalid, a default value will be used.
     *
     * @param page      the page number, must be non-negative. If null or negative, defaults to 0.
     * @param size      the size of the page, must be greater than 0. If null or invalid, defaults to a predefined size.
     *                  The size will be capped at a maximum allowable value.
     * @param sortBy    the field to sort by. If null, defaults to "id".
     * @param direction the sort direction, either "ASC" (ascending) or "DESC" (descending). If null or invalid, defaults to "ASC".
     *
     * @return a {@code PageRequest} instance with the specified or default parameters.
     */
    public static PageRequest of(final Integer page, final Integer size, final String sortBy, final String direction) {
        return PageRequest.builder()
            .page(page != null && page >= 0 ? page : 0)
            .size(size != null && size > 0 ? Math.min(size, MAX_SIZE) : DEFAULT_SIZE)
            .sortBy(sortBy != null ? sortBy : "id")
            .direction(EnumUtils.getEnum(SortDirection.class, direction, SortDirection.ASC))
            .build();
    }

    // --
    // UTILITY METHODS
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Calculates the offset value for the current page based on the page number and the size of each page.
     * The offset represents the starting position of the items for the current page
     * in a sequential collection of data.
     * @return the calculated offset.
     */
    public long offset() { return (long) page * size; }

}
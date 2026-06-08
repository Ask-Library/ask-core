package pe.ask.core.model.pagination;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Represents a paginated response containing a list of items and pagination metadata.
 *
 * @param <T> the type of the content items in the page
 *
 * @author Allan Sagastegui
 */
@Data
@Builder
@AllArgsConstructor
public class PageResponse<T> {

    /**
     * Default constructor for PageResponse.
     */
    public PageResponse() {}

    /**
     * The list of items in the page.
     */
    private List<T> content;

    /**
     * The current page number.
     */
    private int pageNumber;

    /**
     * The size of the page.
     */
    private int pageSize;

    /**
     * The total number of elements.
     */
    private Long totalElements;

    /**
     * The total number of pages.
     */
    private Long totalPages;

    /**
     * Whether this is the last page.
     */
    private boolean isLast;

    /**
     * Whether the page has previous content.
     */
    private boolean hasPrevious;

    /**
     * Whether the page has next content.
     */
    private boolean hasNext;

}
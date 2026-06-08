package pe.ask.core.model.pagination;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Represents a request for paginated data.
 * <p>
 * Contains the page number, page size, sort field, and sort direction.
 * </p>
 *
 * @author Allan Sagastegui
 */
@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class PageRequest {
    /**
     * Default constructor for PageRequest.
     */
    public PageRequest() {
    }

    /**
     * The page number to request.
     */
    @Builder.Default
    private int page = 0;

    /**
     * The size of the page to request.
     */
    @Builder.Default
    private int size = 10;

    /**
     * The field to sort by.
     */
    @Builder.Default
    private String sort = "id";

    /**
     * The direction to sort by.
     */
    @Builder.Default
    private String direction = "ASC";
}

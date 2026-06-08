package pe.ask.core.utils;

import pe.ask.core.model.pagination.PageRequest;
import pe.ask.core.model.pagination.PageResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

/**
 * Utility for managing reactive pagination in Spring Data R2DBC.
 * <p>
 * Concurrently executes the data query and the total count using {@link Mono#zip},
 * automatically calculating pagination metadata (total pages, navigation flags).
 * </p>
 *
 * @author Allan Sagastegui
 */
public class ReactivePaginationUtil {

    private ReactivePaginationUtil() {
    }

    /**
     * Combines a data stream and a total count to generate a structured paginated response
     * without applying any transformation to the elements.
     *
     * @param dataQuery   the Flux executing the main query with limit/offset
     * @param countQuery  the Mono executing the COUNT(*) of the table or filter
     * @param pageRequest the requested pagination parameters
     * @param <T>         the data type of the entity or DTO
     * @return a Mono emitting the complete {@link PageResponse}
     */
    public static <T> Mono<PageResponse<T>> paginate(
            Flux<T> dataQuery,
            Mono<Long> countQuery,
            PageRequest pageRequest
    ) {
        return paginate(dataQuery, countQuery, pageRequest, Function.identity());
    }

    /**
     * Combines a data stream and a total count, transforming each element of the stream
     * from a source type to a target type (e.g., Entity to Domain, Domain to DTO).
     *
     * @param dataQuery   the Flux executing the main query with limit/offset (Source)
     * @param countQuery  the Mono executing the COUNT(*) of the table or filter
     * @param pageRequest the requested pagination parameters
     * @param mapper      the transformation function to map the elements (e.g., mapper::toDomain)
     * @param <S>         the source data type (Source)
     * @param <T>         the target data type (Target)
     * @return a Mono emitting the complete {@link PageResponse} with the transformed elements
     */
    public static <S, T> Mono<PageResponse<T>> paginate(
            Flux<S> dataQuery,
            Mono<Long> countQuery,
            PageRequest pageRequest,
            Function<S, T> mapper
    ) {
        return Mono.zip(dataQuery.map(mapper).collectList(), countQuery)
                .map(tuple -> {
                    List<T> content = tuple.getT1();
                    Long totalElements = tuple.getT2();

                    int pageNumber = pageRequest.getPage();
                    int pageSize = pageRequest.getSize();
                    long totalPages = totalElements == 0 ? 0 : (totalElements + pageSize - 1) / pageSize;

                    boolean hasPrevious = pageNumber > 0;
                    boolean hasNext = (pageNumber + 1) < totalPages;
                    boolean isLast = !hasNext;

                    return PageResponse.<T>builder()
                            .content(content)
                            .pageNumber(pageNumber)
                            .pageSize(pageSize)
                            .totalElements(totalElements)
                            .totalPages(totalPages)
                            .isLast(isLast)
                            .hasPrevious(hasPrevious)
                            .hasNext(hasNext)
                            .build();
                });
    }
}

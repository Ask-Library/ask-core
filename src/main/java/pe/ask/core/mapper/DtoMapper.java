package pe.ask.core.mapper;

/**
 * Contract for mapping between domain models and transfer objects (DTOs/Records).
 * <p>
 * Specifically designed to integrate with compile-time code generator libraries
 * (such as MapStruct) to support immutable Java Records.
 * </p>
 *
 * @param <D> The Domain model (Source).
 * @param <R> The Record or DTO for exposure (Destination).
 *
 * @author Allan Sagastegui
 */
public interface DtoMapper<D, R> {

    /**
     * Converts a domain model into a Record/DTO for the API response.
     *
     * @param domain the domain model to convert
     * @return the resulting DTO
     */
    R toDto(D domain);

    /**
     * Converts an incoming Record/DTO (request) into a domain model.
     *
     * @param dto the DTO to convert
     * @return the resulting domain model
     */
    D toDomain(R dto);
}
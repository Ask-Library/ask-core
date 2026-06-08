package pe.ask.core.mapper;

/**
 * Base contract for mapping between domain models and database entities.
 * <p>
 * Allows injecting generic or compile-time generated implementations
 * (such as MapStruct) to support immutable constructors and Records.
 * </p>
 *
 * @param <D> the domain model type
 * @param <E> the entity model type
 * @author Allan Sagastegui
 */
public interface EntityMapper<D, E> {

    /**
     * Converts a domain model into an entity.
     *
     * @param domain the domain model to convert
     * @return the resulting entity
     */
    E toEntity(D domain);

    /**
     * Converts an entity into a domain model.
     *
     * @param entity the entity to convert
     * @return the resulting domain model
     */
    D toDomain(E entity);
}
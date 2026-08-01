package pe.ask.core.mapper.impl;

import pe.ask.core.exception.MapFailedException;
import pe.ask.core.mapper.EntityMapper;
import pe.ask.core.mapper.util.MapperUtils;
import pe.ask.core.mapper.util.MapperUtils.PropertyCopier;

import java.beans.IntrospectionException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.List;

/**
 * A generic mapper for converting between domain models and database entities.
 * <p> * Constructors and property accessors are resolved during initialization,
 * avoiding repeated introspection during mapping operations.
 * </p>
 * @param <D> the domain model type
 * @param <E> the entity model type
 * @author Allan Sagastegui
 */
public class GenericBeanMapper<D, E> implements EntityMapper<D, E> {
    private final MethodHandle domainConstructor;
    private final MethodHandle entityConstructor;
    private final List<PropertyCopier> domainToEntityCopiers;
    private final List<PropertyCopier> entityToDomainCopiers;

    /**
     * Constructs a mapper and caches constructors and compatible properties.
     * @param domainClass the domain model class
     * @param entityClass the entity model class
     * @throws MapFailedException if the mapper cannot be initialized
     */
    public GenericBeanMapper(Class<D> domainClass, Class<E> entityClass) {
        try {
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            this.domainConstructor = MapperUtils.findNoArgsConstructor(domainClass, lookup);
            this.entityConstructor = MapperUtils.findNoArgsConstructor(entityClass, lookup);
            this.domainToEntityCopiers = MapperUtils.createPropertyCopiers(domainClass, entityClass, lookup);
            this.entityToDomainCopiers = MapperUtils.createPropertyCopiers(entityClass, domainClass, lookup);
        } catch (NoSuchMethodException | IllegalAccessException | IntrospectionException exception) {
            throw mappingConfigurationException(
                    "Classes %s and %s must have accessible public no-args constructors.".formatted(domainClass.getSimpleName(), entityClass.getSimpleName())
            );
        }
    }

    /**
     * Converts a domain model to an entity. *
     * @param domain the domain model to convert
     * @return the mapped entity, or {@code null} when the input is {@code null}
     * @throws MapFailedException if the mapping operation fails
     */
    @Override
    @SuppressWarnings("unchecked")
    public E toEntity(D domain) {
        if (domain == null) {
            return null;
        }
        try {
            E entity = (E) entityConstructor.invoke();
            MapperUtils.copyProperties(domain, entity, domainToEntityCopiers);
            return entity;
        } catch (Throwable exception) {
            throw mappingExecutionException("Could not map domain object to entity.");
        }
    }

    /**
     * Converts an entity to a domain model.
     * @param entity the entity to convert
     * @return the mapped domain model, or {@code null} when the input is {@code null}
     * @throws MapFailedException if the mapping operation fails
     */
    @Override
    @SuppressWarnings("unchecked")
    public D toDomain(E entity) {
        if (entity == null) {
            return null;
        }
        try {
            D domain = (D) domainConstructor.invoke();
            MapperUtils.copyProperties(entity, domain, entityToDomainCopiers);
            return domain;
        } catch (Throwable exception) {
            throw mappingExecutionException("Could not map entity to domain object.");
        }
    }

    private static MapFailedException mappingConfigurationException(String message) {
        return MapFailedException.builder().withMessage(message).build();
    }

    private static MapFailedException mappingExecutionException(String message) {
        return MapFailedException.builder().withMessage(message).build();
    }
}

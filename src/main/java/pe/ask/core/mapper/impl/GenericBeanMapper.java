package pe.ask.core.mapper.impl;

import org.springframework.beans.BeanUtils;
import pe.ask.core.exception.MapFailedException;
import pe.ask.core.mapper.EntityMapper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * A highly optimized generic mapper for converting between domain models and database entities.
 * <p>
 * Implements {@link EntityMapper} and uses {@link java.lang.invoke.MethodHandles} to cache
 * constructors upon initialization, eliminating the overhead of runtime reflection.
 * Note: This default mapper requires public no-args constructors and does not support Java Records.
 * </p>
 *
 * @param <D> the domain model type
 * @param <E> the entity model type
 *
 * @author Allan Sagastegui
 */
public class GenericBeanMapper<D, E> implements EntityMapper<D, E> {

    private final MethodHandle domainConstructor;
    private final MethodHandle entityConstructor;

    /**
     * Constructs a new GenericBeanMapper and caches the default constructors.
     *
     * @param domainClass the class of the domain model
     * @param entityClass the class of the entity model
     * @throws IllegalArgumentException if classes do not have a public no-args constructor
     */
    public GenericBeanMapper(Class<D> domainClass, Class<E> entityClass) {

        try {
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            this.domainConstructor = lookup.findConstructor(domainClass, MethodType.methodType(void.class));
            this.entityConstructor = lookup.findConstructor(entityClass, MethodType.methodType(void.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw MapFailedException.builder()
                    .withMessage("Critical: Classes " + domainClass.getSimpleName() +
                    " and " + entityClass.getSimpleName() + " must have a public no-args constructor. For Records, use a custom mapper like MapStruct.").build();
        }
    }

    /**
     * Converts a domain model to an entity.
     *
     * @param domain the domain model to convert
     * @return the resulting entity
     * @throws MapFailedException if mapping fails
     */
    @Override
    @SuppressWarnings("unchecked")
    public E toEntity(D domain) {
        if (domain == null) {
            return null;
        }
        try {
            E entity = (E) entityConstructor.invoke();
            BeanUtils.copyProperties(domain, entity);
            return entity;
        } catch (Throwable e) {
            throw new MapFailedException();
        }
    }

    /**
     * Converts an entity to a domain model.
     *
     * @param entity the entity to convert
     * @return the resulting domain model
     * @throws MapFailedException if mapping fails
     */
    @Override
    @SuppressWarnings("unchecked")
    public D toDomain(E entity) {
        if (entity == null) {
            return null;
        }
        try {
            D domain = (D) domainConstructor.invoke();
            BeanUtils.copyProperties(entity, domain);
            return domain;
        } catch (Throwable e) {
            throw new MapFailedException();
        }
    }
}
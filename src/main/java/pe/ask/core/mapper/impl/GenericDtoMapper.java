package pe.ask.core.mapper.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import pe.ask.core.exception.MapFailedException;
import pe.ask.core.mapper.DtoMapper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

/**
 * Generic and optimized implementation for mapping between Domain and DTOs/Records.
 * <p>
 * Features:
 * <ul>
 * <li>For traditional POJOs: Uses {@link java.lang.invoke.MethodHandles} to achieve
 * near-native performance.</li>
 * <li>For Java Records: Automatically detects if the DTO is a Record and resolves its
 * canonical constructor dynamically.</li>
 * </ul>
 *
 * @param <D> The Domain model (Source - must be a POJO with a no-args constructor)
 * @param <R> The exposition DTO (Destination - can be a POJO or Record)
 *
 * @author Allan Sagastegui
 */
public class GenericDtoMapper<D, R> implements DtoMapper<D, R> {

    private final MethodHandle domainConstructor;
    private MethodHandle dtoConstructor;

    private final boolean isDtoRecord;
    private Constructor<R> dtoRecordConstructor;
    private RecordComponent[] dtoRecordComponents;

    /**
     * Constructs a new GenericDtoMapper.
     *
     * @param domainClass the class of the domain model
     * @param dtoClass the class of the DTO
     */
    public GenericDtoMapper(Class<D> domainClass, Class<R> dtoClass) {
        this.isDtoRecord = dtoClass.isRecord();

        try {
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();

            this.domainConstructor = lookup.findConstructor(domainClass, MethodType.methodType(void.class));

            if (isDtoRecord) {
                this.dtoRecordComponents = dtoClass.getRecordComponents();
                Class<?>[] paramTypes = Arrays.stream(this.dtoRecordComponents)
                        .map(RecordComponent::getType)
                        .toArray(Class<?>[]::new);
                this.dtoRecordConstructor = dtoClass.getDeclaredConstructor(paramTypes);
            } else {
                this.dtoConstructor = lookup.findConstructor(dtoClass, MethodType.methodType(void.class));
            }

        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw MapFailedException.builder()
                    .withMessage(String.format("Error initializing mapper. Domain: '%s' must have an empty constructor. " +
                            "DTO: '%s' must have an empty constructor or be a valid Record.",
                    domainClass.getSimpleName(), dtoClass.getSimpleName())).build();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public R toDto(D domain) {
        if (domain == null) {
            return null;
        }
        try {
            if (isDtoRecord) {
                BeanWrapper domainWrapper = new BeanWrapperImpl(domain);
                Object[] constructorArgs = new Object[dtoRecordComponents.length];

                for (int i = 0; i < dtoRecordComponents.length; i++) {
                    String fieldName = dtoRecordComponents[i].getName();
                    if (domainWrapper.isReadableProperty(fieldName)) {
                        constructorArgs[i] = domainWrapper.getPropertyValue(fieldName);
                    }
                }
                return dtoRecordConstructor.newInstance(constructorArgs);
            } else {
                R dto = (R) dtoConstructor.invoke();
                BeanUtils.copyProperties(domain, dto);
                return dto;
            }
        } catch (Throwable e) {
            throw new MapFailedException();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public D toDomain(R dto) {
        if (dto == null) {
            return null;
        }
        try {
            D domain = (D) domainConstructor.invoke();
            BeanUtils.copyProperties(dto, domain);
            return domain;
        } catch (Throwable e) {
            throw new MapFailedException();
        }
    }
}
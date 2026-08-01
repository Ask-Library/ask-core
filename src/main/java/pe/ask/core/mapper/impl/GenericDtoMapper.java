package pe.ask.core.mapper.impl;

import pe.ask.core.exception.MapFailedException;
import pe.ask.core.mapper.DtoMapper;
import pe.ask.core.mapper.util.MapperUtils;
import pe.ask.core.mapper.util.MapperUtils.PropertyCopier;
import pe.ask.core.mapper.util.MapperUtils.RecordArgumentReader;

import java.beans.IntrospectionException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.RecordComponent;
import java.util.List;

/**
 * Generic mapper for converting between domain models and DTOs.
 * <p> * The DTO can be a mutable POJO with a public no-args constructor
 * or a Java Record with an accessible canonical constructor.
 * </p>
 * @param <D> the domain model type * @param <R> the DTO type
 * @author Allan Sagastegui
 */
public class GenericDtoMapper<D, R> implements DtoMapper<D, R> {
    private final MethodHandle domainConstructor;
    private final MethodHandle dtoConstructor;
    private final boolean dtoRecord;
    private final List<PropertyCopier> domainToDtoCopiers;
    private final List<PropertyCopier> dtoToDomainCopiers;
    private final List<RecordArgumentReader> dtoRecordReaders;

    /**
     * Creates a mapper and caches its constructors and property accessors.
     * @param domainClass the domain model class
     * @param dtoClass the DTO class
     * @throws MapFailedException if the mapper cannot be initialized
     */
    public GenericDtoMapper(Class<D> domainClass, Class<R> dtoClass) {
        this.dtoRecord = dtoClass.isRecord();
        try {
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            this.domainConstructor = MapperUtils.findNoArgsConstructor(domainClass, lookup);
            if (dtoRecord) {
                RecordComponent[] components = dtoClass.getRecordComponents();
                this.dtoConstructor = MapperUtils.findRecordConstructor(dtoClass, lookup);
                this.dtoRecordReaders = MapperUtils.createRecordArgumentReaders(domainClass, components, lookup);
                this.domainToDtoCopiers = List.of();
            } else {
                this.dtoConstructor = MapperUtils.findNoArgsConstructor(dtoClass, lookup);
                this.domainToDtoCopiers = MapperUtils.createPropertyCopiers(domainClass, dtoClass, lookup);
                this.dtoRecordReaders = List.of();
            }
            this.dtoToDomainCopiers = MapperUtils.createPropertyCopiers(dtoClass, domainClass, lookup);
        } catch (NoSuchMethodException | IllegalAccessException | IntrospectionException exception) {
            throw MapFailedException.builder()
                    .withMessage(
                            "Error initializing mapper. Domain '%s' must have a public no-args constructor. ".formatted(domainClass.getSimpleName())
                                    + "DTO '%s' must have a public no-args constructor or be a public Record.".formatted(dtoClass.getSimpleName())
                    )
                    .build();
        }
    }

    /**
     * Converts a domain model into a DTO.
     * @param domain the domain model to convert
     * @return the mapped DTO, or {@code null} when the input is {@code null}
     * @throws MapFailedException if the mapping operation fails
     */
    @Override
    @SuppressWarnings("unchecked")
    public R toDto(D domain) {
        if (domain == null) {
            return null;
        }
        try {
            if (dtoRecord) {
                Object[] arguments = MapperUtils.readRecordArguments(domain, dtoRecordReaders);
                return (R) dtoConstructor.invokeWithArguments(arguments);
            }
            R dto = (R) dtoConstructor.invoke();
            MapperUtils.copyProperties(domain, dto, domainToDtoCopiers);
            return dto;
        } catch (Throwable exception) {
            throw MapFailedException.builder()
                    .withMessage("Failed to map domain model '%s' to DTO.".formatted(domain.getClass().getSimpleName()))
                    .build();
        }
    }

    /**
     * Converts a DTO into a domain model.
     * @param dto the DTO to convert
     * @return the mapped domain model, or {@code null} when the input is {@code null}
     * @throws MapFailedException if the mapping operation fails
     */
    @Override
    @SuppressWarnings("unchecked")
    public D toDomain(R dto) {
        if (dto == null) {
            return null;
        }
        try {
            D domain = (D) domainConstructor.invoke();
            MapperUtils.copyProperties(dto, domain, dtoToDomainCopiers);
            return domain;
        } catch (Throwable exception) {
            throw MapFailedException.builder()
                    .withMessage("Failed to map DTO '%s' to domain model.".formatted(dto.getClass().getSimpleName()))
                    .build();
        }
    }
}

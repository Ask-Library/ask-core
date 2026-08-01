package pe.ask.core.mapper.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility methods used by reflection-based mappers. * * @author Allan Sagastegui
 */
public final class MapperUtils {
    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPERS =
            Map.of(
                    boolean.class, Boolean.class,
                    byte.class, Byte.class,
                    short.class, Short.class,
                    int.class, Integer.class,
                    long.class, Long.class,
                    float.class, Float.class,
                    double.class, Double.class,
                    char.class, Character.class
            );

    private MapperUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Finds the public no-args constructor for a type. * * @param type the type to inspect * @param lookup the method handles lookup * @return the constructor method handle * @throws NoSuchMethodException if the constructor does not exist * @throws IllegalAccessException if the constructor is not accessible
     */
    public static MethodHandle findNoArgsConstructor(
            Class<?> type,
            MethodHandles.Lookup lookup
    ) throws NoSuchMethodException, IllegalAccessException {
        return lookup.findConstructor(type, MethodType.methodType(void.class));
    }

    /**
     * Finds the canonical constructor of a Java Record. * * @param recordClass the Record class * @param lookup the method handles lookup * @return the canonical constructor method handle * @throws NoSuchMethodException if the constructor does not exist * @throws IllegalAccessException if the constructor is not accessible
     */
    public static MethodHandle findRecordConstructor(
            Class<?> recordClass,
            MethodHandles.Lookup lookup
    ) throws NoSuchMethodException, IllegalAccessException {
        RecordComponent[] components = recordClass.getRecordComponents();
        Class<?>[] parameterTypes = new Class<?>[components.length];
        for (int index = 0; index < components.length; index++) {
            parameterTypes[index] = components[index].getType();
        }
        return lookup.findConstructor(recordClass, MethodType.methodType(void.class, parameterTypes));
    }

    /**
     * Creates the property copiers for two compatible Java Bean types. * * @param sourceClass the source class * @param targetClass the target class * @param lookup the method handles lookup * @return immutable property copier list * @throws IntrospectionException if a type cannot be inspected * @throws IllegalAccessException if an accessor is not accessible
     */
    public static List<PropertyCopier> createPropertyCopiers(
            Class<?> sourceClass,
            Class<?> targetClass,
            MethodHandles.Lookup lookup
    ) throws IntrospectionException, IllegalAccessException {
        Map<String, ReadableProperty> sourceProperties = getReadableProperties(sourceClass, lookup);
        Map<String, WritableProperty> targetProperties = getWritableProperties(targetClass, lookup);
        List<PropertyCopier> copiers = new ArrayList<>();
        for (Map.Entry<String, ReadableProperty> entry : sourceProperties.entrySet()) {
            ReadableProperty sourceProperty = entry.getValue();
            WritableProperty targetProperty = targetProperties.get(entry.getKey());
            if (targetProperty != null && isAssignable(sourceProperty.type(), targetProperty.type())) {
                copiers.add(new PropertyCopier(sourceProperty.reader(), targetProperty.writer(), targetProperty.type().isPrimitive()));
            }
        }
        return List.copyOf(copiers);
    }

    /**
     * Creates readers for the canonical constructor arguments of a Record. * * @param sourceClass the source class * @param components the Record components * @param lookup the method handles lookup * @return immutable Record argument reader list * @throws IntrospectionException if the source cannot be inspected * @throws IllegalAccessException if an accessor is not accessible
     */
    public static List<RecordArgumentReader> createRecordArgumentReaders(
            Class<?> sourceClass,
            RecordComponent[] components,
            MethodHandles.Lookup lookup
    ) throws IntrospectionException, IllegalAccessException {
        Map<String, ReadableProperty> sourceProperties = getReadableProperties(sourceClass, lookup);
        List<RecordArgumentReader> readers = new ArrayList<>(components.length);
        for (RecordComponent component : components) {
            ReadableProperty sourceProperty = sourceProperties.get(component.getName());
            MethodHandle reader = sourceProperty != null && isAssignable(sourceProperty.type(), component.getType()) ? sourceProperty.reader() : null;
            readers.add(new RecordArgumentReader(component.getName(), reader));
        }
        return List.copyOf(readers);
    }

    /**
     * Copies all configured properties from source to target. * * @param source the source object * @param target the target object * @param copiers the configured property copiers * @throws Throwable if reading or writing a property fails
     */
    public static void copyProperties(
            Object source,
            Object target,
            List<PropertyCopier> copiers
    ) throws Throwable {
        for (PropertyCopier copier : copiers) {
            Object value = copier.reader().invoke(source);
            if (value != null || !copier.primitiveTarget()) {
                copier.writer().invoke(target, value);
            }
        }
    }

    /**
     * Reads the values used to invoke a Record canonical constructor. * * @param source the source object * @param readers the Record argument readers * @return the canonical constructor arguments * @throws Throwable if reading an argument fails
     */
    public static Object[] readRecordArguments(
            Object source,
            List<RecordArgumentReader> readers
    ) throws Throwable {
        Object[] arguments = new Object[readers.size()];
        for (int index = 0; index < readers.size(); index++) {
            MethodHandle reader = readers.get(index).reader();
            arguments[index] = reader == null ? null : reader.invoke(source);
        }
        return arguments;
    }

    private static Map<String, ReadableProperty> getReadableProperties(
            Class<?> type,
            MethodHandles.Lookup lookup
    ) throws IntrospectionException, IllegalAccessException {
        BeanInfo beanInfo = Introspector.getBeanInfo(type, Object.class);
        Map<String, ReadableProperty> properties = new HashMap<>();
        for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
            Method getter = descriptor.getReadMethod();
            if (getter != null) {
                properties.put(descriptor.getName(), new ReadableProperty(getter.getReturnType(), lookup.unreflect(getter)));
            }
        }
        if (type.isRecord()) {
            for (RecordComponent component : type.getRecordComponents()) {
                properties.put(component.getName(), new ReadableProperty(component.getType(), lookup.unreflect(component.getAccessor())));
            }
        }
        return properties;
    }

    private static Map<String, WritableProperty> getWritableProperties(
            Class<?> type,
            MethodHandles.Lookup lookup
    ) throws IntrospectionException, IllegalAccessException {
        BeanInfo beanInfo = Introspector.getBeanInfo(type, Object.class);
        Map<String, WritableProperty> properties = new HashMap<>();
        for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
            Method setter = descriptor.getWriteMethod();
            if (setter != null) {
                properties.put(descriptor.getName(), new WritableProperty(setter.getParameterTypes()[0], lookup.unreflect(setter)));
            }
        }
        return properties;
    }

    private static boolean isAssignable(
            Class<?> sourceType,
            Class<?> targetType
    ) {
        Class<?> wrappedSource = wrapPrimitive(sourceType);
        Class<?> wrappedTarget = wrapPrimitive(targetType);
        return wrappedTarget.isAssignableFrom(wrappedSource);
    }

    private static Class<?> wrapPrimitive(Class<?> type) {
        return type.isPrimitive() ? PRIMITIVE_WRAPPERS.getOrDefault(type, type) : type;
    }

    private record ReadableProperty(Class<?> type, MethodHandle reader) {
    }

    private record WritableProperty(Class<?> type, MethodHandle writer) {
    }

    /**
     * Defines a cached property reader and writer. * * @param reader the source getter * @param writer the target setter * @param primitiveTarget whether the setter receives a primitive
     */
    public record PropertyCopier(MethodHandle reader, MethodHandle writer, boolean primitiveTarget) {
    }

    /**
     * Defines a reader for a Record constructor argument. * * @param name the Record component name * @param reader the source property reader, or {@code null} when unavailable
     */
    public record RecordArgumentReader(String name, MethodHandle reader) {
    }
}

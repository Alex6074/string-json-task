package ru.clevertec.helper;

import ru.clevertec.exception.JsonParseException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JsonSerializer {

    public String toJson(Object obj) {
        return serializeObject(obj);
    }

    private String serializeObject(Object obj){
        if (obj == null) {
            return "null";
        }

        Class<?> clazz = obj.getClass();
        StringBuilder json = new StringBuilder();
        try {
            if (isPrimitiveOrWrapper(clazz)) {
              return obj.toString();
            } else if (clazz == String.class || clazz == UUID.class || clazz.isEnum()) {
                return "\"" + obj + "\"";
            } else if (obj instanceof Collection) {
                return serializeCollection((Collection<?>) obj);
            } else if (obj instanceof Map) {
                return serializeMap((Map<?, ?>) obj);
            } else if (clazz == OffsetDateTime.class) {
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSXXX");
                return "\"" + ((OffsetDateTime) obj).format(dateTimeFormatter) + "\"";
            } else if (clazz == LocalDate.class) {
                return "\"" + obj + "\"";
            }

            json.append("{");

            Field[] fields = clazz.getDeclaredFields();
            List<String> jsonFields = new ArrayList<>();

            for (Field field : fields) {
                field.setAccessible(true);
                if (Modifier.isTransient(field.getModifiers())) {
                    continue;
                }
                Object fieldValue = field.get(obj);
                String fieldName = field.getName();
                String serializedField = "\"" + fieldName + "\":" + serializeObject(fieldValue);
                jsonFields.add(serializedField);
            }

            json.append(String.join(",", jsonFields));
            json.append("}");

            return json.toString();
        } catch (IllegalAccessException ex) {
            throw new JsonParseException(ex.getMessage());
        }

    }

    private String serializeCollection(Collection<?> collection) {
        StringBuilder json = new StringBuilder();
        json.append("[");

        List<String> jsonElements = new ArrayList<>();
        for (Object element : collection) {
            jsonElements.add(serializeObject(element));
        }

        json.append(String.join(",", jsonElements));
        json.append("]");
        return json.toString();
    }

    private String serializeMap(Map<?, ?> map) {
        StringBuilder json = new StringBuilder();
        json.append("{");

        List<String> jsonEntries = new ArrayList<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = serializeObject(entry.getKey());
            String value = serializeObject(entry.getValue());
            jsonEntries.add(key + ":" + value);
        }

        json.append(String.join(",", jsonEntries));
        json.append("}");
        return json.toString();
    }

    private boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz == Boolean.class || clazz == Integer.class || clazz == Double.class
                || clazz == Float.class || clazz == Long.class || clazz == Short.class || clazz == Byte.class
                || clazz == BigDecimal.class || clazz == BigInteger.class;
    }
}


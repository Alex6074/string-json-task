package ru.clevertec.helper;

import ru.clevertec.exception.JsonParseException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JsonDeserializer {

    public <T> T fromJson(String jsonString, Class<T> clazz) {
        Object jsonObject = parseJsonValue(jsonString.trim());
        return deserializeObject(jsonObject, clazz);
    }

    private Object parseJsonValue(String json) {
        json = json.trim();

        if (json.startsWith("{")) {
            return parseJsonObject(json);
        } else if (json.startsWith("[")) {
            return parseJsonArray(json);
        } else if (json.startsWith("\"")) {
            return json.substring(1, json.length() - 1);
        } else if (json.equals("null")) {
            return null;
        } else if (json.equalsIgnoreCase("true") || json.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(json);
        } else {
            return new BigDecimal(json);
        }
    }

    private Map<String, Object> parseJsonObject(String json) {
        json = json.substring(1, json.length() - 1).trim();

        Map<String, Object> result = new HashMap<>();
        String[] pairs = splitKeyValuePairs(json);

        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            String key = trimQuotes(keyValue[0]);
            Object value = parseJsonValue(keyValue[1].trim());
            result.put(key, value);
        }

        return result;
    }

    private List<Object> parseJsonArray(String json) {
        json = json.substring(1, json.length() - 1).trim();

        List<Object> result = new ArrayList<>();
        String[] elements = splitJsonElements(json);

        for (String element : elements) {
            result.add(parseJsonValue(element.trim()));
        }

        return result;
    }

    private String[] splitKeyValuePairs(String json) {
        return splitJsonElements(json);
    }

    private String[] splitJsonElements(String json) {
        List<String> elements = new ArrayList<>();
        StringBuilder currentElement = new StringBuilder();
        boolean insideQuotes = false;
        int brackets = 0;

        for (char c : json.toCharArray()) {
            if (c == '"') {
                insideQuotes = !insideQuotes;
            } else if (!insideQuotes) {
                if (c == '{' || c == '[') {
                    brackets++;
                } else if (c == '}' || c == ']') {
                    brackets--;
                } else if (c == ',' && brackets == 0) {
                    elements.add(currentElement.toString().trim());
                    currentElement.setLength(0);
                    continue;
                }
            }
            currentElement.append(c);
        }

        if (!currentElement.isEmpty()) {
            elements.add(currentElement.toString().trim());
        }

        return elements.toArray(new String[0]);
    }

    private <T> T deserializeObject(Object jsonObject, Class<T> clazz) {
        if (jsonObject == null) {
            return null;
        }

        if (jsonObject instanceof Map) {
            return deserializeFromMap((Map<String, Object>) jsonObject, clazz);
        }

        throw new IllegalArgumentException("Unsupported JSON value for object deserialization: " + jsonObject);
    }

    private <T> T deserializeFromMap(Map<String, Object> jsonMap, Class<T> clazz) {
        try {
            T obj = clazz.getDeclaredConstructor().newInstance();
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                if (jsonMap.containsKey(fieldName)) {
                    Object fieldValue = jsonMap.get(fieldName);
                    Object deserializedValue = convertToFieldType(fieldValue, field.getType(), field.getGenericType());
                    field.set(obj, deserializedValue);
                }
            }

            return obj;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
            throw new JsonParseException(ex.getMessage());
        }

    }

    private Object convertToFieldType(Object value, Class<?> fieldType, Type genericType) {
        if (value == null) {
            return null;
        }

        if (fieldType == String.class) {
            return value.toString();
        } else if (fieldType == int.class || fieldType == Integer.class) {
            return Integer.parseInt(value.toString());
        } else if (fieldType == double.class || fieldType == Double.class) {
            return Double.parseDouble(value.toString());
        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
            return Boolean.parseBoolean(value.toString());
        } else if (fieldType == BigDecimal.class){
            return new BigDecimal(value.toString());
        } else if (fieldType == UUID.class) {
            return UUID.fromString(value.toString());
        } else if (fieldType == LocalDate.class) {
            return LocalDate.parse(value.toString());
        } else if (fieldType == OffsetDateTime.class) {
            return OffsetDateTime.parse(value.toString());
        } else if (fieldType == List.class) {
            Type[] listType = ((ParameterizedType) genericType).getActualTypeArguments();
            Class<?> listClass = (Class<?>) listType[0];
            return deserializeList((List<?>) value, listClass);
        } else if (fieldType == Map.class) {
            Type[] mapTypes = ((ParameterizedType) genericType).getActualTypeArguments();
            Class<?> keyType = (Class<?>) mapTypes[0];
            Class<?> valueType = (Class<?>) mapTypes[1];
            return deserializeMap((Map<?, ?>) value, keyType, valueType);
        }

        return deserializeObject(value, fieldType);
    }

    private List<Object> deserializeList(List<?> jsonList, Class<?> listType) {
        List<Object> list = new ArrayList<>();
        for (Object jsonElement : jsonList) {
            list.add(convertToFieldType(jsonElement, listType, listType));
        }
        return list;
    }

    private Map<Object, Object> deserializeMap(Map<?, ?> jsonMap, Class<?> keyType, Class<?> valueType) {
        Map<Object, Object> result = new HashMap<>();
        for (Map.Entry<?, ?> entry : jsonMap.entrySet()) {
            Object key = convertToFieldType(entry.getKey(), keyType, keyType);
            Object value = convertToFieldType(entry.getValue(), valueType, valueType);
            result.put(key, value);
        }
        return result;
    }

    private String trimQuotes(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        value = value.trim();

        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        } else if (value.startsWith("\"")) {
            return value.substring(1);
        } else if (value.endsWith("\"")) {
            return value.substring(0, value.length() - 1);
        }

        return value;
    }

}

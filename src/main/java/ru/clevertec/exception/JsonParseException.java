package ru.clevertec.exception;

public class JsonParseException extends RuntimeException{
    public JsonParseException(String message) {
        super(message);
    }
}

package ru.razborka.marketplace.common.exception;

import java.util.Collections;
import java.util.Map;

public class BusinessException extends RuntimeException {

    private final String code;
    private final Map<String, String> details;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
        this.details = Collections.emptyMap();
    }

    public BusinessException(String code, String message, Map<String, String> details) {
        super(message);
        this.code = code;
        this.details = details == null ? Collections.emptyMap() : Map.copyOf(details);
    }

    public String getCode() {
        return code;
    }

    public Map<String, String> getDetails() {
        return details;
    }
}

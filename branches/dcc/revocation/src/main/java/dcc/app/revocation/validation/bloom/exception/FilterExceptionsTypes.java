package dcc.app.revocation.validation.bloom.exception;

import java.util.Arrays;
import java.util.Optional;

public enum FilterExceptionsTypes {
    INVALID_PARAM("Invalid parameter supplied"),
    INVALID_SIZE("Invalid size"),
    OUT_OF_HEAP("Out of heap memory"),
    TOO_MANY_HASHES("Too many hash rounds!"),
    NO_SUCH_ALGO("Hash function provided not supported!"),
    IO_EXCEPTION("Error in I/O"),
    UNKNOWN_ERROR("Unknown error occured");

    private String type;

    FilterExceptionsTypes(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public static Optional<FilterExceptionsTypes> get(String type) {
        return Arrays.stream(FilterExceptionsTypes.values())
            .filter(obj -> obj.type.equals(type))
            .findFirst();
    }
}
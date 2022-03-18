package dcc.app.revocation.validation.bloom.exception;

import java.util.Optional;

public class FilterException extends Exception {

    private FilterExceptionsTypes exceptionsType;

    /**
     * Constructs a new exception with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public FilterException() {
        super();
        this.exceptionsType = FilterExceptionsTypes.UNKNOWN_ERROR;
    }

    public FilterException(String message, FilterExceptionsTypes type) {
        super(message);
        this.exceptionsType = type;
    }

    public FilterException(FilterExceptionsTypes type) {
        super();
        this.exceptionsType = type;
    }

    public FilterException(String exceptionType) {
        super();
        Optional<FilterExceptionsTypes> filterExceptionsTypes = FilterExceptionsTypes.get(exceptionType);
        this.exceptionsType = filterExceptionsTypes.orElse(FilterExceptionsTypes.UNKNOWN_ERROR);
    }
}

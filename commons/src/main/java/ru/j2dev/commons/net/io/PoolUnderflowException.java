package ru.j2dev.commons.net.io;

public class PoolUnderflowException extends RuntimeException {
    public PoolUnderflowException() {
    }

    public PoolUnderflowException(final String message) {
        super(message);
    }

    public PoolUnderflowException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public PoolUnderflowException(final Throwable cause) {
        super(cause);
    }
}
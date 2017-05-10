package com.firstutility.utils.hierarchygenerator.exception;

public class CantProcessAccountsException extends RuntimeException {

    private static final long serialVersionUID = 5727470414207131360L;

    public CantProcessAccountsException(final String message) {
        super(message);
    }

    public CantProcessAccountsException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

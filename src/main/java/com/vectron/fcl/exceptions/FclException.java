package com.vectron.fcl.exceptions;

public class FclException extends RuntimeException {
    public FclException(String message) {
        super(message);
    }

    public FclException(Throwable cause) {
        super(cause);
    }
}

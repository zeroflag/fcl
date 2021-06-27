package com.vectron.fcl.exceptions;

public class InterOpFailed extends FclException {
    public InterOpFailed(Throwable e) {
        super(e);
    }

    public InterOpFailed(String message) {
        super(message);
    }
}

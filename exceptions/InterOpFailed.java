package com.vectron.fcl.exceptions;

public class InterOpFailed extends FclException {
    public InterOpFailed(Exception e) {
        super(e);
    }

    public InterOpFailed(String message) {
        super(message);
    }
}

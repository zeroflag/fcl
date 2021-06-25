package com.vectron.fcl.exceptions;

import com.vectron.fcl.types.Obj;

public class TypeMismatched extends FclException {
    public TypeMismatched(String operator, Obj a, Obj b) {
        super(String.format("Unsupported types for %s: %s and %s", operator, a, b));
    }

    public TypeMismatched(String operator, Obj a) {
        super(String.format("Unsupported types for %s: %s", operator, a));
    }

    public TypeMismatched(Obj obj, String type) {
        super(obj + " (" + obj.getClass().getSimpleName()  + ") is not convertible to " + type);
    }

    public TypeMismatched(String message) {
        super(message);
    }
}

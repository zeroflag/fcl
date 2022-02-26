package com.vectron.fcl.types;

public class Chr extends Str {

    public Chr(Character chr) {
        super(String.valueOf(chr));
    }

    @Override
    public Bool iterable() {
        return Bool.FALSE;
    }
}

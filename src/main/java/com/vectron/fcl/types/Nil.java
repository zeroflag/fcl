package com.vectron.fcl.types;

import com.vectron.fcl.exceptions.TypeMismatched;

import static com.vectron.fcl.Fcl.STRICT;

public class Nil implements Obj {
    public static final Nil INSTANCE = new Nil();

    private Nil() {}

    @Override
    public long longValue() {
        if (STRICT) throw new TypeMismatched(this, "long");
        return 0l;
    }

    @Override
    public int intValue() {
        if (STRICT) throw new TypeMismatched(this, "int");
        return 0;
    }

    @Override
    public double doubleValue() {
        if (STRICT) throw new TypeMismatched(this, "double");
        return 0.0;
    }

    @Override
    public boolean boolValue() {
        if (STRICT) throw new TypeMismatched(this, "bool");
        return false;
    }

    @Override
    public Num asNum() {
        if (STRICT) throw new TypeMismatched(this, "num");
        return Num.ZERO;
    }

    @Override
    public Str asStr() {
        return new Str(toString());
    }

    @Override
    public Object value() {
        return null;
    }

    @Override
    public Object unwrap() {
        return value();
    }

    @Override
    public int compareTo(Obj o) {
        return -1;
    }

    @Override
    public String toString() {
        return "nil";
    }
}

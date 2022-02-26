package com.vectron.fcl.types;

import com.vectron.fcl.exceptions.TypeMismatched;

import static com.vectron.fcl.Fcl.STRICT;

public class Quot implements Obj {
    private final int address;
    private final int stackFrame;

    public static Quot create(int stackFrame, int address) {
        return new Quot(stackFrame, address);
    }

    public Quot(int stackFrame, int address) {
        this.address = address;
        this.stackFrame = stackFrame;
    }

    public Num address() {
        return new Num(address);
    }

    public Num stackFrame() {
        return new Num(stackFrame);
    }

    @Override
    public long longValue() {
        throw new TypeMismatched(this, "long");
    }

    @Override
    public int intValue() {
        throw new TypeMismatched(this, "int");
    }

    @Override
    public double doubleValue() {
        throw new TypeMismatched(this, "double");
    }

    @Override
    public boolean boolValue() {
        throw new TypeMismatched(this, "bool");
    }

    @Override
    public Object value() {
        throw new TypeMismatched(this, "value");
    }

    @Override
    public Object unwrap() {
        return value();
    }

    @Override
    public Num asNum() {
        if (STRICT) throw new TypeMismatched(this, "num");
        return Num.NAN;
    }

    @Override
    public Str asStr() {
        return new Str(toString());
    }

    @Override
    public int compareTo(Obj o) {
        return -1;
    }

    @Override
    public String toString() {
        return "Quotation: " + address + ", " + stackFrame;
    }

    @Override
    public Bool iterable() {
        return Bool.FALSE;
    }
}

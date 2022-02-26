package com.vectron.fcl.types;

import com.vectron.fcl.exceptions.TypeMismatched;

import java.util.Objects;

import static com.vectron.fcl.Fcl.STRICT;

public class Bool implements Obj, LogicOperand {
    public static final Bool TRUE = new Bool(true);
    public static final Bool FALSE = new Bool(false);
    private final boolean value;

    private Bool(boolean value) {
        this.value = value;
    }

    public Bool and(Obj other) {
        return new Bool(value && other.boolValue());
    }

    public Bool or(Obj other) {
        return new Bool(value || other.boolValue());
    }

    public Bool not() {
        return new Bool(!value);
    }

    @Override
    public boolean boolValue() {
        return value;
    }

    @Override
    public Num asNum() {
        if (STRICT) throw new TypeMismatched(this, "num");
        return value ? Num.ONE : Num.ZERO;
    }

    @Override
    public Object value() {
        return value;
    }

    @Override
    public Object unwrap() {
        return value();
    }

    @Override
    public Bool iterable() {
        return Bool.FALSE;
    }

    @Override
    public long longValue() {
        if (STRICT) throw new TypeMismatched(this, "long");
        return value ? 1l : 0l;
    }

    @Override
    public int intValue() {
        if (STRICT) throw new TypeMismatched(this, "int");
        return value ? 1 : 0;
    }

    @Override
    public double doubleValue() {
        if (STRICT) throw new TypeMismatched(this, "double");
        return value ? 1.0 : 0.0;
    }

    @Override
    public Str asStr() {
        return new Str(toString());
    }

    @Override
    public String toString() {
        return value ? "true" : "false";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bool bool = (Bool) o;
        return value == bool.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public int compareTo(Obj o) {
        if (o instanceof Bool)
            return Boolean.compare(value, o.boolValue());
        else
            return -1;
    }
}

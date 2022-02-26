package com.vectron.fcl.types;

import com.vectron.fcl.exceptions.TypeMismatched;

public class Symbol implements Obj {
    private final String symbol;

    public Symbol(String symbol) {
        this.symbol = symbol;
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
    public Num asNum() {
        return null;
    }

    @Override
    public Str asStr() {
        return new Str(symbol);
    }

    @Override
    public Object value() {
        return symbol;
    }

    @Override
    public Object unwrap() {
        return symbol;
    }

    @Override
    public String toString() {
        return ":" + symbol;
    }

    @Override
    public int compareTo(Obj other) {
        return other instanceof Symbol
                ? symbol.compareTo(((Symbol) other).symbol)
                : -1;
    }

    @Override
    public Bool iterable() {
        return Bool.FALSE;
    }
}

package com.vectron.fcl.types;

public interface Obj extends Comparable<Obj> {
    long longValue();
    int intValue();
    double doubleValue();
    boolean boolValue();
    Num asNum();
    Str asStr();
    Object value();
}
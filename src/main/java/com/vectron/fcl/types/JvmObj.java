package com.vectron.fcl.types;

import com.vectron.fcl.exceptions.TypeMismatched;

import static com.vectron.fcl.Fcl.STRICT;

public class JvmObj implements Obj {
    private final Object object;

    public JvmObj(Object object) {
        this.object = object;
    }

    @Override
    public long longValue() {
        if (object instanceof Number)
            return ((Number)object).longValue();
        throw new TypeMismatched(this, "long");
    }

    @Override
    public int intValue() {
        if (object instanceof Number)
            return ((Number)object).intValue();
        throw new TypeMismatched(this, "int");
    }

    @Override
    public double doubleValue() {
        if (object instanceof Number)
            return ((Number)object).doubleValue();
        throw new TypeMismatched(this, "double");
    }

    @Override
    public boolean boolValue() {
        if (object instanceof Boolean)
            return (boolean) object;
        throw new TypeMismatched(this, "bool");
    }

    @Override
    public Num asNum() {
        if (object instanceof Number)
            return new Num((Number)object);
        if (STRICT) throw new TypeMismatched(this, "num");
        return Num.NAN;
    }

    @Override
    public Str asStr() {
        return new Str(toString());
    }

    @Override
    public Object value() {
        return object;
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
        return "JvmObj:" + object;
    }
}

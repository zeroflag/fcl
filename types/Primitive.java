package com.vectron.fcl.types;

import com.vectron.fcl.exceptions.TypeMismatched;

import static com.vectron.fcl.Fcl.STRICT;

public class Primitive implements Word {
    private final Runnable code;
    private final String name;
    private boolean visible = true;

    public Primitive(String name, Runnable code) {
        this.code = code;
        this.name = name;
    }

    @Override
    public void visible(boolean isVisible) {
        this.visible = isVisible;
    }

    @Override
    public boolean visible() {
        return visible;
    }

    @Override
    public void enter() {
        code.run();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public long longValue() {
        throw new TypeMismatched(this,"long");
    }

    @Override
    public int intValue() {
        throw new TypeMismatched(this,"int");
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
        if (STRICT) throw new TypeMismatched(this, "num");
        return Num.NAN;
    }

    @Override
    public Str asStr() {
        return new Str(toString());
    }

    @Override
    public Object value() {
        return code;
    }

    @Override
    public String toString() {
        return "xt_" + name;
    }

    @Override
    public int compareTo(Obj other) {
        return other instanceof Primitive
                ? name.compareTo(((Primitive) other).name)
                : -1;
    }
}

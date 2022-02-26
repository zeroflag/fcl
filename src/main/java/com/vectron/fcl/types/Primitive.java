package com.vectron.fcl.types;

import com.vectron.fcl.Fcl;
import com.vectron.fcl.exceptions.Aborted;
import com.vectron.fcl.exceptions.TypeMismatched;

import static com.vectron.fcl.Fcl.STRICT;

public class Primitive implements Word {
    private final Runnable code;
    private final String name;
    private boolean visible = true;
    private boolean immediate;

    public Primitive(String name, Runnable code) {
        this.code = code;
        this.name = name;
    }

    @Override
    public void visible(boolean isVisible) {
        this.visible = isVisible;
    }

    @Override
    public boolean immediate() {
        return immediate;
    }

    @Override
    public void predicate(Word word) {
        throw new Aborted("primitive does not support predicate");
    }

    @Override
    public boolean match(String name, Fcl fcl) {
        return visible && this.name.equals(name);
    }

    @Override
    public void immediate(boolean isImmediate) {
        immediate = isImmediate;
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
    public Object unwrap() {
        return value();
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

    @Override
    public Bool iterable() {
        return Bool.FALSE;
    }
}

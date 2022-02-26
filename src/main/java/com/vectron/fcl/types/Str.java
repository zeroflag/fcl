package com.vectron.fcl.types;

import com.vectron.fcl.exceptions.TypeMismatched;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static com.vectron.fcl.Fcl.STRICT;

public class Str implements Obj, ArithmeticOperand {
    private final String value;

    public Str(String value) {
        this.value = value;
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
    public String value() {
        return value;
    }

    @Override
    public Num asNum() {
        if (STRICT) throw new TypeMismatched(this, "num");
        return Num.parse(value);
    }

    @Override
    public Object unwrap() {
        return value();
    }

    @Override
    public Str asStr() {
        return this;
    }

    @Override
    public String toString() {
        return "'" + value + "'";
    }

    public Str substr(int from, int to) {
        return new Str(value.substring(from, to));
    }

    public int size() {
        return value.length();
    }

    public Str at(Obj index) {
        return new Str(Character.toString(value.charAt(index.intValue())));
    }

    public Lst split(String delimiter) {
        Lst result = Lst.empty();
        for (String each : value.split(delimiter))
            result.append(new Str(each));
        return result;
    }

    public Str upper() {
        return new Str(value.toUpperCase());
    }

    public Str lower() {
        return new Str(value.toLowerCase());
    }

    public Str trim() {
        return new Str(value.trim());
    }

    public int indexOf(Obj s) {
        return value.indexOf((String)s.value());
    }

    public Str replace(String olds, String news) {
        return new Str(value.replaceAll(olds, news));
    }

    public Str concat(Obj str) {
        return new Str(value + (String)str.value());
    }

    public Str reverse() {
        return new Str(new StringBuilder(value).reverse().toString());
    }

    @Override
    public Bool iterable() {
        return Bool.TRUE;
    }

    public Iterator<Str> iterator() {
        return new Iterator<Str>() {
            private int index = 0;
            @Override
            public boolean hasNext() {
                return index < value.length();
            }

            @Override
            public Str next() {
                return new Chr(value.charAt(index++));
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Str str = (Str) o;
        return value.equals(str.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public int compareTo(Obj other) {
        return other instanceof Str
                ? value.compareTo(((Str) other).value)
                : -1;
    }

    public Str format(List<Obj> params) {
        Object[] a = new Object[params.size()];
        for (int i = 0; i < params.size(); i++) {
            a[i] = params.get(i).value();
        }
        return new Str(String.format(value, a));
    }

    public Str flatten() { return this; }

    @Override
    public Obj add(Obj other) {
        throw new TypeMismatched("+", this, other);
    }

    @Override
    public Obj sub(Obj other) {
        throw new TypeMismatched("+", this, other);
    }

    @Override
    public Obj mul(Obj other) {
        if (other instanceof Num) {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < other.intValue(); i++) {
                result.append(value);
            }
            return new Str(result.toString());
        }
        throw new TypeMismatched("*", this, other);
    }

    @Override
    public Obj div(Obj other) {
        throw new TypeMismatched("+", this, other);
    }

    @Override
    public Obj pow(Obj other) {
        throw new TypeMismatched("pow", this, other);
    }
}

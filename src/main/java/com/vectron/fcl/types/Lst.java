package com.vectron.fcl.types;

import com.vectron.fcl.exceptions.TypeMismatched;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.vectron.fcl.Fcl.STRICT;

public class Lst implements Obj, ArithmeticOperand {
    private final List<Obj> value = new ArrayList<>();

    public static Lst empty() {
        return new Lst();
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
        if (STRICT) throw new TypeMismatched(this, "num");
        return Num.NAN;
    }

    @Override
    public Object value() {
        return value;
    }

    @Override
    public int compareTo(Obj o) {
        return -1;
    }

    public int size() {
        return value.size();
    }

    public void append(Obj value) {
        this.value.add(value);
    }

    public void prep(Obj value) {
        this.value.add(0, value);
    }

    public Obj at(Obj index) {
        return value.get(index.intValue());
    }

    public int indexOf(Obj item) {
        return value.indexOf(item);
    }

    public Lst concat(Obj other) {
        Lst result = Lst.empty();
        result.value.addAll(value);
        result.value.addAll((List)other.value());
        return result;
    }

    public Lst subList(int start, int stop) {
        Lst result = Lst.empty();
        result.value.addAll(value.subList(start, stop));
        return result;
    }

    public void remove(Obj item) {
        value.remove(item);
    }

    public void removeAt(int index) {
        value.remove(index);
    }

    public void clear() {
        value.clear();
    }

    public Iterator<Obj> iterator() {
        return value.iterator();
    }

    public Lst reverse() {
        Lst result = Lst.empty();
        for (int i = value.size() - 1; i >= 0; i--)
            result.append(value.get(i));
        return result;
    }

    @Override
    public Str asStr() {
        return new Str(toString());
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("[ ");
        for (int i = 0; i < value.size(); i++) {
            Obj each = value.get(i);
            result.append(each);
            if (i < value.size() -1) result.append(" ");
        }
        result.append(" ]");
        return result.toString();
    }

    @Override
    public Lst add(Obj other) {
        if (other instanceof Num) {
            Lst result = Lst.empty();
            for (Obj each : value)
                result.append(each.asNum().add(other));
            return result;
        } else {
            throw new TypeMismatched("+", this, other);
        }
    }

    @Override
    public Lst sub(Obj other) {
        if (other instanceof Num) {
            Lst result = Lst.empty();
            for (Obj each : value)
                result.append(each.asNum().sub(other));
            return result;
        } else {
            throw new TypeMismatched("-", this, other);
        }
    }

    @Override
    public Lst mul(Obj other) {
        if (other instanceof Num) {
            Lst result = Lst.empty();
            for (Obj each : value)
                result.append(each.asNum().mul(other));
            return result;
        } else {
            throw new TypeMismatched("*", this, other);
        }
    }

    @Override
    public Lst div(Obj other) {
        if (other instanceof Num) {
            Lst result = Lst.empty();
            for (Obj each : value)
                result.append(each.asNum().div(other));
            return result;
        } else {
            throw new TypeMismatched("/", this, other);
        }
    }
}

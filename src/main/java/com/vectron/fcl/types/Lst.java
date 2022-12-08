package com.vectron.fcl.types;

import com.vectron.fcl.Fcl;
import com.vectron.fcl.exceptions.TypeMismatched;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.vectron.fcl.Fcl.STRICT;

public class Lst implements Obj, ArithmeticOperand, Iterable<Obj> {
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
    public List<Obj> value() {
        return value;
    }

    @Override
    public List<Object> unwrap() {
        List<Object> result = new ArrayList<>();
        for (Obj each : value)
            result.add(each.unwrap());
        return result;
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

    @Override
    public Bool iterable() {
        return Bool.TRUE;
    }

    @Override
    public Iterator<Obj> iterator() {
        return value.iterator();
    }

    public Lst reverse() {
        Lst result = Lst.empty();
        for (int i = value.size() - 1; i >= 0; i--)
            result.append(value.get(i));
        return result;
    }

    public Lst flatten() {
        Lst result = Lst.empty();
        result.value.addAll(flatten(this));
        return result;
    }

    private static List<Obj> flatten(Lst nested) {
        List<Obj> result = new ArrayList<>();
        for (Obj each : nested.value) {
            if (each instanceof Lst)
                result.addAll(flatten((Lst)each));
            else
                result.add(each);
        }
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
                result.append(Fcl.aOp(each).add(other));
            return result;
        } else if (other.iterable().boolValue()) {
            Lst result = Lst.empty();
            Iterator<Obj> it1 = this.iterator();
            Iterator<Obj> it2 = ((Iterable<Obj>) other).iterator();
            while (it1.hasNext() || it2.hasNext()) {
                Obj a = it1.hasNext() ? it1.next() : Num.ZERO;
                Obj b = it2.hasNext() ? it2.next() : Num.ZERO;
                result.append(Fcl.aOp(a).add(b));
            }
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
                result.append(Fcl.aOp(each).sub(other));
            return result;
        } else if (other.iterable().boolValue()) {
            Lst result = Lst.empty();
            Iterator<Obj> it1 = this.iterator();
            Iterator<Obj> it2 = ((Iterable<Obj>) other).iterator();
            while (it1.hasNext() || it2.hasNext()) {
                Obj a = it1.hasNext() ? it1.next() : Num.ZERO;
                Obj b = it2.hasNext() ? it2.next() : Num.ZERO;
                result.append(Fcl.aOp(a).sub(b));
            }
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
                result.append(Fcl.aOp(each).mul(other));
            return result;
        } else if (other.iterable().boolValue()) {
            Lst result = Lst.empty();
            Iterator<Obj> it1 = this.iterator();
            Iterator<Obj> it2 = ((Iterable<Obj>) other).iterator();
            while (it1.hasNext() || it2.hasNext()) {
                Obj a = it1.hasNext() ? it1.next() : Num.ONE;
                Obj b = it2.hasNext() ? it2.next() : Num.ONE;
                result.append(Fcl.aOp(a).mul(b));
            }
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
                result.append(Fcl.aOp(each).div(other));
            return result;
        } else if (other.iterable().boolValue()) {
            Lst result = Lst.empty();
            Iterator<Obj> it1 = this.iterator();
            Iterator<Obj> it2 = ((Iterable<Obj>) other).iterator();
            while (it1.hasNext() || it2.hasNext()) {
                Obj a = it1.hasNext() ? it1.next() : Num.ZERO;
                Obj b = it2.hasNext() ? it2.next() : Num.ONE;
                result.append(Fcl.aOp(a).div(b));
            }
            return result;
        } else {
            throw new TypeMismatched("/", this, other);
        }
    }

    @Override
    public Obj pow(Obj other) {
        if (other instanceof Num) {
            Lst result = Lst.empty();
            for (Obj each : value)
                result.append(Fcl.aOp(each).pow(other));
            return result;
        } else if (other.iterable().boolValue()) {
            Lst result = Lst.empty();
            Iterator<Obj> it1 = this.iterator();
            Iterator<Obj> it2 = ((Iterable<Obj>) other).iterator();
            while (it1.hasNext() || it2.hasNext()) {
                Obj a = it1.hasNext() ? it1.next() : Num.ONE;
                Obj b = it2.hasNext() ? it2.next() : Num.ONE;
                result.append(Fcl.aOp(a).pow(b));
            }
            return result;
        } else {
            throw new TypeMismatched("pow", this, other);
        }
    }
}

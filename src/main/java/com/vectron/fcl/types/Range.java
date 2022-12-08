package com.vectron.fcl.types;

import com.vectron.fcl.exceptions.InterOpFailed;
import com.vectron.fcl.exceptions.TypeMismatched;

import java.util.Iterator;

import static com.vectron.fcl.Fcl.STRICT;

public class Range implements Obj, Iterable<Obj>, ArithmeticOperand {
    private final Num from;
    private final Num to;
    private final Num by;

    public static Range create(Num by, Num to, Num from) {
        return new Range(from, to, by);
    }

    private Range(Num from, Num to, Num by) {
        if (by.doubleValue() == 0)
            throw new InterOpFailed("Invalid increment for range: " + by);
        this.from = from;
        this.to = to;
        this.by = by;
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
    public Str asStr() {
        return new Str(toString());
    }

    @Override
    public Bool iterable() {
        return Bool.TRUE;
    }

    @Override
    public Iterator<Obj> iterator() {
        return new RangeIterator(from, to, by);
    }

    @Override
    public String toString() {
        return by.doubleValue() == 1
                ? String.format("%s..%s", from, to)
                : String.format("%s..%s by %s", from, to, by);
    }

    @Override
    public Object value() {
        return new RangeIterator(from, to, by);
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
    public Obj add(Obj other) {
        return toLst().add(other);
    }

    @Override
    public Obj sub(Obj other) {
        return toLst().sub(other);
    }

    @Override
    public Obj mul(Obj other) {
        return toLst().mul(other);
    }

    @Override
    public Obj div(Obj other) {
        return toLst().div(other);
    }

    @Override
    public Obj pow(Obj other) {
        return toLst().pow(other);
    }

    private Lst toLst() {
        Lst result = Lst.empty();
        Iterator<Obj> it = iterator();
        while (it.hasNext())
            result.append(it.next());
        return result;
    }

    public static class RangeIterator implements Iterator<Obj> {
        private Num current;
        private final Num to;
        private final Num by;

        public RangeIterator(Num from, Num to, Num by) {
            this.current = from;
            this.to = to;
            this.by = by;
        }

        @Override
        public boolean hasNext() {
            return by.doubleValue() > 0
                    ? current.doubleValue() <= to.doubleValue()
                    : current.doubleValue() >= to.doubleValue();
        }

        @Override
        public Obj next() {
            Num result = current;
            current = (Num)current.add(by);
            return result;
        }
    }
}

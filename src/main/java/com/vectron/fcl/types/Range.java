package com.vectron.fcl.types;

import com.vectron.fcl.exceptions.InterOpFailed;
import com.vectron.fcl.exceptions.TypeMismatched;

import java.util.Iterator;

import static com.vectron.fcl.Fcl.STRICT;

public class Range implements Obj {
    private RangeIterator iterator;
    private final Num from;
    private final Num to;
    private final Num by;
    private Num current;

    public static Range create(Num by, Num to, Num from) {
        return new Range(from, to, by);
    }

    private Range(Num from, Num to, Num by) {
        if (by.doubleValue() == 0)
            throw new InterOpFailed("Invalid increment for range: " + by);
        this.from = from;
        this.to = to;
        this.by = by;
        this.current = from;
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

    public Iterator<Obj> iterator() {
        if (iterator == null)
            iterator = new RangeIterator();
        return iterator;
    }

    @Override
    public String toString() {
        return by.doubleValue() == 1
                ? String.format("%s..%s (%s)", from, to, current)
                : String.format("%s...%s (%s) by %s", from, to, current, by);
    }

    @Override
    public Object value() {
        return iterator;
    }

    @Override
    public Object unwrap() {
        return value();
    }

    @Override
    public int compareTo(Obj o) {
        return -1;
    }

    public class RangeIterator implements Iterator<Obj> {
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

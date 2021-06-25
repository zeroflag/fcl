package com.vectron.fcl.types;

import com.vectron.fcl.exceptions.InterOpFailed;
import com.vectron.fcl.exceptions.TypeMismatched;

import java.util.Iterator;

import static com.vectron.fcl.Fcl.STRICT;

public class Range implements Obj {
    private RangeIterator iterator;
    private final int from;
    private final int to;
    private final int by;
    private int current;

    public static Range create(int by, int to, int from) {
        return new Range(from, to, by);
    }

    private Range(int from, int to, int by) {
        if (by == 0)
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

    public Iterator<Obj> iterator() {
        if (iterator == null)
            iterator = new RangeIterator();
        return iterator;
    }

    @Override
    public String toString() {
        return by == 1
                ? String.format("%d..%d (%d)", from, to, current)
                : String.format("%d...%d (%d) by %d", from, to, current, by);
    }

    @Override
    public Object value() {
        return iterator;
    }

    @Override
    public int compareTo(Obj o) {
        return -1;
    }

    public class RangeIterator implements Iterator<Obj> {
        @Override
        public boolean hasNext() {
            return by > 0 ? current <= to : current >= to;
        }

        @Override
        public Obj next() {
            Num result = new Num(current);
            current += by;
            return result;
        }
    }
}

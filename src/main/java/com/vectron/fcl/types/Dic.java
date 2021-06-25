package com.vectron.fcl.types;

import com.vectron.fcl.exceptions.TypeMismatched;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.vectron.fcl.Fcl.STRICT;

public class Dic implements Obj {
    private final Map<Obj, Obj> value = new LinkedHashMap<>();

    public static Dic empty() {
        return new Dic();
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
    public Object value() {
        return value;
    }

    @Override
    public int compareTo(Obj o) {
        return -1;
    }

    public Iterator<Lst> iterator() {
        return new Iterator<Lst>() {
            private Iterator<Map.Entry<Obj,Obj>> it = value.entrySet().iterator();
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }
            @Override
            public Lst next() {
                Map.Entry<Obj, Obj> next = it.next();
                Lst result = Lst.empty();
                result.append(next.getKey());
                result.append(next.getValue());
                return result;
            }
        };
    }

    public int size() {
        return value.size();
    }

    public void put(Obj key, Obj value) {
        this.value.put(key, value);
    }

    public Obj at(Obj key) {
        return value.get(key);
    }

    public void remove(Obj item) {
        value.remove(item);
    }

    public Lst keys() {
        Lst result = Lst.empty();
        for (Obj each : value.keySet())
            result.append(each);
        return result;
    }

    public Lst values() {
        Lst result = Lst.empty();
        for (Obj each : value.values())
            result.append(each);
        return result;
    }

    public void clear() {
        value.clear();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("#[ ");
        int i = 0;
        for (Map.Entry<Obj, Obj> each : value.entrySet()) {
            result.append(each.getKey());
            result.append(" ");
            result.append(each.getValue());
            if (i < value.size() -1) result.append(" "); i++;
        }
        result.append(" ]#");
        return result.toString();
    }
}

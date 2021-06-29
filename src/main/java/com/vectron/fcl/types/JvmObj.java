package com.vectron.fcl.types;

import com.vectron.fcl.exceptions.TypeMismatched;

import java.util.Collection;
import java.util.Map;

import static com.vectron.fcl.Fcl.STRICT;

public class JvmObj implements Obj {
    private final Object object;

    public static Obj toFcl(Object object) {
        if (object == null)
            return Nil.INSTANCE;
        if (object instanceof Number)
            return new Num((Number)object);
        if (object instanceof CharSequence)
            return new Str(((CharSequence)object).toString());
        if (object instanceof Boolean)
            return (Boolean)object ? Bool.TRUE : Bool.FALSE;
        if (object instanceof Map) {
            Dic result = Dic.empty();
            for (Map.Entry<Object,Object> each : ((Map<Object,Object>) object).entrySet())
                result.put(toFcl(each.getKey()), toFcl(each.getValue()));
            return result;
        }
        if (object instanceof Collection) {
            Lst result = Lst.empty();
            for (Object each : (Collection) object)
                result.append(toFcl(each));
            return result;
        }
        return new JvmObj(object);
    }

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

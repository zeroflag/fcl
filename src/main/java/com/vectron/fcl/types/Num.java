package com.vectron.fcl.types;

import static com.vectron.fcl.Fcl.STRICT;

import com.vectron.fcl.exceptions.NotUnderstood;
import com.vectron.fcl.exceptions.TypeMismatched;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;

public class Num implements Obj, LogicOperand, ArithmeticOperand {
    public static final Num ZERO = new Num(0);
    public static final Num ONE = new Num(1);
    public static final Num MINUS_ONE = new Num(-1);
    public static final Num NAN = new Num(Double.NaN);
    private static final DecimalFormat format;
    private final Number value;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(' ');
        format = new DecimalFormat("###,###.##", symbols);
        format.setMaximumFractionDigits(4);
        format.setGroupingUsed(true);
        format.setParseIntegerOnly(false);
    }

    public Num(Number value) {
        if (value instanceof Integer)
            this.value = ((Integer)value).longValue();
        else
            this.value = value;
    }

    public static Num parse(String str) {
        try {
            if (str.startsWith("0x"))
                return new Num(Long.parseLong(str.substring(2), 16));
            else if (str.startsWith("0b"))
                return new Num(Long.parseLong(str.substring(2), 2));
            return new Num(Long.parseLong(str));
        } catch (NumberFormatException e1) {
            try {
                return new Num(Double.parseDouble(str));
            } catch (NumberFormatException e2) {
                throw new NotUnderstood("Undefined word: " + str);
            }
        }
    }

    @Override
    public String toString() {
        if (value instanceof Long)
            return Long.toString((Long) value);
        else if (value instanceof Double)
            return Double.toString((Double)value);
        return value.toString();
    }

    public String display(int radix) {
        if (value instanceof Long || value instanceof Double) {
            switch (radix) {
                case 16:
                    return value instanceof Long || approximatelyLong()
                            ? String.format("0x%04X", longValue())
                            : format.format(value);
                case 2:
                    return value instanceof Long || approximatelyLong()
                            ? String.format("%8sb", Long.toBinaryString(longValue())).replace(' ', '0')
                            : format.format(value);
                default:
                    return format.format(value);
            }
        }
        return value.toString();
    }

    private boolean approximatelyLong() {
        return Math.abs(longValue() - doubleValue()) < 0.001;
    }

    @Override
    public Obj add(Obj other) {
        if (value instanceof Long && other.value() instanceof Long)
            return new Num((Long) value + (Long)other.value());
        else if (value instanceof Long && other.value() instanceof Double)
            return new Num((Long) value + (Double)other.value());
        else if (value instanceof Double && other.value() instanceof Long)
            return new Num((Double) value + (Long)other.value());
        else if (value instanceof Double && other.value() instanceof Double)
            return new Num((Double) value + (Double) other.value());
        else if (other instanceof ArithmeticOperand && !(other instanceof Num))
            return ((ArithmeticOperand) other).add(this);
        else if (STRICT)
            throw new TypeMismatched("+", this, other);
        return Num.NAN;
    }

    @Override
    public Obj sub(Obj other) {
        if (value instanceof Long && other.value() instanceof Long)
            return new Num((Long) value - (Long)other.value());
        else if (value instanceof Long && other.value() instanceof Double)
            return new Num((Long) value - (Double)other.value());
        else if (value instanceof Double && other.value() instanceof Long)
            return new Num((Double) value - (Long)other.value());
        else if (value instanceof Double && other.value() instanceof Double)
            return new Num((Double) value - (Double) other.value());
        else if (other instanceof ArithmeticOperand && !(other instanceof Num))
            return ((ArithmeticOperand)((ArithmeticOperand) other).mul(Num.MINUS_ONE)).add(this);
        else if (STRICT)
            throw new TypeMismatched("-", this, other);
        return Num.NAN;
    }

    @Override
    public Obj mul(Obj other) {
        if (value instanceof Long && other.value() instanceof Long)
            return new Num((Long) value * (Long)other.value());
        else if (value instanceof Long && other.value() instanceof Double)
            return new Num((Long) value * (Double)other.value());
        else if (value instanceof Double && other.value() instanceof Long)
            return new Num((Double) value * (Long)other.value());
        else if (value instanceof Double && other.value() instanceof Double)
            return new Num((Double) value * (Double) other.value());
        else if (other instanceof ArithmeticOperand && !(other instanceof Num))
            return ((ArithmeticOperand) other).mul(this);
        else if (STRICT)
            throw new TypeMismatched("*", this, other);
        return Num.NAN;
    }

    @Override
    public Obj div(Obj other) {
        if (value instanceof Long && other.value() instanceof Long)
            return new Num(((Long) value).doubleValue() / (Long) other.value());
        else if (value instanceof Long && other.value() instanceof Double)
            return new Num((Long) value / (Double)other.value());
        else if (value instanceof Double && other.value() instanceof Long)
            return new Num((Double) value / (Long)other.value());
        else if (value instanceof Double && other.value() instanceof Double)
            return new Num((Double) value / (Double) other.value());
        else if (other instanceof ArithmeticOperand && !(other instanceof Num))
            return ((ArithmeticOperand) ((ArithmeticOperand) other).pow(Num.MINUS_ONE)).mul(this);
        else if (STRICT)
            throw new TypeMismatched("/", this, other);
        return Num.NAN;
    }

    @Override
    public Obj pow(Obj other) {
        if (other instanceof Num) {
            return new Num(Math.pow(doubleValue(), other.doubleValue()));
        } else if (other.iterable().boolValue()) {
            Lst result = Lst.empty();
            for (Obj obj : (Iterable<Obj>) other)
                result.append(this.pow(obj));
            return result;
        } else if (STRICT) {
            throw new TypeMismatched("pow", this, other);
        }
        return Num.NAN;
    }

    public Num mod(Num other) {
        try {
            return new Num(this.longValue() % other.longValue());
        } catch (TypeMismatched e) {
            if (STRICT) throw e;
            return Num.NAN;
        }
    }

    public Num intDiv(Num other) {
        try {
            return new Num(this.longValue() / other.longValue());
        } catch (TypeMismatched e) {
            if (STRICT) throw e;
            return Num.NAN;
        }
    }

    public Num round() {
        if (value instanceof Long)
            return this;
        else if (value instanceof Double)
            return new Num(Math.round(doubleValue()));
        else if (STRICT)
            throw new TypeMismatched("ROUND", this);
        return Num.NAN;
    }

    @Override
    public Num and(Obj other) {
        if (value instanceof Long && other.value() instanceof Long)
            return new Num((Long)value & other.longValue());
        else if (STRICT)
            throw new TypeMismatched("AND", this, other);
        return Num.NAN;
    }

    @Override
    public Num or(Obj other) {
        if (value instanceof Long && other.value() instanceof Long)
            return new Num((Long)value | other.longValue());
        else if (STRICT)
            throw new TypeMismatched("OR", this, other);
        return Num.NAN;
    }

    @Override
    public Num not() {
        if (value instanceof Long)
            return new Num(~(Long)value );
        else if (STRICT)
            throw new TypeMismatched("Unsupported types for NOT operator: " + value.getClass());
        return Num.NAN;
    }

    public Bool greater(Num other) {
        if (value instanceof Long && other.value instanceof Long)
            return (Long) value > (Long)other.value ? Bool.TRUE : Bool.FALSE;
        else if (value instanceof Long && other.value instanceof Double)
            return (Long) value > (Double)other.value ? Bool.TRUE : Bool.FALSE;
        else if (value instanceof Double && other.value instanceof Long)
            return (Double) value > (Long)other.value  ? Bool.TRUE : Bool.FALSE;
        else if (value instanceof Double && other.value instanceof Double)
            return (Double) value > (Double) other.value ? Bool.TRUE : Bool.FALSE;
        else
            throw new TypeMismatched("<", this, other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Num num = (Num) o;
        return value.equals(num.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public long longValue() {
        if (value instanceof Long)
            return (Long) value;
        else if (value instanceof Double)
            return Math.round((Double) value);
        else
            throw new TypeMismatched(this, "long");
    }

    @Override
    public int intValue() {
        return ((Number)value).intValue();
    }

    @Override
    public boolean boolValue() {
        if (STRICT)
            throw new TypeMismatched(this, "bool");
        else
            return value.longValue() != 0L;
    }

    @Override
    public Num asNum() {
        return this;
    }

    @Override
    public Str asStr() {
        return new Str(toString());
    }

    @Override
    public double doubleValue() {
        return ((Number)value).doubleValue();
    }

    @Override
    public Object value() {
        return value;
    }

    @Override
    public Object unwrap() {
        return value();
    }

    @Override
    public int compareTo(Obj other) {
        return other instanceof Num
                ? Double.compare(doubleValue(), ((Num) other).doubleValue())
                : -1;
    }

    public boolean isReal() {
        if (value instanceof Double)
            return !((Double) value).isNaN() && !((Double) value).isInfinite();
        return true;
    }

    @Override
    public Bool iterable() {
        return Bool.FALSE;
    }
}

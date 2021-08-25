package com.vectron.fcl.types;

import com.vectron.fcl.exceptions.NotUnderstood;
import com.vectron.fcl.exceptions.TypeMismatched;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;

import static com.vectron.fcl.Fcl.STRICT;

public class Num implements Obj, LogicOperand, ArithmeticOperand {
    public static final Num ZERO = new Num(0);
    public static final Num ONE = new Num(1);
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

    public String display() {
        return value instanceof Long || value instanceof Double
                ? format.format(value)
                : value.toString();
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
        else if (other instanceof Lst)
            return ((Lst) other).add(this);
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
        else if (other instanceof Lst)
            return ((Lst) other).mul(this);
        else if (other instanceof Str)
            return ((Str) other).mul(this);
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
        else if (STRICT)
            throw new TypeMismatched("/", this, other);
        return Num.NAN;
    }

    public Num power(Num exponent) {
        if (value instanceof Long && exponent.value instanceof Long)
            return new Num(Math.pow(((Long) value).doubleValue(), ((Long) exponent.value).doubleValue()));
        else if (value instanceof Long && exponent.value instanceof Double)
            return new Num(Math.pow(((Long) value).doubleValue(), exponent.doubleValue()));
        else if (value instanceof Double && exponent.value instanceof Long)
            return new Num(Math.pow((Double)value, ((Long) exponent.value).doubleValue()));
        else if (value instanceof Double && exponent.value instanceof Double)
            return new Num(Math.pow((Double)value, exponent.doubleValue()));
        else if (STRICT)
            throw new TypeMismatched("POW", this, exponent);
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
            return value.longValue() != 0l;
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
}

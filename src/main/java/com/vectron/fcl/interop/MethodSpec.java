package com.vectron.fcl.interop;

import com.vectron.fcl.FclStack;
import com.vectron.fcl.exceptions.InterOpFailed;
import com.vectron.fcl.types.Bool;
import com.vectron.fcl.types.JvmObj;
import com.vectron.fcl.types.Nil;
import com.vectron.fcl.types.Num;
import com.vectron.fcl.types.Obj;
import com.vectron.fcl.types.Str;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class MethodSpec {
    private final Class<?> clazz;
    private final Object receiver;
    private final String methodName;
    private final int arity;
    private final String typeSpec;

    public static MethodSpec parseStatic(String spec) {
        String[] parts = spec.split("/");
        try {
            return new MethodSpec(Class.forName(parts[0]), null, parts[1], typeSpec(parts, 2));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static MethodSpec parseDynamic(String spec, Object receiver) {
        String[] parts = spec.split("/");
        return new MethodSpec(receiver.getClass(), receiver, parts[0], typeSpec(parts, 1));
    }

    private static String typeSpec(String[] parts, int index) {
        String types = "";
        if (parts.length > index) {
            types = parts[index];
        }
        return types;
    }

    private MethodSpec(Class<?> clazz, Object receiver, String methodName, String typeSpec)  {
        this.clazz = clazz;
        this.receiver = receiver;
        this.methodName = methodName;
        this.arity = typeSpec.length();
        this.typeSpec = typeSpec;
    }

    public void invoke(FclStack stack) {
        List<Object> params = new ArrayList<>();
        List<Class<?>> types = new ArrayList<>();
        for (int i = 0; i < arity; i++) {
            Obj value = stack.pop();
            Class<?> clazz = typeOf(typeSpec.charAt(i));
            addParam(params, value, clazz);
            types.add(clazz);
        }
        try {
            Method method = clazz.getMethod(methodName, types.toArray(new Class[0]));
            method.setAccessible(true);
            Object result = method.invoke(receiver, params.toArray(new Object[0]));
            if (!method.getReturnType().getSimpleName().equals("void"))
                processResult(result, stack);
        } catch (ReflectiveOperationException e) {
            throw new InterOpFailed(e);
        }
    }

    public boolean exists() {
        List<Class<?>> types = new ArrayList<>();
        for (int i = 0; i < arity; i++) {
            Class<?> clazz = typeOf(typeSpec.charAt(i));
            types.add(clazz);
        }
        try {
            clazz.getMethod(methodName, types.toArray(new Class[0]));
            return true;
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    public static void processResult(Object result, FclStack stack) {
        if (result != null) {
            if (result instanceof Number)
                stack.push(new Num((Number) result));
            else if (result instanceof String)
                stack.push(new Str((String) result));
            else if (result instanceof Boolean)
                stack.push((boolean)result ? Bool.TRUE : Bool.FALSE);
            else if (result instanceof Obj)
                stack.push((Obj)result);
            else
                stack.push(new JvmObj(result));
        } else {
            stack.push(Nil.INSTANCE);
        }
    }

    private void addParam(List<Object> params, Obj value, Class<?> clazz) {
        if (clazz == Integer.TYPE)
            params.add(value.intValue());
        else if (clazz == Long.TYPE)
            params.add(value.longValue());
        else if (clazz == Double.TYPE)
            params.add(value.doubleValue());
        else if (clazz == String.class)
            params.add((String)value.value());
        else if (clazz == Map.class)
            params.add((Map)value.value());
        else if (clazz == List.class)
            params.add((List)value.value());
        else if (clazz == Obj.class)
            params.add(value);
        else
            throw new InterOpFailed("Unsupported inter-op type: " + clazz);
    }

    private Class<?> typeOf(Character type) {
        switch (type) {
            case 'i': return Integer.TYPE;
            case 'd': return Double.TYPE;
            case 'l': return Long.TYPE;
            case 's': return String.class;
            case 'm': return Map.class;
            case 't': return List.class;
            case 'O': return Obj.class;
            default:
                throw new InterOpFailed("Invalid type spec: " + type);
        }
    }
}

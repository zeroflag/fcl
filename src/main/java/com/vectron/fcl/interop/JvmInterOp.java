package com.vectron.fcl.interop;

import com.vectron.fcl.FclStack;
import com.vectron.fcl.exceptions.InterOpFailed;
import com.vectron.fcl.types.Bool;
import com.vectron.fcl.types.JvmObj;
import com.vectron.fcl.types.Obj;

import java.util.Random;

import static com.vectron.fcl.interop.MethodSpec.processResult;

public class JvmInterOp {
    private static final Random RND = new Random();
    private final FclStack stack;

    public JvmInterOp(FclStack stack) {
        this.stack = stack;
    }

    public void jvmCallStatic() {
        MethodSpec spec = MethodSpec.parseStatic(stack.pop().asStr().value());
        spec.invoke(stack);
    }

    public void jvmCallMethod() {
        String methodName = stack.pop().asStr().value();
        Obj receiver = stack.pop();
        MethodSpec spec = MethodSpec.parseDynamic(
                methodName,
                receiver instanceof JvmObj ? receiver.value() : receiver);
        spec.invoke(stack);
    }

    public void jvmHasMethod() {
        String methodName = stack.pop().asStr().value();
        Obj receiver = stack.pop();
        MethodSpec spec = MethodSpec.parseDynamic(
                methodName,
                receiver instanceof JvmObj ? receiver.value() : receiver);
        stack.push(spec.exists() ? Bool.TRUE : Bool.FALSE);
    }

    public void jvmStaticVar() {
        String spec = stack.pop().asStr().value();
        String[] parts = spec.split("/");
        String className = parts[0];
        String varName = parts[1];
        try {
            Class<?> clazz = Class.forName(className);
            processResult(clazz.getDeclaredField(varName).get(null), stack);
        } catch (ReflectiveOperationException e) {
            throw new InterOpFailed(e);
        }
    }

    public static double random() {
        return RND.nextDouble();
    }
}

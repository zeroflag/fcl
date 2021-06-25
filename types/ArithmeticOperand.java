package com.vectron.fcl.types;

public interface ArithmeticOperand {
    Obj add(Obj other);
    Obj sub(Obj other);
    Obj mul(Obj other);
    Obj div(Obj other);
}

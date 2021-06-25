package com.vectron.fcl.types;

public interface LogicOperand {
    Obj and(Obj other);
    Obj or(Obj other);
    Obj not();
}

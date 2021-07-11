package com.vectron.fcl.types;

import com.vectron.fcl.Fcl;

public interface Word extends Obj {
    void enter();
    String name();
    void visible(boolean isVisible);
    boolean immediate();
    void immediate(boolean isImmediate);
    void predicate(Word word);
    boolean match(String name, Fcl fcl);
}

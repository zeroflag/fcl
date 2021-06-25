package com.vectron.fcl.types;

public interface Word extends Obj {
    void enter();
    String name();
    void visible(boolean isVisible);
    boolean visible();
}

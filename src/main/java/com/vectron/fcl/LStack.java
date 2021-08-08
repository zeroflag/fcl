package com.vectron.fcl;

import com.vectron.fcl.types.Obj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EmptyStackException;

public final class LStack extends ArrayList<Obj> {
    public LStack() {
        super(10);
    }

    public LStack(final Collection<Obj> collection) {
        super(collection);
    }

    public void push(Obj item) {
        add(item);
    }

    public Obj pop() {
        Obj top = peek();
        remove(size() - 1);
        return top;
    }

    public Obj peek() {
        int size = size();
        if (size == 0) throw new EmptyStackException();
        return get(size - 1);
    }
}

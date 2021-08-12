package com.vectron.fcl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EmptyStackException;

public final class LStack<T> extends ArrayList<T> {
    public LStack() {
        super(10);
    }

    public LStack(final Collection<T> collection) {
        super(collection);
    }

    public void push(T item) {
        add(item);
    }

    public T pop() {
        T top = peek();
        remove(size() - 1);
        return top;
    }

    public T peek() {
        int size = size();
        if (size == 0) throw new EmptyStackException();
        return get(size - 1);
    }
}

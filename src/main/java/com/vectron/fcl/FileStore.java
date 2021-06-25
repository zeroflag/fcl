package com.vectron.fcl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

public interface FileStore {
    void save(byte[] raw, String fileName);

    List<String> list();

    List<String> list(Predicate<String> p);

    String read(String fileName);

    FileInputStream open(String fileName) throws FileNotFoundException;

    boolean delete(String fileName);

    interface Predicate<T> {
        boolean isTrue(T v);
    }
}

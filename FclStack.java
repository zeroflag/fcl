package com.vectron.fcl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vectron.forthcalc.domain.ports.FileStore;
import com.vectron.fcl.types.Obj;
import com.vectron.forthcalc.support.FclTypeAdapter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class FclStack {
    private static final Gson gson;
    private final Stack<Obj> stack = new Stack<>();

    static {
        FclTypeAdapter typeAdapter = new FclTypeAdapter();
        gson = new GsonBuilder()
                .registerTypeAdapter(Obj.class, typeAdapter)
                .setLenient()
                .serializeSpecialFloatingPointValues()
                .create();
        typeAdapter.setGSon(gson);
    }

    public void push(Obj obj) {
        stack.push(obj);
    }

    public Obj pop() {
        return stack.pop();
    }

    public Obj peek() {
        return stack.peek();
    }

    public boolean empty() {
        return stack.empty();
    }

    public void clean() {
        stack.clear();
    }

    public int size() {
        return stack.size();
    }

    public Obj at(int index) {
        return stack.get(stack.size() - index -1);
    }

    public void switchStack(FclStack other) {
        List<Obj> copy = new ArrayList<>(other.stack);
        other.stack.clear();
        other.stack.addAll(this.stack);
        this.stack.clear();
        this.stack.addAll(copy);
    }

    public void sortDsc() {
        Collections.sort(stack, (o1, o2) -> o1.compareTo(o2));
    }

    public void sortAsc() {
        Collections.sort(stack, (o1, o2) -> o2.compareTo(o1));
    }

    public void reverse() {
        Collections.reverse(stack);
    }

    public void load(FileStore fileStore, String id) {
        FileInputStream stream = null;
        try {
            stream = fileStore.open(fileName(id));
            Obj[] loaded = gson.fromJson(new BufferedReader(new InputStreamReader(stream)), Obj[].class);
            stack.clear();
            for (Obj each : loaded)
                stack.add(each);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void save(FileStore fileStore, String id) {
        fileStore.save(gson.toJson(stack.toArray(new Obj[0])).getBytes(), fileName(id));
    }

    private String fileName(String id) {
        return String.format("stack%s.json", id);
    }
}

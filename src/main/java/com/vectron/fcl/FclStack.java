package com.vectron.fcl;

import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import com.vectron.fcl.types.Obj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

public class FclStack {
    private final LStack<Obj> stack = new LStack<>();

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
        return stack.size() == 0;
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
        Obj[] loaded = JsonSerializer.load(fileStore, fileName(id));
        stack.clear();
        stack.addAll(asList(loaded));
    }

    public void load(JsonReader jsonReader) {
        stack.clear();
        stack.addAll(asList(JsonSerializer.fromJson(jsonReader)));
    }

    public void save(FileStore fileStore, String id) {
        fileStore.save(JsonSerializer.toJson(stack).getBytes(), fileName(id));
    }

    public JsonElement toJsonTree() {
        return JsonSerializer.toJsonTree(stack);
    }

    private String fileName(String id) {
        return String.format("stack%s.json", id);
    }

    public void populate(FclStack other) {
        stack.clear();
        stack.addAll(other.stack);
    }

    public FclStack copy() {
        FclStack copy = new FclStack();
        copy.populate(this);
        return copy;
    }
}

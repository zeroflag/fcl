package com.vectron.fcl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.vectron.fcl.types.Obj;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Map;

public class JsonSerializer {
    private static final Gson gson;

    static {
        FclTypeAdapter typeAdapter = new FclTypeAdapter();
        gson = new GsonBuilder()
                .registerTypeAdapter(Obj.class, typeAdapter)
                .setLenient()
                .serializeSpecialFloatingPointValues()
                .create();
        typeAdapter.setGSon(gson);
    }

    public static Obj[] load(FileStore fileStore, String fileName) {
        FileInputStream stream = null;
        try {
            stream = fileStore.open(fileName);
            return gson.fromJson(new BufferedReader(new InputStreamReader(stream)), Obj[].class);
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

    public static JsonElement toJsonTree(LStack<Obj> stack) {
        return gson.toJsonTree(stack.toArray(new Obj[0]));
    }

    public static JsonElement toJsonTree(Obj obj) {
        return gson.toJsonTree(obj, Obj.class);
    }

    public static String toJson(LStack<Obj> stack) {
        return gson.toJson(stack.toArray(new Obj[0]));
    }

    public static String toJson(JsonObject json) {
        return gson.toJson(json);
    }

    public static Map<String, Obj> fromJson(String str, Type type) {
        return gson.fromJson(str, type);
    }

    public static Obj[] fromJson(JsonReader jsonReader) {
        return gson.fromJson(jsonReader, Obj[].class);
    }
}

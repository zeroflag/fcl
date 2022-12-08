package com.vectron.fcl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.vectron.fcl.types.Lst;
import com.vectron.fcl.types.Num;
import com.vectron.fcl.types.Obj;
import com.vectron.fcl.types.Range;

import org.junit.Test;

import static org.junit.Assert.*;

public class JsonSerializerTest {
    static FclTypeAdapter typeAdapter = new FclTypeAdapter();
    static Gson gson = new GsonBuilder()
            .registerTypeAdapter(Obj.class, typeAdapter)
            .setLenient()
            .serializeSpecialFloatingPointValues()
            .create();

    static {
        typeAdapter.setGSon(gson);
    }

    @Test
    public void testSerializeList() {
        Lst lst = Lst.empty();
        lst.append(new Num(1));
        lst.append(new Num(2));
        assertEquals(
                "{\"value\":[{\"value\":1,\"__type\":\"num\"},{\"value\":2,\"__type\":\"num\"}],\"__type\":\"lst\"}",
                gson.toJsonTree(lst, Obj.class).toString());
    }

    @Test
    public void testSerializeRange() {
        Range rng = Range.create(new Num(1), new Num(3), new Num(1));
        assertEquals(
                "{\"from\":{\"value\":1\"__type\":\"num\"},\"to\":{\"value\":3\"__type\":\"num\"},\"by\":{\"value\":1\"__type\":\"num\"},\"__type\":\"rng\"}",
                gson.toJsonTree(rng, Obj.class).toString());
    }
}
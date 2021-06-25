package com.vectron.fcl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.vectron.fcl.Fcl;
import com.vectron.fcl.types.Bool;
import com.vectron.fcl.types.Dic;
import com.vectron.fcl.types.JvmObj;
import com.vectron.fcl.types.Lst;
import com.vectron.fcl.types.Nil;
import com.vectron.fcl.types.Num;
import com.vectron.fcl.types.Obj;
import com.vectron.fcl.types.Quot;
import com.vectron.fcl.types.Range;
import com.vectron.fcl.types.Str;

import java.util.HashMap;
import java.util.Map;

public class FclTypeAdapter extends TypeAdapter<Object> {
    private static final String TYPE_FIELD = "__type";
    private Gson gson;
    private static final Map<String, Class<? extends Obj>> typeNameToClazz = new HashMap<>();
    private static final Map<Class<? extends Obj>, String> clazzToTypeName = new HashMap<>();

    static {
        register("num", Num.class);
        register("bool", Bool.class);
        register("str", Str.class);
        register("dic", Dic.class);
        register("lst", Lst.class);
        register("nil", Nil.class);
        register("quot", Quot.class);
        register("rng", Range.class);
        register("jvmobj", JvmObj.class);
        register("var", Fcl.Var.class);
        register("val", Fcl.Val.class);
    }

    private static void register(String typeName, Class<? extends Obj> clazz) {
        typeNameToClazz.put(typeName, clazz);
        clazzToTypeName.put(clazz, typeName);
    }

    @Override
    public void write(JsonWriter out, Object value) {
        JsonElement obj = gson.toJsonTree(value);
        if (obj instanceof JsonObject)
            ((JsonObject)obj).addProperty(TYPE_FIELD, clazzToTypeName.get(value.getClass()));
        gson.toJson(obj, out);
    }

    @Override
    public Object read(JsonReader in) {
        JsonElement obj = gson.fromJson(in, JsonElement.class);
        if (obj instanceof JsonObject) {
            JsonElement objType = ((JsonObject) obj).get(TYPE_FIELD);
            Class<? extends Obj> clazz = typeNameToClazz.get(objType.getAsString());
            if (clazz == Num.class) {
                return Num.parse(((JsonObject)obj).get("value").getAsString());
            }
            return gson.fromJson(obj, clazz);
        } else {
            return gson.fromJson(obj, Object.class);
        }
    }

    public void setGSon(Gson gson) {
        this.gson = gson;
    }
}

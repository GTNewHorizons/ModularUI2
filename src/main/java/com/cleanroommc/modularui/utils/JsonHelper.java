package com.cleanroommc.modularui.utils;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.DrawableSerialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utility methods for reading, writing, and (de)serializing JSON with a shared {@link Gson} instance.
 */
public class JsonHelper {

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(IDrawable.class, new DrawableSerialization())
            .registerTypeAdapter(Alignment.class, new Alignment.Json())
            .create();

    public static final JsonDeserializationContext DESERIALIZER = GSON::fromJson;
    public static final JsonSerializationContext SERIALIZER = new JsonSerializationContext() {
        @Override
        public JsonElement serialize(Object o) {
            return GSON.toJsonTree(o);
        }

        @Override
        public JsonElement serialize(Object o, Type type) {
            return GSON.toJsonTree(o, type);
        }
    };

    public static final JsonParser parser = new JsonParser();

    public static JsonElement serialize(Object object) {
        return GSON.toJsonTree(object);
    }

    public static <T> T deserialize(JsonElement json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    /**
     * Deserializes the first present key's value into the given class.
     *
     * @param json         object to read from
     * @param clazz        target type
     * @param defaultValue value returned if no key is present or the result is null
     * @param keys         candidate keys, checked in order
     * @return the deserialized value, or {@code defaultValue}
     */
    public static <T> T deserialize(JsonObject json, Class<T> clazz, T defaultValue, String... keys) {
        JsonElement element = getJsonElement(json, keys);
        if (element != null) {
            T t = deserialize(element, clazz);
            return t == null ? defaultValue : t;
        }
        return defaultValue;
    }

    /**
     * Deserializes from {@code json}, and if that yields null, deserializes from {@code fallback} instead.
     *
     * @param json         primary object to read from
     * @param fallback     object used when the primary value is null
     * @param clazz        target type
     * @param defaultValue value returned if neither object provides a value
     * @param keys         candidate keys, checked in order
     * @return the deserialized value, or {@code defaultValue}
     */
    public static <T> T deserializeWithFallback(JsonObject json, JsonObject fallback, Class<T> clazz, T defaultValue, String... keys) {
        T t = deserialize(json, clazz, null, keys);
        return t != null ? t : deserialize(fallback, clazz, defaultValue, keys);
    }

    /**
     * Reads a float from the first present key. A non-primitive value under that key yields {@code defaultValue}.
     *
     * @param json         object to read from, may be null
     * @param defaultValue value returned if the object is null or no usable key is found
     * @param keys         candidate keys, checked in order
     * @return the float value, or {@code defaultValue}
     */
    public static float getFloat(JsonObject json, float defaultValue, String @NotNull ... keys) {
        if (json == null) return defaultValue;
        for (String key : keys) {
            if (json.has(key)) {
                JsonElement jsonElement = json.get(key);
                if (jsonElement.isJsonPrimitive()) {
                    return jsonElement.getAsFloat();
                }
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Reads an int from the first present key. A non-primitive value under that key yields {@code defaultValue}.
     *
     * @param json         object to read from, may be null
     * @param defaultValue value returned if the object is null or no usable key is found
     * @param keys         candidate keys, checked in order
     * @return the int value, or {@code defaultValue}
     */
    public static int getInt(JsonObject json, int defaultValue, String @NotNull ... keys) {
        if (json == null) return defaultValue;
        for (String key : keys) {
            if (json.has(key)) {
                JsonElement jsonElement = json.get(key);
                if (jsonElement.isJsonPrimitive()) {
                    return jsonElement.getAsInt();
                }
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Reads an int from {@code json}, falling back to {@code fallback} if the primary object has no value.
     *
     * @param json         primary object to read from
     * @param fallback     object used when the primary value is absent
     * @param defaultValue value returned if neither object provides a value
     * @param keys         candidate keys, checked in order
     * @return the int value, or {@code defaultValue}
     */
    public static int getIntWithFallback(JsonObject json, JsonObject fallback, int defaultValue, String @NotNull ... keys) {
        Integer i = getBoxedInt(json, null, keys);
        return i != null ? i : getInt(fallback, defaultValue, keys);
    }

    /**
     * Reads a boolean from the first present key. A non-primitive value under that key yields {@code defaultValue}.
     *
     * @param json         object to read from, may be null
     * @param defaultValue value returned if the object is null or no usable key is found
     * @param keys         candidate keys, checked in order
     * @return the boolean value, or {@code defaultValue}
     */
    public static boolean getBoolean(JsonObject json, boolean defaultValue, String @NotNull ... keys) {
        if (json == null) return defaultValue;
        for (String key : keys) {
            if (json.has(key)) {
                JsonElement jsonElement = json.get(key);
                if (jsonElement.isJsonPrimitive()) {
                    return jsonElement.getAsBoolean();
                }
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public static boolean getBoolWithFallback(JsonObject json, JsonObject fallback, boolean defaultValue, String @NotNull ... keys) {
        Boolean i = getBoxedBool(json, null, keys);
        return i != null ? i : getBoolean(fallback, defaultValue, keys);
    }

    public static String getString(JsonObject json, String defaultValue, String @NotNull ... keys) {
        if (json == null) return defaultValue;
        for (String key : keys) {
            if (json.has(key)) {
                JsonElement jsonElement = json.get(key);
                return jsonElement.getAsString();
            }
        }
        return defaultValue;
    }

    public static <T> T getObject(JsonObject json, T defaultValue, Function<JsonObject, T> factory, String @NotNull ... keys) {
        if (json == null) return defaultValue;
        for (String key : keys) {
            if (json.has(key)) {
                JsonElement jsonElement = json.get(key);
                if (jsonElement.isJsonObject()) {
                    return factory.apply(jsonElement.getAsJsonObject());
                }
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public static <T> T getElement(JsonObject json, T defaultValue, Function<JsonElement, T> factory, String @NotNull ... keys) {
        if (json == null) return defaultValue;
        for (String key : keys) {
            if (json.has(key)) {
                JsonElement jsonElement = json.get(key);
                return factory.apply(jsonElement);
            }
        }
        return defaultValue;
    }

    public static @Nullable Integer getBoxedInt(JsonObject json, Integer defaultValue, String @NotNull ... keys) {
        if (json == null) return defaultValue;
        for (String key : keys) {
            if (json.has(key)) {
                JsonElement jsonElement = json.get(key);
                if (jsonElement.isJsonPrimitive()) {
                    return jsonElement.getAsInt();
                }
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public static @Nullable Boolean getBoxedBool(JsonObject json, Boolean defaultValue, String @NotNull ... keys) {
        if (json == null) return defaultValue;
        for (String key : keys) {
            if (json.has(key)) {
                JsonElement jsonElement = json.get(key);
                if (jsonElement.isJsonPrimitive()) {
                    return jsonElement.getAsBoolean();
                }
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public static int getColor(JsonObject json, int defaultValue, String... keys) {
        JsonElement element = getJsonElement(json, keys);
        if (element != null) {
            return Color.ofJson(element);
        }
        return defaultValue;
    }

    public static int getColorWithFallback(JsonObject json, JsonObject fallback, int defaultValue, String @NotNull ... keys) {
        JsonElement element = getJsonElement(json, keys);
        if (element != null) {
            return Color.ofJson(element);
        }
        return getColor(fallback, defaultValue, keys);
    }

    public static @Nullable JsonElement getJsonElement(JsonObject json, String @NotNull ... keys) {
        if (json == null) return null;
        for (String key : keys) {
            if (json.has(key)) {
                return json.get(key);
            }
        }
        return null;
    }

    public static JsonElement parse(InputStream inputStream) {
        return parser.parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    public static JsonObject merge(JsonObject base, JsonObject other) {
        for (Map.Entry<String, JsonElement> entry : other.entrySet()) {
            base.add(entry.getKey(), entry.getValue());
        }
        return base;
    }

    public static JsonObject makeJson(Consumer<JsonObject> writer) {
        JsonObject json = new JsonObject();
        writer.accept(json);
        return json;
    }
}

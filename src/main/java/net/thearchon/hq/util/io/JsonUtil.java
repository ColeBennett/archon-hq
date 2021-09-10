package net.thearchon.hq.util.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Map;

public final class JsonUtil {

    public static final Type MAP_STR_TYPE = new TypeToken<Map<String, Object>>(){}.getType();

    public static final GsonBuilder BUILDER;
    public static final Gson GSON_COMPACT;
    public static final Gson GSON_READABLE;
    
    static {
        BUILDER = new GsonBuilder();
        BUILDER.disableHtmlEscaping();
        GSON_COMPACT = BUILDER.create();
        GSON_READABLE = BUILDER.setPrettyPrinting().create();
    }

    public static String toJson(Object obj) {
        return GSON_READABLE.toJson(obj);
    }

    public static String toJsonCompact(Object obj) {
        return GSON_COMPACT.toJson(obj);
    }

    public static <T> T load(String contents, Class<T> clazz) {
        try {
            return GSON_COMPACT.fromJson(contents, clazz);
        } catch (JsonSyntaxException | JsonIOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static <T> T load(String contents, Type type) {
        try {
            return GSON_COMPACT.fromJson(contents, type);
        } catch (JsonSyntaxException | JsonIOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T load(File file, Class<T> clazz) {
        try {
            return GSON_COMPACT.fromJson(new FileReader(file), clazz);
        } catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static <T> T load(File file, Type type) {
        try {
            return GSON_COMPACT.fromJson(new FileReader(file), type);
        } catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static void save(File file, Object obj) {
        save(GSON_READABLE, file, obj);
    }

    public static void saveCompact(File file, Object obj) {
        save(GSON_COMPACT, file, obj);
    }

    public static boolean isJson(String contents) {
        try {
            GSON_COMPACT.fromJson(contents, Object.class);
            return true;
        } catch (JsonSyntaxException e) {
            return false;
        }
    }

    public static boolean isJson(File file) {
        try {
            GSON_COMPACT.fromJson(new FileReader(file), Object.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private static void save(Gson gson, File file, Object obj) {
        if (!file.exists()) {
            if (file.getParentFile() != null) {
                file.getParentFile().mkdir();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(gson.toJson(obj));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JsonUtil() {}
}

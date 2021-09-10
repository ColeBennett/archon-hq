package net.thearchon.hq.util.io;

import com.google.gson.*;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.Map.Entry;

public class JsonConfig {

//    public static void main(String[] args) {
//        JsonConfig c = new JsonConfig(new File("/Users/Cole/Desktop/settings.json"));
//        System.out.println(c.index);
//        for (Entry<String, Object> e : c.index.entrySet()) {
//            System.out.println(e.getKey() + " " + e.getValue());
//        }
//        System.out.println(c.getMap("chat.blockedWords"));
//        System.out.println(c.getMapFromIndex("chat.blockedWords"));
//    }

    private static final String SEPARATOR = ".";
    private static final Type MAP = new TypeToken<Map<String, Object>>(){}.getType();

    /**
     * Flattened hierarchy of objects.
     * Example:
     *  {key: {inner1: val1, inner2: {key2: val2}}}
     * is indexed as
     *  key.inner=1
     *  key.inner2.key2=val2
     */
    private Map<String, Object> index;

    private File file;
    private boolean saveOnWrite;
    private Map<String, Object> root;

    public JsonConfig(String json) {
        this(load(json));
    }

    public JsonConfig(File file) {
        this(load(file));
        this.file = file;
    }

    public JsonConfig() {
        this(new LinkedTreeMap<>());
    }

    public JsonConfig(Map<String, Object> root) {
        this.root = root;
        index(root);
    }

    private int index(Map<String, Object> root) {
        index = new LinkedTreeMap<>();
        if (root.isEmpty()) return -1;
        doIndex(null, root);
        return index.size();
    }

    /**
     * Recursively search through the given map and index its objects.
     * @param pathEntry current node of path
     * @param map map to search through
     */
    private void doIndex(String pathEntry, Map<String, Object> map) {
        for (Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            String path = pathEntry != null ? pathEntry + SEPARATOR + key : key;
            Object val = entry.getValue();
            if (val instanceof Map) {
                doIndex(path, map(val));
            } else {
                index.put(path, val);
            }
        }
    }

    public File getFile() {
        return file;
    }

    public Map<String, Object> getRoot() {
        return root;
    }

    public void setSaveOnWrite(boolean saveOnWrite) {
        this.saveOnWrite = saveOnWrite;
    }

    public boolean isSaveOnWrite() {
        return saveOnWrite;
    }

    public Object get(String path) {
        return index.get(path);
    }

    public Object getOrDefault(String path, Object value) {
        Object val = get(path);
        if (val == null) {
            set(path, value);
        }
        return value;
    }

    public Object set(String path, Object value) {
        isValid(path);
        Object prev = index.put(path, checkValue(value));
        if (saveOnWrite) {
            save();
        }
        return prev;
    }

    public Object setIfAbsent(String path, Object value) {
        isValid(path);
        Object prev = index.putIfAbsent(path, checkValue(value));
        if (saveOnWrite) {
            save();
        }
        return prev;
    }

    // TODO also remove all nested values
    public Object remove(String path) {
        Object value = index.remove(path);
        if (value != null && saveOnWrite) {
            save();
        }
        return value;
    }

    public void clear() {
        index.clear();
    }

    public int size() {
        return index.size();
    }

    private void isValid(String path) {
        if (path.startsWith(SEPARATOR)) {
            throw new IllegalArgumentException("Path cannot start with separator: " + path);
        }
        if (path.endsWith(SEPARATOR)) {
            throw new IllegalArgumentException("Path cannot end with separator: " + path);
        }
    }

    private Object checkValue(Object value) {
        if (value instanceof Character) {
            value = value.toString();
        }
        return value;
    }

    /**
     * Convert the flattened index version of the structure to a JSON object hierarchy.
     * @return converted structure
     */
    private Map<String, Object> indexToRoot() {
        Map<String, Object> root = new LinkedTreeMap<>();
        for (Entry<String, Object> entry : index.entrySet()) {
            String path = entry.getKey();
            Object val = entry.getValue();
            if (path.contains(SEPARATOR)) {
                String[] pathEntries = getPathEntries(path);
                Map<String, Object> parent = root;
                for (int i = 0, len = pathEntries.length; i < len; i++) {
                    String pathEntry = pathEntries[i];
                    if (i == len - 1) {
                        parent.put(pathEntry, val);
                        break;
                    }
                    Map<String, Object> map = map(parent.get(pathEntry));
                    if (map == null) {
                        map = new LinkedTreeMap<>();
                        parent.put(pathEntry, map);
                    }
                    parent = map;
                }
            } else {
                root.put(path, val);
            }
        }
        return root;
    }

    private String[] getPathEntries(String path) {
        return path.split("\\" + SEPARATOR);
    }

    private String deepest(String path) {
        return deepest(getPathEntries(path));
    }

    private String deepest(String[] path) {
        return path[path.length - 1];
    }

    /**
     * Finds all top-level keys.
     * @return set of keys
     */
    public Set<String> getKeys() {
        Set<String> keys = new LinkedHashSet<>();
        for (String key : index.keySet()) {
            if (!key.contains(SEPARATOR)) {
                keys.add(key);
            }
        }
        return keys;
    }

    /**
     * Finds all top-level keys in the given path.
     * @return set of keys
     */
    public Set<String> getKeys(String path) {
        return getKeys(path, false);
    }

    /**
     * Finds all keys in the given path including nested keys.
     * @return set of keys
     */
    public Set<String> getKeys(String path, boolean deep) {
        Set<String> keys = new LinkedHashSet<>();
        String[] rpath = getPathEntries(path);
        for (String key : index.keySet()) {
            if (key.startsWith(path + SEPARATOR)) {
                String[] kpath = getPathEntries(key);
                if (deep) {
                    keys.add(deepest(kpath));
                } else {
                    if (kpath.length - 1 == rpath.length) {
                        keys.add(kpath[rpath.length]);
                    }
                }
            }
        }
        return keys;
    }

    public List<Object> getValues(String path) {
        return getValues(path, false);
    }

    public List<Object> getValues(String path, boolean deep) {
        List<Object> values = new ArrayList<>();
        String[] rpath = getPathEntries(path);
        for (Entry<String, Object> entry : index.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(path + SEPARATOR)) {
                String[] kpath = getPathEntries(key);
                if (deep) {
                    values.add(entry.getValue());
                } else {
                    if (kpath.length - 1 == rpath.length) {
                        values.add(entry.getValue());
                    }
                }
            }
        }
        return values;
    }

    public boolean getBoolean(String path) {
        return (boolean) get(path);
    }

    public byte getByte(String path) {
        Object val = get(path);
        if (!(val instanceof Byte)) {
            return ((Number) val).byteValue();
        }
        return (byte) val;
    }

    public short getShort(String path) {
        Object val = get(path);
        if (!(val instanceof Short)) {
            return ((Number) val).shortValue();
        }
        return (short) val;
    }

    public int getInt(String path) {
        Object val = get(path);
        if (!(val instanceof Integer)) {
            return ((Number) val).intValue();
        }
        return (int) val;
    }

    public long getLong(String path) {
        Object val = get(path);
        if (!(val instanceof Long)) {
            return ((Number) val).longValue();
        }
        return (long) val;
    }

    public double getDouble(String path) {
        Object val = get(path);
        if (!(val instanceof Double)) {
            return ((Number) val).doubleValue();
        }
        return (double) val;
    }

    public float getFloat(String path) {
        Object val = get(path);
        if (!(val instanceof Float)) {
            return ((Number) val).floatValue();
        }
        return (float) val;
    }

    public String getString(String path) {
        Object val = get(path);
        if (!(val instanceof String)) {
            return val.toString();
        }
        return (String) val;
    }

    public List<Double> getDoubleList(String path) {
        return getList(path);
    }

    public List<String> getStringList(String path) {
        return getList(path);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String path) {
        return (List<T>) get(path);
    }

    @SuppressWarnings("unchecked")
    public <V> Map<String, V> getMap(String path) {
        Map<String, V> map = new HashMap<>();
        for (Entry<String, Object> entry : index.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(path + SEPARATOR)) {
                map.put(deepest(key), (V) entry.getValue());
            }
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> getMapFromRoot(String path) {
        Map<String, Object> map = root;
        String[] pathEntries = getPathEntries(path);
        for (String pathEntry : pathEntries) {
            Object val = map.get(pathEntry);
            if (val == null) {
                map = null;
                break;
            }
            if (val instanceof Map) {
                map = (Map<String, Object>) val;
            }
        }
        return (Map<K, V>) map;
    }

    private Map<String, Object> searchRoot(String pathEntry, Map<String, Object> root) {
        Object val = root.get(pathEntry);
        if (val != null) {
            if (val instanceof Map) {
                return searchRoot(pathEntry, root);
            }
        }
        return null;
    }

    public String toJson() {
        return toJson(false);
    }

    public String toJson(boolean compact) {
        return (compact ? GSON_COMPACT : GSON_READABLE).toJson(indexToRoot());
    }

    public void save() {
        save(false);
    }

    public void save(boolean compact) {
        if (file != null) {
            save(file, compact);
        }
    }

    public void save(File file) {
        save(file, false);
    }

    public void save(File file, boolean compact) {
        root = indexToRoot();
        save(compact ? GSON_COMPACT : GSON_READABLE, file, root);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return value != null ? (Map<String, Object>) value : null;
    }
    
    private static final Gson GSON_COMPACT;
    private static final Gson GSON_READABLE;

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.disableHtmlEscaping();
        builder.serializeNulls();
        builder.registerTypeAdapter(Double.class, (JsonSerializer<Double>) (src, typeOfSrc, context) -> {
            if (src == src.longValue()) {
                return new JsonPrimitive(src.longValue());
            }
            return new JsonPrimitive(src);
        });
        GSON_COMPACT = builder.create();
        GSON_READABLE = builder.setPrettyPrinting().create();
    }

    private static Map<String, Object> load(String json) {
        try {
            return GSON_COMPACT.fromJson(json, MAP);
        } catch (JsonSyntaxException | JsonIOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Map<String, Object> load(File file) {
        try {
            return GSON_COMPACT.fromJson(new FileReader(file), MAP);
        } catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void save(Gson gson, File file, Object obj) {
        if (!file.exists()) {
            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdir();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(gson.toJson(obj));
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

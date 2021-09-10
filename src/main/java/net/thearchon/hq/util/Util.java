package net.thearchon.hq.util;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.Map.Entry;

public final class Util {

    public static final Type MAP_TYPE = new TypeToken<Map<String, String>>(){}.getType();

    private static final DecimalFormat DEC_FORMAT = new DecimalFormat("0.##");
    private static final Random RANDOM = new Random();
    private static final int SI_UNIT = 1000;

    public static DecimalFormat getNumberDecFormat() {
        return DEC_FORMAT;
    }

    public static Random random() {
        return RANDOM;
    }

    public static String upperLower(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
    }

    public static String pluralize(String leading, String suffix, int count) {
        return count != 1 ? leading + suffix : leading;
    }

    public static String addCommas(Object number) {
        return NumberFormat.getInstance(Locale.ENGLISH).format(number);
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? SI_UNIT : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static double toKb(long bytes) {
        return bytes / SI_UNIT;
    }

    public static double toMb(long bytes) {
        return bytes / (SI_UNIT ^ 2);
    }

    public static double toGb(long bytes) {
        return bytes / (SI_UNIT ^ 3);
    }

    public static double toTb(long bytes) {
        return bytes / (SI_UNIT ^ 4);
    }

    public static String humanReadableNumber(long count) {
        int unit = 1000;
        if (count < unit) return count + "";
        int exp = (int) (Math.log(count) / Math.log(unit));
        char pre = "KMBTQ".charAt(exp - 1);
        return DEC_FORMAT.format(count / Math.pow(unit, exp)) + pre;
    }

    public static double getPercentageChange(int v1, int v2) {
        return getPercentageChange((double) v1, (double) v2);
    }

    public static double getPercentageChange(double v1, double v2) {
        return ((v2 - v1) / v1) * 100D;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(
            Map<K, V> map, final boolean desc) {
        List<Entry<K, V>> entries = new LinkedList<>(map.entrySet());
        Collections.sort(entries, (o1, o2) -> {
            if (desc) {
                return o2.getValue().compareTo(o1.getValue());
            } else {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        Map<K, V> result = new LinkedHashMap<>(entries.size());
        for (Entry<K, V> entry : entries) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> List<T> sortedValueKeys(final Map<T, ?> map) {
        List<T> keys = new LinkedList<>();
        keys.addAll(map.keySet());
        Collections.sort(keys, (o1, o2) -> {
            Object v1 = map.get(o1);
            Object v2 = map.get(o2);
            if (v1 == null) {
                return (v2 == null) ? 0 : 1;
            } else if (v1 instanceof Comparable) {
                return ((Comparable) v1).compareTo(v2);
            } else {
                return 0;
            }
        });
        Collections.reverse(keys);
        return keys;
    }

    private Util() {}
}

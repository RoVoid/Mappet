package mchorse.mappet.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import mchorse.mappet.Mappet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompareVersions {
    private static List<String> versions;

    public static int compare(String v1) {
        return compare(v1, Mappet.VERSION);
    }

    public static int compare(String v1, String v2) {
        if (v1 == null || v2 == null || v1.equals(v2)) return 0;

        ensureVersionsLoaded();
        if (versions.isEmpty()) return 0;

        int i1 = versions.indexOf(v1);
        int i2 = versions.indexOf(v2);

        if (i1 == -1 && i2 == -1) return 0;
        if (i1 == -1) return -1;
        if (i2 == -1) return 1;

        return Integer.compare(i2, i1);
    }

    public static boolean hasConflicts(String v1) {
        return hasConflicts(v1, Mappet.VERSION);
    }

    public static boolean hasConflicts(String v1, String v2) {
        if (v1 == null || v2 == null || v1.equals(v2)) return false;

        ensureVersionsLoaded();
        if (versions.isEmpty()) return false;

        if (v1.contains("beta") || v2.contains("beta")) return true;
        return !series(v1).equals(series(v2));
    }

    public static String series(String version) {
        if (version == null) return null;
        String[] parts = version.split("\\.");
        return parts.length <= 3 ? version : String.join(".", Arrays.copyOf(parts, 3));
    }

    private static void ensureVersionsLoaded() {
        if (versions != null) return;

        versions = new ArrayList<>();
        try {
            JsonArray json = JsonFetcher.fetchJsonArray(JsonFetcher.SOURCE + "versions.json");
            if (json == null) return;
            for (JsonElement el : json) {
                if (el.isJsonPrimitive()) versions.add(el.getAsString());
            }
        } catch (IOException ignored) {
        }
    }

    public static List<String> getVersions() {
        return versions == null ? new ArrayList<>() : versions;
    }
}

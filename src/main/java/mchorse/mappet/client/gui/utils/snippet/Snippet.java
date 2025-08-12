package mchorse.mappet.client.gui.utils.snippet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mchorse.mappet.Mappet;
import mchorse.mappet.client.gui.scripts.utils.documentation.DocEntry;
import mchorse.mappet.utils.CompareVersions;

import java.util.*;

import static mchorse.mappet.utils.JsonFetcher.safeString;

public class Snippet {
    private final Map<String, String> titles = new HashMap<>();
    private final Set<String> tags = new HashSet<>();
    private final Set<String> versions = new HashSet<>();

    private final Map<String, String> contents = new HashMap<>();
    private SnippetAuthor author;

    public boolean hasActualVersion;

    public Snippet(JsonObject json) {
        if (json.has("title") && json.get("title").isJsonObject()) {
            JsonObject titleObj = json.getAsJsonObject("title");
            for (Map.Entry<String, JsonElement> e : titleObj.entrySet()) {
                titles.put(e.getKey(), safeString(e.getValue()));
            }
        }

        if (json.has("tags") && json.get("tags").isJsonArray()) {
            JsonArray tagArr = json.getAsJsonArray("tags");
            for (JsonElement el : tagArr) {
                String tag = safeString(el);
                if (!tag.isEmpty()) tags.add(tag);
            }
        }

        if (json.has("versions") && json.get("versions").isJsonArray()) {
            JsonArray tagArr = json.getAsJsonArray("versions");
            for (JsonElement el : tagArr) {
                String v = safeString(el);
                if (!v.isEmpty()) versions.add(v);
            }
        }
        hasActualVersion = versions.contains(CompareVersions.series(Mappet.VERSION));
    }

    public String getTitle(String locale) {
        return titles.getOrDefault(locale, titles.getOrDefault("", ""));
    }

    public Set<String> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public Set<String> getVersions() {
        return Collections.unmodifiableSet(versions);
    }

    public boolean hasContent() {
        return !contents.isEmpty();
    }

    public DocEntry getContent(String locale) {
        DocEntry entry = new DocEntry();
        entry.doc = contents.getOrDefault(locale, contents.getOrDefault("", ""));
        return entry;
    }

    public SnippetAuthor getAuthor() {
        return author;
    }

    public void setContent(JsonObject json) {
        JsonObject j = hasActualVersion ? json.getAsJsonObject(CompareVersions.series(Mappet.VERSION)) : json.getAsJsonObject(json
                                                                                                                                      .get("latest")
                                                                                                                                      .getAsString());
        for (Map.Entry<String, JsonElement> e : j.entrySet()) {
            contents.put(e.getKey(), safeString(e.getValue()));
        }
    }

    public void setAuthor(JsonObject json) {
        author = new SnippetAuthor(json);
    }
}

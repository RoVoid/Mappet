package mchorse.mappet.client.gui.utils.snippet;

import com.google.gson.JsonObject;

import static mchorse.mappet.utils.JsonFetcher.safeString;

public class SnippetAuthor {
    private final String name;
    private final String url;
    private final String rank;

    public SnippetAuthor(JsonObject json) {
        name = safeString(json.get("name"));
        url = safeString(json.get("url"));
        rank = safeString(json.get("rank"));
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getRank() {
        return rank;
    }
}

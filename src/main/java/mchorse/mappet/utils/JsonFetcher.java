package mchorse.mappet.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class JsonFetcher {
    private static final JsonParser parser = new JsonParser();

    public static final String SOURCE = "https://raw.githubusercontent.com/RoVoid/Mappet/refs/heads/beta/";
    public static final String SNIPPETS = "https://raw.githubusercontent.com/RoVoid/Mappet-Snippets/refs/heads/main/";

    public static JsonElement fetchJson(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) return null;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String json = reader.lines().collect(Collectors.joining());
            return parser.parse(json);
        }
    }

    public static JsonObject fetchJsonObject(String urlStr) throws IOException {
        JsonElement object = fetchJson(urlStr);
        return object != null && object.isJsonObject() ? object.getAsJsonObject() : null;
    }

    public static JsonArray fetchJsonArray(String urlStr) throws IOException {
        JsonElement object = fetchJson(urlStr);
        return object != null && object.isJsonArray() ? object.getAsJsonArray() : null;
    }

    public static String safeString(JsonElement el) {
        return el != null && !el.isJsonNull() ? el.getAsString().trim() : "";
    }
}

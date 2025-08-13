package mchorse.mappet.api.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mchorse.mappet.Mappet;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static mchorse.mappet.utils.JsonFetcher.fetchJsonObject;

public class SkinUtils {
    static final JsonParser parser = new JsonParser();

    public static String getSkin(String nickname) {
        return getSkin(nickname, "minecraft");
    }

    public static String getSkin(String nickname, String source) {
        Object obj = getSkinObject(nickname, source);
        if (!(obj instanceof Map<?, ?>)) return "";

        Object url = ((Map<?, ?>) obj).get("url");
        return url instanceof String ? (String) url : "";
    }

    public static Object getSkinObject(String nickname) {
        return getSkinObject(nickname, "minecraft");
    }

    public static Object getSkinObject(String nickname, String source) {
        if (nickname == null || nickname.trim().isEmpty()) return null;

        source = source.replaceAll("[^_0-9a-zA-Z]", "").toLowerCase();

        Map<String, Object> result = new HashMap<>();

        String skinUrl;
        Boolean slim;

        try {
            JsonObject skin = null;

            switch (source) {
                case "tl":
                case "tlauncher": {
                    String url = "https://auth.tlauncher.org/skin/profile/texture/login/" + nickname;
                    JsonObject json = fetchJsonObject(url);
                    if (json != null && json.has("SKIN")) {
                        skin = json.getAsJsonObject("SKIN");
                    }
                    break;
                }

                case "elyby": {
                    String url = "http://skinsystem.ely.by/textures/" + nickname;
                    JsonObject json = fetchJsonObject(url);
                    if (json != null && json.has("SKIN")) {
                        skin = json.getAsJsonObject("SKIN");
                    }
                    break;
                }

                default: {
                    String uuidUrl = "https://api.mojang.com/users/profiles/minecraft/" + nickname;
                    JsonObject profile = fetchJsonObject(uuidUrl);
                    if (profile == null || !profile.has("id")) break;

                    String uuid = profile.get("id").getAsString();
                    String sessionUrl = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid;
                    JsonObject session = fetchJsonObject(sessionUrl);
                    if (session == null || !session.has("properties")) break;

                    JsonArray properties = session.getAsJsonArray("properties");
                    for (JsonElement el : properties) {
                        JsonObject prop = el.getAsJsonObject();
                        if (!"textures".equals(prop.get("name").getAsString())) continue;

                        String base64 = prop.get("value").getAsString();
                        String decoded = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
                        JsonObject textureData = parser.parse(decoded).getAsJsonObject();

                        JsonObject textures = textureData.getAsJsonObject("textures");
                        if (textures.has("SKIN")) {
                            skin = textures.getAsJsonObject("SKIN");
                        }

                        break;
                    }
                    break;
                }
            }
            if (skin != null && skin.has("url")) {
                result.put("url", skin.get("url").getAsString());
                result.put("slim", skin.has("metadata"));
            }
        } catch (Exception e) {
            Mappet.logger.error("Failed to fetch skin for " + nickname + ": " + e.getMessage());
        }
        return result;
    }
}

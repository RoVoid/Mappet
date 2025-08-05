package mchorse.mappet.client.gui.utils.triggers;

import com.google.gson.*;
import mchorse.mappet.Mappet;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.InputStream;
import java.util.*;

public class TriggerDocs {
    private static final Map<String, TriggerDoc> docs = new HashMap<>();
    private static String language;

    private static final JsonParser parser = new JsonParser();

    public static void init() {
        docs.clear();
        Minecraft mc = Minecraft.getMinecraft();
        Gson gson = new GsonBuilder().create();

        InputStream stream = null;
        language = mc.getLanguageManager().getCurrentLanguage().getLanguageCode().toLowerCase();
        try {
            stream = mc.getResourceManager()
                       .getResource(new ResourceLocation(Mappet.MOD_ID, "triggers/" + language + ".json"))
                       .getInputStream();
        } catch (Exception e) {
            Mappet.loggerClient.error("Not found docs on your localization!");
            if (language.equalsIgnoreCase("en_us")) return;
            try {
                stream = mc.getResourceManager()
                           .getResource(new ResourceLocation(Mappet.MOD_ID, "triggers/en_us.json"))
                           .getInputStream();
            } catch (Exception e1) {
                Mappet.loggerClient.error("Not found docs");
            }
        }

        if (stream == null) return;

        try (Scanner scanner = new Scanner(stream, "UTF-8")) {
            String json = scanner.useDelimiter("\\A").next();

            JsonObject root = parser.parse(json).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                String key = entry.getKey();
                JsonObject triggerObj = entry.getValue().getAsJsonObject();

                String name = triggerObj.get("name").getAsString();
                String description = triggerObj.get("description").getAsString();
                boolean cancelable = triggerObj.has("cancelable") && triggerObj.get("cancelable").getAsBoolean();

                List<TriggerVariable> vars = new ArrayList<>();
                JsonObject varsObj = triggerObj.getAsJsonObject("variables");

                for (Map.Entry<String, JsonElement> varEntry : varsObj.entrySet()) {
                    String varKey = varEntry.getKey();
                    JsonArray array = varEntry.getValue().getAsJsonArray();

                    if (array.size() >= 2) {
                        String type = array.get(0).getAsString();
                        String var = array.get(1).getAsString();
                        vars.add(new TriggerVariable(type, varKey, var));
                    }
                }

                TriggerDoc doc = new TriggerDoc(key, name, description, cancelable, vars);
                docs.put(key, doc);
            }

        } catch (Exception e) {
            Mappet.loggerClient.error("Failed to load trigger docs: {}", e.getMessage());
        }
    }


    public static TriggerDoc get(String key) {
        if (docs.isEmpty() || !Minecraft.getMinecraft()
                                        .getLanguageManager()
                                        .getCurrentLanguage()
                                        .getLanguageCode()
                                        .equalsIgnoreCase(language)) init();
        return docs.get(key);
    }
}
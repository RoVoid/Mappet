package mchorse.mappet;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mchorse.mappet.client.gui.utils.AnimatedIcon;
import mchorse.mclib.client.gui.utils.Icon;
import mchorse.mclib.client.gui.utils.IconRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MappetIcons {
    private static final JsonParser PARSER = new JsonParser();

    public static Icon CONSOLE;
    public static Icon BRUSH;
    public static Icon PLANET;
    public static Icon KEYBOARD;
    public static Icon ANVIL;
    public static Icon GLASSES;
    public static Icon LETTER_A;
    public static Icon LETTER_CASE;
    public static Icon REGEX;
    public static Icon SYNC;

    public static final Icon NULL = new Icon(new ResourceLocation(Mappet.MOD_ID, "textures/gui/icons/_null.png"), 0, 0, 16, 16, 16, 16);

    public static List<String> getAllNames() {
        return new ArrayList<>(IconRegistry.icons.keySet());
    }

    public static Icon get(String key) {
        Icon icon = IconRegistry.icons.get(key);

        if (icon == null) {
            Mappet.logger.error("Not found icon: " + key);
            return NULL;
        }

        return icon;
    }

    public static void initiate() {
        Side side = FMLCommonHandler.instance().getEffectiveSide();

        Map<String, IconOption> options = side.isClient() ? fromClient() : fromMods();
        for (IconOption option : options.values())
            if (option.force || !IconRegistry.icons.containsKey(option.key)) IconRegistry.icons.put(option.key, option.build());

        CONSOLE = get("console");
        BRUSH = get("brush");
        PLANET = get("planet");
        KEYBOARD = get("keyboard");
        ANVIL = get("anvil");
        GLASSES = get("glasses");
        LETTER_A = get("letter_a");
        LETTER_CASE = get("letter_case");
        REGEX = get("regex");
        SYNC = get("sync");
    }

    /* Sources */

    private static Map<String, IconOption> fromClient() {
        Map<String, IconOption> map = new LinkedHashMap<>();

        try {
            for (IResource r : Minecraft.getMinecraft()
                    .getResourceManager()
                    .getAllResources(new ResourceLocation(Mappet.MOD_ID, "icons.json"))) {
                try (InputStreamReader rd = new InputStreamReader(r.getInputStream())) {
                    parseInto(map, rd);
                }
            }
        } catch (Exception e) {
            Mappet.logger.error("Failed to load icons.json: {}", e.getMessage());
        }

        return map;
    }

    private static Map<String, IconOption> fromMods() {
        Map<String, IconOption> map = new LinkedHashMap<>();

        for (ModContainer mod : Loader.instance().getActiveModList()) {
            File src = mod.getSource();

            try {
                if (src.isFile() && src.getName().endsWith(".jar")) {
                    try (JarFile jar = new JarFile(src)) {
                        JarEntry entry = jar.getJarEntry("assets/mappet/icons.json");

                        if (entry != null) {
                            try (InputStreamReader reader = new InputStreamReader(jar.getInputStream(entry))) {
                                parseInto(map, reader);
                            }
                        }
                    }
                }
                else if (src.isDirectory()) {
                    File file = new File(src, "assets/mappet/icons.json");

                    if (file.isFile()) {
                        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(file.toPath()))) {
                            parseInto(map, reader);
                        }
                    }
                }
            } catch (Exception e) {
                Mappet.logger.error(e.getMessage());
            }
        }

        return map;
    }

    /* Parsing */

    private static void parseInto(Map<String, IconOption> map, InputStreamReader reader) {
        try {
            JsonElement elements = PARSER.parse(reader);
            if (!elements.isJsonArray()) return;

            for (JsonElement element : elements.getAsJsonArray()) {
                IconOption option = parseOption(element);
                if (option == null) continue;
                if (option.force || !map.containsKey(option.key)) map.put(option.key, option);
            }
        } catch (Exception e) {
            Mappet.logger.error("Failed to parse icons.json: " + e.getMessage());
        }
    }

    private static IconOption parseOption(JsonElement element) {
        IconOption option = new IconOption();

        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            option.path = element.getAsString().trim().toLowerCase();
        }
        else if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();

            option.path = str(obj, "path", "").trim().toLowerCase();
            option.key = str(obj, "key", option.path).trim().toLowerCase();
            option.x = num(obj, "x", 0);
            option.y = num(obj, "y", 0);
            option.w = num(obj, "w", 16);
            option.h = num(obj, "h", 16);
            option.tw = num(obj, "tw", option.w);
            option.th = num(obj, "th", option.h);
            option.frames = num(obj, "frames", 0);
            option.frameTicks = num(obj, "ticks", 0);
            option.force = bool(obj, "force", false);
        }

        if (option.path.isEmpty()) return null;
        if (option.key.isEmpty()) option.key = option.path;

        return option;
    }

    /* JSON helpers */

    private static String str(JsonObject obj, String key, String def) {
        JsonElement el = obj.get(key);
        return el != null && !el.isJsonNull() ? el.getAsString() : def;
    }

    private static int num(JsonObject obj, String key, int def) {
        JsonElement el = obj.get(key);
        return el != null && !el.isJsonNull() ? el.getAsInt() : def;
    }

    private static boolean bool(JsonObject obj, String key, boolean def) {
        JsonElement el = obj.get(key);
        return el != null && !el.isJsonNull() ? el.getAsBoolean() : def;
    }

    /* IconOption */

    private static class IconOption {
        String key = "", path = "";
        int x = 0, y = 0, w = 16, h = 16, tw = 16, th = 16;
        int frames = 0, frameTicks = 0;
        boolean force;

        boolean isAnimated() {
            return frames > 0 && frameTicks > 0;
        }

        Icon build() {
            ResourceLocation location = new ResourceLocation(Mappet.MOD_ID, "textures/gui/icons/" + path + ".png");

            return isAnimated() ? new AnimatedIcon(location, x, y, w, h, tw, th, frames, frameTicks) : new Icon(location, x, y, w, h, tw, th);
        }
    }
}
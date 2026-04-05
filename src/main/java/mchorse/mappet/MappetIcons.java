package mchorse.mappet;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MappetIcons {
    private static final JsonParser parser = new JsonParser();
    //    private static final Map<String, Icon> base = new HashMap<>();

    public static Icon CONSOLE;
    public static Icon BRUSH;
    public static Icon PLANET;
    public static Icon KEYBOARD;
    public static Icon ANVIL;
    public static Icon GLASSES;
    public static Icon LETTER_A;
    public static Icon LETTER_CASE;
    public static Icon REGEX;

    public static final Icon NULL = new Icon(new ResourceLocation(Mappet.MOD_ID, "textures/gui/icons/_null.png"), 0, 0, 16, 16, 16, 16);

    public static List<String> getAllNames() {
        return new ArrayList<>(IconRegistry.icons.keySet());
    }

    public static Icon get(String key) {
        Icon icon = IconRegistry.icons.get(key);
        if (icon == null) {
            Mappet.logger.error("Not found icon: " + key);
            icon = NULL;
        }
        return icon;
    }

    public static void initiate() {
        //        if (base.isEmpty() && !IconRegistry.icons.isEmpty()) base.putAll(IconRegistry.icons);

        Side side = FMLCommonHandler.instance().getEffectiveSide();
        Set<IconOption> options = side.isClient() ? fromClient() : fromMods();

        //        IconRegistry.icons.clear();
        //        IconRegistry.icons.putAll(base);

        for (IconOption option : options) {
            if (!option.force && IconRegistry.icons.containsKey(option.key)) continue;
            IconRegistry.icons.put(option.key,
                    new Icon(new ResourceLocation(Mappet.MOD_ID, "textures/gui/icons/" + option.path + ".png"), 0, 0, option.w, option.h,
                            option.tw, option.th));
        }

        CONSOLE = get("console");
        BRUSH = get("brush");
        PLANET = get("planet");
        KEYBOARD = get("keyboard");
        ANVIL = get("anvil");
        GLASSES = get("glasses");
        LETTER_A = get("letter_a");
        LETTER_CASE = get("letter_case");
        REGEX = get("regex");
    }

    private static Set<IconOption> fromClient() {
        Set<IconOption> set = new HashSet<>();
        try {
            for (IResource r : Minecraft.getMinecraft()
                    .getResourceManager()
                    .getAllResources(new ResourceLocation(Mappet.MOD_ID, "icons.json"))) {
                try (InputStreamReader rd = new InputStreamReader(r.getInputStream())) {
                    set.addAll(parse(rd));
                }
            }
        } catch (Exception e) {
            Mappet.loggerClient.error("Failed to load icons.json: {}", e.getMessage());
        }
        return set;
    }

    private static Set<IconOption> fromMods() {
        Set<IconOption> set = new HashSet<>();
        for (ModContainer mod : Loader.instance().getActiveModList()) {
            File src = mod.getSource();
            try {
                if (src.isFile() && src.getName().endsWith(".jar")) {
                    try (JarFile jar = new JarFile(src)) {
                        JarEntry entry = jar.getJarEntry("assets/mappet/icons.json");
                        if (entry != null) try (InputStreamReader reader = new InputStreamReader(jar.getInputStream(entry))) {
                            set.addAll(parse(reader));
                        }
                    }
                }
                else if (src.isDirectory()) { // dev
                    File file = new File(src, "assets/mappet/icons.json");
                    if (file.isFile()) try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(file.toPath()))) {
                        set.addAll(parse(reader));
                    }
                }
            } catch (Exception e) {
                Mappet.logger.error(e.getMessage());
            }
        }
        return set;
    }

    private static Set<IconOption> parse(InputStreamReader reader) {
        Set<IconOption> set = new HashSet<>();
        try {
            JsonElement elements = parser.parse(reader);
            if (!elements.isJsonArray()) return set;

            for (JsonElement element : elements.getAsJsonArray()) {
                IconOption option = new IconOption();
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString())
                    option.path = element.getAsString().trim().toLowerCase();
                else if (element.isJsonObject()) {
                    JsonObject obj = element.getAsJsonObject();
                    option.path = str(obj, "path", "").trim().toLowerCase();
                    option.key = str(obj, "key", option.path).trim().toLowerCase();
                    option.x = num(obj, "x");
                    option.y = num(obj, "y");
                    option.w = num(obj, "w");
                    option.h = num(obj, "h");
                    option.tw = num(obj, "tw");
                    option.th = num(obj, "th");
                    if (obj.has("force") && !obj.get("force").isJsonNull()) option.force = obj.get("force").getAsBoolean();
                }
                if (!option.path.isEmpty()) {
                    if (option.key.isEmpty()) option.key = option.path;
                    set.add(option);
                }
            }
        } catch (Exception e) {
            if (Mappet.logger == null) Mappet.loggerClient.error("Failed to parse icons.json: {}", e.getMessage());
            else Mappet.logger.error("Failed to parse icons.json: " + e.getMessage());
        }
        return set;
    }

    private static String str(JsonObject obj, String key, String defaultValue) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : defaultValue;
    }

    private static int num(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsInt() : 16;
    }

    private static class IconOption {
        public String key = "", path = "";
        public int x = 0, y = 0, w = 16, h = 16, tw = 16, th = 16;
        public boolean force;
    }
}
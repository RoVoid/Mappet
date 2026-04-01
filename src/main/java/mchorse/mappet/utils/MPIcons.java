package mchorse.mappet.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mchorse.mappet.Mappet;
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
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MPIcons {
    private static final JsonParser parser = new JsonParser();
    private static final Map<String, Icon> base = new HashMap<>();

    public static final String CONSOLE = "console";
    public static final String BRUSH = "brush";
    public static final String PLANET = "planet";
    public static final String KEYBOARD = "keyboard";
    public static final String ANVIL = "anvil";
    public static final String GLASSES = "glasses";
    public static final String LETTER_A = "letter_a";
    public static final String LETTER_CASE = "letter_case";
    public static final String REGEX = "regex";

    public static List<String> getAllNames() {
        return new ArrayList<>(IconRegistry.icons.keySet());
    }

    public static Icon get(String key) {
        Icon i = IconRegistry.icons.get(key);
        if (i == null) Mappet.logger.error("Not found icon: " + key);
        return i;
    }

    public static void initiate() {
        if (base.isEmpty() && !IconRegistry.icons.isEmpty()) base.putAll(IconRegistry.icons);

        Side side = FMLCommonHandler.instance().getEffectiveSide();
        Set<IconOption> opts = side.isClient() ? fromClient() : fromMods();

        IconRegistry.icons.clear();
        IconRegistry.icons.putAll(base);

        for (IconOption o : opts) {
            String key = toKey(o.path);
            if (!o.force && IconRegistry.icons.containsKey(key)) continue;

            IconRegistry.icons.put(key,
                    new Icon(new ResourceLocation(Mappet.MOD_ID, "textures/gui/icons/" + o.path + ".png"), 0, 0, o.width, o.height, o.width,
                            o.height));
        }
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
                        JarEntry e = jar.getJarEntry("assets/mappet/icons.json");
                        if (e != null) try (InputStreamReader rd = new InputStreamReader(jar.getInputStream(e))) {
                            set.addAll(parse(rd));
                        }
                    }
                }
                else if (src.isDirectory()) { // dev
                    File f = new File(src, "assets/mappet/icons.json");
                    if (f.isFile()) try (InputStreamReader rd = new InputStreamReader(Files.newInputStream(f.toPath()))) {
                        set.addAll(parse(rd));
                    }
                }
            } catch (Exception e) {
                Mappet.logger.error(e.getMessage());
            }
        }
        return set;
    }

    private static Set<IconOption> parse(InputStreamReader rd) {
        Set<IconOption> set = new HashSet<>();
        try {
            JsonElement el = parser.parse(rd);
            if (!el.isJsonArray()) return set;

            for (JsonElement e : el.getAsJsonArray()) {
                IconOption o = new IconOption();
                if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isString()) o.path = e.getAsString().trim().toLowerCase();
                else if (e.isJsonObject()) {
                    JsonObject j = e.getAsJsonObject();
                    if (j.has("path") && !j.get("path").isJsonNull()) o.path = j.get("path").getAsString().trim().toLowerCase();
                    o.width = num(j, "width");
                    o.height = num(j, "height");
                    if (j.has("force") && !j.get("force").isJsonNull()) o.force = j.get("force").getAsBoolean();
                }
                if (!o.path.isEmpty()) set.add(o);
            }
        } catch (Exception e) {
            if (Mappet.logger == null) Mappet.loggerClient.error("Failed to parse icons.json: {}", e.getMessage());
            else Mappet.logger.error("Failed to parse icons.json: " + e.getMessage());
        }
        return set;
    }

    private static int num(JsonObject j, String k) {
        return j.has(k) && !j.get(k).isJsonNull() ? j.get(k).getAsInt() : 16;
    }

    private static String toKey(String p) {
        if (p == null || p.isEmpty()) return "";
        int l = p.lastIndexOf('/');
        if (l == -1) return p;

        int s = p.lastIndexOf('/', l - 1);
        String pref = s >= 0 ? p.substring(0, s + 1).replace('/', '_') + '_' : "";

        String parent = p.substring(s + 1, l);
        String name = p.substring(l + 1);

        int u = name.indexOf('_');
        String np = u == -1 ? name : name.substring(0, u);

        return pref + (parent.equals(np) ? "" : parent + "_") + name;
    }

    public static class IconOption {
        public String path = "";
        public int width = 16, height = 16;
        public boolean force;
    }
}
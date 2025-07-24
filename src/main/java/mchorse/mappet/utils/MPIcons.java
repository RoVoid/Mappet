package mchorse.mappet.utils;

import com.google.gson.JsonElement;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MPIcons {
    private static final JsonParser parser = new JsonParser();
    private static final Map<String, Icon> defaultIcons = new HashMap<>();

    public static final String CONSOLE = "console";
    public static final String CRAFT_IN = "craft_in";
    public static final String CRAFT_OUT = "craft_out";
    public static final String BRUSH = "brush";
    public static final String PLANET = "planet";

    public static List<String> getAllNames() {
        return new ArrayList<>(IconRegistry.icons.keySet());
    }

    public static Icon get(String key) {
        Icon icon = IconRegistry.icons.get(key);
        if (icon == null) Mappet.logger.error("Not found icon: " + key);
        return icon;
    }

    public static void initiate() {
        if (defaultIcons.isEmpty() && !IconRegistry.icons.isEmpty()) defaultIcons.putAll(IconRegistry.icons);
        System.out.println(defaultIcons.keySet());

        Side side = FMLCommonHandler.instance().getEffectiveSide();
        Set<String> paths = side.isClient() ? getPathsClient() : getPathsFromMods();

        IconRegistry.icons.clear();
        IconRegistry.icons.putAll(defaultIcons);

        for (String path : paths) {
            String key = toIconKey(path);

            if (!IconRegistry.icons.containsKey(key)) {
                Icon icon = new Icon(new ResourceLocation(Mappet.MOD_ID, "textures/gui/icons/" + path + ".png"), 0, 0, 16, 16, 16, 16);
                IconRegistry.register(key, icon);
            }
        }
    }

    public static Set<String> getPathsClient() {
        Set<String> paths = new HashSet<>();

        try {
            List<IResource> resources = Minecraft.getMinecraft().getResourceManager().getAllResources(new ResourceLocation(Mappet.MOD_ID, "icons.json"));

            for (IResource resource : resources) {
                try (InputStreamReader reader = new InputStreamReader(resource.getInputStream())) {
                    paths.addAll(parseIconPaths(reader));
                }
            }
        } catch (Exception e) {
            Mappet.loggerClient.error("Failed to load icons.json: {}", e.getMessage());
        }

        return paths;
    }

    public static Set<String> getPathsFromMods() {
        Set<String> paths = new HashSet<>();

        for (ModContainer mod : Loader.instance().getActiveModList()) {
            File source = mod.getSource();

            if (source.isFile() && source.getName().endsWith(".jar")) {
                try (JarFile jar = new JarFile(source)) {
                    JarEntry entry = jar.getJarEntry("assets/mappet/icons.json");
                    if (entry != null) {
                        try (InputStream input = jar.getInputStream(entry); InputStreamReader reader = new InputStreamReader(input)) {
                            paths.addAll(parseIconPaths(reader));
                        }
                    }
                } catch (Exception e) {
                    Mappet.logger.error(e.getMessage());
                }
            } else if (source.isDirectory()) { // dev environment
                File iconsFile = new File(source, "assets/mappet/icons.json");
                if (iconsFile.exists() && iconsFile.isFile()) {
                    try (InputStream input = Files.newInputStream(iconsFile.toPath()); InputStreamReader reader = new InputStreamReader(input)) {
                        paths.addAll(parseIconPaths(reader));
                    } catch (Exception e) {
                        Mappet.logger.error(e.getMessage());
                    }
                }
            }
        }

        return paths;
    }

    private static Set<String> parseIconPaths(InputStreamReader reader) {
        Set<String> paths = new HashSet<>();
        try {
            JsonElement element = parser.parse(reader);
            if (element.isJsonArray()) {
                for (JsonElement entry : element.getAsJsonArray()) {
                    if (entry.isJsonPrimitive() && entry.getAsJsonPrimitive().isString()) {
                        String path = entry.getAsString().trim().toLowerCase();
                        if (!path.isEmpty()) {
                            paths.add(path);
                        }
                    }
                }
            }
        } catch (Exception e) {
            if(Mappet.logger == null) Mappet.loggerClient.error("Failed to parse icons.json: {}", e.getMessage());
            else Mappet.logger.error("Failed to parse icons.json: " + e.getMessage());
        }
        return paths;
    }

    private static String toIconKey(String path) {
        if (path == null || path.isEmpty()) return "";

        int lastSlash = path.lastIndexOf('/');
        if (lastSlash == -1) return path;

        int secondLastSlash = path.lastIndexOf('/', lastSlash - 1);

        String pathPrefix = path.substring(0, secondLastSlash + 1);
        if (!pathPrefix.isEmpty()) {
            pathPrefix = pathPrefix.replaceAll("/", "_");
            pathPrefix += '_';
        }

        String parent = path.substring(secondLastSlash + 1, lastSlash);
        String name = path.substring(lastSlash + 1);

        int underscore = name.indexOf('_');
        String namePrefix = underscore == -1 ? name : name.substring(0, underscore);

        StringBuilder key = new StringBuilder(pathPrefix);
        if (!parent.equals(namePrefix)) key.append(parent).append('_');
        key.append(name);

        return key.toString();
    }
}

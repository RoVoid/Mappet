package mchorse.mappet.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import mchorse.mappet.Mappet;
import mchorse.mclib.client.gui.utils.Icon;
import mchorse.mclib.client.gui.utils.IconRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import java.io.InputStreamReader;
import java.util.*;

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

        Set<String> paths = new HashSet<>();

        try {
            List<IResource> resources = Minecraft.getMinecraft().getResourceManager()
                    .getAllResources(new ResourceLocation(Mappet.MOD_ID, "icons.json"));

            for (IResource resource : resources) {
                try (InputStreamReader reader = new InputStreamReader(resource.getInputStream())) {
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
                }
            }
        } catch (Exception e) {
            Mappet.logger.error("Failed to load icons.json: " + e.getMessage());
            return;
        }

        IconRegistry.icons.clear();
        IconRegistry.icons.putAll(defaultIcons);

        for (String path : paths) {
            String key = toIconKey(path);

            if (!IconRegistry.icons.containsKey(key)) {
                Icon icon = new Icon(
                        new ResourceLocation(Mappet.MOD_ID, "textures/gui/icons/" + path + ".png"),
                        0, 0, 16, 16, 16, 16
                );
                IconRegistry.register(key, icon);
            }
        }
    }

    private static String toIconKey(String path) {
        if (path == null || path.isEmpty()) return "";

        int lastSlash = path.lastIndexOf('/');
        if (lastSlash == -1) return path;

        int secondLastSlash = path.lastIndexOf('/', lastSlash - 1);

        String pathPrefix = path.substring(0, secondLastSlash + 1);
        if (!pathPrefix.isEmpty()) {
            pathPrefix.replaceAll("/", "_");
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

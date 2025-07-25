package mchorse.mappet.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mchorse.mappet.CommonProxy;
import mchorse.mappet.utils.Utils;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Virtual OGG sounds pack so .ogg files could be used as sound events
 * in Minecraft's system
 */
@SideOnly(Side.CLIENT)
public class SoundPack implements IResourcePack {
    private final File folder;

    public SoundPack(File folder) {
        this.folder = folder;
        this.folder.mkdirs();
    }

    @Override
    public InputStream getInputStream(ResourceLocation location) throws IOException {
        String path = location.getResourcePath();

        if (path.equals("sounds.json")) {
            JsonObject object = generateJson(folder, new JsonObject());
            return IOUtils.toInputStream(object.toString(), Utils.getCharset());
        }

        File file = new File(folder, path.substring(7));
        if (!file.exists()) throw new IOException("Sound not found: " + path);
        return new FileInputStream(file);
    }

    private JsonObject generateJson(File folder, JsonObject object) {
        return generateJson(folder, object, "");
    }

    private JsonObject generateJson(File folder, JsonObject object, String parent) {
        if (!folder.exists()) return object;

        File[] files = folder.listFiles();
        if (files == null) return object;

        for (File file : files) {
            String name = file.getName();

            if (name.endsWith(".ogg")) {
                JsonObject sound = new JsonObject();
                JsonArray elements = new JsonArray();
                String id = parent + name.substring(0, name.length() - 4); // remove ".ogg"

                elements.add("mp.sounds:" + id);
                sound.add("sounds", elements);
                object.add(id, sound);
            } else if (file.isDirectory()) {
                generateJson(file, object, parent + name + ".");
            }
        }

        return object;
    }

    public static List<String> getCustomSoundEvents() {
        List<String> soundEvents = new ArrayList<>();
        File soundsFolder = new File(CommonProxy.configFolder, "sounds");
        SoundPack soundPack = new SoundPack(soundsFolder);
        JsonObject soundJson = soundPack.generateJson(soundsFolder, new JsonObject());

        for (Entry<String, JsonElement> entry : soundJson.entrySet()) {
            soundEvents.add("mp.sounds:" + entry.getKey());
        }

        return soundEvents;
    }

    @Override
    public boolean resourceExists(ResourceLocation location) {
        String path = location.getResourcePath();
        if (path.equals("sounds.json")) return true;
        return new File(folder, path.substring(7)).exists();
    }

    @Override
    public Set<String> getResourceDomains() {
        return Collections.singleton("mp.sounds");
    }

    @Override
    public String getPackName() {
        return "Mappet's sound pack";
    }

    @Nullable
    @Override
    public <T extends IMetadataSection> T getPackMetadata(MetadataSerializer metadataSerializer, String metadataSectionName) {
        return null;
    }

    @Override
    public BufferedImage getPackImage() {
        return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    }
}
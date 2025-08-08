package mchorse.mappet.client.gui.utils.overlays;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.client.gui.utils.keys.LangKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.io.IOUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class GuiSoundOverlayPanel extends GuiResourceLocationOverlayPanel {
    private static final Set<ResourceLocation> extraSounds = new HashSet<>();
    private static long lastUpdate;

    private static Set<ResourceLocation> getSoundEvents() {
        Set<ResourceLocation> locations = new HashSet<>();

        if (lastUpdate < LangKey.lastTime) {
            extraSounds.clear();

            updateSounds("b.a");
            updateSounds("mp.sounds");

            lastUpdate = LangKey.lastTime;
        }

        locations.addAll(ForgeRegistries.SOUND_EVENTS.getKeys());
        locations.addAll(extraSounds);

        return locations;
    }

    private static void updateSounds(String rp) {
        try {
            IResource resource = Minecraft
                    .getMinecraft()
                    .getResourceManager()
                    .getResource(new ResourceLocation(rp, "sounds.json"));
            JsonElement element = new JsonParser().parse(IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8));

            if (element.isJsonObject()) {
                for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                    extraSounds.add(new ResourceLocation(rp, entry.getKey()));
                }
            }
        } catch (Exception ignored1) {
        }
    }

    public GuiSoundOverlayPanel(Minecraft mc, Consumer<ResourceLocation> callback) {
        super(mc, IKey.lang("mappet.gui.overlays.sounds.main"), getSoundEvents(), callback);

        GuiIconElement edit = new GuiIconElement(mc, Icons.SOUND, (b) -> playSound());

        edit.flex().wh(16, 16);
        icons.add(edit);
    }

    private void playSound() {
        if (rls.list.getIndex() <= 0) {
            return;
        }

        ResourceLocation location = new ResourceLocation(rls.list.getCurrentFirst());
        float x = (float) mc.player.posX;
        float y = (float) mc.player.posY;
        float z = (float) mc.player.posZ;

        mc
                .getSoundHandler()
                .playSound(new PositionedSoundRecord(location, SoundCategory.MASTER, 1, 1, false, 0, ISound.AttenuationType.LINEAR, x, y, z));
    }
}
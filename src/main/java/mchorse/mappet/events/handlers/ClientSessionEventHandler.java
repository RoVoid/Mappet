package mchorse.mappet.events.handlers;

import mchorse.mappet.Mappet;
import mchorse.mappet.config.MappetConfig;
import mchorse.mappet.api.dialogues.DialogueManager;
import mchorse.mappet.api.events.EventManager;
import mchorse.mappet.api.factions.FactionManager;
import mchorse.mappet.api.huds.HUDManager;
import mchorse.mappet.api.npcs.NpcManager;
import mchorse.mappet.api.quests.QuestManager;
import mchorse.mappet.api.quests.chains.QuestChainManager;
import mchorse.mappet.api.schematics.SchematicManager;
import mchorse.mappet.api.scripts.ScriptManager;
import mchorse.mappet.client.KeyboardHandler;
import mchorse.mappet.client.RenderingHandler;
import mchorse.mappet.client.SoundPack;
import mchorse.mappet.network.client.ClientHandlerBlackAndWhiteShader;
import mchorse.mappet.network.client.ClientHandlerPlayerPerspective;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Client-side "session" lifecycle: what happens when the client connects
 * to (possibly its own integrated) server, and what happens when it
 * disconnects. Formerly the other half of {@code ForgeEventHandler}.
 * <p>
 * Fix vs. the original: {@code onClientDisconnect} now nulls out
 * {@code Mappet.schematics} to match {@code onClientConnect}, which
 * creates it. Previously {@code schematics} was the only one of the nine
 * managers that got a fresh client-side stand-in on connect but was never
 * cleared on disconnect, leaving a stale reference between sessions.
 */
public class ClientSessionEventHandler {
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        if (!event.isLocal()) {
            Mappet.quests = new QuestManager(null);
            Mappet.schematics = new SchematicManager(null);
            Mappet.events = new EventManager(null);
            Mappet.dialogues = new DialogueManager(null);
            Mappet.npcs = new NpcManager(null);
            Mappet.factions = new FactionManager(null);
            Mappet.chains = new QuestChainManager(null);
            Mappet.scripts = new ScriptManager(null);
            Mappet.huds = new HUDManager(null);
        }

        if (MappetConfig.loadCustomSoundsOnLogin.get()) {
            Minecraft mc = Minecraft.getMinecraft();
            SoundHandler soundHandler = mc.getSoundHandler();

            for (String sound : SoundPack.getCustomSoundEvents()) {
                ISound soundToPlay = PositionedSoundRecord.getRecord(new SoundEvent(new ResourceLocation(sound)), 1.0f, 0);
                soundHandler.playSound(soundToPlay);
            }
        }

        KeyboardHandler.hotkeysNeedLoad = true;

        ClientHandlerPlayerPerspective.setPerspective(-1);
        ClientHandlerBlackAndWhiteShader.enableBlackAndWhiteShader(false);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        Mappet.quests = null;
        Mappet.schematics = null;
        Mappet.events = null;
        Mappet.dialogues = null;
        Mappet.npcs = null;
        Mappet.factions = null;
        Mappet.chains = null;
        Mappet.scripts = null;
        Mappet.huds = null;

        KeyboardHandler.hotkeys.clear();
        RenderingHandler.reset();
    }
}

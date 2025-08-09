package mchorse.mappet;

import mchorse.mappet.api.dialogues.DialogueManager;
import mchorse.mappet.api.events.EventManager;
import mchorse.mappet.api.factions.FactionManager;
import mchorse.mappet.api.huds.HUDManager;
import mchorse.mappet.api.npcs.NpcManager;
import mchorse.mappet.api.quests.QuestManager;
import mchorse.mappet.api.quests.chains.QuestChainManager;
import mchorse.mappet.api.schematics.SchematicManager;
import mchorse.mappet.api.scripts.ScriptManager;
import mchorse.mappet.blocks.ModBlocks;
import mchorse.mappet.client.KeyboardHandler;
import mchorse.mappet.client.RenderingHandler;
import mchorse.mappet.client.SoundPack;
import mchorse.mappet.entities.EntityNpc;
import mchorse.mappet.items.ModItems;
import mchorse.mappet.network.client.ClientHandlerBlackAndWhiteShader;
import mchorse.mappet.network.client.ClientHandlerLockPerspective;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModEventHandler {
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

        ClientHandlerLockPerspective.setLockedPerspective(-1);
        ClientHandlerBlackAndWhiteShader.enableBlackAndWhiteShader(false);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        Mappet.quests = null;
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

    @SubscribeEvent
    public void onBlocksRegister(RegistryEvent.Register<Block> event) {
        ModBlocks.register(event);
    }

    @SubscribeEvent
    public void onItemsRegister(RegistryEvent.Register<Item> event) {
        ModItems.register(event);
    }

    @SubscribeEvent
    public void onEntityRegister(RegistryEvent.Register<EntityEntry> event) {
        event
                .getRegistry()
                .register(EntityEntryBuilder
                        .create()
                        .entity(EntityNpc.class)
                        .name(Mappet.MOD_ID + ".npc")
                        .id(new ResourceLocation(Mappet.MOD_ID, "npc"), 0)
                        .tracker(EntityNpc.RENDER_DISTANCE, 3, false)
                        .build());
        ModBlocks.bindEntities(event);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onModelRegistry(ModelRegistryEvent event) {
        ModItems.bindModels(event);
    }
}
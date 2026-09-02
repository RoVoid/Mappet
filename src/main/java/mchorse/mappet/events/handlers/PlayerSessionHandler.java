package mchorse.mappet.events.handlers;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.scripts.code.entities.ai.repeatingCommand.EntityAIRepeatingCommand;
import mchorse.mappet.api.scripts.code.entities.ai.repeatingCommand.RepeatingCommandDataStorage;
import mchorse.mappet.api.scripts.code.entities.ai.rotations.EntityAIRotations;
import mchorse.mappet.api.scripts.code.entities.ai.rotations.RotationDataStorage;
import mchorse.mappet.blocks.BlockRegion;
import mchorse.mappet.blocks.BlockTrigger;
import mchorse.mappet.capabilities.character.Character;
import mchorse.mappet.capabilities.character.CharacterProvider;
import mchorse.mappet.capabilities.character.ICharacter;
import mchorse.mappet.entities.utils.MappetNpcRespawnManager;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.packets.hotkey.PacketSyncHotkeys;
import mchorse.mappet.network.server.content.ServerHandlerContentExit;
import mchorse.mappet.utils.CompatUtils;
import mchorse.mappet.utils.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;

public class PlayerSessionHandler {
    public static final ResourceLocation CAPABILITY = new ResourceLocation(Mappet.MOD_ID, "character");

    private final QuestTickHandler questTickHandler = new QuestTickHandler();
    private final HUDSyncHandler hudSyncHandler = new HUDSyncHandler();
    private final LockedEntityHandler lockedEntityHandler = new LockedEntityHandler();
    private final ExecutableScheduler executableScheduler = new ExecutableScheduler();

    public QuestTickHandler getQuestTickHandler() {
        return questTickHandler;
    }

    public HUDSyncHandler getHudSyncHandler() {
        return hudSyncHandler;
    }

    public LockedEntityHandler getLockedEntityHandler() {
        return lockedEntityHandler;
    }

    public ExecutableScheduler getExecutableScheduler() {
        return executableScheduler;
    }

    public void reset() {
        questTickHandler.reset();
        executableScheduler.reset();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerPlaceBlock(BlockEvent.PlaceEvent event) {
        if (PlayerUtils.isOperator(event.getPlayer())) return;
        Block block = event.getPlacedBlock().getBlock();
        if (block instanceof BlockTrigger || block instanceof BlockRegion) {
            event.setCanceled(true);
            event.getWorld().setBlockState(event.getPos(), event.getPlacedBlock(), 3);
        }
    }

    /**
     * Attach player capabilities
     */
    @SubscribeEvent
    public void attachPlayerCapability(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) event.addCapability(CAPABILITY, new CharacterProvider());
    }

    @SubscribeEvent
    public void onPlayerLogsIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player == null) return;
        EntityPlayerMP player = (EntityPlayerMP) event.player;

        ICharacter character = Character.get(player);

        if (character != null) {
            questTickHandler.syncQuests(player, character);
            if (!Mappet.settings.hotkeys.keys.isEmpty()) {
                Dispatcher.sendTo(new PacketSyncHotkeys(Mappet.settings), player);
            }

            hudSyncHandler.sendOwnHUDs(player, character);
        }

        hudSyncHandler.sendOtherPlayersGlobalHUDs(player);
    }

    /**
     * WORKS ONLY ON DEDICATED SERVER
     */
    @SubscribeEvent
    public void onPlayerLogsOut(PlayerEvent.PlayerLoggedOutEvent event) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null || !server.isDedicatedServer()) return;
        ServerHandlerContentExit.finishEditing((EntityPlayerMP) event.player);
    }

    /**
     * Copy data from dead player (or player returning from the end) to the new player
     */
    @SubscribeEvent
    public void onPlayerClone(net.minecraftforge.event.entity.player.PlayerEvent.Clone event) {
        EntityPlayer player = event.getEntityPlayer();
        ICharacter character = Character.get(player);
        ICharacter oldCharacter = Character.get(event.getOriginal());
        if (!CompatUtils.isMohist() && character != null) character.copy(oldCharacter, player);
    }

    /**
     * Persist global ("~") states on every world save (autosave, /save-all, etc.),
     * not just on a graceful server shutdown, to avoid losing state changes on crashes.
     */
    @SubscribeEvent
    public void onWorldSave(WorldEvent.Save event) {
        if (event.getWorld().isRemote || event.getWorld().provider.getDimension() != 0) return;
        if (Mappet.states != null) Mappet.states.save();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerSpawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.player.world.isRemote) return;
        EntityPlayerMP player = (EntityPlayerMP) event.player;
        ICharacter character = Character.get(player);
        if (character == null) return;
        questTickHandler.syncQuests(player, character);
        if (!Mappet.settings.hotkeys.keys.isEmpty()) {
            Dispatcher.sendTo(new PacketSyncHotkeys(Mappet.settings), player);
        }
    }

    /**
     * Server-side per-player upkeep (position cache, HUD list). The
     * client-side branch this used to share a method with —
     * {@code RenderingHandler.update()} — now lives in
     * {@link ClientCosmeticsHandler#onPlayerTick}, since it has nothing
     * to do with a player's server-side session.
     */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;
        if (event.player.world.isRemote) return;

        Character character = Character.get(event.player);
        if (character != null) {
            character.getPositionCache().updatePlayer(event.player);
            character.updateDisplayedHUDsList();
        }
    }

    /**
     * Restore AI rotation-lock and repeating-command tasks for living
     * entities that carried that data across a chunk (re)load.
     */
    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (!(event.getEntity() instanceof EntityLiving)) return;

        EntityLiving entityLiving = (EntityLiving) event.getEntity();

        RotationDataStorage rotationDataStorage = RotationDataStorage.getRotationDataStorage(event.getWorld());
        RotationDataStorage.RotationData rotationData = rotationDataStorage.getRotationData(entityLiving.getUniqueID());
        if (rotationData != null) {
            entityLiving.tasks.addTask(0, new EntityAIRotations(entityLiving, rotationData.yaw, rotationData.pitch, rotationData.yawHead, 1.0F));
        }

        RepeatingCommandDataStorage repeatingCommandDataStorage = RepeatingCommandDataStorage.getRepeatingCommandDataStorage(event.getWorld());
        List<RepeatingCommandDataStorage.RepeatingCommandData> repeatingCommandDataList = repeatingCommandDataStorage.getRepeatingCommandData(
                entityLiving.getUniqueID());
        if (repeatingCommandDataList != null) {
            for (RepeatingCommandDataStorage.RepeatingCommandData repeatingCommandData : repeatingCommandDataList) {
                entityLiving.tasks.addTask(10, new EntityAIRepeatingCommand(entityLiving, repeatingCommandData.command, repeatingCommandData.frequency));
            }
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;

        lockedEntityHandler.tick();
        questTickHandler.tick();
        executableScheduler.tick();
    }

    /**
     * Fix vs. the original: {@code WorldTickEvent} fires for both
     * {@code START} and {@code END} phases, but the old handler didn't
     * check the phase at all, so {@code MappetNpcRespawnManager.onTick()}
     * — which unconditionally calls {@code markDirty()} — ran twice per
     * world per tick instead of once.
     */
    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;

        MappetNpcRespawnManager.get(event.world).onTick();
    }
}

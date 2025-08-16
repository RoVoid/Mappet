package mchorse.mappet;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import mchorse.mappet.api.huds.HUDScene;
import mchorse.mappet.api.quests.Quest;
import mchorse.mappet.api.quests.Quests;
import mchorse.mappet.api.scripts.code.entities.ScriptEntity;
import mchorse.mappet.api.scripts.code.entities.ai.repeatingCommand.EntityAIRepeatingCommand;
import mchorse.mappet.api.scripts.code.entities.ai.repeatingCommand.RepeatingCommandDataStorage;
import mchorse.mappet.api.scripts.code.entities.ai.rotations.EntityAIRotations;
import mchorse.mappet.api.scripts.code.entities.ai.rotations.RotationDataStorage;
import mchorse.mappet.api.scripts.user.entities.IScriptEntity;
import mchorse.mappet.api.utils.IExecutable;
import mchorse.mappet.blocks.BlockRegion;
import mchorse.mappet.blocks.BlockTrigger;
import mchorse.mappet.capabilities.character.Character;
import mchorse.mappet.capabilities.character.CharacterProvider;
import mchorse.mappet.capabilities.character.ICharacter;
import mchorse.mappet.client.RenderingHandler;
import mchorse.mappet.commands.data.CommandDataClear;
import mchorse.mappet.entities.EntityNpc;
import mchorse.mappet.entities.utils.MappetNpcRespawnManager;
import mchorse.mappet.events.StateChangedEvent;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.client.ClientHandlerBlackAndWhiteShader;
import mchorse.mappet.network.client.ClientHandlerPlayerPerspective;
import mchorse.mappet.network.common.hotkey.PacketSyncHotkeys;
import mchorse.mappet.network.common.huds.PacketHUDScene;
import mchorse.mappet.network.common.npc.PacketNpcJump;
import mchorse.mappet.network.common.quests.PacketQuest;
import mchorse.mappet.network.common.quests.PacketQuests;
import mchorse.mclib.utils.OpHelper;
import mchorse.mclib.utils.ReflectionUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.time.Instant;
import java.util.*;

public class EventHandler {
    /**
     * Resource location for cosmetic capability
     */
    public static final ResourceLocation CAPABILITY = new ResourceLocation(Mappet.MOD_ID, "character");

    private static Boolean isMohist;

    /**
     * Players that must be checked
     */
    private final Set<EntityPlayer> playersToCheck = new HashSet<>();

    /**
     * Delayed executions
     */
    private final List<IExecutable> executables = new ArrayList<>();

    /**
     * Second executables list to avoid concurrent modification
     * exceptions when adding consequent delayed executions
     */
    private final List<IExecutable> secondList = new ArrayList<>();

//    /**
//     * Set that keeps track of players that just joined (it is necessary to avoid
//     * triggering the player respawn trigger when the player logs in)
//     */
//    private final Set<UUID> loggedInPlayers = new HashSet<>();

    private int skinCounter;

    static int previousPerspective = 0;

    private static boolean isMohist() {
        if (isMohist != null) return isMohist;
        try {
            Class.forName("com.mohistmc.MohistMC");
            isMohist = true;
        } catch (Exception e) {
            isMohist = false;
        }
        return isMohist;
    }

    public List<String> getIds() {
        List<String> ids = new ArrayList<>();
        for (IExecutable executable : executables) {
            ids.add(executable.getId());
        }
        return Lists.newArrayList(Sets.newLinkedHashSet(ids));
    }

    public void addExecutables(List<IExecutable> executionForks) {
        executables.addAll(executionForks);
    }

    public void addExecutable(IExecutable executable) {
        executables.add(executable);
    }

    public int removeExecutables(String id) {
        int size = executables.size();
        executables.removeIf((e) -> e.getId().equals(id));
        return size - executables.size();
    }

    public void reset() {
        playersToCheck.clear();
        executables.clear();
        secondList.clear();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerPlaceBlock(BlockEvent.PlaceEvent event) {
        EntityPlayerMP player = (EntityPlayerMP) event.getPlayer();
        if (OpHelper.isPlayerOp(player)) {
            Block block = event.getPlacedBlock().getBlock();
            if (block instanceof BlockTrigger || block instanceof BlockRegion) event.setCanceled(true);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onPlayerOpenOrCloseContainer(PlayerContainerEvent event) {
        playersToCheck.add(event.getEntityPlayer());
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
        EntityPlayerMP player = (EntityPlayerMP) event.player;
        if (event.player == null) return;

        ICharacter character = Character.get(player);
        Instant lastClear = Mappet.data.getLastClear();

        if (character != null) {
            if (character.getLastClear().isBefore(lastClear)) {
                CommandDataClear.clear(player, Mappet.data.getLastInventory());
                character.updateLastClear(lastClear);
            }
            syncData(player, character);
        }

        if (character != null) {
            Map<String, List<HUDScene>> displayedHUDs = character.getDisplayedHUDs();
            for (Map.Entry<String, List<HUDScene>> entry : displayedHUDs.entrySet()) {
                String id = entry.getKey();
                List<HUDScene> scenes = entry.getValue();
                for (HUDScene scene : scenes) {
                    // Send the PacketHUDScene for each HUDScene
                    Dispatcher.sendTo(new PacketHUDScene(id, scene.serializeNBT()), player);
                }
            }
        }

        // display present global HUDs player on any player that has a global HUD in his displayed HUDs scenes list
        for (EntityPlayerMP p : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
            ICharacter c = Character.get(p);
            if (c != null) {
                Map<String, List<HUDScene>> displayed = c.getDisplayedHUDs();
                for (Map.Entry<String, List<HUDScene>> entry : displayed.entrySet()) {
                    String id = entry.getKey();
                    List<HUDScene> scenes = entry.getValue();
                    for (HUDScene scene : scenes) {
                        if (scene.global) Dispatcher.sendTo(new PacketHUDScene(id, scene.serializeNBT()), player);
                    }
                }
            }
        }

        //loggedInPlayers.add(player.getUniqueID());
    }

//    /**
//     * WORKS ONLY ON DEDICATED SERVER
//     */
//    @SubscribeEvent
//    public void onPlayerLogsOut(PlayerEvent.PlayerLoggedOutEvent event) {
//        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
//        if (server != null && server.isDedicatedServer()) loggedInPlayers.remove(event.player.getUniqueID());
//    }

    /**
     * Copy data from dead player (or player returning from the end) to the new player
     */
    @SubscribeEvent
    public void onPlayerClone(net.minecraftforge.event.entity.player.PlayerEvent.Clone event) {
        EntityPlayer player = event.getEntityPlayer();
        ICharacter character = Character.get(player);
        ICharacter oldCharacter = Character.get(event.getOriginal());
        if (!isMohist() && character != null) character.copy(oldCharacter, player);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerSpawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.player.world.isRemote) return;
        EntityPlayerMP player = (EntityPlayerMP) event.player;
        ICharacter character = Character.get(player);
        if (character == null) return;
        syncData(player, character);
    }

    private void syncData(EntityPlayerMP player, ICharacter character) {
        if (!character.getQuests().quests.isEmpty()) {
            character.getQuests().initiate(player);
            Dispatcher.sendTo(new PacketQuests(character.getQuests()), player);
        }
        if (!Mappet.settings.hotkeys.keys.isEmpty()) {
            Dispatcher.sendTo(new PacketSyncHotkeys(Mappet.settings), player);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (ClientHandlerPlayerPerspective.locked()) {
            if (mc.gameSettings.thirdPersonView != ClientHandlerPlayerPerspective.getPerspective()) {
                mc.gameSettings.thirdPersonView = ClientHandlerPlayerPerspective.getPerspective();
            }
        }
        if (previousPerspective != mc.gameSettings.thirdPersonView) {
            previousPerspective = mc.gameSettings.thirdPersonView;
            ClientHandlerBlackAndWhiteShader.update();
        }
    }

    @SubscribeEvent
    public void onPlayerPickUp(EntityItemPickupEvent event) {
        playersToCheck.add(event.getEntityPlayer());
    }

    @SubscribeEvent
    public void onMobKilled(LivingDeathEvent event) {
        if (!(event.getSource().getTrueSource() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        ICharacter character = Character.get(player);
        if (character != null) {
            for (Quest quest : character.getQuests().quests.values()) {
                quest.mobWasKilled(player, event.getEntity());
            }
            playersToCheck.add(player);
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (!(event.getEntity() instanceof EntityLiving)) return;

        // Handle load AI rotation data
        EntityLiving entityLiving = (EntityLiving) event.getEntity();
        RotationDataStorage rotationDataStorage = RotationDataStorage.getRotationDataStorage(event.getWorld());
        RotationDataStorage.RotationData rotationData = rotationDataStorage.getRotationData(entityLiving.getUniqueID());
        if (rotationData != null) {
            float yaw = rotationData.yaw;
            float pitch = rotationData.pitch;
            float yawHead = rotationData.yawHead;
            entityLiving.tasks.addTask(0, new EntityAIRotations(entityLiving, yaw, pitch, yawHead, 1.0F));
        }

        // Handle load AI repeating command data
        RepeatingCommandDataStorage repeatingCommandDataStorage = RepeatingCommandDataStorage.getRepeatingCommandDataStorage(event.getWorld());
        List<RepeatingCommandDataStorage.RepeatingCommandData> repeatingCommandDataList = repeatingCommandDataStorage.getRepeatingCommandData(entityLiving.getUniqueID());
        if (repeatingCommandDataList != null) {
            for (RepeatingCommandDataStorage.RepeatingCommandData repeatingCommandData : repeatingCommandDataList) {
                String command = repeatingCommandData.command;
                int frequency = repeatingCommandData.frequency;
                entityLiving.tasks.addTask(10, new EntityAIRepeatingCommand(entityLiving, command, frequency));
            }
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;

        //lock entity if they should be locked
        for (Entity entity : getAllEntities()) {
            if (entity == null) continue;
            //lock position if it should be locked
            if (entity.getEntityData().getBoolean("positionLocked")) {
                IScriptEntity scriptEntity = ScriptEntity.create(entity);
                if (scriptEntity == null) continue;
                scriptEntity.setPosition(entity.getEntityData().getDouble("lockX"), entity
                        .getEntityData()
                        .getDouble("lockY"), entity.getEntityData().getDouble("lockZ"));
                scriptEntity.setMotion(0.0, 0.0, 0.0);
            }
            //lock rotation if it should be locked
            if (entity.getEntityData().getBoolean("rotationLocked")) {
                IScriptEntity scriptEntity = ScriptEntity.create(entity);
                if (scriptEntity == null) continue;
                scriptEntity.setRotations(entity.getEntityData().getFloat("lockPitch"), entity
                        .getEntityData()
                        .getFloat("lockYaw"), entity.getEntityData().getFloat("lockYawHead"));
            }
        }

        for (EntityPlayer player : playersToCheck) {
            ICharacter character = Character.get(player);
            if (character == null) continue;

            Quests quests = character.getQuests();
            Iterator<Map.Entry<String, Quest>> it = quests.quests.entrySet().iterator();

            quests.iterating = true;
            while (it.hasNext()) {
                Map.Entry<String, Quest> entry = it.next();
                Quest quest = entry.getValue();

                if (quest.instant && quest.rewardIfComplete(player)) {
                    it.remove();
                    Dispatcher.sendTo(new PacketQuest(entry.getKey(), null), (EntityPlayerMP) player);
                }
                else Dispatcher.sendTo(new PacketQuest(entry.getKey(), entry.getValue()), (EntityPlayerMP) player);
            }
            quests.flush(player);
        }

        playersToCheck.clear();

        /* This block of code might be a bit confusing. However, it essentially
         * what it does is preventing concurrent modification when timer nodes
         * add consequent execution forks, this way I can reliably keep track
         * of order of both the old executions which are not yet executed and
         * of new forks that were added by new timer nodes */
        if (!executables.isEmpty()) {
            /* Copy original event forks to another list and clear them
             * to be ready for new forks */
            secondList.addAll(executables);
            executables.clear();

            /* Execute event forks (and remove those which were finished) */
            secondList.removeIf(IExecutable::update);

            /* Add back to the original list the remaining forks and
             * new forks that were added by consequent timer nodes */
            secondList.addAll(executables);
            executables.clear();
            executables.addAll(secondList);
            secondList.clear();
        }
    }

//    private List<Entity> getAllEntities() {
//        List<Entity> entities = new ArrayList<>();
//        try {
//            entities.addAll(EntitySelector.matchEntities(FMLCommonHandler
//                    .instance()
//                    .getMinecraftServerInstance(), "@e", Entity.class));
//        } catch (Exception ignored) {
//        }
//        return entities;
//    }

    private List<Entity> getAllEntities() {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        List<Entity> entities = new ArrayList<>();
        for (World world : server.worlds) entities.addAll(world.loadedEntityList);
        return entities;
    }


    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;
        if (event.player.world.isRemote) {
            RenderingHandler.update();
            return;
        }
        Character character = Character.get(event.player);
        if (character != null) {
            character.getPositionCache().updatePlayer(event.player);
            character.updateDisplayedHUDsList();
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (skinCounter++ >= 250) {
            updateSkins();
            skinCounter = 0;
        }
    }

    @SideOnly(Side.CLIENT)
    private void updateSkins() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null) return;

        Map<ResourceLocation, ITextureObject> map = ReflectionUtils.getTextures(mc.renderEngine);
        for (EntityPlayer player : mc.world.playerEntities) {
            if (player instanceof EntityOtherPlayerMP) {
                map.put(AbstractClientPlayer.getLocationSkin(player.getName()), map.get(((EntityOtherPlayerMP) player).getLocationSkin()));
            }
            else if (player instanceof EntityPlayerSP) {
                map.put(AbstractClientPlayer.getLocationSkin(player.getName()), map.get(((EntityPlayerSP) player).getLocationSkin()));
            }
        }
    }

    @SubscribeEvent
    public void onStateChange(StateChangedEvent event) {
        for (EntityPlayer player : FMLCommonHandler
                .instance()
                .getMinecraftServerInstance()
                .getPlayerList()
                .getPlayers()) {
            ICharacter character = Character.get(player);

            if (character != null && (event.isGlobal() || character.getStates() == event.states)) {
                int i = 0;

                for (Quest quest : character.getQuests().quests.values()) {
                    i += quest.stateWasUpdated(player) ? 1 : 0;
                }

                if (i > 0) {
                    this.playersToCheck.add(player);
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        MappetNpcRespawnManager.get(event.world).onTick();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (!Minecraft.getMinecraft().gameSettings.keyBindJump.isPressed()) return;

        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player.isRiding() && player.getRidingEntity() instanceof EntityNpc && ((EntityNpc) player.getRidingEntity()).getState().canBeSteered.get()) {
            float jumpPower = ((EntityNpc) player.getRidingEntity()).getState().jumpPower.get();
            Dispatcher.sendToServer(new PacketNpcJump(player.getRidingEntity().getEntityId(), jumpPower));
        }
    }
}
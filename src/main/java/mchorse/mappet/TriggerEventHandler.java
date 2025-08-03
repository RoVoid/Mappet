package mchorse.mappet;

import mchorse.mappet.api.scripts.code.entities.ScriptEntityItem;
import mchorse.mappet.api.scripts.code.entities.ScriptPlayer;
import mchorse.mappet.api.scripts.code.items.ScriptInventory;
import mchorse.mappet.api.scripts.code.items.ScriptItemStack;
import mchorse.mappet.api.triggers.Trigger;
import mchorse.mappet.api.utils.DataContext;
import mchorse.mappet.capabilities.character.Character;
import mchorse.mappet.capabilities.character.ICharacter;
import mchorse.mappet.entities.EntityNpc;
import mchorse.mappet.events.StateChangedEvent;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.common.scripts.PacketClick;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TriggerEventHandler {

    private static Set<Class<? extends Event>> registeredEvents = new HashSet<>();

    public void trigger(Event event, Trigger trigger, DataContext context) {
        context.set("event", event);
        trigger.trigger(context);
        if (event.isCancelable() && context.isCanceled()) {
            if (event instanceof LivingEquipmentChangeEvent || event instanceof TickEvent)
                return; //otherwise game crashes
            event.setCanceled(true);
        }
    }

    public boolean shouldSkipTrigger(Trigger trigger) {
        return Mappet.settings == null || trigger == null || trigger.isEmpty();
    }

    @SubscribeEvent
    public void onAnyEvent(Event event) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

        if (server == null || Mappet.settings == null || !Mappet.enableForgeTriggers.get()) return;

        if (event instanceof TickEvent && ((TickEvent) event).side == Side.CLIENT) return;
        if (event instanceof EntityEvent && (((EntityEvent) event).getEntity() == null || ((EntityEvent) event).getEntity().world.isRemote))
            return;
        if (event instanceof WorldEvent && ((WorldEvent) event).getWorld().isRemote) return;

        String name = getEventClassName(event.getClass());
        Trigger trigger = Mappet.settings.forgeTriggers.get(name);

        if (trigger == null || trigger.isEmpty()) return;
        trigger(event, trigger, new DataContext(server));
    }

    public static String getEventClassName(Class<? extends Event> clazz) {
        return clazz.getName().replace("$", ".");
    }

    public static Set<Class<? extends Event>> getRegisteredEvents() {
        if (!Mappet.enableForgeTriggers.get()) return new HashSet<>();
        if (registeredEvents == null || registeredEvents.isEmpty()) {
            registeredEvents = new Reflections()
                    .getSubTypesOf(Event.class)
                    .stream()
                    .filter(clazz -> !FMLNetworkEvent.class.isAssignableFrom(clazz))
                    .filter(clazz -> !TextureStitchEvent.class.isAssignableFrom(clazz))
                    .filter(clazz -> clazz != Event.class && clazz != CommandEvent.class)
                    .collect(Collectors.toSet());
        }
        return registeredEvents;
    }

    @SubscribeEvent
    public void onPlayerChat(ServerChatEvent event) {
        if (shouldSkipTrigger(Mappet.settings.playerChat)) return;
        DataContext context = new DataContext(event.getPlayer()).set("message", event.getMessage());
        trigger(event, Mappet.settings.playerChat, context);
    }

    @SubscribeEvent
    public void onPlayerBreakBlock(BlockEvent.BreakEvent event) {
        if (shouldSkipTrigger(Mappet.settings.blockBreak)) return;
        IBlockState state = event.getState();
        ResourceLocation id = state.getBlock().getRegistryName();
        if (id == null) return;
        DataContext context = new DataContext(event.getPlayer())
                .set("block", id.toString())
                .set("meta", state.getBlock().getMetaFromState(state))
                .set("position", event.getPos());
        trigger(event, Mappet.settings.blockBreak, context);
    }

    @SubscribeEvent
    public void onPlayerPlaceBlock(BlockEvent.PlaceEvent event) {
        if (event.isCanceled()) return;
        if (shouldSkipTrigger(Mappet.settings.blockPlace)) return;
        IBlockState state = event.getPlacedBlock();
        ResourceLocation id = state.getBlock().getRegistryName();
        if (id == null) return;
        DataContext context = new DataContext(event.getPlayer())
                .set("block", id.toString())
                .set("meta", state.getBlock().getMetaFromState(state))
                .set("position", event.getPos());
        trigger(event, Mappet.settings.blockPlace, context);
    }

    @SubscribeEvent
    public void onEntityHurt(LivingDamageEvent event) {
        if (shouldSkipTrigger(Mappet.settings.entityDamaged)) return;
        DamageSource source = event.getSource();
        if (event.getEntity() == null || event.getEntity().world.isRemote) return;
        DataContext context = new DataContext(event.getEntityLiving(), source.getTrueSource())
                .set("damage", event.getAmount())
                .set("type", source.getDamageType())
                .set("unblockable", source.isUnblockable())
                .set("target", event.getEntityLiving())
                .set("attacker", source.getTrueSource());
        if (source.getImmediateSource() instanceof EntityLivingBase && source.getImmediateSource() != source.getTrueSource())
            context.set("source", source.getImmediateSource());
        trigger(event, Mappet.settings.entityDamaged, context);
    }

    @SubscribeEvent
    public void onEntityAttacked(LivingAttackEvent event) {
        if (shouldSkipTrigger(Mappet.settings.entityAttacked)) return;
        DamageSource source = event.getSource();
        if (!(source.getTrueSource() instanceof EntityLivingBase) || event.getEntity() == null || event.getEntity().world.isRemote)
            return;
        DataContext context = new DataContext(event.getEntityLiving(), source.getTrueSource())
                .set("damage", event.getAmount())
                .set("type", source.getDamageType())
                .set("unblockable", source.isUnblockable())
                .set("target", event.getEntityLiving())
                .set("attacker", source.getTrueSource());
        if (source.getImmediateSource() instanceof EntityLivingBase && source.getImmediateSource() != source.getTrueSource())
            context.set("source", source.getImmediateSource());
        trigger(event, Mappet.settings.entityAttacked, context);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onPlayerOpenOrCloseContainer(PlayerContainerEvent event) {
        Trigger trigger = event instanceof PlayerContainerEvent.Close ? Mappet.settings.playerCloseContainer : Mappet.settings.playerOpenContainer;
        if (shouldSkipTrigger(trigger)) return;

        DataContext context = new DataContext(event.getEntityPlayer());
        Container container = event.getContainer();
        IInventory inventory = null;

        if (container instanceof ContainerChest) {
            ContainerChest chest = (ContainerChest) container;
            if (chest.getLowerChestInventory() instanceof TileEntity) {
                context.set("position", ((TileEntity) chest.getLowerChestInventory()).getPos());
            }
            inventory = chest.getLowerChestInventory();
        }
        else if (container instanceof ContainerPlayer) {
            inventory = event.getEntityPlayer().inventory;
        }
        else {
            Field[] fields = container.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.getType().isAssignableFrom(IInventory.class)) {
                    try {
                        field.setAccessible(true);
                        inventory = (IInventory) field.get(container);
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        if (inventory != null) context.set("inventory", new ScriptInventory(inventory));
        trigger(event, trigger, context);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onPlayerLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        if (!event.getEntityPlayer().world.isRemote || shouldSkipTrigger(Mappet.settings.playerLeftClick)) return;
        Dispatcher.sendToServer(new PacketClick());
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onPlayerRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        if (!event.getEntityPlayer().world.isRemote || event.getHand() == EnumHand.OFF_HAND) return;
        if (shouldSkipTrigger(Mappet.settings.playerRightClick)) return;
        Dispatcher.sendToServer(new PacketClick(1));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if (shouldSkipTrigger(Mappet.settings.blockClick)) return;

        EntityPlayer player = event.getEntityPlayer();
        if (player.world.isRemote) return;

        DataContext context = new DataContext(player)
                .set("position", event.getPos())
                .set("hand", event.getHand() == EnumHand.MAIN_HAND ? "main" : "off");

        trigger(event, Mappet.settings.blockClick, context);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (shouldSkipTrigger(Mappet.settings.blockInteract)) return;

        EntityPlayer player = event.getEntityPlayer();
        if (player.world.isRemote) return;

        IBlockState state = event.getWorld().getBlockState(event.getPos());
        ResourceLocation id = state.getBlock().getRegistryName();
        if (id == null) return;
        DataContext context = new DataContext(player)
                .set("block", id.toString())
                .set("meta", state.getBlock().getMetaFromState(state))
                .set("position", event.getPos())
                .set("hand", event.getHand() == EnumHand.MAIN_HAND ? "main" : "off");

        trigger(event, Mappet.settings.blockInteract, context);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (shouldSkipTrigger(Mappet.settings.playerItemInteract)) return;

        EntityPlayer player = event.getEntityPlayer();
        if (player.world.isRemote) return;

        DataContext context = new DataContext(player)
                .set("position", event.getPos())
                .set("hand", event.getHand() == EnumHand.MAIN_HAND ? "main" : "off");

        trigger(event, Mappet.settings.playerItemInteract, context);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerInteractWithEntity(PlayerInteractEvent.EntityInteract event) {
        if (shouldSkipTrigger(Mappet.settings.playerEntityInteract)) return;

        EntityPlayer player = event.getEntityPlayer();
        if (player.world.isRemote) return;

        DataContext context = new DataContext(player, event.getTarget())
                .set("hand", event.getHand() == EnumHand.MAIN_HAND ? "main" : "off")
                .set("target", event.getTarget());
        trigger(event, Mappet.settings.playerEntityInteract, context);
    }

    @SubscribeEvent
    public void onPlayerLogsIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player == null || shouldSkipTrigger(Mappet.settings.playerLogIn)) return;
        trigger(event, Mappet.settings.playerLogIn, new DataContext(event.player));
    }

    /**
     * WORKS ONLY ON DEDICATED SERVER
     */
    @SubscribeEvent
    public void onPlayerLogsOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (shouldSkipTrigger(Mappet.settings.playerLogOut)) return;
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null || !server.isDedicatedServer()) return;
        trigger(event, Mappet.settings.playerLogOut, new DataContext(event.player));
    }

    @SubscribeEvent
    public void onPlayerSpawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.player.world.isRemote || shouldSkipTrigger(Mappet.settings.playerRespawn)) return;
        trigger(event, Mappet.settings.playerRespawn, new DataContext(event.player));
    }

    @SubscribeEvent
    public void onPlayerPickUp(EntityItemPickupEvent event) {
        if (event.getEntityPlayer().world.isRemote || shouldSkipTrigger(Mappet.settings.playerItemPickup)) return;
        DataContext context = new DataContext(event.getEntityPlayer())
                .set("item", ScriptItemStack.create(event.getItem().getItem()))
                .set("entityItem", ScriptEntityItem.create(event.getItem()));
        trigger(event, Mappet.settings.playerItemPickup, context);
    }

    @SubscribeEvent
    public void onPlayerToss(ItemTossEvent event) {
        if (event.getPlayer().world.isRemote || shouldSkipTrigger(Mappet.settings.playerItemToss)) return;
        DataContext context = new DataContext(event.getPlayer()).set("entityItem", ScriptEntityItem.create(event.getEntityItem()));
        trigger(event, Mappet.settings.playerItemToss, context);
    }

    @SubscribeEvent
    public void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if (shouldSkipTrigger(Mappet.settings.playerJump)) return;
        if (event.getEntityLiving().world.isRemote || !(event.getEntityLiving() instanceof EntityPlayer)) return;
        trigger(event, Mappet.settings.playerJump, new DataContext(event.getEntityLiving()));
    }

    @SubscribeEvent
    public void onPlayerRun(LivingEvent.LivingUpdateEvent event) {
        if (shouldSkipTrigger(Mappet.settings.playerRun)) return;
        if (event.getEntityLiving().world.isRemote || !(event.getEntityLiving() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if (player.isDead || !player.isSprinting()) return;
        trigger(event, Mappet.settings.playerRun, new DataContext(event.getEntityLiving()));
    }

    @SubscribeEvent
    public void onPlayerMoving(LivingEvent.LivingUpdateEvent event) {
        if (shouldSkipTrigger(Mappet.settings.playerMove)) return;
        if (event.getEntityLiving().world.isRemote || !(event.getEntityLiving() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if (player.isDead || player.prevDistanceWalkedModified > player.distanceWalkedModified - 0.01) return;
        trigger(event, Mappet.settings.playerMove, new DataContext(event.getEntityLiving()));
    }

    @SubscribeEvent
    public void onMobKilled(LivingDeathEvent event) {
        if (event.getEntity().world.isRemote) return;

        Trigger trigger = event.getEntity() instanceof EntityPlayer ? Mappet.settings.playerDeath : Mappet.settings.entityDeath;
        if (shouldSkipTrigger(trigger)) return;

        DamageSource source = event.getSource();
        DataContext context = new DataContext(event.getEntityLiving(), source.getTrueSource())
                .set("type", source.getDamageType())
                .set("target", event.getEntityLiving())
                .set("attacker", source.getTrueSource());
        if (source.getTrueSource() != source.getImmediateSource()) {
            context.set("source", source.getImmediateSource());
        }
        if (source.getDamageLocation() != null) {
            context.set("position", new BlockPos(source.getDamageLocation()));
        }

        trigger(event, trigger, context);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) return;
        if (!shouldSkipTrigger(Mappet.settings.serverTick)) {
            trigger(event, Mappet.settings.serverTick, new DataContext(server));
        }
        if (!shouldSkipTrigger(Mappet.settings.playerTick)) {
            for (EntityPlayer player : server.getPlayerList().getPlayers()) {
                trigger(event, Mappet.settings.playerTick, new DataContext(player));
            }
        }
    }

    @SubscribeEvent
    public void onStateChange(StateChangedEvent event) {
        Trigger trigger = Mappet.settings.stateChanged;
        if (shouldSkipTrigger(Mappet.settings.stateChanged)) return;

        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) return;

        DataContext context = null;

        if (event.isGlobal()) context = new DataContext(server);
        else {
            for (EntityPlayer player : server.getPlayerList().getPlayers()) {
                ICharacter character = Character.get(player);
                if (character == null || character.getStates() != event.states) continue;
                context = new DataContext(player);
                break;
            }
            if (context == null) for (EntityNpc npc : getAllNpcs(server)) {
                if (npc == null || npc.getStates() != event.states) continue;
                context = new DataContext(npc);
                break;
            }
        }

        if (context == null) return;
        context.set("key", event.key).set("current", event.current).set("previous", event.previous);

        trigger(event, Mappet.settings.stateChanged, context);
    }

    private List<EntityNpc> getAllNpcs(MinecraftServer server) {
        List<EntityNpc> npcs = new ArrayList<>();
        try {
            for (World world : server.worlds) {
                for (Entity entity : world.loadedEntityList) {
                    if (entity instanceof EntityNpc) npcs.add((EntityNpc) entity);
                }
            }
        } catch (Exception e) {
            Mappet.logger.error("Failed to collect NPCs: " + e.getMessage());
        }
        return npcs;
    }


    @SubscribeEvent
    public void onLivingKnockBack(LivingKnockBackEvent event) {
        EntityLivingBase target = event.getEntityLiving();
        if (target != null && target.getEntityData().getBoolean("positionLocked")) event.setCanceled(true);

        if (target == null || target.world.isRemote || shouldSkipTrigger(Mappet.settings.livingKnockBack)) return;
        DataContext context = new DataContext(target, event.getAttacker())
                .set("strength", event.getStrength())
                .set("ratioX", event.getRatioX())
                .set("ratioZ", event.getRatioZ())
                .set("target", target)
                .set("attacker", event.getAttacker());
        trigger(event, Mappet.settings.livingKnockBack, context);
    }

    @SubscribeEvent
    public void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getEntity().world.isRemote || shouldSkipTrigger(Mappet.settings.projectileImpact)) return;

        Entity hitEntity = event.getRayTraceResult().entityHit;
        DataContext context = new DataContext(event.getEntity(), hitEntity)
                .set("position", event.getRayTraceResult().hitVec)
                .set("projectile", event.getEntity())
                .set("target", hitEntity);

        if (event.getEntity() instanceof EntityThrowable) {
            Entity thrower = ((EntityThrowable) event.getEntity()).getThrower();
            if (thrower != null) context.set("thrower", thrower);
        }

        trigger(event, Mappet.settings.projectileImpact, context);
    }

    @SubscribeEvent
    public void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity().world.isRemote || shouldSkipTrigger(Mappet.settings.onLivingEquipmentChange)) return;

        DataContext context = new DataContext(event.getEntity())
                .set("item", ScriptItemStack.create(event.getTo()))
                .set("previous", ScriptItemStack.create(event.getFrom()));

        if (event.getEntity() instanceof EntityPlayerMP) {
            ScriptPlayer player = new ScriptPlayer((EntityPlayerMP) event.getEntity());
            context.set("slot", player.getHotbarIndex());
        }
        else context.set("slot", event.getSlot().getIndex());

        trigger(event, Mappet.settings.onLivingEquipmentChange, context);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerLeashEntity(PlayerInteractEvent.EntityInteract event) {
        if (shouldSkipTrigger(Mappet.settings.playerEntityLeash)) return;

        EntityPlayer player = event.getEntityPlayer();
        ItemStack item = player.getHeldItem(event.getHand());

        if (player.world.isRemote || item.getItem() != Items.LEAD) return;

        Entity target = event.getTarget();
        if (!(target instanceof EntityLiving) || ((EntityLiving) target).getLeashed() || !((EntityLiving) target).canBeLeashedTo(player))
            return;

        DataContext context = new DataContext(player, target).set("hand", event.getHand() == EnumHand.MAIN_HAND ? "main" : "off");
        trigger(event, Mappet.settings.playerEntityLeash, context);
    }
}
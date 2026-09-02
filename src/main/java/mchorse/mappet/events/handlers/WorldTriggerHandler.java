package mchorse.mappet.events.handlers;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.scripts.code.entities.ScriptEntityItem;
import mchorse.mappet.api.scripts.code.entities.player.ScriptPlayer;
import mchorse.mappet.api.scripts.code.items.ScriptInventory;
import mchorse.mappet.api.scripts.code.items.ScriptItemStack;
import mchorse.mappet.api.triggers.Trigger;
import mchorse.mappet.api.utils.DataContext;
import mchorse.mappet.capabilities.character.Character;
import mchorse.mappet.capabilities.character.ICharacter;
import mchorse.mappet.config.MappetConfig;
import mchorse.mappet.entities.EntityNpc;
import mchorse.mappet.events.StateChangedEvent;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.packets.scripts.PacketClick;
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
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class WorldTriggerHandler {

    private static Set<Class<? extends Event>> registeredEvents = new HashSet<>();

    /**
     * @deprecated use {@link Trigger#triggerFrom(Event, DataContext)} directly.
     */
    @Deprecated
    public void trigger(Event event, Trigger trigger, DataContext context) {
        trigger.triggerFrom(event, context);
    }

    /**
     * @deprecated use {@link Trigger#shouldSkip(Trigger)} directly.
     */
    @Deprecated
    public boolean shouldSkipTrigger(Trigger trigger) {
        return Trigger.shouldSkip(trigger);
    }

    @SubscribeEvent
    public void onAnyEvent(Event event) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

        if (server == null || Mappet.settings == null || !MappetConfig.enableForgeTriggers.get()) return;

        if (event instanceof TickEvent && ((TickEvent) event).side == Side.CLIENT) return;
        if (event instanceof EntityEvent && (((EntityEvent) event).getEntity() == null || ((EntityEvent) event).getEntity().world.isRemote))
            return;
        if (event instanceof WorldEvent && ((WorldEvent) event).getWorld().isRemote) return;

        String name = getEventClassName(event.getClass());
        Trigger trigger = Mappet.settings.forgeTriggers.get(name);

        if (Trigger.shouldSkip(trigger)) return;
        trigger.triggerFrom(event, new DataContext(server));
    }

    public static String getEventClassName(Class<? extends Event> clazz) {
        return clazz.getName().replace("$", ".");
    }

    public static Set<Class<? extends Event>> getRegisteredEvents() {
        if (!MappetConfig.enableForgeTriggers.get()) return new HashSet<>();
        if (registeredEvents == null || registeredEvents.isEmpty()) {
            registeredEvents = new Reflections().getSubTypesOf(Event.class)
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
        if (Trigger.shouldSkip(Mappet.settings.playerChat)) return;
        DataContext context = new DataContext(event.getPlayer()).set("message", event.getMessage());
        Mappet.settings.playerChat.triggerFrom(event, context);
    }

    @SubscribeEvent
    public void onPlayerBreakBlock(BlockEvent.BreakEvent event) {
        if (Trigger.shouldSkip(Mappet.settings.blockBreak)) return;
        IBlockState state = event.getState();
        ResourceLocation id = state.getBlock().getRegistryName();
        if (id == null) return;
        DataContext context = new DataContext(event.getPlayer()).set("block", id.toString())
                .set("meta", state.getBlock().getMetaFromState(state))
                .set("position", event.getPos());
        Mappet.settings.blockBreak.triggerFrom(event, context);
    }

    @SubscribeEvent
    public void onPlayerPlaceBlock(BlockEvent.PlaceEvent event) {
        if (event.isCanceled()) return;
        if (Trigger.shouldSkip(Mappet.settings.blockPlace)) return;
        IBlockState state = event.getPlacedBlock();
        ResourceLocation id = state.getBlock().getRegistryName();
        if (id == null) return;
        DataContext context = new DataContext(event.getPlayer()).set("block", id.toString())
                .set("meta", state.getBlock().getMetaFromState(state))
                .set("position", event.getPos());
        Mappet.settings.blockPlace.triggerFrom(event, context);
    }

    @SubscribeEvent
    public void onEntityHurt(LivingDamageEvent event) {
        DamageSource source = event.getSource();
        if (event.getEntity() == null || event.getEntity().world.isRemote) return;
        if (Trigger.shouldSkip(Mappet.settings.entityDamaged)) return;
        DataContext context = new DataContext(event.getEntityLiving(), source.getTrueSource()).set("damage", event.getAmount())
                .set("type", source.getDamageType())
                .set("unblockable", source.isUnblockable());
        if (source.getImmediateSource() instanceof EntityLivingBase && source.getImmediateSource() != source.getTrueSource())
            context.set("source", source.getImmediateSource());
        Mappet.settings.entityDamaged.triggerFrom(event, context);
    }

    @SubscribeEvent
    public void onEntityAttacked(LivingAttackEvent event) {
        DamageSource source = event.getSource();
        if (!(source.getTrueSource() instanceof EntityLivingBase) || event.getEntity() == null || event.getEntity().world.isRemote) return;
        if (Trigger.shouldSkip(Mappet.settings.entityAttacked)) return;
        DataContext context = new DataContext(event.getEntityLiving(), source.getTrueSource()).set("damage", event.getAmount())
                .set("type", source.getDamageType())
                .set("unblockable", source.isUnblockable());
        if (source.getImmediateSource() instanceof EntityLivingBase && source.getImmediateSource() != source.getTrueSource())
            context.set("source", source.getImmediateSource());
        Mappet.settings.entityAttacked.triggerFrom(event, context);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onPlayerOpenOrCloseContainer(PlayerContainerEvent event) {
        Trigger trigger =
                event instanceof PlayerContainerEvent.Close ? Mappet.settings.playerCloseContainer : Mappet.settings.playerOpenContainer;
        if (Trigger.shouldSkip(trigger)) return;

        EntityPlayer player = event.getEntityPlayer();
        DataContext context = new DataContext(player);

        IInventory inventory = resolveInventory(event.getContainer(), player);
        if (inventory != null) {
            if (inventory instanceof TileEntity) context.set("position", ((TileEntity) inventory).getPos());
            context.set("inventory", new ScriptInventory(inventory));
        }

        trigger.triggerFrom(event, context);
    }

    private IInventory resolveInventory(Container container, EntityPlayer player) {
        if (container instanceof ContainerChest) return ((ContainerChest) container).getLowerChestInventory();
        if (container instanceof ContainerPlayer) return player.inventory;
        for (Field field : container.getClass().getDeclaredFields()) {
            if (IInventory.class.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    return (IInventory) field.get(container);
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }


    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onPlayerLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        if (!event.getEntityPlayer().world.isRemote) return;
        Dispatcher.sendToServer(new PacketClick());
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onPlayerRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        if (!event.getEntityPlayer().world.isRemote || event.getHand() == EnumHand.OFF_HAND) return;
        Dispatcher.sendToServer(new PacketClick(1));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player.world.isRemote) return;

        if (Trigger.shouldSkip(Mappet.settings.blockClick)) return;
        DataContext context = new DataContext(player).set("position", event.getPos())
                .set("hand", event.getHand() == EnumHand.MAIN_HAND ? "main" : "off");

        Mappet.settings.blockClick.triggerFrom(event, context);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player.world.isRemote) return;

        if (Trigger.shouldSkip(Mappet.settings.blockInteract)) return;
        IBlockState state = event.getWorld().getBlockState(event.getPos());
        ResourceLocation id = state.getBlock().getRegistryName();
        if (id == null) return;
        DataContext context = new DataContext(player).set("block", id.toString())
                .set("meta", state.getBlock().getMetaFromState(state))
                .set("position", event.getPos())
                .set("hand", event.getHand() == EnumHand.MAIN_HAND ? "main" : "off");

        Mappet.settings.blockInteract.triggerFrom(event, context);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player.world.isRemote) return;

        if (Trigger.shouldSkip(Mappet.settings.playerItemInteract)) return;
        DataContext context = new DataContext(player).set("position", event.getPos())
                .set("hand", event.getHand() == EnumHand.MAIN_HAND ? "main" : "off");

        Mappet.settings.playerItemInteract.triggerFrom(event, context);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerInteractWithEntity(PlayerInteractEvent.EntityInteract event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player.world.isRemote) return;

        if (Trigger.shouldSkip(Mappet.settings.playerEntityInteract)) return;
        DataContext context = new DataContext(player, event.getTarget()).set("hand", event.getHand() == EnumHand.MAIN_HAND ? "main" : "off");
        Mappet.settings.playerEntityInteract.triggerFrom(event, context);
    }

    @SubscribeEvent
    public void onPlayerLogsIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player == null || Trigger.shouldSkip(Mappet.settings.playerLogIn)) return;
        Mappet.settings.playerLogIn.triggerFrom(event, new DataContext(event.player));
    }

    /**
     * WORKS ONLY ON DEDICATED SERVER
     */
    @SubscribeEvent
    @SideOnly(Side.SERVER)
    public void onPlayerLogsOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (Trigger.shouldSkip(Mappet.settings.playerLogOut)) return;
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null || !server.isDedicatedServer()) return;
        Mappet.settings.playerLogOut.triggerFrom(event, new DataContext(event.player));
    }

    @SubscribeEvent
    public void onPlayerSpawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.player.world.isRemote || Trigger.shouldSkip(Mappet.settings.playerRespawn)) return;
        Mappet.settings.playerRespawn.triggerFrom(event, new DataContext(event.player));
    }

    @SubscribeEvent
    public void onPlayerPickUp(EntityItemPickupEvent event) {
        if (event.getEntityPlayer().world.isRemote || Trigger.shouldSkip(Mappet.settings.playerItemPickup)) return;
        DataContext context = new DataContext(event.getEntityPlayer()).set("item", ScriptItemStack.create(event.getItem().getItem()))
                .set("entityItem", ScriptEntityItem.create(event.getItem()));
        Mappet.settings.playerItemPickup.triggerFrom(event, context);
    }

    @SubscribeEvent
    public void onPlayerToss(ItemTossEvent event) {
        if (event.getPlayer().world.isRemote || Trigger.shouldSkip(Mappet.settings.playerItemToss)) return;
        DataContext context = new DataContext(event.getPlayer()).set("entityItem", ScriptEntityItem.create(event.getEntityItem()));
        Mappet.settings.playerItemToss.triggerFrom(event, context);
    }

    @SubscribeEvent
    public void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntityLiving().world.isRemote || !(event.getEntityLiving() instanceof EntityPlayer)) return;
        if (Trigger.shouldSkip(Mappet.settings.playerJump)) return;
        Mappet.settings.playerJump.triggerFrom(event, new DataContext(event.getEntityLiving()));
    }

    @SubscribeEvent
    public void onPlayerRun(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntityLiving().world.isRemote || !(event.getEntityLiving() instanceof EntityPlayer)) return;
        if (Trigger.shouldSkip(Mappet.settings.playerRun)) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if (player.isDead || !player.isSprinting()) return;
        Mappet.settings.playerRun.triggerFrom(event, new DataContext(player));
    }

    @SubscribeEvent
    public void onPlayerMove(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntityLiving().world.isRemote || !(event.getEntityLiving() instanceof EntityPlayer)) return;
        if (Trigger.shouldSkip(Mappet.settings.playerMove)) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if (player.isDead || player.prevDistanceWalkedModified > player.distanceWalkedModified - 0.01) return;
        DataContext context = new DataContext(player).set("distance", player.distanceWalkedModified);
        Mappet.settings.playerMove.triggerFrom(event, context);
    }

    @SubscribeEvent
    public void onMobKilled(LivingDeathEvent event) {
        if (event.getEntity().world.isRemote) return;

        Trigger trigger = event.getEntity() instanceof EntityPlayer ? Mappet.settings.playerDeath : Mappet.settings.entityDeath;
        if (Trigger.shouldSkip(trigger)) return;

        DamageSource source = event.getSource();
        DataContext context = new DataContext(event.getEntityLiving(), source.getTrueSource()).set("type", source.getDamageType());
        if (source.getTrueSource() != source.getImmediateSource()) {
            context.set("source", source.getImmediateSource());
        }

        trigger.triggerFrom(event, context);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) return;
        if (!Trigger.shouldSkip(Mappet.settings.serverTick)) {
            Mappet.settings.serverTick.triggerFrom(event, new DataContext(server));
        }
        if (!Trigger.shouldSkip(Mappet.settings.playerTick)) {
            for (EntityPlayer player : server.getPlayerList().getPlayers()) {
                Mappet.settings.playerTick.triggerFrom(event, new DataContext(player));
            }
        }
    }

    @SubscribeEvent
    public void onStateChange(StateChangedEvent event) {
        if (Trigger.shouldSkip(Mappet.settings.stateChanged)) return;

        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) return;

        DataContext context = null;

        if (Mappet.states.owns(event.states)) context = new DataContext(server);
        else {
            Object owner = event.states.owner;
            if (owner instanceof EntityPlayer) {
                ICharacter character = Character.get((EntityPlayer) owner);
                if (character != null) context = new DataContext((EntityPlayer) owner);
            }
            else if (owner instanceof EntityNpc) context = new DataContext((EntityNpc) owner);
        }

        if (context == null) return;
        context.set("key", event.key).set("current", event.current).set("previous", event.previous);

        Mappet.settings.stateChanged.triggerFrom(event, context);
    }

    @SubscribeEvent
    public void onLivingKnockBack(LivingKnockBackEvent event) {
        EntityLivingBase target = event.getEntityLiving();
        if (target != null && target.getEntityData().getBoolean("positionLocked")) event.setCanceled(true);

        if (target == null || target.world.isRemote || Trigger.shouldSkip(Mappet.settings.livingKnockBack)) return;
        DataContext context = new DataContext(target, event.getAttacker()).set("strength", event.getStrength())
                .set("ratioX", event.getRatioX())
                .set("ratioZ", event.getRatioZ());
        Mappet.settings.livingKnockBack.triggerFrom(event, context);
    }

    @SubscribeEvent
    public void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getEntity().world.isRemote || Trigger.shouldSkip(Mappet.settings.projectileImpact)) return;

        Entity hitEntity = event.getRayTraceResult().entityHit;
        DataContext context = new DataContext(hitEntity, event.getEntity()).set("position", event.getRayTraceResult().hitVec);

        if (event.getEntity() instanceof EntityThrowable) {
            Entity thrower = ((EntityThrowable) event.getEntity()).getThrower();
            if (thrower != null) context.set("thrower", thrower);
        }

        Mappet.settings.projectileImpact.triggerFrom(event, context);
    }

    @SubscribeEvent
    public void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity().world.isRemote || Trigger.shouldSkip(Mappet.settings.onLivingEquipmentChange)) return;

        DataContext context = new DataContext(event.getEntity()).set("item", ScriptItemStack.create(event.getTo()))
                .set("previous", ScriptItemStack.create(event.getFrom()));

        if (event.getEntity() instanceof EntityPlayerMP) {
            ScriptPlayer player = new ScriptPlayer((EntityPlayerMP) event.getEntity());
            context.set("slot", player.getHotbarIndex());
        }
        else context.set("slot", event.getSlot().getIndex());

        Mappet.settings.onLivingEquipmentChange.triggerFrom(event, context);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerLeashEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntityLiving().world.isRemote || Trigger.shouldSkip(Mappet.settings.playerEntityLeash)) return;

        EntityPlayer player = event.getEntityPlayer();
        ItemStack item = player.getHeldItem(event.getHand());

        if (item.getItem() != Items.LEAD) return;

        Entity target = event.getTarget();
        if (!(target instanceof EntityLiving) || ((EntityLiving) target).getLeashed() || !((EntityLiving) target).canBeLeashedTo(player)) return;

        DataContext context = new DataContext(player, target).set("hand", event.getHand() == EnumHand.MAIN_HAND ? "main" : "off");
        Mappet.settings.playerEntityLeash.triggerFrom(event, context);
    }
}

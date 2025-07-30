package mchorse.mappet.api;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.hotkeys.Hotkeys;
import mchorse.mappet.api.triggers.Trigger;
import mchorse.mappet.events.RegisterServerTriggerEvent;
import mchorse.mappet.utils.NBTToJsonLike;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global server settings
 */
public class ServerSettings implements INBTSerializable<NBTTagCompound> {
    private final File file;

    public final Map<String, Trigger> triggers = new LinkedHashMap<>();

    public final Map<String, Trigger> forgeTriggers = new LinkedHashMap<>();

    public final Hotkeys hotkeys = new Hotkeys();

    public final Trigger blockBreak = register("block_break");
    public final Trigger blockPlace = register("block_place");
    public final Trigger blockInteract = register("block_interact");
    public final Trigger blockClick = register("block_click");

    public final Trigger entityDamaged = register("entity_damaged");
    public final Trigger entityAttacked = register("entity_attacked");
    public final Trigger entityDeath = register("entity_death");

    public final Trigger serverLoad = register("server_load");
    public final Trigger serverTick = register("server_tick");

    public final Trigger playerTick = register("player_tick");
    public final Trigger playerChat = register("player_chat");
    public final Trigger playerLogIn = register("player_login");
    public final Trigger playerLogOut = register("player_logout");
    public final Trigger playerLeftClick = register("player_lmb");
    public final Trigger playerRightClick = register("player_rmb");
    public final Trigger playerRespawn = register("player_respawn");
    public final Trigger playerDeath = register("player_death");
    public final Trigger playerItemPickup = register("player_item_pickup");
    public final Trigger playerItemToss = register("player_item_toss");
    public final Trigger playerItemInteract = register("player_item_interact");
    public final Trigger playerEntityInteract = register("player_entity_interact");
    public final Trigger playerCloseContainer = register("player_close_container");
    public final Trigger playerOpenContainer = register("player_open_container");
    public final Trigger playerJournal = register("player_journal");
    public final Trigger livingKnockBack = register("living_knockback");
    public final Trigger projectileImpact = register("projectile_impact");
    public final Trigger onLivingEquipmentChange = register("living_equipment_change");
    public final Trigger playerEntityLeash = register("player_entity_leash");
    public final Trigger playerJump = register("player_jump");
    public final Trigger playerRun = register("player_run");
    public final Trigger playerMove = register("player_move");

    public final Trigger stateChanged = register("state_changed");

    public Trigger register(String key, Trigger trigger) {
        if (triggers.containsKey(key)) {
            Mappet.logger.warning("Server trigger '" + key + "' is already registered!");
            return null;
        }
        triggers.put(key, trigger);
        return trigger;
    }

    public Trigger register(String key) {
        return register(key, new Trigger());
    }

    public ServerSettings(File file) {
        this.file = file;
        Mappet.EVENT_BUS.post(new RegisterServerTriggerEvent(this));
    }

    public void load() {
        if (file == null || !file.isFile()) return;

        try {
            deserializeNBT(NBTToJsonLike.read(file));
        } catch (Exception e) {
            Mappet.logger.error(e.getMessage());
        }
    }

    public void save() {
        try {
            NBTToJsonLike.write(file, serializeNBT());
        } catch (Exception e) {
            Mappet.logger.error(e.getMessage());
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();

        NBTTagCompound triggersNbt = new NBTTagCompound();
        for (Map.Entry<String, Trigger> entry : triggers.entrySet()) {
            writeTrigger(triggersNbt, entry.getKey(), entry.getValue());
        }
        if (!triggersNbt.hasNoTags()) tag.setTag("Triggers", triggersNbt);

        NBTTagCompound forgeTriggersNbt = new NBTTagCompound();
        for (Map.Entry<String, Trigger> entry : forgeTriggers.entrySet()) {
            this.writeTrigger(forgeTriggersNbt, entry.getKey(), entry.getValue());
        }
        if (!forgeTriggersNbt.hasNoTags()) tag.setTag("ForgeTriggers", forgeTriggersNbt);

        tag.setTag("Hotkeys", hotkeys.serializeNBT());

        return tag;
    }

    private void writeTrigger(NBTTagCompound tag, String key, Trigger trigger) {
        if (trigger == null) return;

        NBTTagCompound triggerTag = trigger.serializeNBT();
        if (!triggerTag.hasNoTags()) tag.setTag(key, triggerTag);
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        if (tag.hasKey("Triggers")) {
            NBTTagCompound triggersNbt = tag.getCompoundTag("Triggers");
            for (Map.Entry<String, Trigger> entry : triggers.entrySet()) {
                readTrigger(triggersNbt, entry.getKey(), entry.getValue());
            }
        }

        forgeTriggers.clear();
        if (tag.hasKey("ForgeTriggers")) {
            NBTTagCompound forgeTriggersNbt = tag.getCompoundTag("ForgeTriggers");
            for (String key : forgeTriggersNbt.getKeySet()) {
                Trigger trigger = new Trigger();
                trigger.deserializeNBT(forgeTriggersNbt.getCompoundTag(key));
                forgeTriggers.put(key, trigger);
            }
        }

        hotkeys.deserializeNBT(tag.getTagList("Hotkeys", Constants.NBT.TAG_COMPOUND));
    }

    private void readTrigger(NBTTagCompound tag, String key, Trigger trigger) {
        if (!tag.hasKey(key, Constants.NBT.TAG_COMPOUND)) return;

        NBTTagCompound triggerTag = tag.getCompoundTag(key);
        if (!triggerTag.hasNoTags()) trigger.deserializeNBT(triggerTag);
    }
}
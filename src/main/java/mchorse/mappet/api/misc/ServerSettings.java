package mchorse.mappet.api.misc;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.misc.hotkeys.TriggerHotkeys;
import mchorse.mappet.api.triggers.Trigger;
import mchorse.mappet.events.RegisterServerTriggerEvent;
import mchorse.mappet.utils.NBTToJsonLike;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global server settings
 */
public class ServerSettings implements INBTSerializable<NBTTagCompound> {
    private final File file;

    private final Map<String, String> keyToAlias = new HashMap<>();

    public final Map<String, Trigger> registered = new LinkedHashMap<>();

    public final Map<String, Trigger> registeredForgeTriggers = new LinkedHashMap<>();

    public final TriggerHotkeys hotkeys = new TriggerHotkeys();

    public final Trigger blockBreak;

    public final Trigger blockPlace;

    public final Trigger blockInteract;

    public final Trigger blockClick;

    public final Trigger entityDamaged;

    public final Trigger entityAttacked;

    public final Trigger entityDeath;

    public final Trigger serverLoad;

    public final Trigger serverTick;

    /* Player triggers */
    public final Trigger playerTick;
    public final Trigger playerChat;

    public final Trigger playerLogIn;

    public final Trigger playerLogOut;

    public final Trigger playerLeftClick;

    public final Trigger playerRightClick;

    public final Trigger playerRespawn;

    public final Trigger playerDeath;

    public final Trigger playerItemPickup;

    public final Trigger playerItemToss;

    public final Trigger playerItemInteract;

    public final Trigger playerEntityInteract;

    public final Trigger playerCloseContainer;

    public final Trigger playerOpenContainer;

    public final Trigger playerJournal;

    public final Trigger livingKnockBack;

    public final Trigger projectileImpact;

    public final Trigger onLivingEquipmentChange;

    public final Trigger playerEntityLeash;

    public final Trigger playerJump;
    public final Trigger playerRun;

    public final Trigger stateChanged;

    public Trigger register(String key, Trigger trigger) {
        return this.register(key, null, trigger);
    }

    public Trigger register(String key, String alias, Trigger trigger) {
        if (this.registered.containsKey(key)) {
            throw new IllegalStateException("Server trigger '" + key + "' is already registered!");
        }

        if (alias != null) {
            this.keyToAlias.put(key, alias);
        }

        this.registered.put(key, trigger);

        return trigger;
    }

    public ServerSettings(File file) {
        this.file = file;

        this.blockBreak = this.register("block_break", "break_block", new Trigger());
        this.blockPlace = this.register("block_place", "place_block", new Trigger());
        this.blockInteract = this.register("block_interact", "interact_block", new Trigger());
        this.blockClick = this.register("block_click", new Trigger());
        this.entityDamaged = this.register("entity_damaged", "damage_entity", new Trigger());
        this.entityAttacked = this.register("entity_attacked", "attack_entity", new Trigger());
        this.entityDeath = this.register("entity_death", new Trigger());
        this.serverLoad = this.register("server_load", new Trigger());
        this.serverTick = this.register("server_tick", new Trigger());

        this.playerTick = this.register("player_tick", new Trigger());
        this.playerChat = this.register("player_chat", "chat", new Trigger());
        this.playerLogIn = this.register("player_login", new Trigger());
        this.playerLogOut = this.register("player_logout", new Trigger());
        this.playerLeftClick = this.register("player_lmb", new Trigger());
        this.playerRightClick = this.register("player_rmb", new Trigger());
        this.playerRespawn = this.register("player_respawn", new Trigger());
        this.playerDeath = this.register("player_death", new Trigger());
        this.playerItemPickup = this.register("player_item_pickup", new Trigger());
        this.playerItemToss = this.register("player_item_toss", new Trigger());
        this.playerItemInteract = this.register("player_item_interact", new Trigger());
        this.playerEntityInteract = this.register("player_entity_interact", new Trigger());
        this.playerCloseContainer = this.register("player_close_container", new Trigger());
        this.playerOpenContainer = this.register("player_open_container", new Trigger());
        this.playerJournal = this.register("player_journal", new Trigger());
        this.livingKnockBack = this.register("living_knockback", new Trigger());
        this.projectileImpact = this.register("projectile_impact", new Trigger());
        this.onLivingEquipmentChange = this.register("living_equipment_change", new Trigger());
        this.playerEntityLeash = this.register("player_entity_leash", new Trigger());
        this.playerJump = this.register("player_jump", new Trigger());
        this.playerRun = this.register("player_run", new Trigger());
        this.stateChanged = this.register("state_changed", new Trigger());

        Mappet.EVENT_BUS.post(new RegisterServerTriggerEvent(this));
    }

    /* Deserialization / Serialization */

    public void load() {
        if (this.file == null || !this.file.isFile()) {
            return;
        }

        try {
            NBTTagCompound tag = NBTToJsonLike.read(this.file);

            if (!tag.hasKey("Hotkeys")) {
                /* Backward compatibility with beta */
                File hotkeys = new File(this.file.getParentFile(), "hotkeys.json");

                if (hotkeys.isFile()) {
                    try {
                        NBTTagCompound hotkeysTag = NBTToJsonLike.read(hotkeys);

                        tag.setTag("Hotkeys", hotkeysTag);
                        hotkeys.delete();
                    } catch (Exception ignored) {
                    }
                }
            }

            this.deserializeNBT(tag);
        } catch (Exception e) {
            Mappet.logger.error(e.getMessage());
        }
    }

    public void save() {
        try {
            NBTToJsonLike.write(this.file, this.serializeNBT());
        } catch (Exception e) {
            Mappet.logger.error(e.getMessage());
        }
    }

    /* NBT */

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound triggers = new NBTTagCompound();

        for (Map.Entry<String, Trigger> entry : this.registered.entrySet()) {
            this.writeTrigger(triggers, entry.getKey(), entry.getValue());
        }

        if (!triggers.hasNoTags()) {
            tag.setTag("Triggers", triggers);
        }

        NBTTagCompound forgeTriggers = new NBTTagCompound();

        for (Map.Entry<String, Trigger> entry : this.registeredForgeTriggers.entrySet()) {
            this.writeTrigger(forgeTriggers, entry.getKey(), entry.getValue());
        }

        if (!forgeTriggers.hasNoTags()) {
            tag.setTag("ForgeTriggers", forgeTriggers);
        }

        tag.setTag("Hotkeys", this.hotkeys.serializeNBT());

        return tag;
    }

    private void writeTrigger(NBTTagCompound tag, String key, Trigger trigger) {
        if (trigger != null) {
            NBTTagCompound triggerTag = trigger.serializeNBT();

            if (!triggerTag.hasNoTags()) {
                tag.setTag(key, triggerTag);
            }
        }
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        if (tag.hasKey("Triggers")) {
            NBTTagCompound triggers = tag.getCompoundTag("Triggers");

            for (Map.Entry<String, Trigger> entry : this.registered.entrySet()) {
                String oldAlias = this.keyToAlias.get(entry.getKey());

                if (triggers.hasKey(oldAlias, Constants.NBT.TAG_COMPOUND)) {
                    this.readTrigger(triggers, oldAlias, entry.getValue());
                } else {
                    this.readTrigger(triggers, entry.getKey(), entry.getValue());
                }
            }
        }

        this.registeredForgeTriggers.clear();

        if (tag.hasKey("ForgeTriggers")) {
            NBTTagCompound forgeTriggers = tag.getCompoundTag("ForgeTriggers");

            for (String key : forgeTriggers.getKeySet()) {
                Trigger trigger = new Trigger();
                trigger.deserializeNBT(forgeTriggers.getCompoundTag(key));
                this.registeredForgeTriggers.put(key, trigger);
            }
        }

        if (tag.hasKey("Hotkeys")) {
            this.hotkeys.deserializeNBT(tag.getCompoundTag("Hotkeys"));
        }
    }

    private void readTrigger(NBTTagCompound tag, String key, Trigger trigger) {
        if (tag.hasKey(key, Constants.NBT.TAG_COMPOUND)) {
            NBTTagCompound triggerTag = tag.getCompoundTag(key);

            if (!triggerTag.hasNoTags()) {
                trigger.deserializeNBT(triggerTag);
            }
        }
    }
}
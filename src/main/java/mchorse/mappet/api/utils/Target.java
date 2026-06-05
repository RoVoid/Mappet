package mchorse.mappet.api.utils;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.states.QuestStates;
import mchorse.mappet.api.states.StatesProvider;
import mchorse.mappet.capabilities.character.Character;
import mchorse.mappet.capabilities.character.ICharacter;
import mchorse.mappet.utils.EntityUtils;
import mchorse.mappet.utils.EnumUtils;
import net.minecraft.command.EntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public class Target implements INBTSerializable<NBTTagCompound> {
    public TargetMode mode;
    public String selector = "";

    private final TargetMode defaultMode;

    public Target(TargetMode mode) {
        this.mode = defaultMode = mode;
    }

    public EntityPlayer getPlayer(DataContext context) {
        if (mode == TargetMode.SUBJECT && context.subject instanceof EntityPlayer) return (EntityPlayer) context.subject;
        if (mode == TargetMode.OBJECT && context.object instanceof EntityPlayer) return (EntityPlayer) context.object;
        if (mode == TargetMode.PLAYER) return context.getPlayer();
        if (mode == TargetMode.SELECTOR) {
            try {
                return EntitySelector.matchOnePlayer(context.getSender(), selector);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public Entity getEntity(DataContext context) {
        if (mode == TargetMode.SUBJECT && context.subject != null) return context.subject;
        if (mode == TargetMode.OBJECT && context.object != null) return context.object;
        if (mode == TargetMode.PLAYER) return context.getPlayer();
        if (mode == TargetMode.NPC) return context.getNpc();
        if (mode == TargetMode.SELECTOR) {
            try {
                return EntitySelector.matchOneEntity(context.getSender(), selector, Entity.class);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public ICharacter getCharacter(DataContext context) {
        return Character.get(getPlayer(context));
    }

    public StatesProvider getStates(DataContext context) {
        return mode == TargetMode.GLOBAL ? Mappet.states : EntityUtils.getStates(getEntity(context));
    }

    public static StatesProvider getStates(Entity entity, TargetMode mode) {
        return mode == TargetMode.GLOBAL ? Mappet.states : EntityUtils.getStates(entity);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setInteger("Target", mode.ordinal());
        tag.setString("Selector", selector);

        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        mode = EnumUtils.getValue(tag.getInteger("Target"), TargetMode.values(), defaultMode);
        selector = tag.getString("Selector");
    }
}
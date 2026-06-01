package mchorse.mappet.utils;

import mchorse.mappet.api.conditions.Condition;
import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public class ConditionModel implements INBTSerializable<NBTTagCompound> {
    public AbstractMorph morph;
    public Condition condition;

    public ConditionModel() {
        morph = getDefaultMorph();
        condition = new Condition();
    }

    public AbstractMorph getDefaultMorph() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Name", "blockbuster.fred");
        return MorphManager.INSTANCE.morphFromNBT(tag);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setTag("condition", condition.serializeNBT());

        NBTTagCompound morph = new NBTTagCompound();
        this.morph.toNBT(morph);
        tag.setTag("morph", morph);

        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        condition.deserializeNBT(nbt.getCompoundTag("condition"));
        morph = MorphManager.INSTANCE.morphFromNBT(nbt.getCompoundTag("morph"));
    }

    @Override
    public String toString() {
        return "ConditionModel[morph_name:" + morph.name + ",condition:" + condition.toString() + "]";
    }
}

package mchorse.mappet.api.conditions.blocks;

import mchorse.mappet.api.utils.DataContext;
import mchorse.mappet.api.utils.TargetMode;
import mchorse.mappet.capabilities.character.ICharacter;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DialogueConditionBlock extends TargetConditionBlock {
    public String marker = "";

    @Override
    public boolean evaluateBlock(DataContext context) {
        if (target.mode == TargetMode.GLOBAL) return false;
        ICharacter character = target.getCharacter(context);
        return character != null && character.getStates().dialogues.wasRead(id, marker);
    }

    @Override
    protected TargetMode getDefaultTarget() {
        return TargetMode.SUBJECT;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String name() {
        return I18n.format("mappet.gui.conditions.dialogue.was_read", id);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag) {
        super.serializeNBT(tag);
        tag.setString("Marker", marker);
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        super.deserializeNBT(tag);
        marker = tag.getString("Marker");
    }
}
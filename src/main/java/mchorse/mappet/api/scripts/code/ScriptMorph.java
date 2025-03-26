package mchorse.mappet.api.scripts.code;

import mchorse.mappet.api.scripts.user.nbt.INBTCompound;
import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.morphs.AbstractMorph;

public class ScriptMorph {
    AbstractMorph morph;

    public ScriptMorph(INBTCompound compound) {
        morph = MorphManager.INSTANCE.morphFromNBT(compound.getNBTTagCompound());
    }

    public AbstractMorph getMinecraftMorph() {
        return morph;
    }
}

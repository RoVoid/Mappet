package mchorse.mappet.api.scripts.code;

import mchorse.mappet.api.scripts.code.nbt.ScriptNBTCompound;
import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.morphs.AbstractMorph;

public class ScriptMorph {
    AbstractMorph morph;

    public ScriptMorph(ScriptNBTCompound compound) {
        morph = MorphManager.INSTANCE.morphFromNBT(compound.asMinecraft());
    }

    public AbstractMorph getMinecraftMorph() {
        return morph;
    }
}

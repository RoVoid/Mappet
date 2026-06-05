package mchorse.mappet.api.triggers.blocks;

import mchorse.mappet.api.states.ScriptStates;
import mchorse.mappet.api.utils.DataContext;
import mchorse.mappet.api.utils.Target;
import mchorse.mappet.api.utils.TargetMode;
import mchorse.mappet.utils.EnumUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

public class StateTriggerBlock extends StringTriggerBlock {
    public Target target = new Target(TargetMode.GLOBAL);
    public StateMode mode = StateMode.SET;
    public Object value = 0D;

    @Override
    public void trigger(DataContext context) {
        ScriptStates states = target.getStates(context).scripts;
        if (states == null) return;

        if (mode == StateMode.ADD && value instanceof Number) states.add(string, ((Number) value).doubleValue());
        else if (mode == StateMode.SET) {
            if (value instanceof Number) states.setNumber(string, ((Number) value).doubleValue());
            else if (value instanceof String) states.setString(string, (String) value);
        }
        else states.resetMasked(string);
    }

    @Override
    protected String getKey() {
        return "State";
    }

    @Override
    protected void serializeNBT(NBTTagCompound tag) {
        super.serializeNBT(tag);

        tag.setTag("Target", target.serializeNBT());
        tag.setInteger("Mode", mode.ordinal());

        if (value instanceof Number) tag.setDouble("Value", ((Number) value).doubleValue());
        else if (value instanceof String) tag.setString("Value", (String) value);
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        super.deserializeNBT(tag);

        target.deserializeNBT(tag.getCompoundTag("Target"));
        mode = EnumUtils.getValue(tag.getInteger("Mode"), StateMode.values(), StateMode.SET);

        if (tag.hasKey("Value", Constants.NBT.TAG_ANY_NUMERIC)) value = tag.getDouble("Value");
        else if (tag.hasKey("Value", Constants.NBT.TAG_STRING)) value = tag.getString("Value");
    }

    public enum StateMode {
        ADD, SET, REMOVE
    }
}
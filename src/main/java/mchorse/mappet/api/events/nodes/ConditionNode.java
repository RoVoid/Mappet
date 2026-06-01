package mchorse.mappet.api.events.nodes;

import mchorse.mappet.api.conditions.Condition;
import mchorse.mappet.api.events.EventContext;
import net.minecraft.nbt.NBTTagCompound;

public class ConditionNode extends EventBaseNode {
    public Condition condition = new Condition();

    public ConditionNode() {}

    @Override
    public int execute(EventContext context) {
        boolean result = condition.execute(context.data);
        context.log("The result of condition is " + result);
        return booleanToExecutionCode(result);

    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = super.serializeNBT();
        tag.setTag("Condition", condition.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        super.deserializeNBT(tag);
        if (tag.hasKey("Condition")) condition.deserializeNBT(tag.getCompoundTag("Condition"));
    }
}
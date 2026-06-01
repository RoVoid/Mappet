package mchorse.mappet.api.conditions.blocks;

import mchorse.mappet.api.utils.AbstractBlock;
import mchorse.mappet.api.utils.DataContext;
import mchorse.mappet.proxy.CommonProxy;
import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractConditionBlock extends AbstractBlock {
    public boolean not;
    public boolean or;

    public boolean evaluate(DataContext context) {
        return not != evaluateBlock(context);
    }

    protected abstract boolean evaluateBlock(DataContext context);

    @Override
    protected void serializeNBT(NBTTagCompound tag) {
        tag.setString("type", type());
        if (not) tag.setBoolean("Not", true);
        if (or) tag.setBoolean("Or", true);
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        not = tag.getBoolean("Not");
        or = tag.getBoolean("Or");
    }

    @Override
    public String type() {
        return CommonProxy.getConditionBlocks().type(this);
    }

    public NBTTagCompound toNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        serializeNBT(tag);
        return tag;
    }

    @Override
    public String toString() {
        return "AbstractConditionBlock[type:" + CommonProxy.getConditionBlocks().type(this) + "]";
    }
}
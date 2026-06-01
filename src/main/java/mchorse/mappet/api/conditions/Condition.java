package mchorse.mappet.api.conditions;

import mchorse.mappet.api.conditions.blocks.AbstractConditionBlock;
import mchorse.mappet.api.utils.DataContext;
import mchorse.mappet.proxy.CommonProxy;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;

public class Condition implements INBTSerializable<NBTTagCompound> {
    public final List<AbstractConditionBlock> blocks = new ArrayList<>();

    private final boolean defaultValue;

    public Condition() {
        this(false);
    }

    public Condition(boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean execute(DataContext context) {
        if (blocks.isEmpty()) return defaultValue;

        boolean result = blocks.get(0).evaluate(context);

        for (int i = 1; i < blocks.size(); i++) {
            AbstractConditionBlock block = blocks.get(i);
            boolean value = block.evaluate(context);
            result = block.or ? result || value : result && value;
        }

        return result;
    }

    public boolean isEmpty() {
        return blocks.isEmpty();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList blocks = new NBTTagList();

        for (AbstractConditionBlock block : this.blocks) {
            NBTTagCompound blockTag = block.serializeNBT();

            blockTag.setString("Type", CommonProxy.getConditionBlocks().type(block));
            blocks.appendTag(blockTag);
        }

        if (blocks.tagCount() > 0) tag.setTag("Blocks", blocks);

        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        NBTTagList blocks = tag.getTagList("Blocks", Constants.NBT.TAG_COMPOUND);

        this.blocks.clear();

        for (int i = 0; i < blocks.tagCount(); i++) {
            NBTTagCompound blockTag = blocks.getCompoundTagAt(i);
            AbstractConditionBlock block = CommonProxy.getConditionBlocks().create(blockTag.getString("Type"));

            if (block != null) {
                block.deserializeNBT(blockTag);
                this.blocks.add(block);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("mappet.condition[");
        for (AbstractConditionBlock block : blocks) {
            result.append(block.toString()).append(",");
        }
        result.append("]");
        return result.toString();
    }
}
package mchorse.mappet.api.triggers;

import mchorse.mappet.MappetFactories;
import mchorse.mappet.api.triggers.blocks.*;
import mchorse.mappet.api.utils.DataContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public class Trigger implements INBTSerializable<NBTTagCompound> {
    public Trigger() {

    }

    public Trigger(List<AbstractTriggerBlock> blocks) {
        this.blocks.addAll(blocks);
    }

    public final List<AbstractTriggerBlock> blocks = new ArrayList<>();

    private boolean empty = true;

    public void copy(Trigger trigger) {
        blocks.clear();

        for (AbstractTriggerBlock block : trigger.blocks) {
            String type = MappetFactories.getTriggerBlocks().type(block);
            AbstractTriggerBlock newBlock = MappetFactories.getTriggerBlocks().create(type);

            newBlock.deserializeNBT(block.serializeNBT());
            blocks.add(newBlock);
        }

        recalculateEmpty();
    }

    public void recalculateEmpty() {
        empty = true;
        for (AbstractTriggerBlock block : blocks)
            if (!block.isEmpty()) empty = false;
    }

    public void trigger(EntityLivingBase target) {
        trigger(new DataContext(target));
    }

    public void trigger(EntityLivingBase target, Entity entity) {
        trigger(new DataContext(target, entity));
    }

    public void trigger(DataContext context) {
        for (AbstractTriggerBlock block : blocks) {
            if (context.isCanceled()) return;
            block.triggerWithFrequency(context);
        }
    }

    public void triggerFrom(Event event, DataContext context) {
        context.set("event", event);
        trigger(context);

        if (event.isCancelable() && context.isCanceled()) {
            if (event instanceof LivingEquipmentChangeEvent || event instanceof TickEvent) return;
            event.setCanceled(true);
        }
    }

    public boolean isEmpty() {
        return empty;
    }

    public static boolean shouldSkip(Trigger trigger) {
        return trigger == null || trigger.isEmpty();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList blocks = new NBTTagList();

        for (AbstractTriggerBlock block : this.blocks) {
            NBTTagCompound blockTag = block.serializeNBT();

            blockTag.setString("Type", MappetFactories.getTriggerBlocks().type(block));
            blocks.appendTag(blockTag);
        }

        tag.setTag("Blocks", blocks);

        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        blocks.clear();

        /* Backward compatibility with alpha and beta builds */
        if (tag.hasKey("Sound")) blocks.add(new SoundTriggerBlock(tag.getString("Sound")));
        if (tag.hasKey("Trigger")) blocks.add(new EventTriggerBlock(tag.getString("Trigger")));
        if (tag.hasKey("Command")) blocks.add(new CommandTriggerBlock(tag.getString("Command")));
        if (tag.hasKey("Dialogue")) blocks.add(new DialogueTriggerBlock(tag.getString("Dialogue")));
        if (tag.hasKey("Script")) blocks.add(new ScriptTriggerBlock(tag.getString("Script"), tag.getString("ScriptFunction")));

        if (tag.hasKey("Blocks")) {
            NBTTagList blocks = tag.getTagList("Blocks", Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < blocks.tagCount(); i++) {
                NBTTagCompound blockTag = blocks.getCompoundTagAt(i);
                AbstractTriggerBlock block = MappetFactories.getTriggerBlocks().create(blockTag.getString("Type"));

                if (block != null) {
                    block.deserializeNBT(blockTag);
                    this.blocks.add(block);
                }
            }
        }

        recalculateEmpty();
    }
}

package mchorse.mappet.api.regions;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.conditions.Checker;
import mchorse.mappet.api.regions.shapes.AbstractShape;
import mchorse.mappet.api.regions.shapes.BoxShape;
import mchorse.mappet.api.states.States;
import mchorse.mappet.api.triggers.Trigger;
import mchorse.mappet.api.utils.DataContext;
import mchorse.mappet.api.utils.TargetMode;
import mchorse.mappet.utils.EntityUtils;
import mchorse.mappet.utils.EnumUtils;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;

public class Region implements INBTSerializable<NBTTagCompound> {
    public boolean passable = true;
    public boolean checkEntities = false;
    public Checker enabled = new Checker(true);
    public int delay;
    public int update = 3;
    public Trigger onEnter = new Trigger();
    public Trigger onExit = new Trigger();
    public Trigger onTick = new Trigger();

    public List<AbstractShape> shapes = new ArrayList<>();

    public States states = new States();

    public Region() {
        shapes.add(new BoxShape());
    }

    /* Automatic state writing */
    public boolean writeState;
    public String state = "";
    public TargetMode target = TargetMode.GLOBAL;
    public boolean additive = true;
    public boolean once;

    public boolean isEnabled(Entity entity) {
        if (once) {
            States states = getStates(entity);
            if (states != null && states.has(state)) return false;
        }

        return enabled.check(new DataContext(entity));
    }

    public boolean isPlayerInside(Entity entity, BlockPos pos) {
        for (AbstractShape shape : shapes)
            if (shape.isEntityInside(entity, pos)) return true;

        return false;
    }

    public boolean isPlayerOutside(double x, double y, double z, BlockPos pos) {
        for (AbstractShape shape : shapes)
            if (shape.isEntityInside(x, y, z, pos)) return false;
        return true;
    }

    public void triggerEnter(Entity entity, BlockPos pos) {
        if (writeState && !state.isEmpty()) {
            States states = getStates(entity);

            if (additive) states.add(state, 1);
            else states.setNumber(state, 1);
        }

        onEnter.trigger(new DataContext(entity).set("x", pos.getX()).set("y", pos.getY()).set("z", pos.getZ()));
    }

    public void triggerExit(Entity entity, BlockPos pos) {
        if (writeState && !state.isEmpty()) {
            States states = getStates(entity);

            if (!additive) states.reset(state);
        }

        onExit.trigger(new DataContext(entity).set("x", pos.getX()).set("y", pos.getY()).set("z", pos.getZ()));
    }

    public void triggerTick(Entity entity, BlockPos pos) {
        onTick.trigger(new DataContext(entity).set("x", pos.getX()).set("y", pos.getY()).set("z", pos.getZ()));
    }

    private States getStates(Entity entity) {
        return target == TargetMode.GLOBAL ? Mappet.states : EntityUtils.getStates(entity);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setBoolean("Passable", passable);
        tag.setTag("Enabled", enabled.serializeNBT());
        tag.setInteger("Delay", delay);
        tag.setInteger("Update", update);
        tag.setBoolean("CheckEntities", checkEntities);
        tag.setTag("OnEnter", onEnter.serializeNBT());
        tag.setTag("OnExit", onExit.serializeNBT());
        tag.setTag("OnTick", onTick.serializeNBT());

        NBTTagList shapes = new NBTTagList();

        for (AbstractShape shape : this.shapes) {
            NBTTagCompound shapeTag = shape.serializeNBT();

            shapeTag.setString("Type", shape.getType());
            shapes.appendTag(shapeTag);
        }

        tag.setTag("Shapes", shapes);
        tag.setBoolean("WriteState", writeState);
        tag.setString("State", state.trim());
        tag.setInteger("Target", target.ordinal());
        tag.setBoolean("Additive", additive);
        tag.setBoolean("Once", once);
        tag.setTag("States", states.serializeNBT());

        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        if (tag.hasKey("Passable")) passable = tag.getBoolean("Passable");

        if (tag.hasKey("Enabled", Constants.NBT.TAG_COMPOUND)) enabled.deserializeNBT(tag.getTag("Enabled"));

        if (tag.hasKey("Delay", Constants.NBT.TAG_ANY_NUMERIC)) delay = tag.getInteger("Delay");

        if (tag.hasKey("Update", Constants.NBT.TAG_ANY_NUMERIC)) update = tag.getInteger("Update");

        if (tag.hasKey("CheckEntities")) checkEntities = tag.getBoolean("CheckEntities");

        if (tag.hasKey("OnEnter", Constants.NBT.TAG_COMPOUND)) onEnter.deserializeNBT(tag.getCompoundTag("OnEnter"));

        if (tag.hasKey("OnExit", Constants.NBT.TAG_COMPOUND)) onExit.deserializeNBT(tag.getCompoundTag("OnExit"));

        if (tag.hasKey("OnTick", Constants.NBT.TAG_COMPOUND)) onTick.deserializeNBT(tag.getCompoundTag("OnTick"));

        if (tag.hasKey("States")) states.deserializeNBT(tag.getCompoundTag("States"));


        shapes.clear();

        if (tag.hasKey("Shape", Constants.NBT.TAG_COMPOUND)) {
            AbstractShape shape = readShape(tag.getCompoundTag("Shape"));

            if (shape != null) shapes.add(shape);
        }
        else if (tag.hasKey("Shapes", Constants.NBT.TAG_LIST)) {
            NBTTagList list = tag.getTagList("Shapes", Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < list.tagCount(); i++) {
                AbstractShape shape = readShape(list.getCompoundTagAt(i));

                if (shape != null) shapes.add(shape);
            }
        }

        if (shapes.isEmpty()) shapes.add(new BoxShape());

        writeState = tag.getBoolean("WriteState");
        state = tag.getString("State");
        target = EnumUtils.getValue(tag.getInteger("Target"), TargetMode.values(), TargetMode.GLOBAL);
        additive = tag.getBoolean("Additive");
        once = tag.getBoolean("Once");
    }

    private AbstractShape readShape(NBTTagCompound shapeTag) {
        if (shapeTag.hasKey("Type")) {
            AbstractShape shape = AbstractShape.fromString(shapeTag.getString("Type"));

            if (shape != null) {
                shape.deserializeNBT(shapeTag);

                return shape;
            }
        }

        return null;
    }
}
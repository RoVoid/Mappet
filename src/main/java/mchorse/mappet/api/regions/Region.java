package mchorse.mappet.api.regions;

import mchorse.mappet.api.conditions.Condition;
import mchorse.mappet.api.regions.shapes.AbstractShape;
import mchorse.mappet.api.regions.shapes.BoxShape;
import mchorse.mappet.api.states.ScriptStates;
import mchorse.mappet.api.states.States;
import mchorse.mappet.api.triggers.Trigger;
import mchorse.mappet.api.utils.DataContext;
import mchorse.mappet.api.utils.Target;
import mchorse.mappet.api.utils.TargetMode;
import mchorse.mappet.utils.EnumUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;

public class Region implements INBTSerializable<NBTTagCompound> {
    public boolean checkEntities = false;
    public Condition enabled = new Condition(true);

    public int delay;
    public int update = 3;
    public Trigger onEnter = new Trigger();
    public Trigger onExit = new Trigger();
    public Trigger onTick = new Trigger();

    public List<AbstractShape> shapes = new ArrayList<>();
    private AxisAlignedBB searchBox;

    public boolean writeState;
    public String state = "";
    public TargetMode target = TargetMode.GLOBAL;
    public StateMode stateMode = StateMode.ADDITIVE;

    public Region() {
        shapes.add(new BoxShape());
        updateSearchBox();
    }

    public void updateSearchBox() {
        searchBox = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
        if (shapes.isEmpty()) return;
        for (AbstractShape shape : shapes) {
            AxisAlignedBB sb = shape.getSearchBox();
            searchBox = searchBox.union(sb);
        }
        searchBox = searchBox.grow(4);
    }

    public AxisAlignedBB getSearchBox(BlockPos pos) {
        return searchBox.offset(pos);
    }

    public boolean isEnabled(Entity entity, boolean was) {
        if (stateMode == StateMode.ONCE && !was) {
            ScriptStates states = Target.getStates(entity, target).scripts;
            if (states != null && states.has(state)) return false;
        }

        return enabled.execute(new DataContext(entity));
    }

    public boolean isEntityInside(Entity entity, BlockPos pos) {
        if (entity instanceof EntityPlayer && ((EntityPlayer) entity).isSpectator()) return false;
        for (AbstractShape shape : shapes) if (shape.isEntityInside(entity, pos)) return true;
        return false;
    }

    public boolean isOutside(double x, double y, double z, BlockPos pos) {
        for (AbstractShape shape : shapes) if (shape.isInside(x, y, z, pos)) return false;
        return true;
    }

    public void triggerEnter(Entity entity, BlockPos pos) {
        if (writeState && !state.isEmpty()) {
            States states = Target.getStates(entity, target).scripts;
            if (stateMode == StateMode.TOGGLE) states.setNumber(state, 1);
            else states.add(state, 1);
        }

        onEnter.trigger(new DataContext(entity).set("x", pos.getX()).set("y", pos.getY()).set("z", pos.getZ()));
    }

    public void triggerExit(Entity entity, BlockPos pos) {
        if (writeState && stateMode == StateMode.TOGGLE && !state.isEmpty()) Target.getStates(entity, target).scripts.reset(state);
        onExit.trigger(new DataContext(entity).set("x", pos.getX()).set("y", pos.getY()).set("z", pos.getZ()));
    }

    public void triggerTick(Entity entity, BlockPos pos) {
        onTick.trigger(new DataContext(entity).set("x", pos.getX()).set("y", pos.getY()).set("z", pos.getZ()));
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setTag("Enabled", enabled.serializeNBT());
        tag.setBoolean("CheckEntities", checkEntities);

        tag.setInteger("Delay", delay);
        tag.setInteger("Update", update);

        tag.setTag("OnEnter", onEnter.serializeNBT());
        tag.setTag("OnExit", onExit.serializeNBT());
        tag.setTag("OnTick", onTick.serializeNBT());

        NBTTagList shapes = new NBTTagList();
        for (AbstractShape shape : this.shapes) shapes.appendTag(shape.serializeNBT());
        tag.setTag("Shapes", shapes);

        tag.setInteger("Target", target.ordinal());

        if (writeState) tag.setBoolean("WriteState", true);
        if (!state.trim().isEmpty()) tag.setString("State", state.trim());
        tag.setInteger("StateMode", stateMode.ordinal());

        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        if (tag == null) return;

        if (tag.hasKey("Enabled", Constants.NBT.TAG_COMPOUND)) enabled.deserializeNBT(tag.getCompoundTag("Enabled"));
        if (tag.hasKey("Delay", Constants.NBT.TAG_ANY_NUMERIC)) delay = tag.getInteger("Delay");
        if (tag.hasKey("Update", Constants.NBT.TAG_ANY_NUMERIC)) update = tag.getInteger("Update");
        if (tag.hasKey("CheckEntities")) checkEntities = tag.getBoolean("CheckEntities");
        if (tag.hasKey("OnEnter", Constants.NBT.TAG_COMPOUND)) onEnter.deserializeNBT(tag.getCompoundTag("OnEnter"));
        if (tag.hasKey("OnExit", Constants.NBT.TAG_COMPOUND)) onExit.deserializeNBT(tag.getCompoundTag("OnExit"));
        if (tag.hasKey("OnTick", Constants.NBT.TAG_COMPOUND)) onTick.deserializeNBT(tag.getCompoundTag("OnTick"));

        shapes.clear();
        if (tag.hasKey("Shapes", Constants.NBT.TAG_LIST)) {
            NBTTagList list = tag.getTagList("Shapes", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound shapeTag = list.getCompoundTagAt(i);
                if (!shapeTag.hasKey("Type")) continue;

                AbstractShape shape = AbstractShape.create(shapeTag.getString("Type"));
                if (shape == null) continue;

                shapes.add(shape);
                shape.deserializeNBT(shapeTag);
            }
        }
        if (shapes.isEmpty()) shapes.add(new BoxShape());
        updateSearchBox();

        writeState = tag.getBoolean("WriteState");
        state = tag.getString("State");
        target = EnumUtils.getValue(tag.getInteger("Target"), TargetMode.values(), TargetMode.GLOBAL);
        stateMode = EnumUtils.getValue(tag.getInteger("StateMode"), StateMode.values(), StateMode.ADDITIVE);
    }

    public enum StateMode {
        ADDITIVE, ONCE, TOGGLE
    }
}
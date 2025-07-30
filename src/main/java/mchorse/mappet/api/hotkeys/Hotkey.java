package mchorse.mappet.api.hotkeys;

import mchorse.mappet.api.conditions.Checker;
import mchorse.mappet.api.triggers.Trigger;
import mchorse.mappet.api.utils.DataContext;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public class Hotkey implements INBTSerializable<NBTTagCompound> {
    public String name = "";
    public int defaultKeycode = -1;
    public int keycode = -1;
    public Mode mode = Mode.DOWN;
    public boolean state = false;
    public Trigger trigger = new Trigger();
    public Checker enabled = new Checker(true);

    public Hotkey() {
    }

    public Hotkey(String name, int defaultKeycode, int mode) {
        this(name, defaultKeycode, Mode.values()[Math.max(0, Math.min(mode, Mode.values().length - 1))]);
    }

    public Hotkey(String name, int defaultKeycode, Mode mode) {
        this.name = name;
        this.defaultKeycode = defaultKeycode;
        this.mode = mode;
    }

    public void execute(DataContext context) {
        if (isEnabled(context)) trigger.trigger(context);
    }

    private boolean isEnabled(DataContext context) {
        return enabled.check(context);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setString("Name", name);
        tag.setInteger("DefaultKeycode", defaultKeycode);
//      tag.setInteger("Keycode", keycode);
        tag.setInteger("Mode", mode.ordinal());
        tag.setTag("Trigger", trigger.serializeNBT());
        tag.setTag("Enabled", enabled.serializeNBT());

        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        name = tag.getString("Name");
        defaultKeycode = tag.getInteger("DefaultKeycode");
//      keycode = tag.getInteger("Keycode");
        mode = Mode.values()[Math.max(0, Math.min(tag.getInteger("Mode"), Mode.values().length - 1))];
        trigger.deserializeNBT(tag.getCompoundTag("Trigger"));
        enabled.deserializeNBT(tag.getTag("Enabled"));
    }

    @Override
    public String toString() {
        return "Hotkey:{ " + "name: " + name + ", defaultKey: " + defaultKeycode + ", key: " + keycode + ", mode: " + mode.ordinal() + ", state: " + state + "}";
    }

    public void setMode(int mode) {
        this.mode = Mode.values()[Math.max(0, Math.min(mode, Mode.values().length - 1))];
    }

    public enum Mode {
        DOWN_AND_UP, DOWN, UP, TOGGLE;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}
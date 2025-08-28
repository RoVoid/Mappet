package mchorse.mappet.api.ui;

import mchorse.mappet.api.ui.utils.UIRootComponent;
import mchorse.mappet.api.utils.AbstractData;
import net.minecraft.nbt.NBTTagCompound;

import java.util.UUID;

public class UI extends AbstractData {
    private UUID id;

    public UIRootComponent root = new UIRootComponent();
    public boolean background = false;
    public boolean closable = true;
    public boolean paused = true;
    public int mouseFlags = 0;
    public int mouseDelay = 0;

    public UI() {
        this(UUID.randomUUID());
    }

    public UI(UUID id) {
        this.id = id;
    }

    public UUID getUIId() {
        return id;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setUniqueId("ID", id);
        tag.setTag("Root", root.serializeNBT());
        tag.setBoolean("Background", background);
        tag.setBoolean("Closeable", closable);
        tag.setBoolean("Paused", paused);
        tag.setInteger("Mouse", mouseFlags);
        tag.setInteger("MouseDelay", mouseDelay);

        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        root.deserializeNBT(tag.getCompoundTag("Root"));

        if (tag.hasKey("IDMost")) id = tag.getUniqueId("ID");
        if (tag.hasKey("Background")) background = tag.getBoolean("Background");
        if (tag.hasKey("Closeable")) closable = tag.getBoolean("Closeable");
        if (tag.hasKey("Paused")) paused = tag.getBoolean("Paused");
        if (tag.hasKey("Mouse")) mouseFlags = Math.min(31, Math.max(0, tag.getInteger("Mouse")));
        if (tag.hasKey("MouseDelay")) mouseDelay = Math.max(0, tag.getInteger("MouseDelay"));
    }
}
package mchorse.mappet.api.scripts.code.ui;

import mchorse.mappet.api.scripts.code.nbt.ScriptNBTCompound;
import mchorse.mappet.api.scripts.code.nbt.ScriptNBTCompound;
import mchorse.mappet.api.ui.UIContext;
import net.minecraft.nbt.NBTTagCompound;

public class MappetUIContext {
    private final UIContext context;
    private ScriptNBTCompound data;

    public MappetUIContext(UIContext context) {
        this.context = context;
    }

    public ScriptNBTCompound getData() {
        if (data == null) {
            data = new ScriptNBTCompound(context.data);
        }

        return data;
    }

    public boolean isClosed() {
        return context.isClosed();
    }

    public String getLast() {
        return context.getLast();
    }

    public String getHotkey() {
        return context.getHotkey();
    }

    public String getContext() {
        return context.getContext();
    }

    public NBTTagCompound getMouse() {
        return context.getMouse();
    }

    public UIComponent get(String id) {
        return context.getById(id);
    }

    public void sendToPlayer() {
        context.sendToPlayer();
    }
}
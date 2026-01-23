package mchorse.mappet.api.scripts.code.ui;

import mchorse.mappet.api.scripts.code.nbt.ScriptNBTCompound;
import mchorse.mappet.api.scripts.user.nbt.INBTCompound;
import mchorse.mappet.api.scripts.user.ui.IMappetUIContext;
import mchorse.mappet.api.ui.UIContext;
import net.minecraft.nbt.NBTTagCompound;

public class MappetUIContext implements IMappetUIContext {
    private final UIContext context;
    private INBTCompound data;

    public MappetUIContext(UIContext context) {
        this.context = context;
    }

    @Override
    public INBTCompound getData() {
        if (data == null) {
            data = new ScriptNBTCompound(context.data);
        }

        return data;
    }

    @Override
    public boolean isClosed() {
        return context.isClosed();
    }

    @Override
    public String getLast() {
        return context.getLast();
    }

    @Override
    public String getHotkey() {
        return context.getHotkey();
    }

    @Override
    public String getContext() {
        return context.getContext();
    }

    @Override
    public NBTTagCompound getMouse() {
        return context.getMouse();
    }

    @Override
    public UIComponent get(String id) {
        return context.getById(id);
    }

    @Override
    public void sendToPlayer() {
        context.sendToPlayer();
    }
}
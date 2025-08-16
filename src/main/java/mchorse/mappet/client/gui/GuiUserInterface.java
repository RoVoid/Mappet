package mchorse.mappet.client.gui;

import mchorse.mappet.api.scripts.code.ui.UIComponent;
import mchorse.mappet.api.ui.UI;
import mchorse.mappet.api.ui.UIContext;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.common.ui.PacketUI;
import mchorse.mclib.client.gui.framework.GuiBase;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.IGuiElement;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;

import java.io.IOException;

public class GuiUserInterface extends GuiBase {
    private final UIContext context;

    public GuiUserInterface(Minecraft mc, UI ui) {
        context = new UIContext(ui);

        GuiElement element = ui.root.create(mc, context);

        element.flex().relative(root).wh(1F, 1F);
        root.add(element);
    }

    public void handleUIChanges(NBTTagCompound data) {
        for (String key : data.getKeySet()) {
            NBTTagCompound tag = data.getCompoundTag(key);
            GuiElement element = context.getElement(key);

            context.getById(key).handleChanges(context, tag, element);
        }

        root.resize();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return context.ui.paused;
    }

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);
        if(context.ui.mouse < 0) return;
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("type", "click");
        tag.setInteger("button", button);
        tag.setInteger("x", x);
        tag.setInteger("y", y);
        tag.setString("on", onMouse(x, y));
        context.setMouse(tag);
        //System.out.println("[GUI] Mouse clicked at " + x + ":" + y + ", button " + button);
    }

    @Override
    protected void mouseReleased(int x, int y, int button) {
        super.mouseReleased(x, y, button);
        if(context.ui.mouse < 0) return;
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("type", "release");
        tag.setInteger("button", button);
        tag.setInteger("x", x);
        tag.setInteger("y", y);
        tag.setString("on", onMouse(x, y));
        context.setMouse(tag);
        //System.out.println("[GUI] Mouse clicked at " + x + ":" + y + ", release " + button);
    }

    @Override
    protected void mouseScrolled(int x, int y, int scroll) {
        super.mouseScrolled(x, y, scroll);
        if(context.ui.mouse < 0) return;
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("type", "scroll");
        tag.setInteger("scroll", scroll);
        tag.setInteger("x", x);
        tag.setInteger("y", y);
        tag.setString("on", onMouse(x, y));
        context.setMouse(tag);
        //System.out.println("[GUI] Mouse clicked at " + x + ":" + y + ", scroll " + scroll);
    }

    @Override
    protected void mouseClickMove(int x, int y, int button, long timeSinceLastClick) {
        super.mouseClickMove(x, y, button, timeSinceLastClick);
        if(context.ui.mouse < 0) return;
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("type", "drag");
        tag.setInteger("button", button);
        tag.setLong("time", timeSinceLastClick);
        tag.setInteger("x", x);
        tag.setInteger("y", y);
        tag.setString("on", onMouse(x, y));
        context.setMouse(tag);
        //System.out.println("[GUI] Mouse clicked at " + x + ":" + y + ", button " + button + ", time " + timeSinceLastClick);
    }

    public String onMouse(int x, int y){
        for(String key : context.getElementKeys()){
            if(context.getElement(key).area.isInside(x, y)) return key;
        }
        return "";
    }

    @Override
    protected void closeScreen() {
        if (context.ui.closable) super.closeScreen();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if (context.isDirtyInProgress()) context.sendToPlayer();
        Dispatcher.sendToServer(new PacketUI(new UI(context.ui.getUIId())));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (context.isDirty()) context.sendToServer();
        if (context.ui.background) drawDefaultBackground();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
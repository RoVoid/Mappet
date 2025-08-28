package mchorse.mappet.client.gui;

import mchorse.mappet.api.ui.UI;
import mchorse.mappet.api.ui.UIContext;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.common.ui.PacketUI;
import mchorse.mclib.client.gui.framework.GuiBase;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
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
    protected void mouseClicked(int x, int y, int button) throws IOException { // 2
        super.mouseClicked(x, y, button);
        if ((context.ui.mouseFlags & 1 << 1) == 0) return;
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("type", "click");
        tag.setInteger("button", button);

        context.setMouse(tag);
        System.out.println("[GUI] Mouse clicked at " + x + ":" + y + ", button " + button);
    }

    @Override
    protected void mouseReleased(int x, int y, int button) { // 4
        super.mouseReleased(x, y, button);
        if ((context.ui.mouseFlags & 1 << 2) == 0) return;
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("type", "release");
        tag.setInteger("button", button);
        pushCoords(tag, x, y);
        context.setMouse(tag);
        System.out.println("[GUI] Mouse clicked at " + x + ":" + y + ", release " + button);
    }

    @Override
    protected void mouseScrolled(int x, int y, int scroll) { // 16
        super.mouseScrolled(x, y, scroll);
        if ((context.ui.mouseFlags & 1 << 4) == 0) return;
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("type", "scroll");
        tag.setInteger("scroll", scroll);
        pushCoords(tag, x, y);
        context.setMouse(tag);
        System.out.println("[GUI] Mouse clicked at " + x + ":" + y + ", scroll " + scroll);
    }

    @Override
    protected void mouseClickMove(int x, int y, int button, long timeSinceLastClick) { // 8
        super.mouseClickMove(x, y, button, timeSinceLastClick);
        if ((context.ui.mouseFlags & 1 << 3) == 0) return;
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("type", "drag");
        tag.setInteger("button", button);
        tag.setLong("time", timeSinceLastClick);
        pushCoords(tag, x, y);
        context.setMouse(tag);
        System.out.println("[GUI] Mouse clicked at " + x + ":" + y + ", button " + button + ", time " + timeSinceLastClick);
    }


    protected String mouseHover(int x, int y) { // 1
        for (String key : context.getElementKeys()) {
            if (context.getElement(key).area.isInside(x, y)) return key;
        }
        return "";
    }

    protected void pushCoords(NBTTagCompound tag, int x, int y) {
        tag.setInteger("x", x);
        tag.setInteger("y", y);
        tag.setFloat("rx", Math.round(x / (float) width * 1000f) / 1000f);
        tag.setFloat("ry", Math.round(y / (float) height * 1000f) / 1000f);
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
        System.out.println(getCurrent().tick + " " + context.ui.mouseDelay + " " + (getCurrent().tick % context.ui.mouseDelay == 0));
        if (context.ui.mouseFlags != 0 && (context.ui.mouseDelay == 0 || getCurrent().tick % context.ui.mouseDelay == 0)) {
            NBTTagCompound tag = context.getMouse();
            if ((context.ui.mouseFlags & 1) != 0) {
                String hover = mouseHover(mouseX, mouseY);
                if (!hover.isEmpty()) {
                    tag.setString("hover", hover);
                    if (!tag.hasKey("x")) pushCoords(tag, mouseX, mouseY);
                    context.setMouse(tag);
                }
            }
            if (!tag.hasNoTags()) context.sendMouse();
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
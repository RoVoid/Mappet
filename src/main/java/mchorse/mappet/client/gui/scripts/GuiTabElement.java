package mchorse.mappet.client.gui.scripts;

import mchorse.mappet.Mappet;
import mchorse.mclib.McLib;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDraw;
import mchorse.mclib.client.gui.utils.Icon;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;

public class GuiTabElement extends GuiElement {

    public String path;
    public String title;

    public boolean selected;

    private final Consumer<GuiTabElement> clickCallback;

    public GuiIconElement icon;

    public static final Icon noSaveIcon = new Icon(new ResourceLocation(Mappet.MOD_ID, "textures/gui/tab.png"), 0, 0, 5, 5, 16, 16);
    public static final Icon closeIcon = new Icon(new ResourceLocation(Mappet.MOD_ID, "textures/gui/tab.png"), 5, 0, 5, 5, 16, 16);

    public GuiTabElement(Minecraft mc, String path, Consumer<GuiTabElement> clickCallback, Consumer<GuiTabElement> closeCallback) {
        super(mc);

        this.clickCallback = clickCallback;

        int index = path.lastIndexOf('/');
        title = index < 0 ? path : path.substring(index + 1);
        this.path = path;

        icon = new GuiIconElement(mc, closeIcon, (b) -> closeCallback.accept(this));
        icon.hoverIcon = closeIcon;
        icon.hoverColor = -1;

        icon.flex().relative(this).w(5).h(1f).anchor(1F, 0).x(1F, -5); // TODO: Подправить
        flex().w(font.getStringWidth(title) + 20);

        add(icon);
    }

    public void edited(boolean state) {icon.icon = state ? noSaveIcon : closeIcon;}

    @Override
    public boolean mouseClicked(GuiContext context) {
        if (super.mouseClicked(context)) return true;
        if (!area.isInside(context)) return false;
        if (clickCallback != null) clickCallback.accept(this);
        return true;
    }

    @Override
    public void draw(GuiContext context) {
        int color = -2013265920 + (selected ? McLib.primaryColor.get() : McLib.backgroundColor.get());
        GuiDraw.drawBorder(area, color);

        font.drawString(title, area.x + 5, area.my(font.FONT_HEIGHT - 1), 16777215, true);

        GuiDraw.drawLockedArea(this);

        super.draw(context);
    }
}

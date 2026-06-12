package mchorse.mappet.client.gui.utils.overlays;

import mchorse.mclib.McLib;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDraw;
import mchorse.mclib.client.gui.framework.elements.utils.GuiLabel;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

public class GuiOverlayPanel extends GuiElement
{
    public GuiLabel title;
    public GuiElement icons;
    public GuiIconElement close;
    public GuiElement content;

    public int background;

    public GuiOverlayPanel(Minecraft mc, IKey title)
    {
        super(mc);

        this.title = Elements.label(title);
        close = new GuiIconElement(mc, Icons.CLOSE, (b) -> close());
        content = new GuiElement(mc);
        icons = new GuiElement(mc);

        this.title.flex().relative(this).xy(10, 10).w(0.5F);
        close.flex().wh(16, 16);
        icons.flex().relative(this).x(1F, -7).y(6).anchorX(1F).row(0).reverse().resize().width(16).height(16);
        content.flex().relative(this).xy(10, 28).w(1F, -20).h(1F, -28);

        icons.add(close);

        add(this.title, icons, content);
    }

    public void close()
    {
        GuiElement parent = getParent();
        if (parent instanceof GuiOverlay) ((GuiOverlay) parent).closeItself();
    }

    @Override
    public boolean mouseClicked(GuiContext context)
    {
        return super.mouseClicked(context) || area.isInside(context);
    }

    @Override
    public void draw(GuiContext context)
    {
        drawBackground(context);
        super.draw(context);
    }

    protected void drawBackground(GuiContext context)
    {
        int color = McLib.primaryColor.get();

        GuiDraw.drawDropShadow(area.x, area.y, area.ex(), area.ey(), 10, 0x44000000 + color, color);
        area.draw(0xff000000);
    }

    public void onClose()
    {}
}
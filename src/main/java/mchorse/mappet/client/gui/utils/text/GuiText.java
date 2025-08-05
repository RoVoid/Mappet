package mchorse.mappet.client.gui.utils.text;

import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.ITextColoring;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

import java.util.List;

public class GuiText extends GuiElement implements ITextColoring {
    private IKey temp = IKey.EMPTY;
    private List<String> text;
    private int lineHeight = 12;
    private int color = 0xffffff;
    private int hoverColor = 0xffffff;
    private boolean shadow = true;
    private int paddingH;
    private int paddingV;
    private float anchorX;

    private int lines;

    public GuiText(Minecraft mc) {
        super(mc);
        flex().h(() -> (float) height());
    }

    private int height() {
        int height = Math.max(lines, 1) * lineHeight - (lineHeight - font.FONT_HEIGHT);
        return height + paddingV * 2;
    }

    public IKey getText() {
        return temp;
    }

    public GuiText text(String text) {
        return text(IKey.str(text));
    }

    public GuiText text(IKey text) {
        this.text = null;
        temp = text;
        lines = 0;
        return this;
    }

    public GuiText lineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
        return this;
    }

    public GuiText color(int color, boolean shadow) {
        this.color = hoverColor = color;
        this.shadow = shadow;
        return this;
    }

    public void hoverColor(int color) {
        hoverColor = color;
    }

    public GuiText padding(int padding) {
        return padding(padding, padding);
    }

    public GuiText padding(int horizontal, int vertical) {
        paddingH = horizontal;
        paddingV = vertical;

        return this;
    }

    public GuiText anchorX(float anchor) {
        anchorX = anchor;

        return this;
    }

    @Override
    public void setColor(int color, boolean shadow) {
        color(color, shadow);
    }

    @Override
    public void resize() {
        super.resize();
        text = null;
    }

    @Override
    public void draw(GuiContext context) {
        if (area.w <= 0) return;

        int offset = font.getCharWidth('@');
        if (text == null) {
            List<String> text = font.listFormattedStringToWidth(temp
                    .get()
                    .replace("\\n", "\n"), area.w - paddingH * 2 - offset);

            lines = text.size();
            getParentContainer().resize();

            this.text = text;
        }

        int y = paddingV;
        int color = area.isInside(context) ? hoverColor : this.color;

        String prev = null;
        for (String line : text) {
            int x = area.x + paddingH;
            if (needOffset(prev)) x += offset;

            if (anchorX != 0) x += (int) ((area.w - paddingH * 2 - font.getStringWidth(line)) * anchorX);

            if (shadow) font.drawStringWithShadow(line, x, area.y + y, color);
            else font.drawString(line, x, area.y + y, color);

            y += lineHeight;

            prev = line;
        }

        super.draw(context);
    }

    private boolean needOffset(String s) {
        if (s == null || s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c)) continue;
            if (c == '§' && i + 1 < s.length()) {
                i++;
                continue;
            }
            return true;
        }
        return false;
    }
}
package mchorse.mappet.client.gui.utils;

import mchorse.mappet.config.MappetConfig;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiClickElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDraw;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.client.gui.utils.keys.LangKey;
import mchorse.mclib.utils.ColorUtils;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

// yeah... just wanted change color in GuiCirculateElement
public class GuiEnumElement<E extends Enum<E>> extends GuiClickElement<GuiEnumElement<E>> {
    public boolean custom;
    public int customColor;

    protected List<IKey> labels = new ArrayList<>();
    protected Set<Integer> disabled = new HashSet<>();

    protected int index;

    private final Class<E> enumClass;

    public GuiEnumElement(Minecraft mc, E defaultValue, Consumer<E> callback) {
        super(mc, (el) -> callback.accept(defaultValue.getDeclaringClass().getEnumConstants()[el.selected()]));
        enumClass = defaultValue.getDeclaringClass();
        index = defaultValue.ordinal();

        hideTooltip = true;

        bakeLabels("");
        flex().h(20);
    }

    public GuiEnumElement(GuiEnumElement<E> other) {
        super(other.mc, other.callback);
        enumClass = other.enumClass;
        index = other.index;

        labels.addAll(other.labels);
        hideTooltip = other.hideTooltip;

        custom = other.custom;
        customColor = other.customColor;

        flex().h(20);
    }

    public GuiEnumElement<E> color(int color) {
        custom = true;
        customColor = color & 16777215;
        return this;
    }

    public GuiEnumElement<E> showTooltip() {
        hideTooltip = false;
        updateTooltip();
        return this;
    }

    @Override
    public GuiEnumElement<E> hideTooltip() {
        hideTooltip = true;
        updateTooltip();
        return this;
    }

    public List<IKey> getLabels() {
        return labels;
    }

    public void bakeLabels(String prefix) {
        if (prefix == null) return;
        labels.clear();
        if (!prefix.isEmpty()) prefix += '.';
        for (E e : enumClass.getEnumConstants()) labels.add(IKey.lang(prefix + e.name().toLowerCase()));
    }

    public void setLabel(int index, IKey key) {
        if (0 <= index && index < labels.size() && key != null) labels.set(index, key);
    }

    public void setLabel(E value, IKey key) {
        if (value != null && key != null) labels.set(value.ordinal(), key);
    }

    public IKey getLabel() {
        return labels.get(index);
    }

    public void disable(int index) {
        if (0 <= index && index < labels.size()) disabled.add(index);
    }

    public void disable(E value) {
        disabled.add(value.ordinal());
    }

    public int selected() {
        return index;
    }

    public E selectedValue() {
        return enumClass.getEnumConstants()[index];
    }

    public void select(int index) {
        select(index, 1);
    }

    public void select(E value) {
        select(value.ordinal(), 1);
    }

    public void select(int index, int direction) {
        if (this.index == index) return;

        int size = labels.size();
        for (int i = 0; i < size; i++) {
            int wrapped = ((index + direction * i) % size + size) % size;
            if (!disabled.contains(wrapped)) {
                this.index = wrapped;
                updateTooltip();
                return;
            }
        }
    }

    private void updateTooltip() {
        if (!hideTooltip && getLabel() instanceof LangKey) tooltip(IKey.lang(((LangKey) getLabel()).key + "_tooltip"));
    }

    @Override
    protected boolean isAllowed(int mouseButton) {
        return mouseButton == 0 || mouseButton == 1;
    }

    @Override
    protected void click(int mouseButton) {
        int direction = mouseButton == 0 ? 1 : -1;
        select(index + direction, direction);
        super.click(mouseButton);
    }

    @Override
    protected GuiEnumElement<E> get() {
        return this;
    }

    @Override
    protected void drawSkin(GuiContext context) {
        int color = -16777216 + (custom ? customColor : MappetConfig.enumColor.get());
        if (hover) color = ColorUtils.multiplyColor(color, 0.85F);

        GuiDraw.drawBorder(area, color);
        String label = getLabel().get();
        int x = area.mx(font.getStringWidth(label));
        int y = area.my(font.FONT_HEIGHT - 1);
        font.drawStringWithShadow(label, (float) x, (float) y, hover ? 16777120 : 16777215);
        GuiDraw.drawLockedArea(this);
    }
}
package mchorse.mappet.client.gui.utils;

import mchorse.mappet.api.utils.Comparison;
import mchorse.mappet.api.utils.ComparisonMode;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

public class GuiComparisonElement extends GuiElement {
    public Comparison comparison;

    private final GuiEnumElement<ComparisonMode> mode;
    private final GuiTrackpadElement value;
    private final GuiTextElement expression;

    public GuiComparisonElement(Minecraft mc, Comparison comparison) {
        super(mc);

        this.comparison = comparison;

        mode = new GuiEnumElement<>(mc, comparison.mode, this::toggleComparison);
        for (ComparisonMode mode : ComparisonMode.values()) this.mode.setLabel(mode, mode.stringify());

        value = new GuiTrackpadElement(mc, (v) -> this.comparison.value = v);
        value.setValue(comparison.value);

        expression = new GuiTextElement(mc, 1000, (t) -> this.comparison.expression = t);
        expression.setText(this.comparison.expression);
        expression.tooltip(IKey.lang("mappet.gui.conditions.expression_tooltip"));

        flex().row(5);
        toggleComparison(mode.selectedValue());
    }

    private void toggleComparison(ComparisonMode mode) {
        comparison.mode = mode;

        GuiElement insert = value;
        IKey label = IKey.lang("mappet.gui.conditions.value");

        if (comparison.mode == ComparisonMode.EXPRESSION) {
            insert = expression;
            label = IKey.lang("mappet.gui.conditions.expression");
        }
        else if (comparison.mode == ComparisonMode.IS_TRUE || comparison.mode == ComparisonMode.IS_FALSE) insert = null;
        else if (comparison.mode.isString) insert = expression;

        removeAll();
        add(Elements.column(mc, 5, Elements.label(IKey.lang("mappet.gui.conditions.comparison")), this.mode));

        if (insert != null) add(Elements.column(mc, 5, Elements.label(label), insert));

        GuiElement container = getParentContainer();

        if (container != null) container.resize();
    }
}
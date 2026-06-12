package mchorse.mappet.client.gui.triggers.panels;

import mchorse.mappet.api.triggers.blocks.StateTriggerBlock;
import mchorse.mappet.client.gui.triggers.GuiTriggerOverlayPanel;
import mchorse.mappet.client.gui.utils.GuiEnumElement;
import mchorse.mappet.client.gui.utils.GuiTargetElement;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiLabel;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

public class GuiStateTriggerBlockPanel extends GuiAbstractTriggerBlockPanel<StateTriggerBlock> {
    public GuiTextElement id;
    public GuiTargetElement target;
    public GuiEnumElement<StateTriggerBlock.StateMode> mode;

    public GuiLabel valueLabel;
    public GuiElement valueRow;
    public GuiIconElement convert;
    public GuiElement value;

    public GuiStateTriggerBlockPanel(Minecraft mc, GuiTriggerOverlayPanel overlay, StateTriggerBlock block) {
        super(mc, overlay, block);

        id = new GuiTextElement(mc, 1000, (v) -> this.block.string = v);
        target = new GuiTargetElement(mc, null);
        mode = new GuiEnumElement<>(mc, block.mode, this::toggleItemCheck);
        mode.bakeLabels("mappet.gui.item_trigger.mode");

        valueLabel = Elements.label(IKey.lang("mappet.gui.conditions.value"));
        valueRow = Elements.row(mc, 0);
        convert = new GuiIconElement(mc, Icons.REFRESH, this::convert);

        id.setText(block.string);
        target.setTarget(block.target);

        add(mode);
        add(Elements.label(IKey.lang("mappet.gui.conditions.state.id")).marginTop(12), id);
        add(target.marginTop(12));
        add(valueLabel.marginTop(12), valueRow);

        toggleItemCheck(mode.selectedValue());
        updateValue();
    }

    private void toggleItemCheck(StateTriggerBlock.StateMode mode) {
        block.mode = mode;
        updateValue();
    }

    private void convert(GuiIconElement element) {
        block.value = block.value instanceof String ? Double.valueOf(0D) : "";
        updateValue();
    }

    private void updateValue() {
        Object object = block.value;

        if (object instanceof String) if (block.mode == StateTriggerBlock.StateMode.ADD) block.value = object = 0D;
        else {
            GuiTextElement element = new GuiTextElement(mc, 10000, this::updateString);

            element.setText((String) object);
            value = element;
        }

        if (object instanceof Number) {
            GuiTrackpadElement element = new GuiTrackpadElement(mc, this::updateNumber);

            element.setValue(((Number) object).doubleValue());
            value = element;
        }

        valueLabel.setVisible(block.mode != StateTriggerBlock.StateMode.REMOVE);
        valueRow.removeAll();

        if (block.mode != StateTriggerBlock.StateMode.REMOVE) {
            valueRow.add(value);

            if (block.mode != StateTriggerBlock.StateMode.ADD) valueRow.add(convert);
        }

        if (hasParent()) getParentContainer().resize();
    }

    private void updateString(String s) {
        block.value = s;
    }

    private void updateNumber(double v) {
        block.value = v;
    }
}
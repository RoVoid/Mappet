package mchorse.mappet.client.gui.conditions.blocks;

import mchorse.mappet.api.conditions.blocks.WorldTimeConditionBlock;
import mchorse.mappet.client.gui.conditions.GuiConditionOverlayPanel;
import mchorse.mappet.client.gui.utils.GuiEnumElement;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

public class GuiWorldTimeConditionBlockPanel extends GuiAbstractConditionBlockPanel<WorldTimeConditionBlock> {
    public GuiEnumElement<WorldTimeConditionBlock.TimeCheck> type;
    public GuiTrackpadElement min;
    public GuiTrackpadElement max;

    private final GuiElement[] elements;

    public GuiWorldTimeConditionBlockPanel(Minecraft mc, GuiConditionOverlayPanel overlay, WorldTimeConditionBlock block) {
        super(mc, overlay, block);

        type = new GuiEnumElement<>(mc, block.check, this::toggleMode);

        for (WorldTimeConditionBlock.TimeCheck check : WorldTimeConditionBlock.TimeCheck.values()) {
            type.setLabel(check, IKey.lang(check.getKey()));
        }

        min = new GuiTrackpadElement(mc, (v) -> this.block.min = v.intValue());
        min.limit(0, 24000, true).setValue(block.min);
        max = new GuiTrackpadElement(mc, (v) -> this.block.max = v.intValue());
        max.limit(0, 24000, true).setValue(block.max);

        GuiElement a = Elements.label(IKey.lang("mappet.gui.conditions.world_time.range")).marginTop(12);
        GuiElement b = Elements.row(mc, 5, min, max);

        add(Elements.label(IKey.lang("mappet.gui.conditions.world_time.check")).marginTop(12), type);
        add(a, b);

        elements = new GuiElement[]{a, b};
        toggleMode(type.selectedValue());
    }

    private void toggleMode(WorldTimeConditionBlock.TimeCheck timeCheck) {
        block.check = timeCheck;
        boolean visible = block.check == WorldTimeConditionBlock.TimeCheck.RANGE;
        for (GuiElement element : elements) element.setVisible(visible);
    }
}
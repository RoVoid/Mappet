package mchorse.mappet.client.gui.conditions.blocks;

import mchorse.mappet.api.conditions.blocks.ItemConditionBlock;
import mchorse.mappet.client.gui.conditions.GuiConditionOverlayPanel;
import mchorse.mappet.client.gui.utils.GuiEnumElement;
import mchorse.mappet.client.gui.utils.GuiTargetElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiSlotElement;
import mchorse.mclib.client.gui.utils.Elements;
import net.minecraft.client.Minecraft;

public class GuiItemConditionBlockPanel extends GuiAbstractConditionBlockPanel<ItemConditionBlock> {
    public GuiTargetElement target;
    public GuiEnumElement<ItemConditionBlock.ItemCheck> check;
    public GuiSlotElement slot;

    public GuiItemConditionBlockPanel(Minecraft mc, GuiConditionOverlayPanel overlay, ItemConditionBlock iBlock) {
        super(mc, overlay, iBlock);

        target = new GuiTargetElement(mc, iBlock.target).skipGlobal();
        check = new GuiEnumElement<>(mc, iBlock.check, (ic) -> block.check = ic);
        check.bakeLabels("mappet.gui.conditions.item.types");

        slot = new GuiSlotElement(mc, 0, (stack) -> block.stack = stack.copy());
        slot.marginTop(-2).marginBottom(-2);
        slot.setStack(iBlock.stack);

        add(Elements.row(mc, 5, slot, check));
        add(target.marginTop(12));
    }
}
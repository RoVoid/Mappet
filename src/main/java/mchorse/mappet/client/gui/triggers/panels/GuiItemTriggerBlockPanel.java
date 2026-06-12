package mchorse.mappet.client.gui.triggers.panels;

import mchorse.mappet.api.triggers.blocks.ItemTriggerBlock;
import mchorse.mappet.client.gui.triggers.GuiTriggerOverlayPanel;
import mchorse.mappet.client.gui.utils.GuiEnumElement;
import mchorse.mappet.client.gui.utils.GuiTargetElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiSlotElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

public class GuiItemTriggerBlockPanel extends GuiAbstractTriggerBlockPanel<ItemTriggerBlock> {
    public GuiTargetElement target;
    public GuiEnumElement<ItemTriggerBlock.ItemMode> mode;
    public GuiSlotElement slot;
    public GuiToggleElement ignoreNBT;

    public GuiItemTriggerBlockPanel(Minecraft mc, GuiTriggerOverlayPanel overlay, ItemTriggerBlock block) {
        super(mc, overlay, block);

        target = new GuiTargetElement(mc, null).skipGlobal();
        mode = new GuiEnumElement<>(mc, block.mode, this::toggleItemCheck);
        mode.bakeLabels("mappet.gui.item_trigger.mode");

        slot = new GuiSlotElement(mc, 0, (stack) -> this.block.stack = stack.copy());
        slot.marginTop(-2).marginBottom(-2);

        ignoreNBT = new GuiToggleElement(mc, IKey.lang("mappet.gui.item_trigger.ignoreNBT"), (b) -> this.block.ignoreNBT = b.isToggled());
        ignoreNBT.setVisible(this.block.mode == ItemTriggerBlock.ItemMode.TAKE);

        target.setTarget(block.target);
        slot.setStack(block.stack);
        ignoreNBT.toggled(block.ignoreNBT);

        add(Elements.row(mc, 5, slot, mode));
        add(ignoreNBT.marginTop(12));
        add(target.marginTop(12));
    }

    private void toggleItemCheck(ItemTriggerBlock.ItemMode mode) {
        block.mode = mode;
        ignoreNBT.setVisible(block.mode == ItemTriggerBlock.ItemMode.TAKE);
    }
}
package mchorse.mappet.client.gui;

import mchorse.mclib.client.gui.framework.elements.buttons.GuiSlotElement;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import java.util.function.Consumer;

public class AbstractGuiSlotElement extends GuiSlotElement {
    public boolean locked = false;

    public AbstractGuiSlotElement(Minecraft mc, int slot, Consumer<ItemStack> callback) {
        super(mc, slot, callback);
    }

    @Override
    protected void click(int mouseButton) {
        if (!locked) {
            super.click(mouseButton);
        }
    }


}

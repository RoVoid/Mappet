package mchorse.mappet.client.gui.conditions;

import mchorse.mappet.api.conditions.Condition;
import mchorse.mappet.client.gui.utils.overlays.GuiOverlay;
import mchorse.mclib.client.gui.framework.GuiBase;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;

public class GuiOpenConditionButtonElement extends GuiButtonElement {
    private Condition condition;

    public GuiOpenConditionButtonElement(Minecraft mc) {
        this(mc, null);
    }

    public GuiOpenConditionButtonElement(Minecraft mc, Condition condition) {
        super(mc, IKey.lang("mappet.gui.checker.edit"), null);

        callback = this::openConditionEditor;

        flex().h(20).row(0);
        setCondition(condition);
    }

    private void openConditionEditor(GuiButtonElement b) {
        GuiConditionOverlayPanel panel = new GuiConditionOverlayPanel(mc, condition);
        GuiOverlay.addOverlay(GuiBase.getCurrent(), panel, 0.6F, 0.8F);
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public NBTTagCompound toNBT() {
        return condition.serializeNBT();
    }
}
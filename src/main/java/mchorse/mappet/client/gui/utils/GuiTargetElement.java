package mchorse.mappet.client.gui.utils;

import mchorse.mappet.api.utils.Target;
import mchorse.mappet.api.utils.TargetMode;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

public class GuiTargetElement extends GuiElement {
    public Target target;

    private final GuiEnumElement<TargetMode> mode;
    private final GuiTextElement selector;

    public GuiTargetElement(Minecraft mc, Target target) {
        super(mc);

        mode = GuiMappetUtils.createTargetCirculate(mc, TargetMode.GLOBAL, (m) -> {
            this.target.mode = m;
            updateTarget();
        });
        selector = new GuiTextElement(mc, 1000, (t) -> this.target.selector = t);
        flex().column(5).stretch().vertical();

        setTarget(target);
    }

    public void setTarget(Target target) {
        this.target = target;
        if (target != null) {
            mode.select(target.mode);
            selector.setText(target.selector);
        }
        updateTarget();
    }

    public GuiTargetElement skipGlobal() {
        return skip(TargetMode.GLOBAL);
    }

    public GuiTargetElement skip(TargetMode mode) {
        this.mode.disable(mode);
        return this;
    }

    private void updateTarget() {
        if (target == null) return;

        removeAll();
        add(Elements.label(IKey.lang("mappet.gui.conditions.target")), mode);

        if (target.mode == TargetMode.SELECTOR) add(Elements.label(IKey.lang("mappet.gui.conditions.selector")), selector);

        GuiElement container = getParentContainer();
        if (container != null) container.resize();
    }
}
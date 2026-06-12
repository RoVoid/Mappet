package mchorse.mappet.client.gui.regions;

import mchorse.mappet.api.regions.Region;
import mchorse.mappet.api.regions.shapes.AbstractShape;
import mchorse.mappet.api.regions.shapes.BoxShape;
import mchorse.mappet.api.utils.TargetMode;
import mchorse.mappet.client.gui.conditions.GuiOpenConditionButtonElement;
import mchorse.mappet.client.gui.triggers.GuiTriggerElement;
import mchorse.mappet.client.gui.utils.GuiEnumElement;
import mchorse.mappet.client.gui.utils.GuiMappetUtils;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiLabel;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

public class GuiRegionEditor extends GuiElement {
    public GuiToggleElement checkEntities;
    public GuiOpenConditionButtonElement enabled;

    public GuiTrackpadElement delay;
    public GuiTrackpadElement update;

    public GuiTriggerElement onEnter;
    public GuiTriggerElement onExit;
    public GuiTriggerElement onTick;

    public GuiToggleElement writeState;
    public GuiElement stateOptions;
    public GuiTextElement state;
    public GuiEnumElement<TargetMode> target;
    public GuiEnumElement<Region.StateMode> stateMode;

    public GuiElement shapes;

    private Region region;

    public GuiRegionEditor(Minecraft mc) {
        super(mc);

        enabled = new GuiOpenConditionButtonElement(mc);
        delay = new GuiTrackpadElement(mc, (value) -> region.delay = value.intValue()).limit(0).integer();
        update = new GuiTrackpadElement(mc, (value) -> region.update = value.intValue()).limit(1).integer();
        checkEntities = new GuiToggleElement(mc, IKey.lang("mappet.gui.region.check_entities"), (b) -> region.checkEntities = b.isToggled());

        onEnter = new GuiTriggerElement(mc);
        onExit = new GuiTriggerElement(mc);
        onTick = new GuiTriggerElement(mc);

        writeState = new GuiToggleElement(mc, IKey.lang("mappet.gui.region.write_states"), (b) -> toggleStates());
        stateOptions = Elements.column(mc, 5);
        state = new GuiTextElement(mc, (t) -> region.state = t);

        target = GuiMappetUtils.createTargetCirculate(mc, TargetMode.GLOBAL, (target) -> region.target = target);
        for (TargetMode target : TargetMode.values())
            if (!(target == TargetMode.SUBJECT || target == TargetMode.GLOBAL)) this.target.disable(target.ordinal());

        stateMode = new GuiEnumElement<>(mc, Region.StateMode.ADDITIVE, (mode) -> region.stateMode = mode).showTooltip();
        stateMode.bakeLabels("mappet.gui.region");

        shapes = Elements.column(mc, 5);

        add(checkEntities);
        add(Elements.label(IKey.lang("mappet.gui.region.enabled")).marginTop(6), enabled);
        add(Elements.label(IKey.lang("mappet.gui.region.delay")).marginTop(12), delay);
        add(Elements.label(IKey.lang("mappet.gui.region.update")).marginTop(12), update);
        add(Elements.label(IKey.lang("mappet.gui.region.on_enter")).background().marginTop(12).marginBottom(5), onEnter);
        add(Elements.label(IKey.lang("mappet.gui.region.on_exit")).background().marginTop(12).marginBottom(5), onExit);
        add(Elements.label(IKey.lang("mappet.gui.region.on_tick")).background().marginTop(12).marginBottom(5), onTick);

        add(writeState.marginTop(12));
        add(stateOptions);

        GuiLabel shapesLabel = Elements.label(IKey.lang("mappet.gui.region.shapes")).background();
        GuiIconElement addShape = new GuiIconElement(mc, Icons.ADD, this::addShape);

        addShape.flex().relative(shapesLabel).xy(1F, 0.5F).w(10).anchor(1F, 0.5F);
        shapesLabel.marginTop(12).add(addShape);

        add(shapesLabel);
        add(shapes);

        flex().column(5).vertical().stretch();
    }

    private void addShape(GuiIconElement element) {
        AbstractShape shape = new BoxShape();
        GuiShapeEditor editor = new GuiShapeEditor(mc);

        region.shapes.add(shape);
        shapes.add(editor.marginTop(12));
        editor.set(region, shape);
    }

    private void toggleStates() {
        region.writeState = writeState.isToggled();

        stateOptions.removeAll();

        if (region.writeState) {
            stateOptions.add(Elements.label(IKey.lang("mappet.gui.conditions.state.id")).marginTop(6), state);
            stateOptions.add(target, stateMode);
        }

        getParentContainer().resize();
    }

    public void set(Region region) {
        this.region = region;

        if (region != null) {
            checkEntities.toggled(region.checkEntities);
            enabled.setCondition(region.enabled);
            delay.setValue(region.delay);
            update.setValue(region.update);
            onEnter.set(region.onEnter);
            onExit.set(region.onExit);
            onTick.set(region.onTick);

            shapes.removeAll();

            for (AbstractShape shape : region.shapes) {
                GuiShapeEditor editor = new GuiShapeEditor(mc);

                shapes.add(editor.marginTop(12));
                editor.set(region, shape);
            }

            writeState.toggled(region.writeState);
            state.setText(region.state);
            target.select(region.target);
            stateMode.select(region.stateMode);

            toggleStates();
        }
    }
}

package mchorse.mappet.client.gui.nodes.events;

import mchorse.mappet.api.events.nodes.ConditionNode;
import mchorse.mappet.client.gui.conditions.GuiOpenConditionButtonElement;
import mchorse.mappet.client.gui.nodes.GuiEventBaseNodePanel;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

public class GuiConditionNodePanel extends GuiEventBaseNodePanel<ConditionNode>
{
    public GuiOpenConditionButtonElement checker;

    public GuiConditionNodePanel(Minecraft mc)
    {
        super(mc);

        this.checker = new GuiOpenConditionButtonElement(mc);

        this.add(Elements.label(IKey.lang("mappet.gui.nodes.event.condition")).marginTop(12), this.checker, this.binary);
    }

    @Override
    public void set(ConditionNode node)
    {
        super.set(node);

        this.checker.setCondition(node.condition);
    }
}
package mchorse.mappet.client.gui.triggers.panels;

import mchorse.mappet.api.events.nodes.EventBaseNode;
import mchorse.mappet.api.triggers.blocks.EventTriggerBlock;
import mchorse.mappet.api.utils.content.ContentTypes;
import mchorse.mappet.api.utils.content.IContentType;
import mchorse.mappet.api.utils.nodes.NodeSystem;
import mchorse.mappet.client.gui.triggers.GuiTriggerOverlayPanel;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

public class GuiEventTriggerBlockPanel extends GuiDataTriggerBlockPanel<EventTriggerBlock>
{
    public GuiEventTriggerBlockPanel(Minecraft mc, GuiTriggerOverlayPanel overlay, EventTriggerBlock block)
    {
        super(mc, overlay, block);
        this.addPicker();
        this.addData();
        this.addDelay();
    }

    @Override
    protected IKey getLabel()
    {
        return IKey.lang("mappet.gui.overlays.event");
    }

    @Override
    protected IContentType<NodeSystem<EventBaseNode>> getType()
    {
        return ContentTypes.EVENT;
    }
}
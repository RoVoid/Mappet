package mchorse.mappet.client.gui.npc;

import mchorse.mappet.api.npcs.NpcState;
import mchorse.mclib.client.gui.framework.elements.GuiScrollElement;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.ScrollDirection;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

public abstract class GuiNpcPanel extends GuiScrollElement {
    protected NpcState state;

    public GuiNpcPanel(Minecraft mc, IKey label) {
        super(mc, ScrollDirection.VERTICAL);

        this.flex().column(5).scroll().vertical().stretch();

        this.add(Elements.label(label).marginBottom(12).marginTop(24));
    }

    public void set(NpcState state) {
        this.state = state;
    }


}
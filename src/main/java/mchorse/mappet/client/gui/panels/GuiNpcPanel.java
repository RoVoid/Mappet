package mchorse.mappet.client.gui.panels;

import mchorse.mappet.api.npcs.Npc;
import mchorse.mappet.api.npcs.NpcState;
import mchorse.mappet.api.utils.ContentType;
import mchorse.mappet.client.gui.GuiMappetDashboard;
import mchorse.mappet.client.gui.npc.GuiNpcEditor;
import mchorse.mappet.client.gui.npc.utils.GuiNpcStatesOverlayPanel;
import mchorse.mappet.client.gui.utils.overlays.GuiOverlay;
import mchorse.mclib.client.gui.framework.GuiBase;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDraw;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.ColorUtils;
import net.minecraft.client.Minecraft;

public class GuiNpcPanel extends GuiMappetDashboardPanel<Npc> {
    public static final IKey EMPTY = IKey.lang("mappet.gui.npcs.info.empty");

    public GuiIconElement states;
    public GuiNpcEditor npcEditor;

    private String state = "";

    public GuiNpcPanel(Minecraft mc, GuiMappetDashboard dashboard) {
        super(mc, dashboard);

        list.setFileIcon(Icons.PROCESSOR);

        states = new GuiIconElement(mc, Icons.MORE, (b) -> openNpcStates());
        states.flex().relative(editor);

        npcEditor = new GuiNpcEditor(mc, false);
        npcEditor.flex().relative(editor).y(10).w(1F, 220).h(1F, -10);
        npcEditor.setVisible(false);

        editor.add(npcEditor, states);

        fill(null);
    }

    private void openNpcStates() {
        GuiNpcStatesOverlayPanel overlay = new GuiNpcStatesOverlayPanel(mc, data, this::pickState);
        GuiOverlay.addOverlay(GuiBase.getCurrent(), overlay.set(state), 0.4F, 0.6F);
    }

    private void pickState(String name) {
        state = name;

        NpcState state = data.states.get(name);

        npcEditor.setVisible(state != null);

        if (state != null) npcEditor.set(state);

        resize();
    }

    @Override
    public void fill(Npc data, boolean allowed) {
        super.fill(data, allowed);

        npcEditor.setVisible(data != null);
        states.setVisible(data != null);

        if (data != null) {
            String key = "default";

            if (!data.states.containsKey(key) && !data.states.isEmpty()) {
                key = data.states.keySet().iterator().next();
            }

            pickState(key);
        }
    }

    @Override
    public ContentType getType() {
        return ContentType.NPC;
    }

    @Override
    public String getTitle() {
        return "mappet.gui.panels.npcs";
    }

    @Override
    public void draw(GuiContext context) {
        if (npcEditor.isVisible()) {
            GuiDraw.drawTextBackground(font, state, states.area.ex() + 3, states.area.my() - 4, 0xffffff, ColorUtils.HALF_BLACK, 2);
        }
        else {
            int w = (editor.area.ex() - area.x) / 2;
            int x = (area.x + editor.area.ex()) / 2 - w / 2;

            GuiDraw.drawMultiText(font, EMPTY.get(), x, area.my(), 0xffffff, w, 12, 0.5F, 0.5F);
        }

        super.draw(context);
    }
}
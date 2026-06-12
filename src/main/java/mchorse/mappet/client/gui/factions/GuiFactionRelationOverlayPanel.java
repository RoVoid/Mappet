package mchorse.mappet.client.gui.factions;

import mchorse.mappet.api.factions.FactionAttitude;
import mchorse.mappet.api.factions.FactionRelation;
import mchorse.mappet.client.gui.panels.GuiFactionPanel;
import mchorse.mappet.client.gui.utils.GuiEnumElement;
import mchorse.mappet.client.gui.utils.overlays.GuiEditorOverlayPanel;
import mchorse.mclib.client.gui.framework.elements.input.GuiColorElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiListElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDraw;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.util.List;
import java.util.function.Consumer;

public class GuiFactionRelationOverlayPanel extends GuiEditorOverlayPanel<FactionRelation.Threshold>
{
    public GuiTextElement title;
    public GuiColorElement color;
    public GuiTrackpadElement score;
    public GuiEnumElement<FactionAttitude> attitude;

    public GuiFactionRelationOverlayPanel(Minecraft mc, FactionRelation relation)
    {
        super(mc, IKey.lang("mappet.gui.factions.overlay.main"));

        list.sorting().setList(relation.thresholds);

        title = new GuiTextElement(mc, 1000, (t) -> item.title = t);
        color = new GuiColorElement(mc, (c) -> item.color = c);
        score = new GuiTrackpadElement(mc, (v) -> item.score = v.intValue()).integer();
        attitude = GuiFactionPanel.createButton(mc, (a) -> item.attitude = a);

        editor.add(Elements.label(IKey.lang("mappet.gui.factions.overlay.title")), title);
        editor.add(Elements.label(IKey.lang("mappet.gui.factions.overlay.color")).marginTop(12), color);
        editor.add(Elements.label(IKey.lang("mappet.gui.factions.overlay.score")).marginTop(12), score);
        editor.add(Elements.label(IKey.lang("mappet.gui.factions.overlay.attitude")).marginTop(12), attitude);

        pickItem(relation.thresholds.isEmpty() ? null : relation.thresholds.get(0), true);
    }

    @Override
    protected GuiListElement<FactionRelation.Threshold> createList(Minecraft mc)
    {
        return new GuiThresholdListElement(mc, (l) -> pickItem(l.get(0), false));
    }

    @Override
    protected IKey getAddLabel()
    {
        return IKey.lang("mappet.gui.factions.overlay.context.add");
    }

    @Override
    protected IKey getRemoveLabel()
    {
        return IKey.lang("mappet.gui.factions.overlay.context.remove");
    }

    @Override
    protected void addNewItem()
    {
        list.add(new FactionRelation.Threshold());
    }

    @Override
    protected void fillData(FactionRelation.Threshold item)
    {
        title.setText(item.title);
        color.picker.setColor(item.color);
        score.setValue(item.score);
        attitude.select(item.attitude);
    }

    public static class GuiThresholdListElement extends GuiListElement<FactionRelation.Threshold>
    {
        public GuiThresholdListElement(Minecraft mc, Consumer<List<FactionRelation.Threshold>> callback)
        {
            super(mc, callback);
        }

        @Override
        protected void drawElementPart(FactionRelation.Threshold element, int i, int x, int y, boolean hover, boolean selected)
        {
            Gui.drawRect(x, y, x + 4, y + scroll.scrollItemSize, 0xff000000 + element.color);
            GuiDraw.drawHorizontalGradientRect(x + 4, y, x + 24, y + scroll.scrollItemSize, 0x44000000 + element.color, element.color);
            super.drawElementPart(element, i, x + 4, y, hover, selected);
        }

        @Override
        protected String elementToString(FactionRelation.Threshold element)
        {
            return element.score + " " + element.title;
        }
    }
}
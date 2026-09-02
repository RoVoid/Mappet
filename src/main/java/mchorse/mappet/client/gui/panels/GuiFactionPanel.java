package mchorse.mappet.client.gui.panels;

import mchorse.mappet.api.factions.Faction;
import mchorse.mappet.api.factions.FactionAttitude;
import mchorse.mappet.api.utils.content.ContentTypes;
import mchorse.mappet.api.utils.content.IContentType;
import mchorse.mappet.client.gui.GuiMappetDashboard;
import mchorse.mappet.client.gui.conditions.GuiOpenConditionButtonElement;
import mchorse.mappet.client.gui.factions.GuiFactionRelationOverlayPanel;
import mchorse.mappet.client.gui.factions.GuiFactions;
import mchorse.mappet.client.gui.factions.GuiFactionsOverlayPanel;
import mchorse.mappet.client.gui.utils.GuiEnumElement;
import mchorse.mappet.client.gui.utils.overlays.GuiOverlay;
import mchorse.mclib.client.gui.framework.GuiBase;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.GuiScrollElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiColorElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDraw;
import mchorse.mclib.client.gui.framework.elements.utils.GuiLabel;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GuiFactionPanel extends GuiMappetDashboardPanel<Faction> {
    public static final IKey EMPTY = IKey.lang("mappet.gui.factions.info.empty");

    public GuiTextElement title;
    public GuiOpenConditionButtonElement visible;
    public GuiColorElement color;
    public GuiTrackpadElement score;

    public GuiEnumElement<FactionAttitude> playerAttitude;
    public GuiEnumElement<FactionAttitude> othersAttitude;

    public GuiButtonElement openOwnRelation;
    public GuiFactions relations;

    private static GuiEnumElement<FactionAttitude> enumTemplate;

    public static GuiEnumElement<FactionAttitude> createButton(Minecraft mc, Consumer<FactionAttitude> callback) {
        if (enumTemplate == null) {
            enumTemplate = new GuiEnumElement<>(mc, FactionAttitude.PASSIVE, callback);
            enumTemplate.bakeLabels("mappet.gui.faction_attitudes");
        }
        return new GuiEnumElement<>(enumTemplate);
    }

    public GuiFactionPanel(Minecraft mc, GuiMappetDashboard dashboard) {
        super(mc, dashboard);

        folderList.setFileIcon(Icons.BOOKMARK);

        title = new GuiTextElement(mc, 1000, (t) -> data.title = t);
        visible = new GuiOpenConditionButtonElement(mc);
        color = new GuiColorElement(mc, (c) -> data.color = c);
        score = new GuiTrackpadElement(mc, (v) -> data.score = v.intValue());
        score.limit(0).integer();

        playerAttitude = createButton(mc, (a) -> data.playerAttitude = a);
        othersAttitude = createButton(mc, (a) -> data.othersAttitude = a);
        openOwnRelation = new GuiButtonElement(mc, IKey.lang("mappet.gui.factions.relations.open"), (b) -> openRelation());
        relations = new GuiFactions(mc);

        GuiElement a = new GuiElement(mc);
        a.flex().column(4).vertical().stretch();
        a.add(Elements.label(IKey.lang("mappet.gui.factions.title")), title);

        GuiElement b = new GuiElement(mc);
        b.flex().w(140).column(4).vertical().stretch();
        b.add(Elements.label(IKey.lang("mappet.gui.factions.color")), color);

        GuiElement c = new GuiElement(mc);
        c.flex().column(4).vertical().stretch();
        c.add(Elements.label(IKey.lang("mappet.gui.factions.others_attitude")), othersAttitude);

        GuiElement d = new GuiElement(mc);
        d.flex().column(4).vertical().stretch();
        d.add(Elements.label(IKey.lang("mappet.gui.factions.player_attitude")), playerAttitude);

        GuiLabel label = Elements.label(IKey.lang("mappet.gui.factions.relations.label")).background();
        GuiIconElement add = new GuiIconElement(mc, Icons.ADD, (button) -> {
            List<String> keys = new ArrayList<>(folderSearch.list.getList());

            keys.removeIf((key) -> data.relations.containsKey(key));
            keys.remove(data.getId());

            if (!keys.isEmpty())
                GuiOverlay.addOverlay(GuiBase.getCurrent(), new GuiFactionsOverlayPanel(this.mc, keys, this::addRelation), 200, 140);
        });

        add.flex().relative(label).xy(1F, 0.5F).w(10).anchor(1F, 0.5F);
        label.add(add);

        GuiScrollElement scrollEditor = createScrollEditor();

        scrollEditor.add(Elements.row(mc, 5, a, b));
        scrollEditor.add(Elements.label(IKey.lang("mappet.gui.factions.visible")).marginTop(12), visible);
        scrollEditor.add(Elements.label(IKey.lang("mappet.gui.factions.score")).marginTop(12), score);
        scrollEditor.add(Elements.row(mc, 5, c, d).marginTop(12));
        scrollEditor.add(label.marginTop(12), relations);
        scrollEditor.add(openOwnRelation);

        editor.add(scrollEditor);

        fill(null);
    }

    private void addRelation(String string) {
        relations.addRelation(string, FactionAttitude.PASSIVE, true);
    }

    private void openRelation() {
        GuiFactionRelationOverlayPanel overlay = new GuiFactionRelationOverlayPanel(mc, data.ownRelation);
        GuiOverlay.addOverlay(GuiBase.getCurrent(), overlay, 0.5F, 0.7F);
    }

    @Override
    public IContentType<Faction> getType() {
        return ContentTypes.FACTION;
    }

    @Override
    public String getTitle() {
        return "mappet.gui.panels.factions";
    }

    @Override
    public void fill(Faction data, String editorName) {
        super.fill(data, editorName);

        editor.setVisible(data != null);

        if (data != null) {
            title.setText(data.title);
            visible.setCondition(data.visible);
            color.picker.setColor(data.color);
            score.setValue(data.score);

            playerAttitude.select(data.playerAttitude);
            othersAttitude.select(data.othersAttitude);

            relations.set(data.relations);
        }

        resize();
    }

    @Override
    public void draw(GuiContext context) {
        super.draw(context);

        if (editor.isVisible()) return;

        int w = editor.area.w / 2;
        int x = editor.area.mx() - w / 2;

        GuiDraw.drawMultiText(font, EMPTY.get(), x, area.my(), 0xffffff, w, 12, 0.5F, 0.5F);
    }
}
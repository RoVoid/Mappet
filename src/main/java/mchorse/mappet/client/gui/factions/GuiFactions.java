package mchorse.mappet.client.gui.factions;

import mchorse.mappet.api.factions.FactionAttitude;
import mchorse.mappet.utils.Colors;
import mchorse.mappet.client.gui.panels.GuiFactionPanel;
import mchorse.mappet.client.gui.utils.GuiEnumElement;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.context.GuiSimpleContextMenu;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

import java.util.Map;

public class GuiFactions extends GuiElement {
    private Map<String, FactionAttitude> relations;

    public GuiFactions(Minecraft mc) {
        super(mc);
        flex().column(5).stretch().vertical();
    }

    /**
     * Add a relation to the relation map
     */
    public void addRelation(String faction, FactionAttitude attitude, boolean put) {
        GuiEnumElement<FactionAttitude> button = GuiFactionPanel.createButton(mc, (a) -> relations.put(faction, a));
        GuiElement row = Elements.row(mc, 5, Elements.label(IKey.str(faction), 20).anchor(0, 0.5F), button);

        button.select(attitude);
        row.context(() -> new GuiSimpleContextMenu(mc).action(Icons.REMOVE, IKey.lang("mappet.gui.factions.relations.context.remove"), () -> {
            row.removeFromParent();
            relations.remove(faction);
            getParentContainer().resize();
        }, Colors.NEGATIVE));

        add(row);

        if (put) relations.put(faction, attitude);

        getParentContainer().resize();
    }

    /**
     * Fill in faction's relation data
     */
    public void set(Map<String, FactionAttitude> relations) {
        this.relations = relations;
        removeAll();
        for (String key : relations.keySet()) addRelation(key, relations.get(key), false);
    }
}
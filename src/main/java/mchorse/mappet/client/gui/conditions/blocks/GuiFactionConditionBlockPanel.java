package mchorse.mappet.client.gui.conditions.blocks;

import mchorse.mappet.api.conditions.blocks.FactionConditionBlock;
import mchorse.mappet.api.utils.TargetMode;
import mchorse.mappet.api.utils.content.ContentTypes;
import mchorse.mappet.client.gui.conditions.GuiConditionOverlayPanel;
import mchorse.mappet.client.gui.utils.GuiComparisonElement;
import mchorse.mappet.client.gui.utils.GuiEnumElement;
import mchorse.mappet.client.gui.utils.GuiMappetUtils;
import mchorse.mappet.client.gui.utils.GuiTargetElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

public class GuiFactionConditionBlockPanel extends GuiAbstractConditionBlockPanel<FactionConditionBlock> {
    public GuiButtonElement id;
    public GuiTargetElement target;
    public GuiComparisonElement comparison;
    public GuiEnumElement<FactionConditionBlock.FactionCheck> faction;

    public GuiFactionConditionBlockPanel(Minecraft mc, GuiConditionOverlayPanel overlay, FactionConditionBlock fcBlock) {
        super(mc, overlay, fcBlock);

        id = new GuiButtonElement(mc, IKey.lang("mappet.gui.overlays.faction"), (t) -> openFactions());
        comparison = new GuiComparisonElement(mc, fcBlock.comparison);
        target = new GuiTargetElement(mc, fcBlock.target).skipGlobal().skip(TargetMode.NPC);
        faction = new GuiEnumElement<>(mc, fcBlock.faction, factionCheck -> block.faction = factionCheck);
        faction.bakeLabels("mappet.gui.faction_attitudes");
        faction.setLabel(FactionConditionBlock.FactionCheck.SCORE, IKey.lang("mappet.gui.conditions.faction.score"));

        add(Elements.row(mc, 5, Elements.column(mc, 5, Elements.label(IKey.lang("mappet.gui.conditions.faction.id")).marginTop(12), id),
                Elements.column(mc, 5, Elements.label(IKey.lang("mappet.gui.conditions.faction.check")).marginTop(12), faction)));
        add(target.marginTop(12));
        add(comparison.marginTop(12));
    }

    private void openFactions() {
        GuiMappetUtils.openPicker(ContentTypes.FACTION, block.id, (name) -> block.id = name);
    }
}
package mchorse.mappet.client.gui.conditions.blocks;

import mchorse.mappet.api.conditions.blocks.QuestConditionBlock;
import mchorse.mappet.api.utils.TargetMode;
import mchorse.mappet.api.utils.content.ContentTypes;
import mchorse.mappet.client.gui.conditions.GuiConditionOverlayPanel;
import mchorse.mappet.client.gui.utils.GuiEnumElement;
import mchorse.mappet.client.gui.utils.GuiMappetUtils;
import mchorse.mappet.client.gui.utils.GuiTargetElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

public class GuiQuestConditionBlockPanel extends GuiAbstractConditionBlockPanel<QuestConditionBlock> {
    public GuiButtonElement id;
    public GuiTargetElement target;
    public GuiEnumElement<QuestConditionBlock.QuestCheck> quest;

    public GuiQuestConditionBlockPanel(Minecraft mc, GuiConditionOverlayPanel overlay, QuestConditionBlock block) {
        super(mc, overlay, block);

        id = new GuiButtonElement(mc, IKey.lang("mappet.gui.overlays.quest"), (t) -> openQuests());
        target = new GuiTargetElement(mc, block.target).skip(TargetMode.NPC);
        quest = new GuiEnumElement<>(mc, block.quest, (qc) -> block.quest = qc);
        quest.bakeLabels("mappet.gui.conditions.quest.types");

        add(Elements.row(mc, 5, Elements.column(mc, 5, Elements.label(IKey.lang("mappet.gui.conditions.quest.id")).marginTop(12), id),
                Elements.column(mc, 5, Elements.label(IKey.lang("mappet.gui.conditions.quest.check")).marginTop(12), quest)));
        add(target.marginTop(12));
    }

    private void openQuests() {
        GuiMappetUtils.openPicker(ContentTypes.QUEST, block.id, (name) -> block.id = name);
    }
}
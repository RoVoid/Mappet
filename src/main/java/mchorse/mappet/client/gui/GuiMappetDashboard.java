package mchorse.mappet.client.gui;

import mchorse.mappet.client.RenderingHandler;
import mchorse.mappet.client.gui.panels.*;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.common.content.PacketContentExit;
import mchorse.mappet.utils.MPIcons;
import mchorse.mclib.client.gui.framework.GuiBase;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.mclib.GuiAbstractDashboard;
import mchorse.mclib.client.gui.mclib.GuiDashboardPanels;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.client.gui.creative.GuiCreativeMorphsMenu;
import mchorse.metamorph.util.MMIcons;
import net.minecraft.client.Minecraft;

import java.util.function.Consumer;

public class GuiMappetDashboard extends GuiAbstractDashboard {
    public static GuiMappetDashboard dashboard;

    public GuiServerSettingsPanel settings;
    public GuiQuestPanel quest;
    public GuiEventPanel event;
    public GuiDialoguePanel dialogue;
    public GuiRegionPanel region;
    public GuiConditionModelPanel conditionModel;
    public GuiNpcPanel npc;
    public GuiFactionPanel faction;
    public GuiQuestChainPanel chain;
    public GuiScriptPanel script;
    public GuiHUDScenePanel hud;
    public GuiLogPanel logs;

    public GuiCreativeMorphsMenu morphs;

    public static GuiMappetDashboard get(Minecraft mc) {
        if (dashboard == null) dashboard = new GuiMappetDashboard(mc);
        return dashboard;
    }

    public GuiMappetDashboard(Minecraft mc) {
        super(mc);
    }

    @Override
    protected GuiDashboardPanels createDashboardPanels(Minecraft mc) {
        return new GuiDashboardPanels(mc);
    }

    public GuiCreativeMorphsMenu getMorphMenu() {
        if (morphs == null) morphs = new GuiCreativeMorphsMenu(Minecraft.getMinecraft(), null).pickUponExit();
        return morphs;
    }

    public void openMorphMenu(GuiElement parent, boolean editing, AbstractMorph morph, Consumer<AbstractMorph> callback) {
        GuiBase.getCurrent().unfocus();

        GuiCreativeMorphsMenu menu = getMorphMenu();

        menu.callback = callback;
        menu.flex().reset().relative(parent).wh(1F, 1F);
        menu.resize();
        menu.setSelected(morph);

        if (editing) menu.enterEditMorph();

        menu.removeFromParent();
        parent.add(menu);
    }

    @Override
    protected void registerPanels(Minecraft mc) {
        settings = new GuiServerSettingsPanel(mc, this);
        quest = new GuiQuestPanel(mc, this);
        event = new GuiEventPanel(mc, this);
        dialogue = new GuiDialoguePanel(mc, this);
        region = new GuiRegionPanel(mc, this);
        conditionModel = new GuiConditionModelPanel(mc, this);
        npc = new GuiNpcPanel(mc, this);
        faction = new GuiFactionPanel(mc, this);
        chain = new GuiQuestChainPanel(mc, this);
        script = new GuiScriptPanel(mc, this);
        hud = new GuiHUDScenePanel(mc, this);
        logs = new GuiLogPanel(mc, this);

        panels.registerPanel(settings, IKey.lang("mappet.gui.panels.settings"), Icons.GEAR);
        panels.registerPanel(quest, IKey.lang("mappet.gui.panels.quests"), Icons.EXCLAMATION);
        panels.registerPanel(event, IKey.lang("mappet.gui.panels.events"), Icons.FILE);
        panels.registerPanel(dialogue, IKey.lang("mappet.gui.panels.dialogues"), Icons.BUBBLE);
        panels.registerPanel(region, IKey.lang("mappet.gui.panels.regions"), Icons.FULLSCREEN);
        panels.registerPanel(conditionModel, IKey.lang("mappet.gui.panels.condition_models"), Icons.BLOCK);
        panels.registerPanel(npc, IKey.lang("mappet.gui.panels.npcs"), Icons.PROCESSOR);
        panels.registerPanel(faction, IKey.lang("mappet.gui.panels.factions"), Icons.BOOKMARK);
        panels.registerPanel(chain, IKey.lang("mappet.gui.panels.chains"), Icons.FOLDER);
        panels.registerPanel(script, IKey.lang("mappet.gui.panels.scripts"), MMIcons.PROPERTIES);
        panels.registerPanel(hud, IKey.lang("mappet.gui.panels.huds"), Icons.POSE);
        panels.registerPanel(logs, IKey.lang("mappet.gui.panels.logs"), MPIcons.get(MPIcons.CONSOLE));

        panels.setPanel(settings);
    }

    @Override
    protected void closeScreen() {
        super.closeScreen();

        Dispatcher.sendToServer(new PacketContentExit());
        RenderingHandler.currentStage = null;
    }
}
package mchorse.mappet.client.gui;

import mchorse.mappet.client.RenderingHandler;
import mchorse.mappet.client.gui.panels.*;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.packets.content.PacketContentExit;
import mchorse.mappet.MappetIcons;
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
    public GuiUIConstructorPanel ui;
    public GuiTranslationPanel translation;
    public GuiLogPanel logs;
    public GuiSnippetsPanel snippets;

    public GuiCreativeMorphsMenu morphs;



    public GuiCraftingTablePanel crafting;


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
        ui = new GuiUIConstructorPanel(mc, this);
        translation = new GuiTranslationPanel(mc, this);
        logs = new GuiLogPanel(mc, this);
        snippets = new GuiSnippetsPanel(mc, this);

        crafting = new GuiCraftingTablePanel(mc, this);


        panels.registerPanel(settings, IKey.lang("mappet.gui.panels.settings"), Icons.GEAR);
        panels.registerPanel(quest, IKey.lang(quest.getTitle()), Icons.EXCLAMATION);
        panels.registerPanel(event, IKey.lang(event.getTitle()), Icons.FILE);
        panels.registerPanel(dialogue, IKey.lang(dialogue.getTitle()), Icons.BUBBLE);
        panels.registerPanel(region, IKey.lang(region.getTitle()), Icons.FULLSCREEN);
        panels.registerPanel(conditionModel, IKey.lang(conditionModel.getTitle()), Icons.BLOCK);
        panels.registerPanel(npc, IKey.lang(npc.getTitle()), Icons.PROCESSOR);
        panels.registerPanel(faction, IKey.lang(faction.getTitle()), Icons.BOOKMARK);
        panels.registerPanel(chain, IKey.lang(chain.getTitle()), Icons.FOLDER);
        panels.registerPanel(script, IKey.lang(script.getTitle()), MMIcons.PROPERTIES);
        panels.registerPanel(hud, IKey.lang(hud.getTitle()), Icons.POSE);
        panels.registerPanel(ui, IKey.lang(ui.getTitle()), Icons.PROCESSOR);
        panels.registerPanel(translation, IKey.lang(translation.getTitle()), MappetIcons.LETTER_A);
        panels.registerPanel(logs, IKey.lang("mappet.gui.panels.logs"), MappetIcons.CONSOLE);
        panels.registerPanel(snippets, IKey.lang("mappet.gui.panels.snippets"), MappetIcons.GLASSES);


        panels.registerPanel(crafting, IKey.lang("mappet.gui.panels.crafting"), Icons.WRENCH);

        
        panels.setPanel(settings);
    }

    @Override
    protected void closeScreen() {
        super.closeScreen();

        Dispatcher.sendToServer(new PacketContentExit());
        RenderingHandler.currentStage = null;
    }
}

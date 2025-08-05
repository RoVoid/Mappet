package mchorse.mappet.client.gui.panels;

import mchorse.mappet.Mappet;
import mchorse.mappet.TriggerEventHandler;
import mchorse.mappet.api.ServerSettings;
import mchorse.mappet.api.states.States;
import mchorse.mappet.api.triggers.Trigger;
import mchorse.mappet.client.gui.GuiMappetDashboard;
import mchorse.mappet.client.gui.hotkey.GuiHotkeysOverlayPanel;
import mchorse.mappet.client.gui.states.GuiStatesEditor;
import mchorse.mappet.client.gui.triggers.GuiTriggerElement;
import mchorse.mappet.client.gui.utils.overlays.GuiOverlay;
import mchorse.mappet.client.gui.utils.overlays.GuiStringOverlayPanel;
import mchorse.mappet.client.gui.utils.text.GuiText;
import mchorse.mappet.client.gui.utils.triggers.TriggerDoc;
import mchorse.mappet.client.gui.utils.triggers.TriggerDocs;
import mchorse.mappet.client.gui.utils.triggers.TriggerVariable;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.common.content.PacketRequestServerSettings;
import mchorse.mappet.network.common.content.PacketRequestStates;
import mchorse.mappet.network.common.content.PacketServerSettings;
import mchorse.mappet.network.common.content.PacketStates;
import mchorse.mappet.utils.MPIcons;
import mchorse.mclib.client.gui.framework.GuiBase;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.GuiScrollElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.context.GuiSimpleContextMenu;
import mchorse.mclib.client.gui.framework.elements.list.GuiLabelListElement;
import mchorse.mclib.client.gui.framework.elements.modals.GuiModal;
import mchorse.mclib.client.gui.framework.elements.modals.GuiPromptModal;
import mchorse.mclib.client.gui.framework.elements.utils.GuiLabel;
import mchorse.mclib.client.gui.mclib.GuiDashboardPanel;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.Label;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.Direction;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GuiServerSettingsPanel extends GuiDashboardPanel<GuiMappetDashboard> {
    public GuiElement states;

    public GuiStatesEditor statesEditor;

    public GuiLabel statesTitle;

    public GuiIconElement statesSwitch;

    public GuiIconElement statesAdd;

    public GuiLabelListElement<String> triggers;

    public GuiTriggerElement trigger;

    public GuiIconElement hotkeys;

    public GuiIconElement triggersToggle;

    public GuiScrollElement editor;

    public GuiLabelListElement<String> forgeTriggers;

    public GuiTriggerElement forgeTrigger;

    public GuiElement globalTriggersLayout;

    public GuiElement forgeTriggersLayout;

    private ServerSettings settings;

    private static String lastTarget = "~";

    private String lastTrigger = "player_chat";

    private String lastForgeTrigger = "";

    public GuiServerSettingsPanel(Minecraft mc, GuiMappetDashboard dashboard) {
        super(mc, dashboard);

        states = new GuiElement(mc);
        states.flex().relative(this).wh(0.5F, 1F);

        statesEditor = new GuiStatesEditor(mc);
        statesEditor.flex().relative(states).y(25).w(1F).h(1F, -25);
        statesTitle = Elements.label(IKey.str("")).anchor(0, 0.5F).background();
        statesTitle.flex().relative(states).xy(10, 10).wh(120, 20);
        statesSwitch = new GuiIconElement(mc, Icons.SEARCH, this::openSearch);
        statesSwitch.flex().relative(states).x(1F, -50).y(10);
        statesAdd = new GuiIconElement(mc, Icons.ADD, this::addState);
        statesAdd.flex().relative(states).x(1F, -30).y(10);

        globalTriggersLayout = new GuiElement(mc);
        globalTriggersLayout.flex().relative(this).wh(0.5F, 0.5F);

        triggers = new GuiLabelListElement<>(mc, (l) -> fillTrigger(l.get(0), false));
        triggers.background().flex().relative(this).x(0.5F, 10).y(35).w(0.5F, -20).h(246);

        trigger = new GuiTriggerElement(mc).onClose(this::updateCurrentTrigger);
        trigger.flex().relative(this).x(1F, -10).y(1F, -10).wh(120, 20).anchor(1F, 1F);
        editor = new GuiScrollElement(mc);
        editor.flex().relative(this).x(0.5F).y(281).w(0.5F).h(1F, -311).column(5).scroll().stretch().padding(10);

        GuiLabel triggersLabel = Elements.label(IKey.lang("mappet.gui.settings.title")).anchor(0, 0.5F).background();

        triggersLabel.flex().relative(this).x(0.5F, 10).y(10).wh(120, 20);

        forgeTriggersLayout = new GuiElement(mc);
        forgeTriggersLayout.flex().relative(this).wh(0.5F, 0.5F);
        forgeTriggersLayout.setVisible(false);

        forgeTriggers = new GuiLabelListElement<>(mc, (l) -> fillForgeTrigger(l.get(0)));
        forgeTriggers.background().flex().relative(this).x(0.5F, 10).y(35).w(0.5F, -20).h(246);
        forgeTriggers.context(() -> new GuiSimpleContextMenu(mc)
                .action(Icons.ADD, IKey.lang("mappet.gui.settings.forge.add"), this::addForgeTrigger)
                .action(Icons.ADD, IKey.lang("mappet.gui.settings.forge.add_from_list"), this::addForgeTriggerFromList)
                .action(Icons.REMOVE, IKey.lang("mappet.gui.settings.forge.remove"), this::removeCurrentForgeTrigger));

        forgeTrigger = new GuiTriggerElement(mc).onClose(this::updateCurrentForgeTrigger);
        forgeTrigger.flex().relative(this).x(1F, -10).y(1F, -10).wh(120, 20).anchor(1F, 1F);

        GuiText forgeAttention = new GuiText(this.mc).text(IKey.lang("mappet.gui.settings.forge.attention"));
        forgeAttention.flex().relative(this).x(0.5F).y(281).w(0.5F).h(1F, -311);
        forgeAttention.padding(10);


        GuiLabel forgeTriggersLabel = Elements
                .label(IKey.lang("mappet.gui.settings.forge_title"))
                .anchor(0, 0.5F)
                .background();

        forgeTriggersLabel.flex().relative(this).x(0.5F, 10).y(10).wh(120, 20);

        hotkeys = new GuiIconElement(mc, MPIcons.get(MPIcons.KEYBOARD), (b) -> openHotkeysEditor());
        hotkeys.tooltip(IKey.lang("mappet.gui.settings.hotkeys"), Direction.LEFT);
        hotkeys.flex().relative(this).x(1F, -16).y(20).wh(20, 20).anchor(0.5F, 0.5F);

        triggersToggle = new GuiIconElement(mc, Icons.PROCESSOR, (i) -> toggleTriggerLayouts());
        boolean enableForgeTriggers = Mappet.enableForgeTriggers.get();
        IKey tooltip = IKey.lang("mappet.gui.settings.forge." + (enableForgeTriggers ? "toggle_triggers" : "disabled"));
        triggersToggle.tooltip(tooltip, Direction.LEFT).setEnabled(enableForgeTriggers);
        triggersToggle.disabledColor(0xFF880000);
        triggersToggle.flex().relative(this).x(1F, -48).y(20).wh(20, 20).anchor(0.5F, 0.5F);


        states.add(statesTitle, statesSwitch, statesAdd, statesEditor);
        globalTriggersLayout.add(triggers, editor, trigger, triggersLabel);
        forgeTriggersLayout.add(forgeTriggers, forgeTrigger, forgeTriggersLabel, forgeAttention);
        add(states, triggersToggle, hotkeys, globalTriggersLayout, forgeTriggersLayout);
    }

    public void toggleTriggerLayouts() {
        boolean trigger = globalTriggersLayout.isVisible();
        triggersToggle.both(trigger ? MPIcons.get(MPIcons.ANVIL) : Icons.PROCESSOR);
        globalTriggersLayout.setVisible(!trigger);
        forgeTriggersLayout.setVisible(trigger);
    }

    public void addForgeTrigger() {
        GuiModal.addFullModal(this, () -> new GuiPromptModal(mc, IKey.lang("mappet.gui.panels.modals.add"), this::addForgeTrigger));
    }

    public void addForgeTrigger(String name) {
        for (Label<String> l : forgeTriggers.getList()) {
            if (l.title.get().equals(name)) return;
        }
        forgeTriggers.add(IKey.str(name), name);
        settings.forgeTriggers.put(name, new Trigger());
    }

    public void removeCurrentForgeTrigger() {
        Label<String> current = forgeTriggers.getCurrentFirst();
        if (current == null) return;
        String name = current.value;
        forgeTriggers.remove(current);
        settings.forgeTriggers.remove(name);
        forgeTrigger.setVisible(false);
    }

    public void addForgeTriggerFromList() {
        Set<String> events = TriggerEventHandler
                .getRegisteredEvents()
                .stream()
                .map(TriggerEventHandler::getEventClassName)
                .collect(Collectors.toSet());
        GuiStringOverlayPanel overlay = new GuiStringOverlayPanel(mc, IKey.lang("mappet.gui.forge.pick"), false, events, this::addForgeTrigger);

        GuiOverlay.addOverlay(GuiBase.getCurrent(), overlay.set(lastTarget), 0.5F, 0.6F);
    }

    private void updateCurrentTrigger() {
        Trigger trigger = settings.triggers.get(lastTrigger);
        triggers.getCurrentFirst().title = createTooltip(lastTrigger, trigger);
    }

    private void updateCurrentForgeTrigger() {
        Trigger trigger = settings.forgeTriggers.get(lastForgeTrigger);
        forgeTriggers.getCurrentFirst().title = createForgeTooltip(lastForgeTrigger, trigger);
    }

    public IKey createTooltip(String key, Trigger trigger) {
        IKey title = IKey.str(TriggerDocs.get(key).name);
        if (trigger.blocks.isEmpty()) return title;
        IKey count = IKey.str(" §7(§6" + trigger.blocks.size() + "§7)§r");
        return IKey.comp(title, count);
    }

    public IKey createForgeTooltip(String key, Trigger trigger) {
        IKey title = IKey.str(key);
        if (trigger.blocks.isEmpty()) return title;
        IKey count = IKey.str(" §7(§6" + trigger.blocks.size() + "§7)§r");
        return IKey.comp(title, count);
    }

    private void openSearch(GuiIconElement element) {
        List<String> targets = new ArrayList<>();

        targets.add("~");

        for (EntityPlayer player : mc.world.playerEntities) {
            targets.add(player.getGameProfile().getName());
        }

        GuiStringOverlayPanel overlay = new GuiStringOverlayPanel(mc, IKey.lang("mappet.gui.states.pick"), false, targets, (target) -> {
            if (target.isEmpty()) return;
            save();
            Dispatcher.sendToServer(new PacketRequestStates(target));
        });

        GuiOverlay.addOverlay(GuiBase.getCurrent(), overlay.set(lastTarget), 0.4F, 0.6F);
    }

    private void addState(GuiIconElement element) {
        statesEditor.addNew();
    }

    private void openHotkeysEditor() {
        GuiHotkeysOverlayPanel overlay = new GuiHotkeysOverlayPanel(mc, settings.hotkeys);
        GuiOverlay.addOverlay(GuiBase.getCurrent(), overlay, 0.5F, 0.7F);
    }

    public void fill(NBTTagCompound tag) {
        settings = new ServerSettings(null);
        settings.deserializeNBT(tag);

        triggers.clear();
        for (String key : settings.triggers.keySet()) triggers.add(createTooltip(key, settings.triggers.get(key)), key);
        triggers.sort();
        triggers.setCurrentValue(lastTrigger);

        forgeTriggers.clear();
        for (String key : settings.forgeTriggers.keySet())
            forgeTriggers.add(createForgeTooltip(key, settings.forgeTriggers.get(key)), key);
        forgeTriggers.sort();
        forgeTrigger.setVisible(false);

        fillTrigger(triggers.getCurrentFirst(), true);

        resize();
    }

    private void fillTrigger(Label<String> trigger, boolean select) {
        TriggerDoc triggerDoc = TriggerDocs.get(trigger.value);
        editor.removeAll();
        editor.add(new GuiText(mc).text(triggerDoc.description).marginTop(8));
        if (triggerDoc.cancelable)
            editor.add(new GuiText(mc).text(IKey.lang("mappet.gui.settings.cancelable")).marginTop(8));
        if (!triggerDoc.variables.isEmpty()) {
            editor.add(Elements
                    .label(IKey.lang("mappet.gui.settings.variables"))
                    .background()
                    .marginTop(16)
                    .marginBottom(8));
            for (TriggerVariable variable : triggerDoc.variables) {
                String text = "§6" + variable.type + " §r" + variable.name + "§7: §r" + variable.description;
                editor.add(new GuiText(mc).text(text));
            }
        }

        this.trigger.set(settings.triggers.get(trigger.value));
        if (select) triggers.setCurrentScroll(trigger);
        lastTrigger = trigger.value;

        resize();
    }

    private void fillForgeTrigger(Label<String> trigger) {
        forgeTrigger.set(settings.forgeTriggers.get(trigger.value));
        forgeTrigger.setVisible(true);
        lastForgeTrigger = trigger.value;
        resize();
    }

    public void fillStates(String target, NBTTagCompound data) {
        States states = new States();

        statesTitle.label = target.equals("~") ? IKey.lang("mappet.gui.states.server") : IKey.format("mappet.gui.states.player", target);
        states.deserializeNBT(data);
        statesEditor.set(states);
        lastTarget = target;
    }

    public void save() {
        if (settings != null) Dispatcher.sendToServer(new PacketServerSettings(settings.serializeNBT()));
        if (statesEditor.get() != null) {
            Dispatcher.sendToServer(new PacketStates(lastTarget, statesEditor
                    .get()
                    .serializeNBT(), statesEditor.getChanges()));
        }
    }

    @Override
    public void appear() {
        super.appear();
        Dispatcher.sendToServer(new PacketRequestServerSettings());
        Dispatcher.sendToServer(new PacketRequestStates(lastTarget));
    }

    @Override

    public void disappear() {
        super.disappear();
        save();
    }

    @Override
    public void close() {
        super.close();
        save();
        statesEditor.set(null);
    }
}
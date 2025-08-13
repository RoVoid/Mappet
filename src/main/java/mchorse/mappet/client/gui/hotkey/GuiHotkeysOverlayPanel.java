package mchorse.mappet.client.gui.hotkey;

import mchorse.mappet.api.hotkeys.Hotkey;
import mchorse.mappet.api.hotkeys.Hotkeys;
import mchorse.mappet.client.gui.conditions.GuiCheckerElement;
import mchorse.mappet.client.gui.triggers.GuiTriggerElement;
import mchorse.mappet.client.gui.utils.AlphaNumericLengthComparator;
import mchorse.mappet.client.gui.utils.GuiMappetUtils;
import mchorse.mappet.client.gui.utils.overlays.GuiOverlayPanel;
import mchorse.mappet.utils.Colors;
import mchorse.mclib.client.gui.framework.elements.GuiScrollElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.context.GuiContextMenu;
import mchorse.mclib.client.gui.framework.elements.context.GuiSimpleContextMenu;
import mchorse.mclib.client.gui.framework.elements.input.GuiKeybindElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiListElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.Direction;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class GuiHotkeysOverlayPanel extends GuiOverlayPanel {
    public GuiHotkeyList list;

    public GuiScrollElement editor;
    public GuiTextElement id;
    public GuiTextElement name;
    public GuiKeybindElement key;
    public GuiButtonElement mode;
    public GuiTriggerElement trigger;
    public GuiCheckerElement enabled;

    private final Hotkeys hotkeys;
    private Hotkey hotkey;

    public GuiHotkeysOverlayPanel(Minecraft mc, Hotkeys hotkeys) {
        super(mc, IKey.lang("mappet.gui.hotkeys.title"));

        this.hotkeys = hotkeys;

        list = new GuiHotkeyList(mc, (l) -> pickHotkey(l.get(0), false));
        list.sorting().setList(new ArrayList<>(hotkeys.keys.values()));
        list.sort();
        list.context(() -> {
            GuiSimpleContextMenu menu = new GuiSimpleContextMenu(this.mc).action(Icons.ADD, IKey.lang("mappet.gui.hotkeys.context.add"), this::addHotkey);
            if (!this.hotkeys.keys.isEmpty()) {
                menu.action(Icons.REMOVE, IKey.lang("mappet.gui.hotkeys.context.remove"), this::removeHotkey, Colors.NEGATIVE);
            }
            return menu.shadow();
        });

        editor = new GuiScrollElement(mc);

        id = new GuiTextElement(mc, 50, null);
        id.tooltip(IKey.lang("mappet.gui.hotkeys.id"), Direction.TOP);
        name = new GuiTextElement(mc, 50, null);
        name.tooltip(IKey.lang("mappet.gui.hotkeys.name"), Direction.TOP);

        key = new GuiKeybindElement(mc, (k) -> {
            if (k == Keyboard.KEY_ESCAPE) {
                hotkey.defaultKeycode = 0;
                key.setKeybind(0);
            }
            else {
                hotkey.defaultKeycode = k;
            }
        });
        key.tooltip(IKey.lang("mappet.gui.hotkeys.key"));
        mode = new GuiButtonElement(mc, IKey.EMPTY, (b) -> {
            int newModeIndex = hotkey.mode.ordinal() + 1;
            if (newModeIndex >= Hotkey.Mode.values().length) newModeIndex = 0;
            hotkey.mode = Hotkey.Mode.values()[newModeIndex];
            mode.label = IKey.lang("mappet.gui.hotkeys.mode." + hotkey.mode.toString());
        });
        mode.tooltip(IKey.lang("mappet.gui.hotkeys.mode"));

        trigger = new GuiTriggerElement(mc);
        enabled = new GuiCheckerElement(mc);

        list.flex().relative(content).w(120).h(1F);
        editor.flex().relative(content).x(120).w(1F, -120).h(1F).column(5).vertical().stretch().scroll().padding(10);

        GuiScrollElement layout = new GuiScrollElement(mc);
        layout.flex().relative(editor).w(1f).h(20).row(5).padding(10);
        layout.add(id, name);

        GuiScrollElement layout1 = new GuiScrollElement(mc);
        layout1.flex().relative(editor).w(1f).h(20).row(5).padding(10);
        layout1.add(key, mode);

        editor.add(layout, layout1);
        editor.add(trigger.marginTop(12));
        editor.add(Elements.label(IKey.lang("mappet.gui.hotkeys.enabled")).marginTop(12), enabled);

        content.add(editor, list);

        pickHotkey(hotkeys.keys.isEmpty() ? null : list.getList().get(0), true);
    }

    private void changeHotkeyId(String newId) {
        newId = newId.trim().replaceAll("[^0-9a-z_.]", "");
        id.field.setText(newId);

        if (hotkey.id.equals(newId)) return;
        if (newId.isEmpty() || hotkeys.keys.containsKey(newId)) {
            id.field.setText(hotkey.id);
            return;
        }

        hotkeys.keys.remove(hotkey.id);
        hotkey.id = newId;
        hotkeys.keys.put(newId, hotkey);
    }

    private void renameHotkey(String newName) {
        newName = newName.trim().replaceAll("[^0-9a-zA-Z_ -]", "");
        name.field.setText(newName);

        if (hotkey.name.equals(newName)) return;
        if (newName.isEmpty()) {
            name.field.setText(hotkey.name);
            return;
        }

        hotkey.name = newName;

        list.sort();
    }

    private void addHotkey() {
        Hotkey hotkey = new Hotkey();
        int index = hotkeys.keys.size();
        while (hotkeys.keys.containsKey("key" + index)) index++;
        hotkey.id = "key" + index;
        hotkey.name = "Key " + index;
        hotkeys.keys.put(hotkey.id, hotkey);
        list.add(hotkey);
        list.update();
        list.sort();
        pickHotkey(hotkey, true);
    }

    private void removeHotkey() {
        hotkeys.keys.remove(list.getCurrentFirst().id);
        int index = list.getIndex();
        list.getList().remove(index);
        list.update();
        list.sort();
        pickHotkey(hotkeys.keys.isEmpty() ? null : list.getList().get(Math.max(0, index - 1)), true);
    }

    private void pickHotkey(Hotkey hotkey, boolean scrollTo) {
        editor.setVisible(hotkey != null);

        if (hotkey == null) return;

        if (this.hotkey != null && !this.hotkey.id.equals(hotkey.id)) {
            changeHotkeyId(id.field.getText());
            renameHotkey(name.field.getText());
        }

        this.hotkey = hotkey;

        id.setText(hotkey.id);
        name.setText(hotkey.name);
        key.setKeybind(hotkey.defaultKeycode);
        mode.label = IKey.lang("mappet.gui.hotkeys.mode." + hotkey.mode.toString());
        trigger.set(hotkey.trigger);
        enabled.set(hotkey.enabled);

        id.unfocus(null);
        name.unfocus(null);

        if (scrollTo) list.setCurrentScroll(hotkey);
        else list.setCurrentDirect(hotkey);
    }

    @Override
    public void draw(GuiContext context) {
        super.draw(context);

        if (hotkeys.keys.isEmpty()) GuiMappetUtils.drawRightClickHere(context, list.area);
    }

    @Override
    public boolean mouseClicked(GuiContext context) {
        if (hotkey != null) {
            if (!id.isFocused() && !id.field.getText().equals(hotkey.id)) changeHotkeyId(id.field.getText());
            if (!name.isFocused() && !name.field.getText().equals(hotkey.name)) renameHotkey(name.field.getText());
        }
        return super.mouseClicked(context);
    }

    @Override
    public void onClose() {
        if (hotkey != null) {
            changeHotkeyId(id.field.getText());
            renameHotkey(name.field.getText());
        }
        super.onClose();
    }

    public static class GuiHotkeyList extends GuiListElement<Hotkey> {
        public GuiHotkeyList(Minecraft mc, Consumer<List<Hotkey>> callback) {
            super(mc, callback);
        }

        @Override
        public boolean mouseClicked(GuiContext context) {
            if (!context.awaitsRightClick && area.isInside(context) && context.mouseButton == 1 && !context.hasContextMenu()) {
                GuiContextMenu menu = createContextMenu(context);
                if (menu != null) {
                    context.setContextMenu(menu);
                    return true;
                }
            }
            if (scroll.mouseClicked(context)) return true;
            if (scroll.isInside(context) && context.mouseButton < 2) {
                int index = scroll.getIndex(context.mouseX, context.mouseY);
                setIndex(index);
                if (exists(index) && callback != null) {
                    callback.accept(getCurrent());
                    return context.mouseButton == 0;
                }
            }
            return false;
        }

        @Override
        protected String elementToString(Hotkey element) {
            return element.name;
        }

        @Override
        protected boolean sortElements() {
            list.sort(Comparator.comparing((Hotkey h) -> h.name, new AlphaNumericLengthComparator()));
            return true;
        }
    }
}
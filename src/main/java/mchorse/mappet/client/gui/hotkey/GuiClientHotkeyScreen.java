package mchorse.mappet.client.gui.hotkey;

import mchorse.mappet.api.hotkeys.Hotkey;
import mchorse.mappet.client.KeyboardHandler;
import mchorse.mclib.client.gui.framework.GuiBase;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.GuiScrollElement;
import mchorse.mclib.client.gui.framework.elements.IGuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiKeybindElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiLabel;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public class GuiClientHotkeyScreen extends GuiBase {
    public GuiScrollElement keybinds;

    public GuiKeybindElement key;
    public GuiButtonElement reset;

    public GuiClientHotkeyScreen(Minecraft mc) {
        keybinds = new GuiScrollElement(mc);
        keybinds.flex().relative(viewport).x(120).w(1F, -120).h(1F).column(5).vertical().stretch().scroll().padding(10);

        for (Hotkey hotkey : KeyboardHandler.hotkeys.values()) {
            keybinds.add(new GuiHotkeyElement(mc, hotkey));
        }

        root.add(keybinds);
        root.resize();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawGradientRect(keybinds.scroll.x, keybinds.scroll.y, keybinds.area.getW(), keybinds.area.getH(), -1072689136, -804253680);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void onGuiClosed() {
        for (IGuiElement element : keybinds.getChildren()) {
            if (element instanceof GuiHotkeyElement) {
                Hotkey newHotkey = ((GuiHotkeyElement) element).hotkey;
                KeyboardHandler.hotkeys.get(newHotkey.name).keycode = newHotkey.keycode;
            }
        }

        super.onGuiClosed();
    }

    private static class GuiHotkeyElement extends GuiElement {
        Hotkey hotkey;

        public GuiLabel name;
        public GuiKeybindElement key;
        public GuiButtonElement reset;

        public GuiHotkeyElement(Minecraft mc, Hotkey hotkey) {
            super(mc);
            this.hotkey = hotkey;
            name = Elements.label(IKey.str(hotkey.name));
            key = new GuiKeybindElement(mc, (k) -> {
                if (k == Keyboard.KEY_ESCAPE) {
                    this.hotkey.keycode = 0;
                    key.setKeybind(0);
                } else {
                    this.hotkey.keycode = k;
                }
            });
            reset = new GuiButtonElement(mc, IKey.lang("mappet.gui.hotkeys.key.reset"), (b) -> {
                key.setKeybind(this.hotkey.defaultKeycode);
                this.hotkey.keycode = -1;
            });
            add(name, key, reset);
            resize();
        }
    }
}
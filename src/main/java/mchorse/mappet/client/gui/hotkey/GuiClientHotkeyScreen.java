package mchorse.mappet.client.gui.hotkey;

import mchorse.mappet.api.hotkeys.Hotkey;
import mchorse.mappet.client.KeyboardHandler;
import mchorse.mappet.client.gui.utils.AlphaNumericLengthComparator;
import mchorse.mclib.client.gui.framework.GuiBase;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.GuiScrollElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiKeybindElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiLabel;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GuiClientHotkeyScreen extends GuiBase {
    public GuiScrollElement keybinds;

    public GuiKeybindElement key;
    public GuiButtonElement reset;

    public GuiClientHotkeyScreen(Minecraft mc) {
        keybinds = new GuiScrollElement(mc);

        List<Hotkey> list = new ArrayList<>(KeyboardHandler.hotkeys.values());
        list.sort(Comparator.comparing((Hotkey h) -> h.name, new AlphaNumericLengthComparator()));
        for (Hotkey hotkey : list) {
            keybinds.add(new GuiHotkeyElement(mc, hotkey));
        }

        keybinds.flex().relative(root).x(0.3f).y(0.25f).w(0.4F, -40).h(0.5F, 5).column(2).vertical().stretch().scroll().padding(10);

        root.add(keybinds);
        root.resize();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        keybinds.area.draw(ColorUtils.HALF_BLACK);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void onGuiClosed() {
        KeyboardHandler.saveClientKeys();
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
                } else this.hotkey.keycode = k;
            });
            key.setKeybind(hotkey.keycode == -1 ? hotkey.defaultKeycode : hotkey.keycode);
            reset = new GuiButtonElement(mc, IKey.lang("mappet.gui.hotkeys.reset"), (b) -> {
                key.setKeybind(this.hotkey.defaultKeycode);
                this.hotkey.keycode = -1;
            });

            name.flex().relative(this).w(0.4f);
            key.flex().relative(this).w(0.2f);
            reset.marginRight(10);
            flex().h(20).row(10).padding(2);
            add(name, key, reset);
        }

        @Override
        public void draw(GuiContext context) {
            super.draw(context);
        }
    }
}
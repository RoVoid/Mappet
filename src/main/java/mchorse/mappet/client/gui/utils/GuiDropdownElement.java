package mchorse.mappet.client.gui.utils;

import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiStringListElement;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

import java.util.function.Consumer;

public class GuiDropdownElement extends GuiButtonElement {

    public GuiStringListElement list;

    public boolean listVisible = false;
    public int height = 100;

    public Consumer<String> listCallback;

    public GuiDropdownElement(Minecraft mc, String label, Consumer<String> callback) {
        super(mc, IKey.str(label == null ? "" : label), null);

        this.callback = (b) -> switchVisible();
        listCallback = callback;

        list = new GuiStringListElement(mc, (l) -> {
            if (listCallback != null) listCallback.accept(l.get(0));
            this.label = IKey.str(l.get(0));
        });
        list.flex().relative(this).anchor(0, 0).y(1F).w(1F).h(height);
        switchVisible(false);
        add(list);
    }

    public void switchVisible() {switchVisible(!listVisible);}

    public void switchVisible(boolean force) {
        listVisible = force;
        list.setVisible(listVisible);
        list.flex().h(listVisible ? height : 0);
        list.resize();
        System.out.println("Switch visible: " + listVisible);
    }
}

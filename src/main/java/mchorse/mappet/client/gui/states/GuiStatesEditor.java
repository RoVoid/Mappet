package mchorse.mappet.client.gui.states;

import mchorse.mappet.api.states.States;
import mchorse.mclib.client.gui.framework.elements.GuiScrollElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDraw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GuiStatesEditor extends GuiScrollElement {
    private States states;
    private final States prestates = new States();

    public GuiStatesEditor(Minecraft mc) {
        super(mc);
        flex().column(5).vertical().stretch().scroll().padding(10);
    }

    public States get() {
        return states;
    }

    public List<String> getChanges() {
        List<String> changes = new ArrayList<>();
        for (String key : prestates.keys()) {
            if (!prestates.areValuesEqual(key, states.values().get(key))) changes.add(key);
        }
        return changes;
    }

    public GuiStatesEditor set(States states) {
        this.states = states;
        prestates.copy(states, true);

        removeAll();

        if (states != null) {
            for (String key : states.keys()) add(new GuiState(mc, key, states));
        }

        sortElements();
        resize();

        return this;
    }

    private void sortElements() {
        getChildren().sort(Comparator.comparing(a -> ((GuiState) a).getKey()));
    }

    public void addNew() {
        if (states == null) {
            return;
        }

        int index = states.keys().size() + 1;
        String key = "state_" + index;

        while (states.has(key)) {
            index += 1;
            key = "state_" + index;
        }

        states.values().put(key, 0);
        add(new GuiState(mc, key, states));

        sortElements();

        getParentContainer().resize();
    }

    @Override
    public void draw(GuiContext context) {
        super.draw(context);

        if (states != null && states.values().isEmpty()) {
            int w = area.w / 2;
            int x = area.mx(w);

            GuiDraw.drawMultiText(font, I18n.format("mappet.gui.states.empty"), x, area.my(), 0xffffff, w, 12, 0.5F, 0.5F);
        }
    }
}
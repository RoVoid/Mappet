package mchorse.mappet.client.gui.scripts.utils.documentation;

import mchorse.mclib.client.gui.framework.elements.GuiScrollElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiListElement;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;

import java.util.List;
import java.util.function.Consumer;

public class DocMethod extends DocEntry {
    public DocMethod(String name) {
        super(name);
    }

    public String getName() {
        if (entries.size() == 1) return name;
        return name + "(" + TextFormatting.GRAY + "..." + TextFormatting.RESET + ")";
    }

    @Override
    public void append(Minecraft mc, GuiScrollElement target) {
        GuiScrollElement panel = new GuiScrollElement(mc);
        GuiMethodVariantList list = new GuiMethodVariantList(mc, l -> {
            panel.scroll.scrollTo(0);
            panel.removeAll();
            l.get(0).append(mc, panel);
        });
    }

    public boolean removeDiscardMethods() {
        entries.removeIf(variant -> ((DocMethodVariant) variant).annotations.contains("mchorse.mappet.api.ui.utils.DiscardMethod"));
        return entries.isEmpty();
    }

    @Override
    public List<DocEntry> getEntries() {
        return parent == null ? null : parent.getEntries();
    }

    public static class GuiMethodVariantList extends GuiListElement<DocMethodVariant> {

        public GuiMethodVariantList(Minecraft mc, Consumer<List<DocMethodVariant>> callback) {
            super(mc, callback);
        }

        @Override
        protected String elementToString(DocMethodVariant element) {
            return element.getName();
        }
    }
}
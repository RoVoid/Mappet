package mchorse.mappet.client.gui.scripts.utils.documentation;

import mchorse.mappet.client.gui.scripts.GuiDocumentationOverlayPanel;
import mchorse.mclib.client.gui.framework.elements.GuiScrollElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiListElement;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;

import java.util.List;
import java.util.function.Consumer;

import static net.minecraftforge.fml.common.eventhandler.ListenerList.resize;

public class DocMethod extends DocEntry {
    public DocMethod(String name) {
        super(name);
    }

    public String getName() {
        if (entries.size() == 1) return entries.get(0).getName();
        return name + "(" + TextFormatting.GRAY + "..." + TextFormatting.RESET + ")";
    }

    @Override
    public void append(Minecraft mc, GuiScrollElement target) {
        if (entries.size() == 1) {
            entries.get(0).append(mc, target);
            return;
        }

        int offset = entries.size() * 25;

        GuiScrollElement variantDocs = new GuiScrollElement(mc);
        variantDocs.flex().relative(target).y(offset).w(1F).h(1F, offset).column(4).vertical().stretch().scroll();

        GuiMethodVariantList list = new GuiMethodVariantList(mc, l -> {
            variantDocs.scroll.scrollTo(0);
            variantDocs.removeAll();
            l.get(0).append(mc, variantDocs);
            System.out.println(l.get(0).getName());
            GuiDocumentationOverlayPanel.instance.resize();
        });
        list.flex().relative(target).w(1F).h(offset);
        for (DocEntry variant : entries) list.add((DocMethodVariant) variant);

        target.add(list, variantDocs);
    }

    public boolean removeDiscardMethods() {
        entries.removeIf(variant -> ((DocMethodVariant) variant).annotations.contains("mchorse.mappet.api.ui.utils.DiscardMethod"));
        return !entries.isEmpty();
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
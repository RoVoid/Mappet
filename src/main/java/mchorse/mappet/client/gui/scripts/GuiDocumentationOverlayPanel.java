package mchorse.mappet.client.gui.scripts;

import mchorse.mappet.client.gui.scripts.utils.documentation.DocDelegate;
import mchorse.mappet.client.gui.scripts.utils.documentation.DocEntry;
import mchorse.mappet.client.gui.scripts.utils.documentation.DocMethod;
import mchorse.mappet.client.gui.scripts.utils.documentation.Docs;
import mchorse.mappet.client.gui.utils.overlays.GuiOverlayPanel;
import mchorse.mappet.utils.MPIcons;
import mchorse.mclib.client.gui.framework.elements.GuiScrollElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiListElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiSearchListElement;
import mchorse.mclib.client.gui.utils.GuiUtils;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GuiDocumentationOverlayPanel extends GuiOverlayPanel {
    public static GuiDocumentationOverlayPanel instance;

    private static Docs docs;
    private static DocEntry pickedEntry;
    private static boolean updatePanel;

    public GuiDocEntrySearchList searchList;
    public GuiScrollElement documentation;
    public GuiIconElement javadocs;
    public GuiIconElement copy;

    public static List<DocMethod> searchMethod(String text) {
        List<DocMethod> list = new ArrayList<>();
        for (DocMethod docMethod : getDocs().methods) {
            if (docMethod.name.equals(text)) list.add(docMethod);
        }
        return list;
    }

    public static Docs getDocs() {
        initDocs();
        return docs;
    }

    private static void initDocs() {
        if (docs != null) return;

        docs = new Docs();
        pickedEntry = null;
        updatePanel = false;

        docs.copyMethods("UILabelBaseComponent",
                         "UIButtonComponent",
                         "UILabelComponent",
                         "UITextComponent",
                         "UITextareaComponent",
                         "UITextboxComponent",
                         "UIToggleComponent");
        docs.remove("MappetUIBuilder");
        docs.remove("MappetUIContext");
        docs.remove("UIParentComponent");
        docs.remove("UILabelBaseComponent");

        docs.remove("GradientGraphic");
        docs.remove("IconGraphic");
        docs.remove("ImageGraphic");
        docs.remove("RectGraphic");
        docs.remove("ShadowGraphic");
        docs.remove("TextGraphic");

        DocEntry top = new DocEntry();
        DocEntry entities = new DocEntry("/ Entities");
        DocEntry player = new DocEntry("/ Player");
        DocEntry nbt = new DocEntry("/ NBT");
        DocEntry items = new DocEntry("/ Items");
        DocEntry blocks = new DocEntry("/ Blocks");
        DocEntry ui = new DocEntry("/ UI");
        DocEntry score = new DocEntry("/ Score");
        DocEntry world = new DocEntry("/ World");
        DocEntry math = new DocEntry("/ Math");

        for (DocEntry docClass : docs.classes) {
            if (docClass.name.contains(".ui") || docClass.displayName.equals("Graphic")) docClass.setParent(ui);
            else if (docClass.name.contains(".math")) docClass.setParent(math);
            else if (docClass.name.contains(".entities.player")) docClass.setParent(player);
            else if (docClass.name.contains(".entities")) docClass.setParent(entities);
            else if (docClass.name.contains(".nbt")) docClass.setParent(nbt);
            else if (docClass.name.contains(".items")) docClass.setParent(items);
            else if (docClass.name.contains(".blocks")) docClass.setParent(blocks);
            else if (docClass.name.contains(".score")) docClass.setParent(score);
            else if (docClass.name.contains(".world")) docClass.setParent(world);
            else docClass.setParent(top);
        }

        player.setParent(entities);

        top.addChildren(entities, nbt, items, blocks, ui, score, world, math);
        pickedEntry = top;
    }

    public GuiDocumentationOverlayPanel(Minecraft mc) {
        this(mc, null);
    }

    public GuiDocumentationOverlayPanel(Minecraft mc, DocMethod method) {
        super(mc, IKey.lang("mappet.gui.scripts.documentation.title"));

        searchList = new GuiDocEntrySearchList(mc, (l) -> pick(l.get(0)));
        searchList.label(IKey.lang("mappet.gui.search"));
        documentation = new GuiScrollElement(mc);

        searchList.flex().relative(content).w(240).h(1F);
        documentation.flex().relative(content).x(240).w(1F, -240).h(1F).column(4).vertical().stretch().scroll().padding(10);

        content.add(searchList, documentation);
        javadocs = new GuiIconElement(mc, MPIcons.get(MPIcons.PLANET), (b) -> openJavadocs());
        javadocs.tooltip(IKey.lang("mappet.gui.scripts.documentation.javadocs")).flex().wh(16, 16);
        copy = new GuiIconElement(mc, Icons.COPY, (b) -> copyName());
        copy.tooltip(IKey.lang("mappet.gui.scripts.documentation.copy")).flex().wh(16, 16);
        copy.setVisible(false);
        icons.flex().row(0).reverse().resize().width(32).height(16);
        icons.addAfter(close, javadocs);
        icons.addAfter(javadocs, copy);

        setupDocs(method);

        instance = this;
    }

    private void pick(DocEntry entry) {
        copy.setVisible(!entry.name.isEmpty() && !entry.name.equals("../"));

        boolean isMethod = entry instanceof DocMethod;
        entry = entry.getEntry();

        if (updatePanel && pickedEntry == entry) {
            updatePanel = false;

            searchList.list.clear();
            if (entry.parent != null) searchList.list.add(new DocDelegate(entry.parent));
            searchList.list.add(entry.getEntries());
            searchList.list.sort();

            if (isMethod) searchList.list.setCurrentScroll(entry);
        }
        else {
            pickedEntry = entry;
            updatePanel = !isMethod;
        }

        documentation.scroll.scrollTo(0);
        documentation.removeAll();
        if (!pickedEntry.name.isEmpty()) entry.render(mc, documentation);

        resize();
    }

    private void setupDocs(DocMethod method) {
        initDocs();
        updatePanel = true;
        if (method != null) pickedEntry = method;
        pick(pickedEntry);
    }

    private void openJavadocs() {
        GuiUtils.openWebLink(I18n.format("mappet.gui.scripts.documentation.javadocs_url"));
    }

    private void copyName() {
        String rawName = searchList.list.getCurrentFirst() == null ? pickedEntry.displayName : searchList.list.getCurrentFirst().displayName;

        rawName = rawName.replaceAll(".*/", "");
//        rawName = rawName.replaceAll("\\(.*?\\)", "");
        rawName = rawName.trim();
//        rawName = rawName.replaceAll("§.", "");

        GuiScreen.setClipboardString(rawName);
    }


    public static class GuiDocEntrySearchList extends GuiSearchListElement<DocEntry> {
        public GuiDocEntrySearchList(Minecraft mc, Consumer<List<DocEntry>> callback) {
            super(mc, callback);
        }

        @Override
        protected GuiListElement<DocEntry> createList(Minecraft minecraft, Consumer<List<DocEntry>> consumer) {
            return new GuiDocEntryList(minecraft, consumer);
        }
    }

    public static class GuiDocEntryList extends GuiListElement<DocEntry> {
        public GuiDocEntryList(Minecraft mc, Consumer<List<DocEntry>> callback) {
            super(mc, callback);
            scroll.scrollItemSize = 16;
            scroll.scrollSpeed *= 2;
        }

        @Override
        protected boolean sortElements() {
            list.sort((a, b) -> {
                if (a instanceof DocMethod && b instanceof DocMethod) {
                    boolean aDep = ((DocMethod) a).isDeprecated;
                    boolean bDep = ((DocMethod) b).isDeprecated;
                    if (aDep != bDep) return aDep ? 1 : -1;
                }
                return a.getName().compareToIgnoreCase(b.getName());
            });
            return true;
        }


        @Override
        protected String elementToString(DocEntry element) {
            return element.getName();
        }
    }
}
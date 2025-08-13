package mchorse.mappet.client.gui.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mchorse.mappet.Mappet;
import mchorse.mappet.client.gui.GuiMappetDashboard;
import mchorse.mappet.client.gui.utils.SafeWebLinkOpener;
import mchorse.mappet.client.gui.utils.snippet.Snippet;
import mchorse.mappet.utils.JsonFetcher;
import mchorse.mclib.client.gui.framework.elements.GuiScrollElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiListElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiSearchListElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiLabel;
import mchorse.mclib.client.gui.mclib.GuiDashboardPanel;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

public class GuiSnippetsPanel extends GuiDashboardPanel<GuiMappetDashboard> {

    public GuiScrollElement snippet;

    public GuiSnippetSearchList searchList;

    private final Map<Integer, Snippet> snippetsCache = new HashMap<>();

    private static String locale = "";

    private int index = -1;

    public GuiSnippetsPanel(Minecraft mc, GuiMappetDashboard dashboard) {
        super(mc, dashboard);

        locale = mc.getLanguageManager().getCurrentLanguage().getLanguageCode().toLowerCase();
        snippetsCache.clear();

        JsonArray jsonList;
        try {
            jsonList = JsonFetcher.fetchJsonArray(JsonFetcher.SNIPPETS + "list.json");
            if (jsonList == null) throw new IOException();
        } catch (IOException e) {
            Mappet.loggerClient.error("Error while trying to load snippets list from URL: {}", e.getMessage());
            GuiLabel error = Elements.label(IKey.lang("mappet.snippet.list.error"));
            error.flex().relative(this).anchor(0.5f, 0.5f);
            add(error);
            return;
        }

        JsonObject jsonAuthors;
        try {
            jsonAuthors = JsonFetcher.fetchJsonObject(JsonFetcher.SNIPPETS + "authors.json");
            if (jsonAuthors == null) throw new IOException();
        } catch (IOException e) {
            Mappet.loggerClient.error("Error while trying to load snippets authors from URL: {}", e.getMessage());
            GuiLabel error = Elements.label(IKey.lang("mappet.snippet.list.error"));
            error.flex().relative(this).anchor(0.5f, 0.5f);
            add(error);
            return;
        }

        searchList = new GuiSnippetSearchList(mc, (l) -> pick(searchList.list.getIndex()));
        searchList.label(IKey.lang("mappet.gui.search"));

        for (int i = 0; i < jsonList.size(); i++) {
            JsonObject json = jsonList.get(i).getAsJsonObject();
            Snippet sn = new Snippet(json);
            sn.setAuthor(jsonAuthors.getAsJsonObject(json.get("author").getAsString()));
            snippetsCache.put(i, sn);
            searchList.list.getList().add(sn);
        }
        searchList.list.update();

        searchList.list.background();
        searchList.flex().relative(this).x(10).y(10).w(160).h(1f, -10);

        snippet = new GuiScrollElement(mc);
        snippet.flex().relative(this).x(170).w(1f, -160).h(1f).column(4).vertical().stretch().scroll().padding(20);

        add(searchList, snippet);

        if (!snippetsCache.isEmpty()) {
            pick(0);
            searchList.list.setIndex(0);
        }
    }

    private void pick(int index) {
        if (this.index != index) pickForce(index);
    }

    private void pickForce(int index) {
        if (index < 0) return;

        Snippet sn = snippetsCache.get(index);
        if (sn == null) {
            Mappet.loggerClient.warn("Snippet at index {} not found in cache", index);
            return;
        }

        if (!sn.hasContent()) {
            try {
                JsonObject json = JsonFetcher.fetchJsonObject(JsonFetcher.SNIPPETS + index + ".json");
                if (json == null) throw new IOException("Received null JSON for snippet " + index);
                sn.setContent(json);
            } catch (IOException e) {
                Mappet.loggerClient.error("Error loading snippet {} from URL: ", index, e);
                return;
            }
        }

        this.index = index;

        snippet.removeAll();

        String tagsStr = sn.getTags().isEmpty() ? "" : " §r(§6#" + String.join(" #", sn.getTags()) + "§r)";
        GuiLabel title = Elements.label(IKey.str("§l" + sn.getTitle(locale) + tagsStr));
        title.marginBottom(10);
        snippet.add(title);

        String rankSuffix = sn.getAuthor().getRank().isEmpty() ? "" : "." + sn.getAuthor().getRank();
        snippet.add(Elements.label(IKey.format("mappet.snippet.author" + rankSuffix, sn.getAuthor().getName())));

        GuiLink url = new GuiLink(Minecraft.getMinecraft(),
                                  IKey.format("mappet.snippet.url", sn.getAuthor().getUrl()),
                                  sn.getAuthor().getUrl());
        snippet.add(url);

        IKey versionKey;
        if (sn.hasActualVersion) versionKey = IKey.format("mappet.snippet.version", Mappet.VERSION);
        else {
            String v = sn.getVersions().isEmpty() ? "unknown" : new ArrayList<>(sn.getVersions()).get(0);
            versionKey = IKey.format("mappet.snippet.version.mismatched", v);
        }
        GuiLabel version = Elements.label(versionKey);
        version.marginBottom(15);
        snippet.add(version);

        sn.getContent(locale).append(Minecraft.getMinecraft(), snippet);

        snippet.resize();
    }

    @Override
    public void draw(GuiContext context) {
        Gui.drawRect(area.x - 2, area.y - 2, area.ex() + 2, area.ey() + 2, -1442840576);
        super.draw(context);
    }

    public void update() {
        pickForce(index);
    }

    public static class GuiSnippetSearchList extends GuiSearchListElement<Snippet> {
        public GuiSnippetSearchList(Minecraft mc, Consumer<List<Snippet>> callback) {
            super(mc, callback);
        }

        @Override
        protected GuiListElement<Snippet> createList(Minecraft minecraft, Consumer<List<Snippet>> consumer) {
            return new GuiSnippetList(minecraft, consumer);
        }
    }

    public static class GuiSnippetList extends GuiListElement<Snippet> {
        private static final Field filterField;
        private static final Field filteredField;

        static {
            try {
                filterField = GuiListElement.class.getDeclaredField("filter");
                filterField.setAccessible(true);

                filteredField = GuiListElement.class.getDeclaredField("filtered");
                filteredField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("Reflection fields initialization failed", e);
            }
        }

        public GuiSnippetList(Minecraft mc, Consumer<List<Snippet>> callback) {
            super(mc, callback);
            scroll.scrollItemSize = 16;
            scroll.scrollSpeed *= 2;
        }

        @Override
        protected boolean sortElements() {
            list.sort((a, b) -> {
                if (!a.hasActualVersion && !b.hasActualVersion) return 0;
                if (!a.hasActualVersion) return -1;
                if (!b.hasActualVersion) return 1;
                return a.getTitle(locale).compareTo(b.getTitle(locale));
            });
            return true;
        }

        @Override
        protected String elementToString(Snippet element) {
            return (element.hasActualVersion ? "" : "§7") + element.getTitle(locale) + " @" + element.getAuthor().getName();
        }

        @Override
        public void filter(String filter) {
            filter = filter.toLowerCase();

            try {
                if (!filter.equals(getFilterField())) {
                    setFilterField(filter);
                    getFilteredField().clear();

                    if (filter.isEmpty()) {
                        update();
                        return;
                    }

                    List<String> tags = new ArrayList<>();
                    String author = "";
                    List<String> titleParts = new ArrayList<>();

                    for (String part : filter.split("\\s+")) {
                        if (part.startsWith("#") && part.length() > 1) tags.add(part.substring(1));
                        else if (part.startsWith("@") && part.length() > 1 && author.isEmpty()) author = part.substring(1);
                        else if (!part.isEmpty()) titleParts.add(part);
                    }

                    for (int i = 0; i < list.size(); i++) {
                        Snippet element = list.get(i);

                        String title = element.getTitle(locale).toLowerCase();
                        String authorName = element.getAuthor().getName().toLowerCase();
                        Set<String> snippetTags = element.getTags();

                        boolean titleMatch = false;
                        for (String part : titleParts) {
                            if (title.contains(part)) {
                                titleMatch = true;
                                break;
                            }
                        }

                        boolean tagsMatch = false;
                        for (String tag : tags) {
                            if (snippetTags.contains(tag)) {
                                tagsMatch = true;
                                break;
                            }
                        }

                        if (titleMatch || authorName.contains(author) || tagsMatch) getFilteredField().add(new Pair<>(element, i));
                    }

                    update();
                }
            } catch (Exception e) {
                Mappet.loggerClient.error("Filter error: ", e);
            }
        }

        private String getFilterField() throws IllegalAccessException {
            return (String) filterField.get(this);
        }

        private void setFilterField(String filter) throws IllegalAccessException {
            filterField.set(this, filter);
        }

        @SuppressWarnings("unchecked")
        private List<Pair<Snippet>> getFilteredField() throws IllegalAccessException {
            return (List<Pair<Snippet>>) filteredField.get(this);
        }
    }

    public static class GuiLink extends GuiLabel {
        private final String url;

        public GuiLink(Minecraft mc, IKey key, String url) {
            super(mc, key);
            this.url = url;
            flex().h(Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT);
        }

        @Override
        public boolean mouseClicked(GuiContext context) {
            if (!area.isInside(context)) return false;
            if (url == null || url.isEmpty()) return super.mouseClicked(context);
            new SafeWebLinkOpener().requestToOpenWebLink(url);
            return true;
        }
    }
}
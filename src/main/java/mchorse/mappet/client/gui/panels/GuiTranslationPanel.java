package mchorse.mappet.client.gui.panels;

import mchorse.mappet.api.translations.Translation;
import mchorse.mappet.api.translations.TranslationManager;
import mchorse.mappet.api.utils.content.ContentTypes;
import mchorse.mappet.api.utils.content.IContentType;
import mchorse.mappet.client.gui.GuiMappetDashboard;
import mchorse.mappet.client.gui.utils.overlays.GuiOverlay;
import mchorse.mappet.client.gui.utils.overlays.GuiStringOverlayPanel;
import mchorse.mappet.MappetIcons;
import mchorse.mclib.client.gui.framework.GuiBase;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.GuiScrollElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GuiTranslationPanel extends GuiMappetDashboardPanel<Translation> {
    public static final Map<String, String> LOCALES = new HashMap<>();

    public GuiIconElement add;
    public GuiScrollElement inputs;

    public GuiTranslationPanel(Minecraft mc, GuiMappetDashboard dashboard) {
        super(mc, dashboard);
        folderList.setFileIcon(MappetIcons.LETTER_A);

        add = new GuiIconElement(mc, Icons.ADD, (e) -> addInput());
        inputs = new GuiScrollElement(mc);

        add.flex().relative(editor).x(20).y(10).w(20).h(20);
        inputs.flex().relative(editor).x(10).y(30).w(0.9f).h(1f, -20).column(5).vertical().stretch().scroll().padding(10);

        editor.setVisible(false);

        editor.add(add, inputs);

        for (String locale : TranslationManager.localesCodes) LOCALES.put(getFormattedLocale(mc, locale), locale);
    }

    public static String getFormattedLocale(Minecraft mc, String locale) {
        return mc != null && TranslationManager.localesCodes.contains(locale) ? mc.getLanguageManager().getLanguage(locale).toString()
                : locale;
    }

    public void addInput() {
        if (data == null) return;

        String locale = mc.getLanguageManager().getCurrentLanguage().getLanguageCode();
        if (data.entries.containsKey(locale))
            locale = TranslationManager.localesCodes.stream().filter(l -> !data.entries.containsKey(l)).findFirst().orElse(null);
        if (locale == null) return; // Всё-таки языков конечное число

        inputs.add(new GuiInputElement(mc, data, locale).setRemoveCallback(() -> add.setVisible(true)));

        add.setVisible(data.entries.size() < TranslationManager.localesCodes.size());
        editor.resize();
        save = true;
    }

    @Override
    public IContentType<Translation> getType() {
        return ContentTypes.TRANSLATION;
    }

    @Override
    public String getTitle() {
        return "mappet.gui.panels.translation";
    }

    @Override
    public void fill(Translation data, String editorName) {
        super.fill(data, editorName);

        editor.setVisible(data != null);
        inputs.removeAll();

        if (data == null) return;

        for (String locale : data.entries.keySet())
            inputs.add(new GuiInputElement(mc, data, locale).setRemoveCallback(() -> add.setVisible(true)));
        inputs.getChildren().sort(Comparator.comparing(a -> ((GuiInputElement) a).getLocale()));

        editor.resize();
    }

    private static class GuiInputElement extends GuiElement {
        GuiButtonElement locale;
        GuiTextElement value;
        GuiIconElement remove;

        Translation data;

        String id;

        public Runnable removeCallback = null;

        public GuiInputElement(Minecraft mc, Translation data, String locale) {
            super(mc);
            this.data = data;
            id = locale;

            if (!data.entries.containsKey(id)) data.entries.put(id, "");

            this.locale = new GuiButtonElement(mc, IKey.str(getFormattedLocale(mc, locale)), (b) -> open());
            value = new GuiTextElement(mc, 1000, this::editValue);
            remove = new GuiIconElement(mc, Icons.REMOVE, this::removeLocale);

            this.locale.flex().relative(this).w(0.3f);

            value.flex().relative(this).w(0.6f);
            value.setText(data.entries.get(id));

            remove.flex().w(20);

            flex().row(10);

            add(remove, this.locale, value);
        }

        public GuiInputElement setRemoveCallback(Runnable callback) {
            removeCallback = callback;
            return this;
        }

        public void open() {
            Set<String> locales = TranslationManager.localesCodes.stream()
                    .filter(l -> !data.entries.containsKey(l))
                    .map(l -> getFormattedLocale(mc, l))
                    .collect(Collectors.toSet());

            GuiStringOverlayPanel overlay = new GuiStringOverlayPanel(mc, IKey.EMPTY, locales, null) {
                @Override
                public void onClose() {
                    String name = getValue();
                    if (name != null && !name.isEmpty()) {
                        id = LOCALES.get(name);
                        locale.label = IKey.str(name);
                    }
                    super.onClose();
                }
            };

            GuiOverlay.addOverlay(GuiBase.getCurrent(), overlay, 0.5F, 0.7F);
        }

        public String getLocale() {
            return id;
        }

        private void editValue(String value) {
            data.entries.put(id, value);
        }

        private void removeLocale(GuiIconElement element) {
            data.entries.remove(id);

            if (removeCallback != null) removeCallback.run();

            GuiElement parent = getParentContainer();
            removeFromParent();
            parent.resize();
        }
    }
}
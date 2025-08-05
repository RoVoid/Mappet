package mchorse.mappet.client.gui.scripts.themes;

import mchorse.mappet.Mappet;
import mchorse.mappet.client.gui.scripts.GuiCodeEditor;
import mchorse.mappet.client.gui.scripts.style.SyntaxStyle;
import mchorse.mappet.client.gui.utils.overlays.GuiEditorOverlayPanel;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiColorElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiListElement;
import mchorse.mclib.client.gui.framework.elements.modals.GuiModal;
import mchorse.mclib.client.gui.framework.elements.modals.GuiPromptModal;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class GuiThemeEditorOverlayPanel extends GuiEditorOverlayPanel<GuiThemeEditorOverlayPanel.SyntaxStyleEntry> {
    public static final String CODE_SAMPLE = "/* Multi-line\n" + "   comment test */\n" + "function main(e) {\n" + "    // This is a single-line comment\n" + "    const PI = 3.14;\n" + "    let hex = 0xFF;\n" + "    var negative = -42;\n" + "    var str1 = \"double quotes\";\n" + "    var str2 = 'single quotes';\n" + "    var isNull = null;\n" + "    var isTrue = true && false || undefined;\n" + "    if (e != null && e.subject() != null) {\n" + "        this.x = Math.max(e.x + 1, 10);\n" + "        JSON.stringify(e);\n" + "        return;\n" + "    }\n" + "    prototype.call(this);\n" + "    customFunc(PI);\n" + "}";

    public GuiIconElement open;

    public GuiTextElement title;
    public GuiToggleElement shadow;

    public GuiColorElement function;
    public GuiColorElement method;
    public GuiColorElement operator;
    public GuiColorElement number;
    public GuiColorElement constant;
    public GuiColorElement identifier;
    public GuiColorElement keyword;
    public GuiColorElement special;
    public GuiColorElement string;
    public GuiColorElement comment;
    public GuiColorElement other;

    public GuiColorElement lineNumbers;
    public GuiColorElement background;

    public GuiCodeEditor preview;
    private SyntaxStyleEntry prevItem = null;

    public GuiThemeEditorOverlayPanel(Minecraft mc) {
        super(mc, IKey.lang("mappet.gui.syntax_theme.main"));

        open = new GuiIconElement(mc, Icons.FOLDER, (b) -> Themes.open());
        open.tooltip(IKey.lang("mappet.gui.syntax_theme.folder")).flex().wh(16, 16);

        title = new GuiTextElement(mc, 100, (s) -> item.style.title = s);
        shadow = new GuiToggleElement(mc, IKey.lang("mappet.gui.syntax_theme.shadow"), (b) -> item.style.shadow = b.isToggled());

        function = new GuiColorElement(mc, (c) -> {
            item.style.functions = c;
            preview.resetHighlight();
        });
        function.tooltip(IKey.lang("mappet.gui.syntax_theme.colors.function"));

        method = new GuiColorElement(mc, (c) -> {
            item.style.methods = c;
            preview.resetHighlight();
        });
        method.tooltip(IKey.lang("mappet.gui.syntax_theme.colors.method"));

        operator = new GuiColorElement(mc, (c) -> {
            item.style.operators = c;
            preview.resetHighlight();
        });
        operator.tooltip(IKey.lang("mappet.gui.syntax_theme.colors.operator"));

        number = new GuiColorElement(mc, (c) -> {
            item.style.numbers = c;
            preview.resetHighlight();
        });
        number.tooltip(IKey.lang("mappet.gui.syntax_theme.colors.number"));

        constant = new GuiColorElement(mc, (c) -> {
            item.style.constants = c;
            preview.resetHighlight();
        });
        constant.tooltip(IKey.lang("mappet.gui.syntax_theme.colors.constant"));

        identifier = new GuiColorElement(mc, (c) -> {
            item.style.identifiers = c;
            preview.resetHighlight();
        });
        identifier.tooltip(IKey.lang("mappet.gui.syntax_theme.colors.identifier"));

        keyword = new GuiColorElement(mc, (c) -> {
            item.style.keywords = c;
            preview.resetHighlight();
        });
        keyword.tooltip(IKey.lang("mappet.gui.syntax_theme.colors.keyword"));

        special = new GuiColorElement(mc, (c) -> {
            item.style.special = c;
            preview.resetHighlight();
        });
        special.tooltip(IKey.lang("mappet.gui.syntax_theme.colors.special"));

        string = new GuiColorElement(mc, (c) -> {
            item.style.strings = c;
            preview.resetHighlight();
        });
        string.tooltip(IKey.lang("mappet.gui.syntax_theme.colors.string"));

        comment = new GuiColorElement(mc, (c) -> {
            item.style.comments = c;
            preview.resetHighlight();
        });
        comment.tooltip(IKey.lang("mappet.gui.syntax_theme.colors.comment"));

        other = new GuiColorElement(mc, (c) -> {
            item.style.other = c;
            preview.resetHighlight();
        });
        other.tooltip(IKey.lang("mappet.gui.syntax_theme.colors.other"));

        lineNumbers = new GuiColorElement(mc, (c) -> item.style.lineNumbers = c);
        lineNumbers.tooltip(IKey.lang("mappet.gui.syntax_theme.background_colors.line_numbers"));
        background = new GuiColorElement(mc, (c) -> item.style.background = c);
        background.tooltip(IKey.lang("mappet.gui.syntax_theme.background_colors.background"));

        preview = new GuiCodeEditor(mc, null);

        editor.add(Elements.label(IKey.lang("mappet.gui.syntax_theme.title")), title, shadow);
        editor.add(Elements.label(IKey.lang("mappet.gui.syntax_theme.colors.title")).marginTop(12));
        editor.add(Elements.row(mc, 5, function, method));
        editor.add(Elements.row(mc, 5, operator, number));
        editor.add(Elements.row(mc, 5, constant, identifier));
        editor.add(Elements.row(mc, 5, keyword, special));
        editor.add(Elements.row(mc, 5, string, comment));
        editor.add(other);

        editor.add(Elements.label(IKey.lang("mappet.gui.syntax_theme.background_colors.title")).marginTop(12));
        editor.add(Elements.row(mc, 5, lineNumbers, background));

        content.flex().h(0.5F);
        preview.flex().relative(this).y(0.5F, 28).w(1F).hTo(area, 1F);
        preview.setText(CODE_SAMPLE);

        add(preview.background());
        icons.add(open);

        loadThemes();
    }

    private void loadThemes() {
        /* Load theme files from the folder */
        for (File file : Themes.themes()) {
            SyntaxStyle style = Themes.readTheme(file);
            if (style == null) continue;
            SyntaxStyleEntry entry = new SyntaxStyleEntry(file, style);
            list.add(entry);
        }

        /* If there are no files, just load the default one */
        if (list.getList().isEmpty()) {
            list.add(new SyntaxStyleEntry(Themes.getThemeFile("monokai.json"), new SyntaxStyle()));
        }

        for (SyntaxStyleEntry entry : list.getList()) {
            if (entry.file.getName().equals(Mappet.scriptEditorSyntaxStyle.getFileName())) {
                pickItem(entry, true);
                break;
            }
        }

        /* Just in case if something went wrong with the config */
        if (list.isDeselected()) {
            list.setIndex(0);
            pickItem(list.getCurrentFirst(), true);
        }
    }

    @Override
    protected GuiListElement<SyntaxStyleEntry> createList(Minecraft mc) {
        return new GuiSyntaxStyleListElement(mc, (l) -> pickItem(l.get(0), false));
    }

    @Override
    protected IKey getAddLabel() {
        return IKey.lang("mappet.gui.syntax_theme.context.add");
    }

    @Override
    protected IKey getRemoveLabel() {
        return IKey.lang("mappet.gui.syntax_theme.context.remove");
    }

    @Override
    protected void addItem() {
        GuiModal.addFullModal(this, () -> new GuiPromptModal(mc, IKey.lang("mappet.gui.syntax_theme.modal.add"), this::addNewTheme));
    }

    private void addNewTheme(String string) {
        File file = Themes.getThemeFile(string);
        if (file.isFile()) return;

        SyntaxStyle style = new SyntaxStyle();
        SyntaxStyleEntry entry = new SyntaxStyleEntry(file, style);

        style.title = "";
        list.add(entry);
        list.update();
        pickItem(entry, true);
    }

    @Override
    protected void removeItem() {
        if (list.getList().size() <= 1) return;
        list.getCurrentFirst().file.delete();
        super.removeItem();
    }

    @Override
    protected void pickItem(SyntaxStyleEntry item, boolean select) {
        if (item == null) return;

        if (prevItem != null) prevItem.save();
        prevItem = item;

        preview.getHighlighter().setStyle(item.style);
        preview.resetHighlight();

        super.pickItem(item, select);
    }

    @Override
    protected void fillData(SyntaxStyleEntry item) {
        title.setText(item.style.title);
        shadow.toggled(item.style.shadow);

        function.picker.setColor(item.style.functions);
        method.picker.setColor(item.style.methods);
        operator.picker.setColor(item.style.operators);
        number.picker.setColor(item.style.numbers);
        constant.picker.setColor(item.style.constants);
        identifier.picker.setColor(item.style.identifiers);
        keyword.picker.setColor(item.style.keywords);
        special.picker.setColor(item.style.special);
        string.picker.setColor(item.style.strings);
        comment.picker.setColor(item.style.comments);
        other.picker.setColor(item.style.other);

        lineNumbers.picker.setColor(item.style.lineNumbers);
        background.picker.setColor(item.style.background);
    }

    @Override
    public void onClose() {
        SyntaxStyleEntry item = list.getCurrentFirst();
        item.save();
        Mappet.scriptEditorSyntaxStyle.set(item.file.getName(), item.style);
        Mappet.scriptCodeTemplate.updateStyle();
        super.onClose();
    }

    public static class GuiSyntaxStyleListElement extends GuiListElement<SyntaxStyleEntry> {
        public GuiSyntaxStyleListElement(Minecraft mc, Consumer<List<SyntaxStyleEntry>> callback) {
            super(mc, callback);
            scroll.scrollItemSize = 16;
        }

        @Override
        protected String elementToString(SyntaxStyleEntry element) {
            if (element.style.title.trim().isEmpty()) return element.file.getName();
            return element.style.title + " (" + element.file.getName() + ")";
        }
    }

    public static class SyntaxStyleEntry {
        public File file;
        public SyntaxStyle style;

        public SyntaxStyleEntry(File file, SyntaxStyle style) {
            this.file = file;
            this.style = style;
        }

        public void save() {
            Themes.writeTheme(file, style);
        }
    }
}
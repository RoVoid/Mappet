package mchorse.mappet.client.gui.scripts.codeEditor;

import mchorse.mappet.config.MappetConfig;
import mchorse.mappet.client.gui.scripts.GuiCodeEditor;
import mchorse.mappet.MappetIcons;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class SearchPanel extends GuiElement {
    public GuiTextElement search;
    public GuiTextElement replace;

    public GuiIconElement searchPrevIcon;
    public GuiIconElement searchNextIcon;
    public GuiIconElement replaceOneIcon;
    public GuiIconElement replaceAllIcon;
    public GuiIconElement closeIcon;
    public GuiIconElement regexIcon;
    public GuiIconElement ignoreCaseIcon;

    private String searchString = "";
    private String replaceString = "";

    private final GuiCodeEditor code;

    private boolean regex = false;
    private boolean ignoreCase = false;
    private boolean invalidRegex = false;

    private int matchCount = 0;
    private int currentMatch = -1;

    public static final int COLOR_ON = 0xFF00FF00;
    public static final int COLOR_OFF = 0xFF888888;

    public SearchPanel(Minecraft mc, GuiCodeEditor code) {
        super(mc);

        this.code = code;

        closeIcon = new GuiIconElement(mc, Icons.CLOSE, (b) -> closeSearch());
        closeIcon.tooltip(IKey.lang("mappet.gui.scripts.search.close"));

        searchPrevIcon = new GuiIconElement(mc, Icons.MOVE_UP, (b) -> navigate(true));
        searchPrevIcon.tooltip(IKey.lang("mappet.gui.scripts.search.prev"));

        searchNextIcon = new GuiIconElement(mc, Icons.MOVE_DOWN, (b) -> navigate(false));
        searchNextIcon.tooltip(IKey.lang("mappet.gui.scripts.search.next"));

        regexIcon = new GuiIconElement(mc, MappetIcons.REGEX, (b) -> {
            toggleIcon(b);
            regex = b.iconColor == COLOR_ON;
            refreshSearch(true);
        }).iconColor(COLOR_OFF).hoverColor(COLOR_OFF);
        regexIcon.tooltip(IKey.lang("mappet.gui.scripts.search.regex"));

        ignoreCaseIcon = new GuiIconElement(mc, MappetIcons.LETTER_CASE, (b) -> {
            toggleIcon(b);
            ignoreCase = b.iconColor == COLOR_ON;
            refreshSearch(true);
        }).iconColor(COLOR_OFF).hoverColor(COLOR_OFF);
        ignoreCaseIcon.tooltip(IKey.lang("mappet.gui.scripts.search.ignore_case"));

        replaceOneIcon = new GuiIconElement(mc, Icons.REVERSE, (b) -> replaceOne());
        replaceOneIcon.tooltip(IKey.lang("mappet.gui.scripts.search.replace_one"));

        replaceAllIcon = new GuiIconElement(mc, Icons.DUPE, (b) -> replaceAll());
        replaceAllIcon.tooltip(IKey.lang("mappet.gui.scripts.search.replace_all"));

        search = new GuiTextElement(mc, Integer.MAX_VALUE, (s) -> {
            searchString = s;
            refreshSearch(true);
        });
        search.field.setMaxStringLength(Integer.MAX_VALUE);

        replace = new GuiTextElement(mc, Integer.MAX_VALUE, (s) -> replaceString = s);
        replace.field.setMaxStringLength(Integer.MAX_VALUE);

        GuiElement rowIcons = Elements.row(mc, 4, regexIcon, ignoreCaseIcon, searchPrevIcon, searchNextIcon,
                closeIcon);
        GuiElement rowSearch = Elements.row(mc, 4, search, replaceOneIcon, replaceAllIcon);
        GuiElement rowReplace = Elements.row(mc, 4, replace);

        rowIcons.flex().relative(this).x(5).y(5).w(1F, -10).h(16);
        rowSearch.flex().relative(this).x(5).y(25).w(1F, -10).h(20);
        rowReplace.flex().relative(this).x(5).y(49).w(1F, -10).h(20);

        add(rowIcons, rowSearch, rowReplace);
        setVisible(false);
    }

    @Override
    public boolean keyTyped(GuiContext context) {
        if (!isVisible()) {
            return false;
        }

        if (context.keyCode == Keyboard.KEY_RETURN || context.keyCode == Keyboard.KEY_NUMPADENTER) {
            navigate(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT));
            return true;
        }

        if (context.keyCode == Keyboard.KEY_ESCAPE) {
            closeSearch();
            return true;
        }

        return super.keyTyped(context);
    }

    @Override
    public void draw(GuiContext context) {
        int backgroundColor = MappetConfig.codeSearchBackgroundColor == null ? 0xCC000000 : MappetConfig.codeSearchBackgroundColor.get();

        area.draw(backgroundColor);
        super.draw(context);

        if (!search.field.isFocused() && search.field.getText().isEmpty()) {
            font.drawStringWithShadow(IKey.lang("mappet.gui.scripts.search.search").get(), search.area.x + 5,
                    search.area.y + 6, 0x888888);
        }

        if (!replace.field.isFocused() && replace.field.getText().isEmpty()) {
            font.drawStringWithShadow(IKey.lang("mappet.gui.scripts.search.replace").get(), replace.area.x + 5,
                    replace.area.y + 6, 0x888888);
        }

        String counter;
        int color;

        if (invalidRegex) {
            counter = IKey.lang("mappet.gui.scripts.search.regex_error").get();
            color = 0xFFFF5555;
        }
        else if (matchCount == 0) {
            counter = "0/0";
            color = 0xAAAAAA;
        }
        else {
            counter = (currentMatch + 1) + "/" + matchCount;
            color = MappetConfig.codeSearchColor == null ? 0x22FFFFAA : MappetConfig.codeSearchColor.get();
        }

        int x = searchNextIcon.area.ex() + 6;
        int y = searchNextIcon.area.y + 4;
        font.drawStringWithShadow(counter, x, y, color);
    }

    public void toggleSearch() {
        if (isVisible()) {
            closeSearch();
            return;
        }

        setVisible(true);
        ((GuiTextEditorSearchable) code).setSearching(true);
        search.field.setFocused(true);
        refreshSearch(true);
    }

    public void closeSearch() {
        setVisible(false);
        invalidRegex = false;
        ((GuiTextEditorSearchable) code).setSearching(false);
    }

    public void refreshSearch(boolean jumpToFirst) {
        Pattern pattern = compilePattern();
        GuiTextEditorSearchable searchable = (GuiTextEditorSearchable) code;

        searchable.setPattern(pattern);
        matchCount = searchable.refreshSearchResults(jumpToFirst);
        currentMatch = searchable.getCurrentMatchIndex();
    }

    public void onEditorChanged() {
        if (!isVisible()) {
            return;
        }

        refreshSearch(false);
    }

    private Pattern compilePattern() {
        invalidRegex = false;

        if (searchString == null || searchString.isEmpty()) {
            return null;
        }

        int flags = ignoreCase ? Pattern.CASE_INSENSITIVE : 0;

        try {
            return Pattern.compile(searchString, flags + (regex ? Pattern.MULTILINE : Pattern.LITERAL));
        } catch (PatternSyntaxException e) {
            invalidRegex = true;
            return null;
        }
    }

    private void navigate(boolean backwards) {
        GuiTextEditorSearchable searchable = (GuiTextEditorSearchable) code;

        if (searchable.navigateMatch(backwards)) {
            currentMatch = searchable.getCurrentMatchIndex();
        }
    }

    public void navigateByKeyboard(boolean backwards) {
        navigate(backwards);
    }

    private void replaceOne() {
        Pattern pattern = compilePattern();

        if (pattern == null || matchCount == 0 || invalidRegex) {
            return;
        }

        GuiTextEditorSearchable searchable = (GuiTextEditorSearchable) code;
        searchable.replaceCurrentMatch(pattern, replaceString);
        refreshSearch(false);
    }

    private void replaceAll() {
        Pattern pattern = compilePattern();

        if (pattern == null || invalidRegex) {
            return;
        }

        GuiTextEditorSearchable searchable = (GuiTextEditorSearchable) code;
        searchable.replaceAllMatches(pattern, replaceString);
        refreshSearch(false);
    }

    private void toggleIcon(GuiIconElement icon) {
        int color = icon.iconColor == COLOR_ON ? COLOR_OFF : COLOR_ON;
        icon.iconColor(color);
        icon.hoverColor(color);
    }
}

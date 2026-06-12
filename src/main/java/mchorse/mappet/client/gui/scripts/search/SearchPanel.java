package mchorse.mappet.client.gui.scripts.search;

import mchorse.mappet.MappetIcons;
import mchorse.mappet.config.MappetConfig;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiLabel;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class SearchPanel extends GuiElement {
    private static final int COLOR_ON = 0xFF00FF00;
    private static final int COLOR_OFF = 0xFF888888;

    private static final int DEFAULT_BACKGROUND_COLOR = 0xCC000000;
    private static final int DEFAULT_MATCH_COLOR = 0x22FFFFAA;
    private static final int ERROR_COLOR = 0xFFFF5555;
    private static final int EMPTY_COLOR = 0xAAAAAA;

    public GuiTextElement search;
    public GuiTextElement replace;

    public GuiLabel matchText;

    public GuiIconElement closeIcon;

    public GuiIconElement regexIcon;
    public GuiIconElement ignoreCaseIcon;

    public GuiIconElement searchPrevIcon;
    public GuiIconElement searchNextIcon;

    public GuiIconElement replaceOneIcon;
    public GuiIconElement replaceAllIcon;

    private final GuiTextEditorSearchable searchable;

    private String searchString = "";
    private String replaceString = "";

    private boolean regex;
    private boolean ignoreCase;
    private boolean invalidRegex;

    private int matchCount;
    private int currentMatch = -1;

    public SearchPanel(Minecraft mc, GuiTextEditorSearchable searchable) {
        super(mc);

        this.searchable = searchable;

        matchText = new GuiLabel(mc, IKey.str("")).anchor(1, 0.5f);

        closeIcon = new GuiIconElement(mc, Icons.CLOSE, b -> closeSearch());
        closeIcon.tooltip(IKey.lang("mappet.gui.scripts.search.close"));

        searchPrevIcon = new GuiIconElement(mc, Icons.MOVE_UP, b -> navigate(true));
        searchPrevIcon.tooltip(IKey.lang("mappet.gui.scripts.search.prev"));

        searchNextIcon = new GuiIconElement(mc, Icons.MOVE_DOWN, b -> navigate(false));
        searchNextIcon.tooltip(IKey.lang("mappet.gui.scripts.search.next"));

        regexIcon = new GuiIconElement(mc, MappetIcons.REGEX, b -> {
            toggleIcon(b);
            regex = b.iconColor == COLOR_ON;
            refreshSearch(true);
        });

        regexIcon.iconColor(COLOR_OFF).hoverColor(COLOR_OFF);
        regexIcon.tooltip(IKey.lang("mappet.gui.scripts.search.regex"));

        ignoreCaseIcon = new GuiIconElement(mc, MappetIcons.LETTER_CASE, b -> {
            toggleIcon(b);
            ignoreCase = b.iconColor == COLOR_ON;
            refreshSearch(true);
        });

        ignoreCaseIcon.iconColor(COLOR_OFF).hoverColor(COLOR_OFF);
        ignoreCaseIcon.tooltip(IKey.lang("mappet.gui.scripts.search.ignore_case"));

        replaceOneIcon = new GuiIconElement(mc, Icons.REVERSE, b -> replaceOne());
        replaceOneIcon.tooltip(IKey.lang("mappet.gui.scripts.search.replace_one"));

        replaceAllIcon = new GuiIconElement(mc, Icons.DUPE, b -> replaceAll());
        replaceAllIcon.tooltip(IKey.lang("mappet.gui.scripts.search.replace_all"));

        search = new GuiTextElement(mc, Integer.MAX_VALUE, s -> {
            searchString = s;
            refreshSearch(true);
        });

        search.field.setMaxStringLength(Integer.MAX_VALUE);

        replace = new GuiTextElement(mc, Integer.MAX_VALUE, s -> replaceString = s);
        replace.field.setMaxStringLength(Integer.MAX_VALUE);

        GuiElement rowIcons = Elements.row(mc, 4, regexIcon, ignoreCaseIcon, matchText, closeIcon);

        GuiElement rowSearch = Elements.row(mc, 4, search, searchPrevIcon, searchNextIcon);

        GuiElement rowReplace = Elements.row(mc, 4, replace, replaceOneIcon, replaceAllIcon);

        rowIcons.flex().relative(this).x(5).y(5).w(1F, -10).h(16);
        rowSearch.flex().relative(this).x(5).y(25).w(1F, -10).h(20);
        rowReplace.flex().relative(this).x(5).y(49).w(1F, -10).h(20);

        add(rowIcons, rowSearch, rowReplace);

        setVisible(false);
    }

    @Override
    public boolean keyTyped(GuiContext context) {
        if (!isVisible()) return false;

        switch (context.keyCode) {
            case Keyboard.KEY_RETURN:
            case Keyboard.KEY_NUMPADENTER:
                navigate(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT));
                return true;

            case Keyboard.KEY_ESCAPE:
                closeSearch();
                return true;

            default:
                return super.keyTyped(context);
        }
    }

    @Override
    public void draw(GuiContext context) {
        area.draw(getBackgroundColor());
        super.draw(context);
        drawPlaceholders();
    }

    private void drawPlaceholders() {
        if (!search.field.isFocused() && search.field.getText().isEmpty())
            font.drawStringWithShadow(IKey.lang("mappet.gui.scripts.search.search").get(), search.area.x + 5, search.area.y + 6, COLOR_OFF);

        if (!replace.field.isFocused() && replace.field.getText().isEmpty())
            font.drawStringWithShadow(IKey.lang("mappet.gui.scripts.search.replace").get(), replace.area.x + 5, replace.area.y + 6, COLOR_OFF);
    }

    public void toggleSearch() {
        if (isVisible()) {
            closeSearch();
            return;
        }
        setVisible(true);
        searchable.setSearching(true);
        search.field.setFocused(true);
        refreshSearch(true);
    }

    public void closeSearch() {
        setVisible(false);
        invalidRegex = false;
        searchable.setSearching(false);
    }

    private void updateMatchLabel() {
        if (invalidRegex) matchText.label = IKey.lang("mappet.gui.scripts.search.regex_error");
        else if (matchCount == 0) matchText.label = IKey.str("0/0");
        else matchText.label = IKey.str((currentMatch + 1) + "/" + matchCount);
    }

    public void refreshSearch(boolean jumpToFirst) {
        Pattern pattern = compilePattern();

        searchable.setPattern(pattern);

        matchCount = searchable.refreshSearchResults(jumpToFirst);
        currentMatch = searchable.getCurrentMatchIndex();

        updateMatchLabel();
    }

    public void onEditorChanged() {
        if (isVisible()) refreshSearch(false);
    }

    public void navigateByKeyboard(boolean backwards) {
        navigate(backwards);
    }

    private void navigate(boolean backwards) {
        if (!searchable.navigateMatch(backwards)) return;
        currentMatch = searchable.getCurrentMatchIndex();
        updateMatchLabel();
    }

    private void replaceOne() {
        Pattern pattern = getValidPattern();
        if (pattern == null || matchCount == 0) return;

        searchable.replaceCurrentMatch(pattern, replaceString);
        refreshSearch(false);
    }

    private void replaceAll() {
        Pattern pattern = getValidPattern();
        if (pattern == null) return;

        searchable.replaceAllMatches(pattern, replaceString);
        refreshSearch(false);
    }

    private Pattern getValidPattern() {
        Pattern pattern = compilePattern();
        if (pattern == null || invalidRegex) return null;
        return pattern;
    }

    private Pattern compilePattern() {
        invalidRegex = false;

        if (searchString == null || searchString.isEmpty()) return null;

        int flags = 0;

        if (ignoreCase) flags |= Pattern.CASE_INSENSITIVE;

        if (regex) flags |= Pattern.MULTILINE;
        else flags |= Pattern.LITERAL;

        try {
            return Pattern.compile(searchString, flags);
        } catch (PatternSyntaxException e) {
            invalidRegex = true;
            updateMatchLabel();
            return null;
        }
    }

    private int getBackgroundColor() {
        return MappetConfig.codeSearchBackgroundColor == null ? DEFAULT_BACKGROUND_COLOR : MappetConfig.codeSearchBackgroundColor.get();
    }

    private int getMatchColor() {
        return MappetConfig.codeSearchColor == null ? DEFAULT_MATCH_COLOR : MappetConfig.codeSearchColor.get();
    }

    private void toggleIcon(GuiIconElement icon) {
        int color = icon.iconColor == COLOR_ON ? COLOR_OFF : COLOR_ON;

        icon.iconColor(color);
        icon.hoverColor(color);
    }
}
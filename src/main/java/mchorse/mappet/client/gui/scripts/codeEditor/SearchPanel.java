package mchorse.mappet.client.gui.scripts.codeEditor;

import mchorse.mappet.client.gui.scripts.GuiCodeEditor;
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

import mchorse.mappet.modules.utils.UtilsModule;

public class SearchPanel extends GuiElement
{
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

    public SearchPanel(Minecraft mc, GuiCodeEditor code)
    {
        super(mc);

        this.code = code;

        this.closeIcon = new GuiIconElement(mc, Icons.CLOSE, (b) -> this.closeSearch());
        this.closeIcon.tooltip(IKey.lang("mappet.gui.scripts.search.close"));

        this.searchPrevIcon = new GuiIconElement(mc, Icons.MOVE_UP, (b) -> this.navigate(true));
        this.searchPrevIcon.tooltip(IKey.lang("mappet.gui.scripts.search.prev"));

        this.searchNextIcon = new GuiIconElement(mc, Icons.MOVE_DOWN, (b) -> this.navigate(false));
        this.searchNextIcon.tooltip(IKey.lang("mappet.gui.scripts.search.next"));

        this.regexIcon = new GuiIconElement(mc, Icons.SEARCH, (b) -> {
            this.toggleIcon(b);
            this.regex = b.iconColor == COLOR_ON;
            this.refreshSearch(true);
        }).iconColor(COLOR_OFF).hoverColor(COLOR_OFF);
        this.regexIcon.tooltip(IKey.lang("mappet.gui.scripts.search.regex"));

        this.ignoreCaseIcon = new GuiIconElement(mc, Icons.HELP, (b) -> {
            this.toggleIcon(b);
            this.ignoreCase = b.iconColor == COLOR_ON;
            this.refreshSearch(true);
        }).iconColor(COLOR_OFF).hoverColor(COLOR_OFF);
        this.ignoreCaseIcon.tooltip(IKey.lang("mappet.gui.scripts.search.ignore_case"));

        this.replaceOneIcon = new GuiIconElement(mc, Icons.REVERSE, (b) -> this.replaceOne());
        this.replaceOneIcon.tooltip(IKey.lang("mappet.gui.scripts.search.replace_one"));

        this.replaceAllIcon = new GuiIconElement(mc, Icons.DUPE, (b) -> this.replaceAll());
        this.replaceAllIcon.tooltip(IKey.lang("mappet.gui.scripts.search.replace_all"));

        this.search = new GuiTextElement(mc, Integer.MAX_VALUE, (s) -> {
            this.searchString = s;
            this.refreshSearch(true);
        });
        this.search.field.setMaxStringLength(Integer.MAX_VALUE);

        this.replace = new GuiTextElement(mc, Integer.MAX_VALUE, (s) -> this.replaceString = s);
        this.replace.field.setMaxStringLength(Integer.MAX_VALUE);

        GuiElement rowIcons = Elements.row(mc, 4, this.regexIcon, this.ignoreCaseIcon, this.searchPrevIcon, this.searchNextIcon, this.closeIcon);
        GuiElement rowSearch = Elements.row(mc, 4, this.search, this.replaceOneIcon, this.replaceAllIcon);
        GuiElement rowReplace = Elements.row(mc, 4, this.replace);

        rowIcons.flex().relative(this).x(5).y(5).w(1F, -10).h(16);
        rowSearch.flex().relative(this).x(5).y(25).w(1F, -10).h(20);
        rowReplace.flex().relative(this).x(5).y(49).w(1F, -10).h(20);

        this.add(rowIcons, rowSearch, rowReplace);
        this.setVisible(false);
    }

    @Override
    public boolean keyTyped(GuiContext context)
    {
        if (!this.isVisible())
        {
            return false;
        }

        if (context.keyCode == Keyboard.KEY_RETURN || context.keyCode == Keyboard.KEY_NUMPADENTER)
        {
            this.navigate(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT));
            return true;
        }

        if (context.keyCode == Keyboard.KEY_ESCAPE)
        {
            this.closeSearch();
            return true;
        }

        return super.keyTyped(context);
    }

    @Override
    public void draw(GuiContext context)
    {
        int backgroundColor = UtilsModule.getInstance().codeSearchBackgroundColor == null
                ? 0xCC000000
                : UtilsModule.getInstance().codeSearchBackgroundColor.get();

        this.area.draw(backgroundColor);
        super.draw(context);

        if (!this.search.field.isFocused() && this.search.field.getText().isEmpty())
        {
            this.font.drawStringWithShadow(IKey.lang("mappet.gui.scripts.search.search").get(), this.search.area.x + 5, this.search.area.y + 6, 0x888888);
        }

        if (!this.replace.field.isFocused() && this.replace.field.getText().isEmpty())
        {
            this.font.drawStringWithShadow(IKey.lang("mappet.gui.scripts.search.replace").get(), this.replace.area.x + 5, this.replace.area.y + 6, 0x888888);
        }

        String counter;
        int color;

        if (this.invalidRegex)
        {
            counter = IKey.lang("mappet.gui.scripts.search.regex_error").get();
            color = 0xFFFF5555;
        }
        else if (this.matchCount == 0)
        {
            counter = "0/0";
            color = 0xAAAAAA;
        }
        else
        {
            counter = (this.currentMatch + 1) + "/" + this.matchCount;
            color = UtilsModule.getInstance().codeSearchColor == null
                    ? 0x22FFFFAA
                    : UtilsModule.getInstance().codeSearchColor.get();
        }

        int x = this.searchNextIcon.area.ex() + 6;
        int y = this.searchNextIcon.area.y + 4;
        this.font.drawStringWithShadow(counter, x, y, color);
    }

    public void toggleSearch()
    {
        if (this.isVisible())
        {
            this.closeSearch();
            return;
        }

        this.setVisible(true);
        ((GuiTextEditorSearchable) this.code).setSearching(true);
        this.search.field.setFocused(true);
        this.refreshSearch(true);
    }

    public void closeSearch()
    {
        this.setVisible(false);
        this.invalidRegex = false;
        ((GuiTextEditorSearchable) this.code).setSearching(false);
    }

    public void refreshSearch(boolean jumpToFirst)
    {
        Pattern pattern = this.compilePattern();
        GuiTextEditorSearchable searchable = (GuiTextEditorSearchable) this.code;

        searchable.setPattern(pattern);
        this.matchCount = searchable.refreshSearchResults(jumpToFirst);
        this.currentMatch = searchable.getCurrentMatchIndex();
    }

    public void onEditorChanged()
    {
        if (!this.isVisible())
        {
            return;
        }

        this.refreshSearch(false);
    }

    private Pattern compilePattern()
    {
        this.invalidRegex = false;

        if (this.searchString == null || this.searchString.isEmpty())
        {
            return null;
        }

        int flags = this.ignoreCase ? Pattern.CASE_INSENSITIVE : 0;

        try
        {
            return Pattern.compile(this.searchString, flags + (this.regex ? Pattern.MULTILINE : Pattern.LITERAL));
        }
        catch (PatternSyntaxException e)
        {
            this.invalidRegex = true;
            return null;
        }
    }

    private void navigate(boolean backwards)
    {
        GuiTextEditorSearchable searchable = (GuiTextEditorSearchable) this.code;

        if (searchable.navigateMatch(backwards))
        {
            this.currentMatch = searchable.getCurrentMatchIndex();
        }
    }

    public void navigateByKeyboard(boolean backwards)
    {
        this.navigate(backwards);
    }

    private void replaceOne()
    {
        Pattern pattern = this.compilePattern();

        if (pattern == null || this.matchCount == 0 || this.invalidRegex)
        {
            return;
        }

        GuiTextEditorSearchable searchable = (GuiTextEditorSearchable) this.code;
        searchable.replaceCurrentMatch(pattern, this.replaceString);
        this.refreshSearch(false);
    }

    private void replaceAll()
    {
        Pattern pattern = this.compilePattern();

        if (pattern == null || this.invalidRegex)
        {
            return;
        }

        GuiTextEditorSearchable searchable = (GuiTextEditorSearchable) this.code;
        searchable.replaceAllMatches(pattern, this.replaceString);
        this.refreshSearch(false);
    }

    private void toggleIcon(GuiIconElement icon)
    {
        int color = icon.iconColor == COLOR_ON ? COLOR_OFF : COLOR_ON;
        icon.iconColor(color);
        icon.hoverColor(color);
    }
}

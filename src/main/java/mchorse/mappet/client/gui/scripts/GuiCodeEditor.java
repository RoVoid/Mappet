package mchorse.mappet.client.gui.scripts;

import mchorse.mappet.client.gui.scripts.codeEditor.GuiTextEditorSearchable;
import mchorse.mappet.client.gui.scripts.codeEditor.SearchPanel;
import mchorse.mappet.client.gui.scripts.style.SyntaxHighlighter;
import mchorse.mappet.client.gui.scripts.utils.HighlightedTextLine;
import mchorse.mappet.client.gui.scripts.utils.TextLineNumber;
import mchorse.mappet.client.gui.scripts.utils.TextSegment;
import mchorse.mappet.client.gui.utils.text.GuiMultiTextElement;
import mchorse.mappet.client.gui.utils.text.undo.TextEditUndo;
import mchorse.mappet.client.gui.utils.text.utils.Cursor;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDraw;
import mchorse.mclib.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuiCodeEditor extends GuiMultiTextElement<HighlightedTextLine> implements GuiTextEditorSearchable
{
    private SyntaxHighlighter highlighter;
    private int placements;
    private boolean lines = true;

    private final List<TextLineNumber> numbers = new ArrayList<>(40);
    private int lineNumber = 0;

    private SearchPanel searchPanel;
    private Pattern pattern;
    private boolean searching;
    private final List<SearchMatch> searchMatches = new ArrayList<>();
    private int currentSearchMatch = -1;

    public GuiCodeEditor(Minecraft mc, Consumer<String> callback) {
        super(mc, callback);
        highlighter = new SyntaxHighlighter();
    }

    public void setSearchPanel(SearchPanel searchPanel)
    {
        this.searchPanel = searchPanel;
    }

    @Override
    protected HighlightedTextLine createTextLine(String line) {
        return new HighlightedTextLine(line);
    }

    public GuiCodeEditor disableLines() {
        lines = false;

        return this;
    }

    public SyntaxHighlighter getHighlighter() {
        return highlighter;
    }

    public void setHighlighter(SyntaxHighlighter highlighter) {
        this.highlighter = highlighter;
    }

    public void resetHighlight() {
        for (HighlightedTextLine textLine : text) textLine.resetSegments();
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        resetHighlight();

        if (this.searching)
        {
            if (this.searchPanel != null) this.searchPanel.onEditorChanged();
            else refreshSearchResults(false);
        }
    }

    @Override
    protected void recalculateSizes() {
        /* Calculate how many pixels will number lines will occupy horizontally */
        double power = Math.ceil(Math.log10(text.size() + 1));
        placements = (int) power * 6;
        super.recalculateSizes();
    }

    @Override
    protected void changedLine(int i) {
        String line = text.get(i).text;
        if (line.contains("/*") || line.contains("*/")) changedLineAfter(i);
        else {
            super.changedLine(i);
            text.get(i).resetSegments();
        }

        if (this.searching)
        {
            if (this.searchPanel != null) this.searchPanel.onEditorChanged();
            else refreshSearchResults(false);
        }
    }

    @Override
    protected void changedLineAfter(int index) {
        super.changedLineAfter(index);
        while (index < text.size()) text.get(index++).resetSegments();

        if (this.searching)
        {
            if (this.searchPanel != null) this.searchPanel.onEditorChanged();
            else refreshSearchResults(false);
        }
    }

    /* Change input behavior */

    @Override
    protected String getFromChar(char typedChar) {
        if (wasDoubleInsert(typedChar, ')', '(') || wasDoubleInsert(typedChar, ']', '[') || wasDoubleInsert(typedChar, '}',
                '{') || wasDoubleInsert(typedChar, '"', '"') || wasDoubleInsert(typedChar, '\'', '\'')) {
            moveCursor(1, 0);
            playSound(SoundEvents.BLOCK_STONE_PLACE);
            return "";
        }

        switch (typedChar) {
            case '(':
                return "()";
            case '[':
                return "[]";
            case '{':
                return "{}";
            case '"':
                return "\"\"";
            case '\'':
                return "''";
        }

        return super.getFromChar(typedChar);
    }

    private boolean wasDoubleInsert(char input, char target, char supplementary) {
        if (input != target) {
            return false;
        }

        String line = text.get(cursor.line).text;

        return line.length() >= 2 && cursor.offset > 0 && cursor.offset < line.length() && line.charAt(
                cursor.offset) == target && line.charAt(cursor.offset - 1) == supplementary;
    }

    @Override
    protected void keyNewLine(TextEditUndo undo) {
        String line = text.get(cursor.line).text;
        boolean unwrap = line.length() >= 2 && cursor.offset > 0 && cursor.offset < line.length() && line.charAt(
                cursor.offset) == '}' && line.charAt(cursor.offset - 1) == '{';

        int indent = getIndent(line) + (unwrap ? 4 : 0);

        super.keyNewLine(undo);

        String margin = createIndent(indent);

        writeString(margin);
        cursor.offset = indent;

        undo.postText += margin;

        if (unwrap) {
            super.keyNewLine(undo);

            margin = createIndent(indent - 4);

            writeString(margin);
            cursor.line -= 1;
            cursor.offset = indent;

            undo.postText += margin;
        }
    }

    @Override
    protected void keyBackspace(TextEditUndo undo, boolean ctrl) {
        String line = text.get(cursor.line).text;

        line = cursor.start(line);

        if (!line.isEmpty() && line.trim().isEmpty()) {
            int offset = 4 - line.length() % 4;

            startSelecting();
            cursor.offset -= offset;

            String deleted = getSelectedText();

            deleteSelection();
            deselect();

            undo.text = deleted;
        }
        else {
            super.keyBackspace(undo, ctrl);
        }
    }

    @Override
    protected void keyTab(TextEditUndo undo) {
        if (isSelected()) {
            boolean shift = GuiScreen.isShiftKeyDown();
            Cursor min = getMin();

            if (shift) {
                min.offset = Math.max(min.offset - 4, 0);
            }

            Cursor temp = new Cursor();
            List<String> splits = GuiMultiTextElement.splitNewlineString(getSelectedText());

            for (int i = 0; i < splits.size(); i++) {
                if (shift) {
                    int indent = getIndent(splits.get(i));

                    splits.set(i, splits.get(i).substring(Math.min(indent, 4)));
                }
                else {
                    splits.set(i, "    " + splits.get(i));
                }
            }

            String result = String.join("\n", splits);

            temp.copy(min);
            deleteSelection();
            writeString(result);
            getMin().set(min.line, splits.get(splits.size() - 1).length());
            min.copy(temp);

            if (!shift) {
                min.offset += 4;
            }

            undo.postText = result;
        }
        else {
            super.keyTab(undo);
        }
    }

    public int getIndent(int i) {
        if (hasLine(i)) {
            return getIndent(text.get(i).text);
        }

        return 0;
    }

    public int getIndent(String line) {
        for (int j = 0; j < line.length(); j++) {
            char c = line.charAt(j);

            if (c != ' ') {
                return j;
            }
        }

        return line.length();
    }

    public String createIndent(int i) {
        StringBuilder builder = new StringBuilder();

        while (i > 0) {
            builder.append(' ');

            i -= 1;
        }

        return builder.toString();
    }

    /* Replacing rendering */

    @Override
    protected void drawTextLine(String line, int i, int j, int nx, int ny) {
        /* Cache line number to be later rendered in drawForeground() */
        if (lines && j == 0) {
            String label = String.valueOf(i + 1);

            int x = area.x + 5 + placements - font.getStringWidth(label);

            if (lineNumber >= numbers.size()) {
                numbers.add(new TextLineNumber());
            }

            numbers.get(lineNumber).set(label, x, ny);
            lineNumber += 1;
        }

        /* Draw  */
        HighlightedTextLine textLine = text.get(i);
        if (textLine.segments == null) {
            List<TextSegment> lastSegments = null;
            if (i > 0 && text.get(i - 1) != null) lastSegments = text.get(i - 1).segments;

            TextSegment lastSegment = null;
            if (lastSegments != null && !lastSegments.isEmpty()) lastSegment = lastSegments.get(lastSegments.size() - 1);

            textLine.setSegments(highlighter.parse(font, textLine.text, lastSegment));

            if (textLine.wrappedLines != null) textLine.calculateWrappedSegments(font);
        }

        List<TextSegment> segments = textLine.segments;

        if (textLine.wrappedSegments != null) {
            segments = j < textLine.wrappedSegments.size() ? textLine.wrappedSegments.get(j) : null;
        }

        if (segments != null) {
            GlStateManager.enableBlend();
            for (TextSegment s : segments) {
                int color = s.alpha < 0x1A ? s.color & 0xFFFFFF : s.color; // Because of OpenGL
                boolean shadow = (s.alpha < 0x1A || s.alpha == 0xFF) && highlighter.getStyle().shadow;
                font.drawString(s.text, nx, ny, color, shadow);
                nx += s.width;
            }
            GlStateManager.disableBlend();

        }
    }

    @Override
    protected int getShiftX() {
        return lines ? 10 + placements : 0;
    }

    @Override
    protected void drawBackground() {
        area.draw(0xff000000 + ColorUtils.multiplyColor(highlighter.getStyle().background, 0.8F));
    }

    @Override
    protected void drawForeground(GuiContext context) {
        if (lines) {
            /* Draw line numbers background */
            int x = area.x + getShiftX();

            Gui.drawRect(area.x, area.y, x, area.ey(), 0xff000000 + highlighter.getStyle().background);

            /* Draw cached line numbers */
            for (TextLineNumber number : numbers) {
                if (!number.draw) {
                    break;
                }

                font.drawString(number.line, number.x, number.y, highlighter.getStyle().lineNumbers);
                number.draw = false;
            }

            lineNumber = 0;

            /* Draw shadow to the right of line numbers when scrolling */
            int a = (int) (Math.min(horizontal.scroll / 10F, 1F) * 0x44);

            if (a > 0) {
                GuiDraw.drawHorizontalGradientRect(x, area.y, x + 10, area.ey(), a << 24, 0);
            }
        }
    }

    @Override
    public void setSearching(boolean searching)
    {
        this.searching = searching;

        if (!searching)
        {
            this.pattern = null;
            this.searchMatches.clear();
            this.currentSearchMatch = -1;
            this.deselect();
            return;
        }

        refreshSearchResults(true);
    }

    @Override
    public boolean isSearching()
    {
        return this.searching;
    }

    @Override
    public Pattern getPattern()
    {
        return this.pattern;
    }

    @Override
    public void setPattern(Pattern pattern)
    {
        this.pattern = pattern;
    }

    @Override
    public int refreshSearchResults(boolean jumpToFirst)
    {
        int keepOffset = -1;

        if (this.currentSearchMatch >= 0 && this.currentSearchMatch < this.searchMatches.size())
        {
            keepOffset = this.searchMatches.get(this.currentSearchMatch).start;
        }

        this.searchMatches.clear();
        this.currentSearchMatch = -1;

        if (!this.searching || this.pattern == null)
        {
            return 0;
        }

        Matcher matcher = this.pattern.matcher(this.getText());

        while (matcher.find())
        {
            if (matcher.start() == matcher.end())
            {
                continue;
            }

            this.searchMatches.add(new SearchMatch(matcher.start(), matcher.end()));
        }

        if (this.searchMatches.isEmpty())
        {
            return 0;
        }

        if (jumpToFirst || keepOffset < 0)
        {
            this.currentSearchMatch = 0;
        }
        else
        {
            this.currentSearchMatch = this.findNearestMatch(keepOffset);
        }

        this.focusCurrentMatch();

        return this.searchMatches.size();
    }

    @Override
    public boolean navigateMatch(boolean backwards)
    {
        if (this.searchMatches.isEmpty())
        {
            return false;
        }

        if (this.currentSearchMatch < 0)
        {
            this.currentSearchMatch = 0;
        }
        else
        {
            int amount = backwards ? -1 : 1;
            this.currentSearchMatch = (this.currentSearchMatch + amount + this.searchMatches.size()) % this.searchMatches.size();
        }

        this.focusCurrentMatch();

        return true;
    }

    @Override
    public int getMatchCount()
    {
        return this.searchMatches.size();
    }

    @Override
    public int getCurrentMatchIndex()
    {
        return this.currentSearchMatch;
    }

    @Override
    public boolean replaceCurrentMatch(Pattern pattern, String replacement)
    {
        if (this.currentSearchMatch < 0 || this.currentSearchMatch >= this.searchMatches.size())
        {
            return false;
        }

        SearchMatch match = this.searchMatches.get(this.currentSearchMatch);
        String fullText = this.getText();
        String matched = fullText.substring(match.start, match.end);
        String result = replacement;

        if (pattern != null)
        {
            Matcher matcher = pattern.matcher(matched);

            if (matcher.find())
            {
                result = matcher.replaceFirst(replacement);
            }
        }

        String updated = fullText.substring(0, match.start) + result + fullText.substring(match.end);

        this.selectAll();
        this.pasteText(updated);

        int caret = match.start + result.length();
        Cursor caretCursor = this.toCursor(caret);
        this.cursor.copy(caretCursor);
        this.deselect();
        this.moveViewportToCursor();

        this.refreshSearchResults(false);

        if (!this.searchMatches.isEmpty())
        {
            this.currentSearchMatch = this.findNearestMatch(match.start);
            this.focusCurrentMatch();
        }

        return true;
    }

    @Override
    public int replaceAllMatches(Pattern pattern, String replacement)
    {
        if (pattern == null)
        {
            return 0;
        }

        Matcher counter = pattern.matcher(this.getText());
        int amount = 0;

        while (counter.find())
        {
            if (counter.start() != counter.end())
            {
                amount += 1;
            }
        }

        if (amount == 0)
        {
            return 0;
        }

        String replaced = pattern.matcher(this.getText()).replaceAll(replacement);

        this.selectAll();
        this.pasteText(replaced);

        this.refreshSearchResults(false);

        return amount;
    }

    private void focusCurrentMatch()
    {
        if (this.currentSearchMatch < 0 || this.currentSearchMatch >= this.searchMatches.size())
        {
            return;
        }

        SearchMatch match = this.searchMatches.get(this.currentSearchMatch);
        this.selectRange(match.start, match.end);
        this.moveViewportToCursor();
    }

    private void selectRange(int startIndex, int endIndex)
    {
        Cursor start = this.toCursor(startIndex);
        Cursor end = this.toCursor(endIndex);

        this.selection.copy(start);
        this.cursor.copy(end);
    }

    private Cursor toCursor(int index)
    {
        int remaining = Math.max(index, 0);

        if (this.text.isEmpty())
        {
            return new Cursor(0, 0);
        }

        for (int i = 0; i < this.text.size(); i++)
        {
            String line = this.text.get(i).text;
            int length = line.length();

            if (remaining <= length)
            {
                return new Cursor(i, remaining);
            }

            remaining -= length;

            if (i < this.text.size() - 1)
            {
                if (remaining == 0)
                {
                    return new Cursor(i + 1, 0);
                }

                remaining -= 1;
            }
        }

        int last = this.text.size() - 1;
        return new Cursor(last, this.text.get(last).text.length());
    }

    private int findNearestMatch(int globalOffset)
    {
        for (int i = 0; i < this.searchMatches.size(); i++)
        {
            if (this.searchMatches.get(i).start >= globalOffset)
            {
                return i;
            }
        }

        return this.searchMatches.size() - 1;
    }

    private static class SearchMatch
    {
        public final int start;
        public final int end;

        private SearchMatch(int start, int end)
        {
            this.start = start;
            this.end = end;
        }
    }
}

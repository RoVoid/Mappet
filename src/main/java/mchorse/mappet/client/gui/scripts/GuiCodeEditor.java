package mchorse.mappet.client.gui.scripts;

import mchorse.mappet.client.gui.scripts.completion.CompletionSuggestion;
import mchorse.mappet.client.gui.scripts.completion.ICompletionContext;
import mchorse.mappet.client.gui.scripts.completion.ICompletionProvider;
import mchorse.mappet.client.gui.scripts.completion.JsCompletionProvider;
import mchorse.mappet.client.gui.scripts.search.GuiTextEditorSearchable;
import mchorse.mappet.client.gui.scripts.search.SearchPanel;
import mchorse.mappet.client.gui.scripts.style.SyntaxHighlighter;
import mchorse.mappet.client.gui.scripts.utils.HighlightedTextLine;
import mchorse.mappet.client.gui.scripts.utils.TextLineNumber;
import mchorse.mappet.client.gui.scripts.utils.TextSegment;
import mchorse.mappet.client.gui.utils.text.GuiMultiTextElement;
import mchorse.mappet.client.gui.utils.text.GuiMultiTextElement.TextHighlight;
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

public class GuiCodeEditor extends GuiMultiTextElement<HighlightedTextLine> implements GuiTextEditorSearchable, ICompletionContext {

    private SyntaxHighlighter highlighter;
    private int placements;
    private boolean lines = true;

    /* Line numbers */
    private final List<TextLineNumber> numbers = new ArrayList<>(40);
    private int lineNumber = 0;

    /* Search */
    private SearchPanel searchPanel;
    private Pattern pattern;
    private boolean searching;
    private final List<SearchMatch> searchMatches = new ArrayList<>();
    private int currentSearchMatch = -1;

    /* Hints / autocomplete */
    private boolean withHints;

    /** Bumped on every text edit; lets completion providers cache their parse of the document. */
    private int version;

    public GuiCodeEditor(Minecraft mc, Consumer<String> callback) {
        super(mc, callback);
        highlighter = new SyntaxHighlighter();
    }

    public void setSearchPanel(SearchPanel searchPanel) {this.searchPanel = searchPanel;}

    public void withHints() {withHints = true;}

    @Override
    protected HighlightedTextLine createTextLine(String line) {return new HighlightedTextLine(line);}

    public GuiCodeEditor disableLines() {
        lines = false;
        return this;
    }

    public SyntaxHighlighter getHighlighter() {return highlighter;}

    public void setHighlighter(SyntaxHighlighter highlighter) {this.highlighter = highlighter;}

    public void resetHighlight() {for (HighlightedTextLine textLine : text) textLine.resetSegments();}

    @Override
    public void setText(String text) {
        super.setText(text);
        resetHighlight();
        version++;

        if (searching) {
            if (searchPanel != null) searchPanel.onEditorChanged();
            else refreshSearchResults(false);
        }
    }

    @Override
    protected void recalculateSizes() {
        double power = Math.ceil(Math.log10(text.size() + 1));
        placements = (int) power * 6;
        super.recalculateSizes();
    }

    @Override
    protected void changedLine(int i) {
        version++;
        String line = text.get(i).text;
        if (line.contains("/*") || line.contains("*/")) changedLineAfter(i);
        else {
            super.changedLine(i);
            text.get(i).resetSegments();
        }

        notifySearch();
    }

    @Override
    protected void changedLineAfter(int index) {
        version++;
        super.changedLineAfter(index);
        while (index < text.size()) text.get(index++).resetSegments();
        notifySearch();
    }

    private void notifySearch() {
        if (!searching) return;
        if (searchPanel != null) searchPanel.onEditorChanged();
        else refreshSearchResults(false);
    }

    /* Code editor key handling */

    @Override
    protected boolean handleKeys(GuiContext context, TextEditUndo undo, boolean ctrl, boolean shift) {
        if (readOnly) return super.handleKeys(context, undo, ctrl, shift);

        int keyCode = context.keyCode;

        // Ctrl+/ — toggle comment
        if (ctrl && keyCode == org.lwjgl.input.Keyboard.KEY_SLASH) {
            keyToggleComment(undo);
            return true;
        }

        // Ctrl+D — duplicate line
        if (ctrl && keyCode == org.lwjgl.input.Keyboard.KEY_D) {
            deselect();
            String copy = text.get(cursor.line).text;
            moveCursorToLineEnd();
            writeNewLine();
            moveCursorToLineStart();
            writeString(copy);
            undo.ready().post(copy + "\n" + copy, cursor, selection);
            playSound(SoundEvents.ENTITY_GENERIC_EXPLODE);
            return true;
        }

        // Tab — accept the top autocomplete suggestion; falls through to normal indent behavior
        // when there's nothing worth completing.
        if (keyCode == org.lwjgl.input.Keyboard.KEY_TAB && !shift && acceptCompletion(undo)) {
            return true;
        }

        return super.handleKeys(context, undo, ctrl, shift);
    }

    /** Applies the top autocomplete suggestion in place, as a single undo-able edit. Returns false (nothing applied) when there's no suggestion, or the top one already matches what's typed — callers should fall through to normal Tab/indent handling in that case. */
    private boolean acceptCompletion(TextEditUndo undo) {
        if (completionProvider == null || !completionProvider.isTriggered(this)) return false;

        List<CompletionSuggestion> suggestions = completionProvider.getSuggestions(this);
        if (suggestions.isEmpty()) return false;

        String line = text.get(cursor.line).text;
        String prefix = identifierPrefix(line, cursor.getOffset(line));

        CompletionSuggestion top = suggestions.get(0);
        if (top.name.equalsIgnoreCase(prefix)) return false;

        int[] outOffset = new int[1];
        String newLine = completionProvider.complete(this, top, outOffset);

        text.get(cursor.line).text = newLine;
        cursor.offset = outOffset[0];
        changedLine(cursor.line);
        undo.ready().post(newLine, cursor, selection);

        return true;
    }

    private void keyToggleComment(TextEditUndo undo) {
        Cursor min = new Cursor();
        Cursor max = new Cursor();

        if (isSelected()) {
            min.copy(getMin());
            max.copy(getMax());
        }
        else {
            min.copy(cursor);
            max.copy(cursor);
        }

//        boolean inMultiComment = text.get(min.line).segments.get(0).is(TextSegment.Token.MULTI_COMMENTS);
//        boolean isMultiComment = text.get(min.line).text.startsWith("/*");

        // TODO: Multi comment support

        boolean uncomment = true;
        for (int i = min.line; i <= max.line; i++) {
            String line = text.get(i).text.trim();
            if (line.isEmpty() || line.startsWith("//")) continue;
            uncomment = false;
            break;
        }

        for (int i = min.line; i <= max.line; i++) {
            String line = text.get(i).text;

            int j = 0;
            while (j < line.length() && Character.isWhitespace(line.charAt(j))) j++;

            if (uncomment && line.startsWith("//", j)) text.get(i).text = line.substring(0, j) + line.substring(j + 2);
            else if (!uncomment && !line.trim().isEmpty()) text.get(i).text = "// " + line;
        }

        if (isSelected()) {
            String selected = getSelectedText();
            deleteSelection();
            writeString(selected);
        }
        else {
            String cur = text.get(cursor.line).text;
            text.get(cursor.line).text = "";
            writeString(cur);
        }

        undo.ready().post("", cursor, selection);
        changedLineAfter(min.line);
        playSound(SoundEvents.BLOCK_CHEST_LOCKED);
    }

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
        if (input != target) return false;
        String line = text.get(cursor.line).text;
        return line.length() >= 2 && cursor.offset > 0 && cursor.offset < line.length() && line.charAt(cursor.offset) == target && line.charAt(
                cursor.offset - 1) == supplementary;
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
        String line = cursor.start(text.get(cursor.line).text);

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
            if (shift) min.offset = Math.max(min.offset - 4, 0);

            Cursor temp = new Cursor();
            List<String> splits = GuiMultiTextElement.splitNewlineString(getSelectedText());

            for (int i = 0; i < splits.size(); i++) {
                if (shift) {
                    int indent = getIndent(splits.get(i));
                    splits.set(i, splits.get(i).substring(Math.min(indent, 4)));
                }
                else splits.set(i, "    " + splits.get(i));
            }

            String result = String.join("\n", splits);
            temp.copy(min);
            deleteSelection();
            writeString(result);
            getMin().set(min.line, splits.get(splits.size() - 1).length());
            min.copy(temp);
            if (!shift) min.offset += 4;

            undo.postText = result;
        }
        else {
            undo.postText = "    ";
            deleteSelection();
            deselect();
            writeString(undo.postText);
        }
    }

    /* ICompletionContext — lets completion providers see the whole document, not just one line */

    @Override
    public int getLineCount() {return text.size();}

    @Override
    public String getLine(int index) {return hasLine(index) ? text.get(index).text : "";}

    @Override
    public int getCursorLine() {return cursor.line;}

    @Override
    public int getCursorOffset() {return cursor.getOffset(text.get(cursor.line).text);}

    @Override
    public int getVersion() {return version;}

    public int getIndent(int i) {return hasLine(i) ? getIndent(text.get(i).text) : 0;}

    public int getIndent(String line) {
        for (int j = 0; j < line.length(); j++) if (line.charAt(j) != ' ') return j;
        return line.length();
    }

    public String createIndent(int i) {
        StringBuilder builder = new StringBuilder();
        while (i-- > 0) builder.append(' ');
        return builder.toString();
    }

    /* Hints / autocomplete */

    /**
     * Defaults to JS; swap via {@link #setCompletionProvider(ICompletionProvider)} for other languages.
     * <p>
     * To expose the mappet script API to autocomplete, populate this instance before use, e.g.:
     * <pre>{@code
     * JsCompletionProvider provider = (JsCompletionProvider) editor.getCompletionProvider();
     * provider.getTypeRegistry().registerClass("Player", EntityPlayer.class);
     * provider.addGlobal("player", EntityPlayer.class);
     * provider.addGlobal("event", ScriptEvent.class);
     * }</pre>
     */
    private ICompletionProvider completionProvider = new JsCompletionProvider();

    public void setCompletionProvider(ICompletionProvider completionProvider) {this.completionProvider = completionProvider;}

    public ICompletionProvider getCompletionProvider() {return completionProvider;}

    /** Light gray used for the type annotation drawn after each suggestion's label (e.g. "getHealth() : number"). */
    private static final int SUGGESTION_TYPE_COLOR = 0xff888888;
    private static final String SUGGESTION_TYPE_SEPARATOR = " : ";

    private void drawMatchingMethodsOverlay(List<CompletionSuggestion> suggestions, int x, int y, int cursorW, int i) {
        int maxWidth = 0;
        for (CompletionSuggestion s : suggestions) maxWidth = Math.max(maxWidth, suggestionLineWidth(s));

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 0.1);

        int rectOffset = (font.FONT_HEIGHT + 4) * suggestions.size();

        if (i < 15) Gui.drawRect(x + cursorW + 5, y + font.FONT_HEIGHT + 5 + rectOffset, x + cursorW + 10 + maxWidth, y + font.FONT_HEIGHT + 5,
                0xee000000);
        else Gui.drawRect(x + cursorW + 5, y - 5 - rectOffset, x + cursorW + 10 + maxWidth, y - 5, 0xee000000);

        for (int ii = 1; ii <= suggestions.size(); ii++) {
            int textOffset = (font.FONT_HEIGHT + 4) * ii + 3;
            textOffset = i < 15 ? textOffset + font.FONT_HEIGHT - 9 : -textOffset;

            CompletionSuggestion suggestion = suggestions.get(ii - 1);
            int labelX = x + cursorW + 7;
            font.drawString(suggestion.label, labelX, y + textOffset, textColor, textShadow);

            if (suggestion.typeLabel != null) {
                int typeX = labelX + font.getStringWidth(suggestion.label + SUGGESTION_TYPE_SEPARATOR);
                font.drawString(suggestion.typeLabel, typeX, y + textOffset, SUGGESTION_TYPE_COLOR, textShadow);
            }
        }

        GlStateManager.popMatrix();
    }

    /** Full pixel width of a suggestion's row, i.e. its label plus the type annotation drawn after it. */
    private int suggestionLineWidth(CompletionSuggestion s) {
        int width = font.getStringWidth(s.label);
        if (s.typeLabel != null) width += font.getStringWidth(SUGGESTION_TYPE_SEPARATOR + s.typeLabel);
        return width;
    }

    /** The partial identifier right before {@code offset}, e.g. "pla" in "player.getPla" -> "getPla". */
    private String identifierPrefix(String line, int offset) {
        int end = Math.max(0, Math.min(offset, line.length()));
        int start = end;

        while (start > 0) {
            char c = line.charAt(start - 1);
            if (Character.isLetterOrDigit(c) || c == '_' || c == '$') start--;
            else break;
        }

        return line.substring(start, end);
    }

    /** Bright color used to highlight whichever parameter the cursor is currently on within a call's "(...)". */
    private static final int SIGNATURE_ACTIVE_PARAM_COLOR = 0xffFFD866;

    /**
     * Draws a small "methodName(param: Type, ...)" overlay above/below the cursor while it's inside
     * a call's parentheses, with the parameter matching the cursor's position highlighted.
     */
    private void drawSignatureHintOverlay(JsCompletionProvider.SignatureHint hint, int x, int y, int cursorW, int i) {
        List<String> params = splitSignatureParams(hint.signature);

        String prefix = hint.methodName + "(";
        String suffix = ") : " + hint.returnType;

        int width = font.getStringWidth(prefix) + font.getStringWidth(suffix);
        for (int p = 0; p < params.size(); p++) {
            width += font.getStringWidth(params.get(p));
            if (p < params.size() - 1) width += font.getStringWidth(", ");
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 0.1);

        int rowHeight = font.FONT_HEIGHT + 4;
        int drawY = i < 15 ? y + font.FONT_HEIGHT + 5 + rowHeight : y - 5 - rowHeight;
        int textY = drawY + 3;

        Gui.drawRect(x + cursorW + 5, drawY, x + cursorW + 10 + width, drawY + rowHeight, 0xee000000);

        int drawX = x + cursorW + 7;
        font.drawString(prefix, drawX, textY, textColor, textShadow);
        drawX += font.getStringWidth(prefix);

        for (int p = 0; p < params.size(); p++) {
            boolean active = p == hint.activeParameter;
            int color = active ? SIGNATURE_ACTIVE_PARAM_COLOR : textColor;
            font.drawString(params.get(p), drawX, textY, color, textShadow);
            drawX += font.getStringWidth(params.get(p));

            if (p < params.size() - 1) {
                font.drawString(", ", drawX, textY, textColor, textShadow);
                drawX += font.getStringWidth(", ");
            }
        }

        font.drawString(suffix, drawX, textY, SUGGESTION_TYPE_COLOR, textShadow);

        GlStateManager.popMatrix();
    }

    /** Splits a "(a: Type, b: Type)" signature into its individual "a: Type" parts. Empty for "()". */
    private List<String> splitSignatureParams(String signature) {
        List<String> result = new ArrayList<>();
        if (signature == null || signature.length() < 2) return result;

        String inner = signature.substring(1, signature.length() - 1);
        if (inner.isEmpty()) return result;

        for (String part : inner.split(", ")) result.add(part);
        return result;
    }

    /* Rendering */

    @Override
    protected void drawTextLine(String line, int i, int j, int nx, int ny) {
        if (lines && j == 0) {
            String label = String.valueOf(i + 1);
            int x = area.x + 5 + placements - font.getStringWidth(label);

            if (lineNumber >= numbers.size()) numbers.add(new TextLineNumber());
            numbers.get(lineNumber).set(label, x, ny);
            lineNumber++;
        }

        // Hints overlay
        if (withHints && completionProvider != null && cursor.line == i && j == 0) {
            int cursorW = line.isEmpty() ? 0 : font.getStringWidth(cursor.start(line));

            boolean drewSuggestions = false;

            if (completionProvider.isTriggered(this)) {
                List<CompletionSuggestion> suggestions = completionProvider.getSuggestions(this);

                if (!suggestions.isEmpty()) {
                    drawMatchingMethodsOverlay(suggestions, nx, ny, cursorW, i);
                    drewSuggestions = true;
                }
            }

            // Parameter hints inside "methodName(...)" — only when there's no dot-chain suggestion
            // list already occupying the overlay space above/below the cursor.
            if (!drewSuggestions && completionProvider instanceof JsCompletionProvider) {
                JsCompletionProvider.SignatureHint hint = ((JsCompletionProvider) completionProvider).getSignatureHint(this);
                if (hint != null) drawSignatureHintOverlay(hint, nx, ny, cursorW, i);
            }
        }

        HighlightedTextLine textLine = text.get(i);

        if (textLine.segments == null) {
            List<TextSegment> lastSegments = i > 0 && text.get(i - 1) != null ? text.get(i - 1).segments : null;
            TextSegment lastSegment = lastSegments != null && !lastSegments.isEmpty() ? lastSegments.get(lastSegments.size() - 1) : null;

            textLine.setSegments(highlighter.parse(font, textLine.text, lastSegment));
            if (textLine.wrappedLines != null) textLine.calculateWrappedSegments(font);
        }

        List<TextSegment> segments = textLine.segments;
        if (textLine.wrappedSegments != null) segments = j < textLine.wrappedSegments.size() ? textLine.wrappedSegments.get(j) : null;

        if (segments != null) {
            GlStateManager.enableBlend();
            for (TextSegment s : segments) {
                int color = s.alpha < 0x1A ? s.color & 0xFFFFFF : s.color;
                boolean shadow = (s.alpha < 0x1A || s.alpha == 0xFF) && highlighter.getStyle().shadow;
                font.drawString(s.text, nx, ny, color, shadow);
                nx += s.width;
            }
            GlStateManager.disableBlend();
        }
    }

    @Override
    protected int getShiftX() {return lines ? 10 + placements : 0;}

    @Override
    protected void drawBackground() {
        area.draw(0xff000000 + ColorUtils.multiplyColor(highlighter.getStyle().background, 0.8F));
    }

    @Override
    protected void drawForeground(GuiContext context) {
        if (!lines) return;

        int x = area.x + getShiftX();
        Gui.drawRect(area.x, area.y, x, area.ey(), 0xff000000 + highlighter.getStyle().background);

        for (TextLineNumber number : numbers) {
            if (!number.draw) break;
            font.drawString(number.line, number.x, number.y, highlighter.getStyle().lineNumbers);
            number.draw = false;
        }

        lineNumber = 0;

        int a = (int) (Math.min(horizontal.scroll / 10F, 1F) * 0x44);
        if (a > 0) GuiDraw.drawHorizontalGradientRect(x, area.y, x + 10, area.ey(), a << 24, 0);
    }

    /* Search */

    /** Match highlight (semi-transparent yellow). Current match gets {@link #CURRENT_MATCH_COLOR}. */
    private static final int MATCH_COLOR = 0x66FFD866;
    private static final int CURRENT_MATCH_COLOR = 0xAAFF9900;

    @Override
    public void setSearching(boolean searching) {
        this.searching = searching;

        if (!searching) {
            pattern = null;
            searchMatches.clear();
            currentSearchMatch = -1;
            clearHighlights();
            return;
        }

        refreshSearchResults(true);
    }

    @Override
    public boolean isSearching() {return searching;}

    @Override
    public Pattern getPattern() {return pattern;}

    @Override
    public void setPattern(Pattern pattern) {this.pattern = pattern;}

    @Override
    public int refreshSearchResults(boolean jumpToFirst) {
        int keepOffset = currentSearchMatch >= 0 && currentSearchMatch < searchMatches.size() ? searchMatches.get(currentSearchMatch).start : -1;

        searchMatches.clear();
        currentSearchMatch = -1;

        if (!searching || pattern == null) return 0;

        Matcher matcher = pattern.matcher(getText());
        while (matcher.find()) if (matcher.start() != matcher.end()) searchMatches.add(new SearchMatch(matcher.start(), matcher.end()));

        if (searchMatches.isEmpty()) return 0;

        currentSearchMatch = jumpToFirst || keepOffset < 0 ? 0 : findNearestMatch(keepOffset);
        focusCurrentMatch();
        return searchMatches.size();
    }

    @Override
    public boolean navigateMatch(boolean backwards) {
        if (searchMatches.isEmpty()) return false;
        if (currentSearchMatch < 0) currentSearchMatch = 0;
        else currentSearchMatch = (currentSearchMatch + (backwards ? -1 : 1) + searchMatches.size()) % searchMatches.size();
        focusCurrentMatch();
        return true;
    }

    /** Rebuilds the drawn highlight ranges for every match, marking the current one differently. */
    private void rebuildMatchHighlights() {
        List<TextHighlight> newHighlights = new ArrayList<>();

        for (int i = 0; i < searchMatches.size(); i++) {
            SearchMatch match = searchMatches.get(i);
            int color = i == currentSearchMatch ? CURRENT_MATCH_COLOR : MATCH_COLOR;
            newHighlights.add(new TextHighlight(toCursor(match.start), toCursor(match.end), color));
        }

        setHighlights(newHighlights);
    }

    @Override
    public int getMatchCount() {return searchMatches.size();}

    @Override
    public int getCurrentMatchIndex() {return currentSearchMatch;}

    @Override
    public boolean replaceCurrentMatch(Pattern pattern, String replacement) {
        if (currentSearchMatch < 0 || currentSearchMatch >= searchMatches.size()) return false;

        SearchMatch match = searchMatches.get(currentSearchMatch);
        String fullText = getText();
        String matched = fullText.substring(match.start, match.end);
        String result = replacement;

        if (pattern != null) {
            Matcher matcher = pattern.matcher(matched);
            if (matcher.find()) result = matcher.replaceFirst(replacement);
        }

        String updated = fullText.substring(0, match.start) + result + fullText.substring(match.end);
        selectAll();
        pasteText(updated);

        cursor.copy(toCursor(match.start + result.length()));
        deselect();
        moveViewportToCursor();
        refreshSearchResults(false);

        if (!searchMatches.isEmpty()) {
            currentSearchMatch = findNearestMatch(match.start);
            focusCurrentMatch();
        }

        return true;
    }

    @Override
    public int replaceAllMatches(Pattern pattern, String replacement) {
        if (pattern == null) return 0;

        Matcher counter = pattern.matcher(getText());
        int amount = 0;
        while (counter.find()) if (counter.start() != counter.end()) amount++;
        if (amount == 0) return 0;

        String replaced = pattern.matcher(getText()).replaceAll(replacement);
        selectAll();
        pasteText(replaced);
        refreshSearchResults(false);
        return amount;
    }

    private void focusCurrentMatch() {
        rebuildMatchHighlights();

        if (currentSearchMatch < 0 || currentSearchMatch >= searchMatches.size()) return;
        SearchMatch match = searchMatches.get(currentSearchMatch);
        moveViewportTo(toCursor(match.end));
    }

    private Cursor toCursor(int index) {
        int remaining = Math.max(index, 0);
        if (text.isEmpty()) return new Cursor(0, 0);

        for (int i = 0; i < text.size(); i++) {
            int length = text.get(i).text.length();
            if (remaining <= length) return new Cursor(i, remaining);
            remaining -= length;
            if (i < text.size() - 1) remaining--;
        }

        int last = text.size() - 1;
        return new Cursor(last, text.get(last).text.length());
    }

    private int findNearestMatch(int globalOffset) {
        for (int i = 0; i < searchMatches.size(); i++) if (searchMatches.get(i).start >= globalOffset) return i;
        return searchMatches.size() - 1;
    }

    private static class SearchMatch {
        public final int start;
        public final int end;

        private SearchMatch(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
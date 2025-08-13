package mchorse.mappet.client.gui.scripts;

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

public class GuiCodeEditor extends GuiMultiTextElement<HighlightedTextLine> {
    private SyntaxHighlighter highlighter;
    private int placements;
    private boolean lines = true;

    private final List<TextLineNumber> numbers = new ArrayList<>(40);
    private int lineNumber = 0;

    public GuiCodeEditor(Minecraft mc, Consumer<String> callback) {
        super(mc, callback);
        highlighter = new SyntaxHighlighter();
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
    }

    @Override
    protected void changedLineAfter(int index) {
        super.changedLineAfter(index);
        while (index < text.size()) text.get(index++).resetSegments();
    }

    /* Change input behavior */

    @Override
    protected String getFromChar(char typedChar) {
        if (wasDoubleInsert(typedChar, ')', '(') || wasDoubleInsert(typedChar, ']', '[') || wasDoubleInsert(typedChar,
                                                                                                            '}',
                                                                                                            '{') || wasDoubleInsert(
                typedChar,
                '"',
                '"') || wasDoubleInsert(typedChar, '\'', '\'')) {
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

        return line.length() >= 2 && cursor.offset > 0 && cursor.offset < line.length() && line.charAt(cursor.offset) == target && line.charAt(
                cursor.offset - 1) == supplementary;
    }

    @Override
    protected void keyNewLine(TextEditUndo undo) {
        String line = text.get(cursor.line).text;
        boolean unwrap = line.length() >= 2 && cursor.offset > 0 && cursor.offset < line.length() && line.charAt(cursor.offset) == '}' && line.charAt(
                cursor.offset - 1) == '{';

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
            if (lastSegments != null && !lastSegments.isEmpty())
                lastSegment = lastSegments.get(lastSegments.size() - 1);

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
}
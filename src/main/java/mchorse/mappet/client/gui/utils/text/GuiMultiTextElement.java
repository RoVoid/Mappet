package mchorse.mappet.client.gui.utils.text;

import com.google.common.collect.ImmutableList;
import mchorse.mappet.client.gui.utils.GuiMappetUtils;
import mchorse.mappet.client.gui.utils.text.undo.TextEditUndo;
import mchorse.mappet.client.gui.utils.text.utils.Cursor;
import mchorse.mappet.client.gui.utils.text.utils.StringGroup;
import mchorse.mappet.config.MappetConfig;
import mchorse.mclib.McLib;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.IFocusedGuiElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDraw;
import mchorse.mclib.client.gui.framework.elements.utils.ITextColoring;
import mchorse.mclib.client.gui.utils.ScrollArea;
import mchorse.mclib.client.gui.utils.ScrollDirection;
import mchorse.mclib.utils.ColorUtils;
import mchorse.mclib.utils.MathUtils;
import mchorse.mclib.utils.undo.UndoManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.SoundEvent;
import org.lwjgl.input.Keyboard;

import javax.vecmath.Vector2d;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GuiMultiTextElement<T extends TextLine> extends GuiElement implements IFocusedGuiElement, ITextColoring {

    public ScrollArea horizontal = new ScrollArea();
    public ScrollArea vertical = new ScrollArea();
    public Consumer<String> callback;

    /* Visual */
    private boolean background;
    protected int padding = 10;
    protected int lineHeight = 12;
    protected int textColor = 0xffffff;
    protected boolean textShadow;
    protected boolean wrapping;

    /* State */
    protected boolean readOnly;
    private boolean focused;
    private int dragging;
    private int lastW;
    private boolean dirty;

    /* Text data */
    protected List<T> text = new ArrayList<>();
    public final Cursor cursor = new Cursor();
    public final Cursor selection = new Cursor(-1, 0);

    /* Mouse */
    private int lastMX;
    private int lastMY;
    private long lastClick;

    /* Undo */
    private UndoManager<GuiMultiTextElement> undo;

    /* Group selection state */
    private StringGroup lastGroup;

    public static List<String> splitNewlineString(String string) {
        List<String> splits = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        for (int i = 0, c = string.length(); i < c; i++) {
            char ch = string.charAt(i);
            if (ch == '\n') {
                splits.add(builder.toString());
                builder = new StringBuilder();
            }
            else builder.append(ch);
        }

        splits.add(builder.toString());
        return splits;
    }

    public GuiMultiTextElement(Minecraft mc, Consumer<String> callback) {
        super(mc);
        this.callback = callback;

        horizontal.direction = ScrollDirection.HORIZONTAL;
        horizontal.cancelScrollEdge = true;
        horizontal.scrollSpeed = lineHeight * 2;
        vertical.cancelScrollEdge = true;
        vertical.scrollSpeed = lineHeight * 2;

        clear();
    }

    /* Builder */

    public GuiMultiTextElement<T> background() {return background(true);}

    public GuiMultiTextElement<T> background(boolean background) {
        this.background = background;
        return this;
    }

    public GuiMultiTextElement<T> padding(int padding) {
        this.padding = padding;
        return this;
    }

    public GuiMultiTextElement<T> lineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
        return this;
    }

    public GuiMultiTextElement<T> wrap() {return wrap(!wrapping);}

    public GuiMultiTextElement<T> wrap(boolean wrapping) {
        this.wrapping = wrapping;
        return this;
    }

    public GuiMultiTextElement<T> readOnly() {return wrap(!readOnly);}

    public GuiMultiTextElement<T> readOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    @Override
    public void setColor(int textColor, boolean textShadow) {
        this.textColor = textColor;
        this.textShadow = textShadow;
    }

    /* Text API */

    public void setText(String text) {
        this.text.clear();

        for (String line : text.split("\n", -1)) this.text.add(createTextLine(line));

        cursor.set(0, 0);
        selection.set(-1, 0);
        horizontal.scroll = 0;
        vertical.scroll = 0;
        dirty = false;
        undo = new UndoManager<GuiMultiTextElement>(100).simpleMerge();

        if (area.w > 0) {
            recalculateWrapping();
            recalculateSizes();
        }
    }

    protected T createTextLine(String line) {return (T) new TextLine(line);}

    public String getText() {return text.stream().map(t -> t.text).collect(Collectors.joining("\n"));}

    public List<T> getLines() {return text;}

    public int getWrappedWidth() {return area.w - padding * 3 - getShiftX();}

    /* Selection API */

    public boolean isSelected() {return !selection.isEmpty();}

    public void startSelecting() {selection.copy(cursor);}

    public void deselect() {selection.set(-1, 0);}

    public void swapSelection() {
        if (isSelected()) {
            Cursor temp = new Cursor();
            temp.copy(selection);
            selection.copy(cursor);
            cursor.copy(temp);
        }
    }

    public void selectAll() {
        cursor.set(0, 0);
        startSelecting();
        cursor.line = text.size() - 1;
        moveCursorToLineEnd();
    }

    public String getSelectedText() {return isSelected() ? getText(cursor, selection) : "";}

    public String getText(Cursor a, Cursor b) {
        StringJoiner joiner = new StringJoiner("\n");
        Cursor min = a.isThisLessTo(b) ? a : b;
        Cursor max = a.isThisLessTo(b) ? b : a;

        for (int i = min.line; i <= Math.min(max.line, text.size() - 1); i++) {
            String line = text.get(i).text;
            if (i == min.line && i == max.line) joiner.add(line.substring(min.getOffset(line), max.getOffset(line)));
            else if (i == min.line) joiner.add(min.end(line));
            else if (i == max.line) joiner.add(max.start(line));
            else joiner.add(line);
        }

        return joiner.toString();
    }

    public boolean selectGroup(int direction, boolean select) {
        List<Cursor> groups = findGroup(direction, cursor);
        if (groups.isEmpty()) return false;

        Cursor min = groups.get(0);
        Cursor max = groups.get(1);

        if (select) {
            if (direction == 0) {
                cursor.offset = max.offset;
                selection.set(cursor.line, min.offset);
            }
            else {
                if (!isSelected()) startSelecting();
                cursor.offset = direction < 0 ? min.offset : max.offset;
            }
        }
        else {
            deselect();
            cursor.offset = direction < 0 ? min.offset : max.offset;
        }

        return true;
    }

    public int measureGroup(int direction, Cursor cursor) {
        if (direction == 0) return 0;
        List<Cursor> group = findGroup(direction, cursor);
        if (group.isEmpty()) return 0;
        return group.get(direction < 0 ? 0 : 1).offset - cursor.offset;
    }

    public List<Cursor> findGroup(int direction, Cursor cursor) {
        String line = text.get(cursor.line).text;
        if (line.isEmpty() || this.cursor.offset >= line.length() - 1) return Collections.emptyList();

        int offset = cursor.offset;
        int first = direction < 0 && offset > 0 ? offset - 1 : offset;
        String character = String.valueOf(line.charAt(first));
        StringGroup group = StringGroup.get(character);

        int min = offset;
        int max = offset;

        lastGroup = null;
        if (direction <= 0) while (min > 0) {
            if (matchSelectGroup(group, String.valueOf(line.charAt(min - 1)))) min--;
            else break;
        }

        lastGroup = null;
        if (direction >= 0) while (max < line.length()) {
            if (matchSelectGroup(group, String.valueOf(line.charAt(max)))) max++;
            else break;
        }

        return ImmutableList.of(new Cursor(cursor.line, min), new Cursor(cursor.line, max));
    }

    private boolean matchSelectGroup(StringGroup group, String character) {
        if (group.match(character)) return lastGroup == null;
        if (group == StringGroup.SPACE) {
            if (lastGroup == null) lastGroup = StringGroup.get(character);
            return StringGroup.get(character) == lastGroup;
        }
        return false;
    }

    public void selectTextful(String text, boolean reverse) {
        deselect();
        List<String> splits = splitNewlineString(text);
        selection.copy(cursor);

        for (int i = 0; i < splits.size(); i++) {
            String line = this.text.get(selection.line).text;
            int l = splits.get(reverse ? splits.size() - (i + 1) : i).length();
            selection.offset += reverse ? -l : l;

            if (i < splits.size() - 1) {
                if (reverse && selection.offset < 0) return;
                if (!reverse && selection.offset + l < line.length()) return;
                selection.line += reverse ? -1 : 1;
                selection.offset = reverse ? this.text.get(selection.line).text.length() : 0;
            }
        }
    }

    public void checkSelection(boolean selecting) {
        if (selecting && !isSelected()) startSelecting();
        else if (!selecting && isSelected()) deselect();
    }

    /* Writing API */

    public void clear() {setText("");}

    protected void changedLine(int i) {
        calculateWrappedLine(text.get(i));
        recalculateSizes();
    }

    protected void changedLineAfter(int index) {
        while (index < text.size()) calculateWrappedLine(text.get(index++));
        recalculateSizes();
    }

    public void writeNewLine() {
        if (!hasLine(cursor.line)) return;

        String line = text.get(cursor.line).text;

        if (cursor.offset == 0 || line.isEmpty()) text.add(cursor.line, createTextLine(""));
        else if (cursor.offset >= line.length()) text.add(cursor.line + 1, createTextLine(""));
        else {
            text.get(cursor.line).text = cursor.start(line);
            text.add(cursor.line + 1, createTextLine(cursor.end(line)));
            moveCursorToLineStart();
        }

        changedLineAfter(cursor.line);
        cursor.line += 1;
        cursor.offset = 0;
    }

    public void writeCharacter(String character) {
        if (!hasLine(cursor.line)) return;

        String line = text.get(cursor.line).text;
        int index = cursor.offset;

        if (index >= line.length()) line += character;
        else if (index == 0) line = character + line;
        else line = cursor.start(line) + character + cursor.end(line);

        text.get(cursor.line).text = line;
        changedLine(cursor.line);
    }

    public void writeString(String string) {
        List<String> splits = splitNewlineString(string);
        int size = splits.size();

        if (size == 1) {
            writeCharacter(string);
            cursor.offset += string.length();
        }
        else {
            int line = cursor.line;
            String remainder = cursor.end(text.get(line).text);
            text.get(line).text = cursor.start(text.get(line).text);

            for (int i = 0; i < size; i++) {
                if (i != 0) {
                    cursor.line++;
                    moveCursorToLineStart();
                    text.add(cursor.line, createTextLine(""));
                }
                writeCharacter(splits.get(i));
            }

            cursor.offset = splits.get(size - 1).length();
            writeCharacter(remainder);
            changedLineAfter(line);
        }
    }

    public void pasteText(String text) {
        TextEditUndo undo = new TextEditUndo(this);
        deleteSelection();
        writeString(text);
        undo.ready().post(text, cursor, selection);
        this.undo.pushUndo(undo);
    }

    public UndoManager<GuiMultiTextElement> getUndo() {return undo;}

    public char deleteCharacter() {
        if (!hasLine(cursor.line)) return '\0';

        String line = text.get(cursor.line).text;
        int index = Math.min(cursor.offset, line.length());

        if (index == 0) {
            if (cursor.line == 0) return '\0';
            cursor.line--;
            moveCursorToLineEnd();
            text.get(cursor.line).text += text.remove(cursor.line + 1).text;
            changedLineAfter(cursor.line);
            return '\n';
        }

        if (index == line.length()) {
            text.get(cursor.line).text = line.substring(0, line.length() - 1);
            moveCursorToLineEnd();
            changedLine(cursor.line);
            return line.charAt(line.length() - 1);
        }

        text.get(cursor.line).text = cursor.start(line, -1) + cursor.end(line);
        moveCursor(-1, 0);
        changedLine(cursor.line);
        return line.charAt(index);
    }

    public void deleteSelection() {
        if (!isSelected()) return;

        Cursor min = getMin();
        Cursor max = getMax();

        if (min.line == max.line) {
            String line = text.get(min.line).text;
            text.get(min.line).text = (min.offset <= 0 && max.offset >= line.length()) ? "" : min.start(line) + max.end(line);
        }
        else {
            String end = "";
            for (int i = max.line; i >= min.line; i--) {
                String line = text.get(i).text;
                if (i == max.line) {
                    end = max.end(line);
                    text.remove(i);
                }
                else if (i == min.line) text.get(i).text = min.start(line) + end;
                else text.remove(i);
            }
        }

        changedLineAfter(min.line);
        cursor.copy(min);
        deselect();
    }

    public boolean hasLine(int line) {return line >= 0 && line < text.size();}

    public Cursor getMin() {return selection.isThisLessTo(cursor) ? selection : cursor;}

    public Cursor getMax() {return selection.isThisLessTo(cursor) ? cursor : selection;}

    /* Cursor movement */

    public void moveCursor(int x, int y) {moveCursor(x, y, true);}

    public void moveCursor(int x, int y, boolean jumpLine) {
        if (!hasLine(cursor.line)) return;

        String line = text.get(cursor.line).text;

        if (x != 0) {
            int nx = cursor.offset + (x > 0 ? 1 : -1);
            if (nx < 0) {
                if (jumpLine && hasLine(cursor.line - 1)) {
                    cursor.line--;
                    moveCursorToLineEnd();
                }
                else moveCursorToLineStart();
            }
            else if (nx > line.length()) {
                if (jumpLine && hasLine(cursor.line + 1)) {
                    cursor.line++;
                    moveCursorToLineStart();
                }
                else moveCursorToLineEnd();
            }
            else cursor.offset = nx;
        }

        if (y != 0) {
            int ny = cursor.line + (y > 0 ? 1 : -1);
            if (hasLine(ny)) {
                cursor.line = ny;
                cursor.offset = MathUtils.clamp(cursor.offset, 0, text.get(cursor.line).text.length());
            }
        }
    }

    public void moveCursorToLineStart() {cursor.offset = 0;}

    public void moveCursorToLineEnd() {if (hasLine(cursor.line)) cursor.offset = text.get(cursor.line).text.length();}

    public void moveCursorTo(Cursor cursor, int x, int y) {
        x -= area.x + padding;
        y -= area.y + padding;
        x += horizontal.scroll - getShiftX();
        y += vertical.scroll;

        if (wrapping) moveToCursorWrapped(cursor, x, y);
        else moveCursorToUnwrapped(cursor, x, y);
    }

    private void moveToCursorWrapped(Cursor cursor, int x, int y) {
        if (text.isEmpty()) return;

        T current = null;
        int line = y < 0 ? 0 : y / lineHeight;
        int l = 0, s = 0;

        for (int i = 0, c = text.size(); i < c; i++) {
            T textLine = text.get(i);
            if (line >= l && line < l + textLine.getLines()) {
                current = textLine;
                cursor.line = i;
                s = line - l;
                break;
            }
            l += textLine.getLines();
        }

        if (current == null) {
            current = text.get(text.size() - 1);
            cursor.line = text.size() - 1;
            s = current.getLines() - 1;
        }

        cursor.offset = 0;
        String lineText = current.text;

        if (current.wrappedLines != null) {
            for (int i = 0; i < s; i++) cursor.offset += current.wrappedLines.get(i).length();
            lineText = current.wrappedLines.get(s);
        }

        if (x > font.getStringWidth(lineText)) {
            cursor.offset += lineText.length();
            return;
        }
        if (x < 0) return;

        int w = 0, i = 0;
        while (x > w) {
            w = font.getStringWidth(lineText.substring(0, i));
            cursor.offset++;
            i++;
        }
        if (cursor.offset > 0) cursor.offset -= 2;
    }

    private void moveCursorToUnwrapped(Cursor cursor, int x, int y) {
        cursor.line = MathUtils.clamp(y / lineHeight, 0, text.size() - 1);
        String line = text.get(cursor.line).text;
        int w = font.getStringWidth(line);

        if (x <= 0) moveCursorToLineStart();
        else if (x > w) moveCursorToLineEnd();
        else {
            cursor.offset = 0;
            w = font.getStringWidth(cursor.start(line));
            while (x > w) {
                w = font.getStringWidth(cursor.start(line, 1));
                cursor.offset++;
            }
            if (cursor.offset > 0) cursor.offset--;
        }
    }

    public void moveViewportToCursor() {
        if (!hasLine(cursor.line)) return;
        Vector2d pos = getCursorPosition(cursor);
        pos.x += horizontal.scroll;
        pos.y += vertical.scroll;
        horizontal.scrollIntoView((int) pos.x, 4 + padding * 2, getShiftX());
        vertical.scrollIntoView((int) pos.y, lineHeight + padding * 2, getShiftX());
    }

    /* Focusable */

    @Override
    public boolean isFocused() {return focused;}

    @Override
    public void focus(GuiContext context) {
        focused = true;
        Keyboard.enableRepeatEvents(true);
    }

    @Override
    public void unfocus(GuiContext context) {
        focused = false;
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void selectAll(GuiContext context) {selectAll();}

    @Override
    public void unselect(GuiContext context) {deselect();}

    /* GUI */

    @Override
    public void resize() {
        super.resize();
        if (lastW != area.w) {
            lastW = area.w;
            recalculateWrapping();
        }
        recalculateSizes();
        horizontal.clamp();
        vertical.clamp();
    }

    public void recalculate() {
        for (T textLine : text) calculateWrappedLine(textLine);
        recalculateSizes();
    }

    protected void recalculateWrapping() {if (wrapping) for (T textLine : text) calculateWrappedLine(textLine);}

    protected void calculateWrappedLine(T textLine) {
        if (wrapping) textLine.calculateWrappedLines(font, getWrappedWidth());
        else textLine.resetWrapping();
    }

    protected void recalculateSizes() {
        int w = 0, h = 0;
        for (T textLine : text) {
            if (!wrapping) w = Math.max(font.getStringWidth(textLine.text), w);
            h += textLine.getLines() * lineHeight;
        }

        int offset = getShiftX();
        horizontal.copy(area);
        horizontal.x += offset;
        horizontal.w -= offset;
        horizontal.scrollSize = wrapping ? w : getHorizontalSize(w);
        vertical.copy(area);
        vertical.scrollSize = h - (lineHeight - font.FONT_HEIGHT) + padding * 2;
    }

    @Override
    public boolean mouseClicked(GuiContext context) {
        if (super.mouseClicked(context)) return true;
        if (horizontal.mouseClicked(context) || vertical.mouseClicked(context)) return true;

        boolean wasFocused = focused;
        boolean shift = GuiScreen.isShiftKeyDown();
        focused = area.isInside(context);

        if (focused) {
            if (context.mouseButton == 0) {
                if (System.currentTimeMillis() < lastClick) {
                    selectGroup(0, true);
                    lastClick -= 500;
                }
                else {
                    if (!shift) {
                        deselect();
                        dragging = 1;
                    }
                    else if (!isSelected()) startSelecting();
                    moveCursorTo(cursor, context.mouseX, context.mouseY);
                    lastClick = System.currentTimeMillis() + 200;
                }
            }
            else if (context.mouseButton == 2) dragging = 3;

            lastMX = context.mouseX;
            lastMY = context.mouseY;
        }

        if (wasFocused != focused) context.focus(wasFocused ? null : this);
        return focused;
    }

    @Override
    public boolean mouseScrolled(GuiContext context) {
        if (super.mouseScrolled(context)) return true;
        if (GuiScreen.isShiftKeyDown()) return horizontal.mouseScroll(context);
        if (vertical.scrollSize < area.h) return false;
        return vertical.mouseScroll(context);
    }

    @Override
    public void mouseReleased(GuiContext context) {
        super.mouseReleased(context);
        horizontal.mouseReleased(context);
        vertical.mouseReleased(context);
        dragging = 0;
    }

    @Override
    public boolean keyTyped(GuiContext context) {
        if (super.keyTyped(context)) return true;
        if (!focused) return false;

        if (context.keyCode == Keyboard.KEY_ESCAPE) {
            context.unfocus();
            return true;
        }

        boolean ctrl = GuiScreen.isCtrlKeyDown();
        boolean shift = GuiScreen.isShiftKeyDown();
        TextEditUndo undo = new TextEditUndo(this);

        if (handleKeys(context, undo, ctrl, shift)) moveViewportToCursor();
        if (undo.ready) this.undo.pushUndo(undo);

        dirty = true;
        horizontal.clamp();
        vertical.clamp();

        return false;
    }

    protected boolean handleKeys(GuiContext context, TextEditUndo undo, boolean ctrl, boolean shift) {
        int keyCode = context.keyCode;


        if (ctrl && keyCode == Keyboard.KEY_A) {
            selectAll();
            return false;
        }

        if (keyCode == Keyboard.KEY_UP || keyCode == Keyboard.KEY_DOWN || keyCode == Keyboard.KEY_RIGHT || keyCode == Keyboard.KEY_LEFT) {
            int x = keyCode == Keyboard.KEY_RIGHT ? 1 : keyCode == Keyboard.KEY_LEFT ? -1 : 0;
            int y = keyCode == Keyboard.KEY_UP ? -1 : keyCode == Keyboard.KEY_DOWN ? 1 : 0;

            if (x != 0 && ctrl) {
                if (!selectGroup(x, shift)) {
                    checkSelection(shift);
                    moveCursor(x, 0);
                }
            }
            else {
                checkSelection(shift);
                moveCursor(x, y);
            }

            playSound(SoundEvents.BLOCK_CLOTH_STEP);
            return true;
        }

        if (keyCode == Keyboard.KEY_HOME) {
            checkSelection(shift);
            moveCursorToLineStart();
            playSound(SoundEvents.ENTITY_ARROW_SHOOT);
            return true;
        }
        if (keyCode == Keyboard.KEY_END) {
            checkSelection(shift);
            moveCursorToLineEnd();
            playSound(SoundEvents.ENTITY_ARROW_HIT);
            return true;
        }

        if (ctrl && (keyCode == Keyboard.KEY_C || keyCode == Keyboard.KEY_X) && isSelected()) {
            GuiScreen.setClipboardString(getSelectedText());
            if (keyCode == Keyboard.KEY_X && !readOnly) {
                deleteSelection();
                deselect();
                undo.ready().post("", cursor, selection);
                playSound(SoundEvents.BLOCK_ANVIL_USE);
            }
            else playSound(SoundEvents.ENTITY_ITEM_PICKUP);
            return keyCode == Keyboard.KEY_X;
        }

        if(readOnly) return false;

        if (ctrl && keyCode == Keyboard.KEY_Z) {
            boolean r = this.undo.undo(this);
            if (r) playSound(SoundEvents.BLOCK_CHEST_CLOSE);
            return r;
        }
        if (ctrl && keyCode == Keyboard.KEY_Y) {
            boolean r = this.undo.redo(this);
            if (r) playSound(SoundEvents.BLOCK_CHEST_OPEN);
            return r;
        }

        if (ctrl && keyCode == Keyboard.KEY_V) {
            String pasted = GuiScreen.getClipboardString();
            deleteSelection();
            deselect();
            writeString(pasted);
            undo.ready().post(pasted, cursor, selection);
            playSound(SoundEvents.ENTITY_GENERIC_EXPLODE);
            return true;
        }

        if (keyCode == Keyboard.KEY_TAB) {
            keyTab(undo.ready());
            undo.post(undo.postText, cursor, selection);
            playSound(shift ? SoundEvents.BLOCK_PISTON_CONTRACT : SoundEvents.BLOCK_PISTON_EXTEND);
            return true;
        }

        if (keyCode == Keyboard.KEY_RETURN) {
            keyNewLine(undo.ready());
            undo.post(undo.postText, cursor, selection);
            playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT);
            return true;
        }

        if (keyCode == Keyboard.KEY_BACK || keyCode == Keyboard.KEY_DELETE) {
            boolean delete = keyCode == Keyboard.KEY_DELETE;

            if (isSelected()) {
                deleteSelection();
                deselect();
                playSound(SoundEvents.ENTITY_GENERIC_EXPLODE);
            }
            else {
                if (delete) {
                    int measure = ctrl ? Math.max(measureGroup(1, cursor), 1) : 1;
                    StringBuilder str = new StringBuilder(undo.text);
                    for (int i = 0; i < measure; i++) {
                        moveCursor(1, 0);
                        str.append(deleteCharacter());
                    }
                    undo.text = str.toString();
                }
                else keyBackspace(undo, ctrl);
                playSound(SoundEvents.BLOCK_STONE_BREAK);
            }

            undo.ready().post("", cursor, selection);
            return true;
        }

        if (ChatAllowedCharacters.isAllowedCharacter(context.typedChar)) {
            String character = getFromChar(context.typedChar);
            if (!character.isEmpty()) {
                deleteSelection();
                deselect();
                writeCharacter(character);
                moveCursor(1, 0);
                undo.ready().post(character, cursor, selection);
                playSound(SoundEvents.BLOCK_STONE_PLACE);
            }
            return true;
        }

        return false;
    }

    protected void playSound(SoundEvent event) {if (MappetConfig.scriptEditorSounds.get()) GuiMappetUtils.playSound(event);}

    protected String getFromChar(char typedChar) {return String.valueOf(typedChar);}

    protected void keyNewLine(TextEditUndo undo) {
        deleteSelection();
        deselect();
        writeNewLine();
        undo.postText += "\n";
    }

    protected void keyBackspace(TextEditUndo undo, boolean ctrl) {
        int measure = ctrl ? Math.max(Math.abs(measureGroup(-1, cursor)), 1) : 1;
        for (int i = 0; i < measure; i++) undo.text = deleteCharacter() + undo.text;
    }

    protected void keyTab(TextEditUndo undo) {
        undo.postText = "\t";
        deleteSelection();
        deselect();
        writeString(undo.postText);
    }

    /* Draw */

    @Override
    public void draw(GuiContext context) {
        handleLogic(context);
        if (background) drawBackground();

        super.draw(context);
        GuiDraw.scissor(area.x, area.y, area.w, area.h, context);

        int x = area.x + padding;
        int y = area.y + padding;
        Cursor min = getMin();
        Cursor max = getMax();

        if (isSelected()) drawSelectionBar(x, y, min, max);

        for (int i = 0, ci = text.size(); i < ci; i++) {
            T textLine = text.get(i);
            int newX = x - horizontal.scroll + getShiftX();
            int newY = y - vertical.scroll;

            if (newY > area.ey()) break;

            boolean drawCursor = cursor.line == i && focused;

            if (newY + font.FONT_HEIGHT + (textLine.getLines() - 1) * lineHeight >= area.y) {
                int cursorW = 0, cursorA = 0;

                if (drawCursor) {
                    cursorW = textLine.text.isEmpty() ? 0 : font.getStringWidth(cursor.start(textLine.text));
                    cursorA = (int) (Math.sin((context.tick + context.partialTicks) / 2D) * 127.5 + 127.5) << 24;
                }

                if (textLine.wrappedLines == null) {
                    if (drawCursor) Gui.drawRect(newX + cursorW, newY - 1, newX + cursorW + 1, newY + font.FONT_HEIGHT + 1, cursorA + 0xffffff);
                    drawTextLine(textLine.text, i, 0, newX, newY);
                }
                else {
                    int wrappedW = 0;
                    for (int j = 0, cj = textLine.wrappedLines.size(); j < cj; j++) {
                        String wrappedLine = textLine.wrappedLines.get(j);
                        int lineW = font.getStringWidth(wrappedLine);
                        int lineY = newY + j * lineHeight;

                        if (drawCursor && (cursorW >= wrappedW && cursorW < wrappedW + lineW || cursor.offset >= textLine.text.length()))
                            Gui.drawRect(newX + cursorW - wrappedW, lineY - 1, newX + cursorW - wrappedW + 1, lineY + font.FONT_HEIGHT + 1,
                                    cursorA + 0xffffff);

                        drawTextLine(wrappedLine, i, j, newX, lineY);
                        wrappedW += lineW;
                    }
                }
            }

            y += textLine.getLines() * lineHeight;
        }

        horizontal.drawScrollbar();
        vertical.drawScrollbar();
        drawForeground(context);
        GuiDraw.unscissor(context);
    }

    protected int getShiftX() {return 0;}

    protected int getHorizontalSize(int w) {return w + padding * 2 + getShiftX();}

    protected void drawTextLine(String line, int i, int j, int nx, int ny) {font.drawString(line, nx, ny, textColor, textShadow);}

    protected void drawBackground() {
        area.draw(0xffa0a0a0);
        area.draw(0xff000000, 1);
    }

    protected void drawForeground(GuiContext context) {}

    private void handleLogic(GuiContext context) {
        if (dirty) {
            dirty = false;
            if (callback != null) callback.accept(getText());
        }

        if (dragging == 1 && (Math.abs(context.mouseX - lastMX) > 4 || Math.abs(context.mouseY - lastMY) > 4)) {
            startSelecting();
            dragging = 2;
        }
        if (focused && dragging == 2) {
            moveCursorTo(cursor, context.mouseX, context.mouseY);
            moveViewportToCursor();
        }

        if (dragging == 3) {
            horizontal.scroll += lastMX - context.mouseX;
            horizontal.clamp();
            vertical.scroll += lastMY - context.mouseY;
            vertical.clamp();
            lastMX = context.mouseX;
            lastMY = context.mouseY;
        }

        horizontal.drag(context);
        vertical.drag(context);
    }

    private void drawSelectionBar(int x, int y, Cursor min, Cursor max) {
        Vector2d minPos = getCursorPosition(min);
        Vector2d maxPos = getCursorPosition(max);
        drawSelectionArea(x + (int) minPos.x, y + (int) minPos.y, x + (int) maxPos.x, y + (int) maxPos.y);
    }

    protected Vector2d getCursorPosition(Cursor cursor) {
        Vector2d pos = new Vector2d();

        if (wrapping) getCursorPositionWrapped(cursor, pos);
        else {
            String line = text.get(cursor.line).text;
            pos.x = font.getStringWidth(cursor.start(line));
            pos.y = cursor.line * lineHeight;
        }

        pos.x = pos.x - horizontal.scroll + getShiftX();
        pos.y = pos.y - vertical.scroll;
        return pos;
    }

    private void getCursorPositionWrapped(Cursor cursor, Vector2d pos) {
        int lines = 0, offset = 0;

        for (int i = 0, c = text.size(); i < c; i++) {
            T textLine = text.get(i);

            if (i == cursor.line) {
                if (textLine.wrappedLines == null) {offset = font.getStringWidth(cursor.start(textLine.text));}
                else {
                    int textOffset = 0;
                    for (int j = 0; j < textLine.wrappedLines.size(); j++) {
                        String wrappedLine = textLine.wrappedLines.get(j);
                        if (cursor.offset >= textOffset && cursor.offset < textOffset + wrappedLine.length()) {
                            offset = font.getStringWidth(wrappedLine.substring(0, cursor.offset - textOffset));
                            break;
                        }
                        lines++;
                        textOffset += wrappedLine.length();
                    }
                    if (cursor.offset >= textLine.text.length()) {
                        lines--;
                        offset = font.getStringWidth(textLine.wrappedLines.get(textLine.wrappedLines.size() - 1));
                    }
                }
                break;
            }
            lines += textLine.getLines();
        }

        pos.x = offset;
        pos.y = lines * lineHeight;
    }

    private void drawSelectionArea(int x1, int y1, int x2, int y2) {
        final int selectionPad = 2;
        int color = ColorUtils.HALF_BLACK + McLib.primaryColor.get();
        boolean middle = y2 > y1 + lineHeight;
        boolean bottom = y2 > y1;

        int endX = bottom || middle ? area.ex() : x2 + selectionPad;
        int endY = bottom && !middle ? y2 : y1 + font.FONT_HEIGHT;
        if (!bottom && !middle) endY += selectionPad;

        Gui.drawRect(x1 - selectionPad, y1 - selectionPad, endX, endY, color);
        if (middle) Gui.drawRect(area.x, y1 + font.FONT_HEIGHT, area.ex(), y2, color);
        if (bottom) Gui.drawRect(area.x, y2, x2 + selectionPad, y2 + font.FONT_HEIGHT + selectionPad, color);
    }
}
package mchorse.mappet.client.gui.scripts.utils;

import com.google.common.collect.ImmutableSet;
import mchorse.mappet.utils.NBTUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class SyntaxHighlighter {
    private SyntaxStyle style;

    public Set<String> operators, primaryKeywords, secondaryKeywords, special, typeKeywords;
    public Pattern functionName;

    /* Parsing runtime data */
    private String buffer;
    private int last;

    public SyntaxHighlighter() {
        this.style = new SyntaxStyle();
    }

    public SyntaxHighlighter(NBTTagCompound tag) {
        this.operators = loadKeywords(tag, "operators");
        this.primaryKeywords = loadKeywords(tag, "primaryKeywords");
        this.secondaryKeywords = loadKeywords(tag, "secondaryKeywords");
        this.special = loadKeywords(tag, "special");
        this.typeKeywords = loadKeywords(tag, "typeKeywords");
        this.functionName = Pattern.compile(tag.getString("functionName"), Pattern.CASE_INSENSITIVE);
        this.style = new SyntaxStyle();
    }

    public SyntaxStyle getStyle() {
        return this.style;
    }

    public void setStyle(SyntaxStyle style) {
        if (style == null) return;
        this.style = style;
    }

    /**
     * Parse text segments that will be used for syntax highlighting.
     */
    public List<TextSegment> parse(FontRenderer font, List<HighlightedTextLine> textLines, String line, int lineIndex) {
        List<TextSegment> list = new ArrayList<>();
        List<TextSegment> prevLine = lineIndex > 0 ? textLines.get(lineIndex - 1).segments : null;

        if (prevLine != null && !prevLine.isEmpty()) {
            TextSegment last = prevLine.get(prevLine.size() - 1);

            if (last.color == this.style.comments && !last.text.startsWith("//") && !last.text.trim().endsWith("*/")) {
                list.add(new TextSegment(line, this.style.comments, 0));
                return list;
            }
        }

        this.buffer = "";
        char string = '\0';
        this.last = 0;

        main:
        for (int i = 0, c = line.length(); i < c; i++) {
            char character = line.charAt(i);
            char next = i < c - 1 ? line.charAt(i + 1) : '\0';

            // Strings
            if (character == '\'' || character == '"') {
                if (string == '\0') {
                    list.add(new TextSegment(this.buffer, this.style.other, font.getStringWidth(this.buffer)));
                    this.buffer = "";
                    string = character;
                } else if (string == character) {
                    char prev = i > 0 ? line.charAt(i - 1) : '\0';

                    if (prev != '\\') {
                        string = '\0';
                        this.buffer += character;
                        list.add(new TextSegment(this.buffer, this.style.strings, font.getStringWidth(this.buffer)));
                        this.buffer = "";
                        continue;
                    }
                }
            }

            boolean isString = string != '\0';

            // Multiline comments
            if (!isString && character == '/' && i < c - 1 && line.charAt(i + 1) == '*') {
                int lastI = i;
                i += 2;

                while (i < c) {
                    character = line.charAt(i);

                    if (character == '*' && i < c - 1 && line.charAt(i + 1) == '/') {
                        String comment = line.substring(lastI, i + 2);
                        list.add(new TextSegment(this.buffer, this.style.other, font.getStringWidth(this.buffer)));
                        list.add(new TextSegment(comment, this.style.comments, font.getStringWidth(comment)));
                        i += 1;
                        this.buffer = "";
                        continue main;
                    }

                    i += 1;
                }

                String comment = line.substring(lastI);
                list.add(new TextSegment(this.buffer, this.style.other, font.getStringWidth(this.buffer)));
                list.add(new TextSegment(comment, this.style.comments, 0));
                return list;
            }

            // One line comments
            if (!isString && character == '/' && i < c - 1 && line.charAt(i + 1) == '/') {
                String comment = line.substring(i);
                list.add(new TextSegment(this.buffer, this.style.other, font.getStringWidth(this.buffer)));
                list.add(new TextSegment(comment, this.style.comments, 0));
                return list;
            }

            // Operators
            if (!isString && operators.contains(String.valueOf(character))) {
                boolean isNumericalMinus = character == '-' && Character.isDigit(next);

                if (!isNumericalMinus) {
                    String sign = String.valueOf(character);
                    list.add(new TextSegment(this.buffer, this.style.other, font.getStringWidth(this.buffer)));
                    list.add(new TextSegment(sign, this.style.primary, font.getStringWidth(sign)));
                    this.buffer = "";
                    this.last = i;
                    continue;
                }
            }

            this.buffer += character;

            // Keywords
            if (!isString && ((next != '\0' && this.isIllegalName(next)) || i == c - 1)) {
                if (this.last < i) {
                    char last = line.charAt(this.last);
                    boolean predicateForNumbers = (last == '-' || last == '.') && Character.isDigit(line.charAt(this.last + 1));

                    if (this.isIllegalName(last) && !predicateForNumbers) {
                        this.last += 1;
                    }
                }

                String keyword = line.substring(this.last, i + 1);
                if (primaryKeywords.contains(keyword)) {
                    this.pushKeyword(list, keyword, this.style.primary, i, font);
                } else if (special.contains(keyword)) {
                    this.pushKeyword(list, keyword, this.style.special, i, font);
                } else if (secondaryKeywords.contains(keyword) || this.isFunctionCall(list, keyword, next)) {
                    this.pushKeyword(list, keyword, this.style.secondary, i, font);
                } else if (this.isNumberOrConstant(keyword)) {
                    this.pushKeyword(list, keyword, this.style.numbers, i, font);
                } else if (this.isIdentifier(list)) {
                    this.pushKeyword(list, keyword, this.style.identifier, i, font);
                }
            }

            if (this.isIllegalName(character)) {
                this.last = i;
            }
        }

        if (!this.buffer.trim().isEmpty()) {
            list.add(new TextSegment(this.buffer, this.style.other, 0));
        }

        return list;
    }

    private boolean isIllegalName(char character) {
        return !Character.isLetterOrDigit(character) && character != '_';
    }

    /**
     * Generic method to push keyword.
     */
    private void pushKeyword(List<TextSegment> list, String keyword, int color, int i, FontRenderer font) {
        if (this.buffer.length() > keyword.length()) {
            String other = this.buffer.substring(0, this.buffer.length() - keyword.length());

            list.add(new TextSegment(other, this.style.other, font.getStringWidth(other)));
        }

        list.add(new TextSegment(keyword, color, font.getStringWidth(keyword)));

        this.buffer = "";
        this.last = i + 1;
    }

    /**
     * Check whether the current state qualifies as a function call.
     */
    private boolean isFunctionCall(List<TextSegment> list, String keyword, char next) {
        if (!list.isEmpty()) {
//            TextSegment previous = list.get(list.size() - 1);
            boolean bufferIsKeyword = this.buffer.trim().equals(keyword);

//            if (previous.color == this.style.strings || previous.text.trim().equals(keyword.trim())) {
//                return false;
//            }

//            if (bufferIsKeyword && previous.color != this.style.other) {
//                return false;
//            }

            if (next != '(' || bufferIsKeyword) {
                return false;
            }
        }

        return next == '(' && functionName.matcher(keyword).matches();
    }

    /**
     * Check whether the given keyword is some constant or a number literal.
     */
    private boolean isNumberOrConstant(String keyword) {
        if (typeKeywords.contains(keyword)) {
            return true;
        }

        try {
            Double.parseDouble(keyword);
            return true;
        } catch (NumberFormatException ignored) {
        }

        int length = keyword.trim().length();

        if (keyword.startsWith("0x") && length >= 3 && length <= 10) {
            try {
                Long.parseLong(keyword.substring(2), 16);
                return true;
            } catch (Exception ignored) {
            }
        }

        return false;
    }

    private boolean isIdentifier(List<TextSegment> list) {
        if (!list.isEmpty()) {
            TextSegment previous = list.get(list.size() - 1);
            return previous.text.trim().equals("function") && previous.color == this.getStyle().secondary;
        }
        return false;
    }

    private Set<String> loadKeywords(NBTTagCompound tag, String key) {
        return ImmutableSet.copyOf(NBTUtils.getStringArray(tag.getTagList(key, 8)));
    }

    public NBTTagCompound toNBT() {
        return this.toNBT(new NBTTagCompound());
    }

    public NBTTagCompound toNBT(NBTTagCompound tag) {
        saveKeywords(tag, "operators", operators);
        saveKeywords(tag, "primaryKeywords", primaryKeywords);
        saveKeywords(tag, "secondaryKeywords", secondaryKeywords);
        saveKeywords(tag, "special", special);
        saveKeywords(tag, "typeKeywords", typeKeywords);
        tag.setString("functionName", functionName.toString());
        return tag;
    }

    private void saveKeywords(NBTTagCompound tag, String key, Set<String> keywords) {
        NBTTagList tagList = new NBTTagList();
        NBTUtils.writeStringList(tagList, keywords);
        tag.setTag(key, tagList);
    }
}

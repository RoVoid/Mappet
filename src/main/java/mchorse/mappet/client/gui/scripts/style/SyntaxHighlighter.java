package mchorse.mappet.client.gui.scripts.style;

import mchorse.mappet.Mappet;
import mchorse.mappet.client.gui.scripts.themes.Themes;
import mchorse.mappet.client.gui.scripts.utils.TextSegment;
import mchorse.mappet.client.gui.scripts.utils.TextSegment.TOKEN;
import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Признаю, ничего не смыслю в Lexer-ах ╰(￣ω￣ｏ)

public class SyntaxHighlighter {
    public final String doubleQuoted = "\"[^\"\\n]*\"";
    public final String singleQuoted = "'[^'\\n]*'";
    public final String backtickQuoted = "`[^'\\n]*`";
    public final String comment = "//.*";
    public final String multiComments = "/\\*[^*\\n]*(?:\\*(?!/)[^*\\n]*)*\\*?/?";
    public final String number = "(?<!\\w)-?(?:0x[\\da-fA-F]+|\\d+(?:\\.\\d+)?)(?!\\w)";
    public final String constant = "\\b(?:true|false|null|undefined)\\b";
    public final String keyword = "\\b(?:break|continue|switch|case|default|try|catch|delete|do|while|finally|if|else|for|each|in|instanceof|new|throw|typeof|with|yield|return|import)\\b";
    public final String identifier = "\\b(?:const|function|var|let|prototype)\\b";
    public final String special = "\\b(?:Java|Math|JSON|this|mappet|math)\\b";
    public final String method = "\\.\\s*(\\w+)\\s*(?=\\()";
    public final String function = "\\b\\w+\\s*(?=\\()";
    public final String operator = "[+\\-*/=<>!&|%^~]+";

    private final Pattern pattern;

    private SyntaxStyle style;

    public SyntaxHighlighter() {
        style = Mappet.scriptEditorSyntaxStyle.get();

        // Собираем pattern в правильном порядке
        String combinedPattern =
                "(" + doubleQuoted + ")" + "|" +                  // group 1  — string ""
                        "(" + singleQuoted + ")" + "|" +          // group 2  — string ''
                        "(" + backtickQuoted + ")" + "|" +        // group 3  — string ``
                        "(" + comment + ")" + "|" +               // group 4  — comment
                        "(" + multiComments + ")" + "|" +         // group 5  — multiComments
                        "(" + number + ")" + "|" +                // group 6  — number
                        "(" + constant + ")" + "|" +              // group 7  — constant
                        "(" + keyword + ")" + "|" +               // group 8  — keyword
                        "(" + identifier + ")" + "|" +            // group 9  — identifier
                        "(" + special + ")" + "|" +               // group 10 — special
                         method + "|" +                // group 11 — method
                        "(" + function + ")" + "|" +              // group 12 — function
                        "(" + operator + ")";                     // group 13 — operator

        pattern = Pattern.compile(combinedPattern);
    }

    public SyntaxStyle getStyle() {
        return style;
    }

    public void setStyle(SyntaxStyle style) {
        if (style == null) return;
        this.style = style;
    }

    public List<TextSegment> parse(FontRenderer font, String line, TextSegment lastSegment) {
        List<TextSegment> list = new ArrayList<>();
        if (line == null || line.isEmpty()) return list;

        boolean inComment = false;
        char inString = '\0';

        if (lastSegment != null) {
            // Продолжение многострочного комментария
            if (lastSegment.token == TOKEN.MULTI_COMMENTS && !lastSegment.text.trim().endsWith("*/")) {
                inComment = true;
                if (!line.contains("*/")) {
                    list.add(new TextSegment(TOKEN.MULTI_COMMENTS, line, style.comments, font.getStringWidth(line)));
                    return list;
                }
            }

            // Продолжение многострочной строки
            if (lastSegment.token == TOKEN.STRING && lastSegment.text.trim().endsWith("\\")) {
                inString = lastSegment.text.trim().charAt(0);
                if (!line.contains("" + inString)) {
                    list.add(new TextSegment(TOKEN.STRING, line, style.strings, font.getStringWidth(line)));
                    return list;
                }
            }
        }

        if (inComment) {
            int index = line.indexOf("*/");
            String str = line.substring(0, index + 2);
            line = line.substring(index + 2);
            list.add(new TextSegment(TOKEN.MULTI_COMMENTS, str, style.comments, font.getStringWidth(str)));
        }

        if (inString != '\0') {
            int index = line.indexOf(inString);
            String str = line.substring(0, index + 1);
            line = line.substring(index + 1);
            list.add(new TextSegment(TOKEN.STRING, str, style.strings, font.getStringWidth(str)));
        }

        Matcher matcher = pattern.matcher(line);
        int lastEnd = 0;

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            if (start > lastEnd) {
                String skipped = line.substring(lastEnd, start);
                int width = font.getStringWidth(skipped);
                list.add(new TextSegment(TOKEN.OTHER, skipped, style.other, width));
            }

            String match = matcher.group();
            TOKEN token;
            int color;

            if (matcher.group(1) != null) {
                token = TOKEN.STRING;
                color = style.strings;
            } else if (matcher.group(2) != null) {
                token = TOKEN.STRING;
                color = style.strings;
            } else if (matcher.group(3) != null) {
                token = TOKEN.STRING;
                color = style.strings;
            } else if (matcher.group(4) != null) {
                token = TOKEN.COMMENT;
                color = style.comments;
            } else if (matcher.group(5) != null) {
                token = TOKEN.MULTI_COMMENTS;
                color = style.comments;
            } else if (matcher.group(6) != null) {
                token = TOKEN.NUMBER;
                color = style.numbers;
            } else if (matcher.group(7) != null) {
                token = TOKEN.CONSTANT;
                color = style.constants;
            } else if (matcher.group(8) != null) {
                token = TOKEN.KEYWORD;
                color = style.keywords;
            } else if (matcher.group(9) != null) {
                token = TOKEN.IDENTIFIER;
                color = style.identifiers;
            } else if (matcher.group(10) != null) {
                token = TOKEN.SPECIAL;
                color = style.special;
            } else if (matcher.group(11) != null) {
                token = TOKEN.METHOD;
                color = style.methods;
            } else if (matcher.group(12) != null) {
                token = TOKEN.FUNCTION;
                color = style.functions;
            } else if (matcher.group(13) != null) {
                token = TOKEN.OPERATOR;
                color = style.operators;
            } else {
                token = TOKEN.OTHER;
                color = style.other;
            }

            list.add(new TextSegment(token, match, color, font.getStringWidth(match)));
            lastEnd = end;
        }

        // Добавить остаток строки (если что-то осталось)
        if (lastEnd < line.length()) {
            String tail = line.substring(lastEnd);
            int width = font.getStringWidth(tail);
            list.add(new TextSegment(TOKEN.OTHER, tail, style.other, width));
        }

        return list;
    }
}

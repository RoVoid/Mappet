package mchorse.mappet.client.gui.scripts.style;

import mchorse.mappet.client.gui.scripts.utils.TextSegment;
import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Признаю, ничего не смыслю в Lexer-ах ╰(￣ω￣ｏ)

public class SyntaxHighlighter {
    public enum TOKENS {
        COMMENT,        // //
        MULTI_COMMENTS, // /* */
        STRING,         // '' "" ``
        FUNCTION,       // function func(); funcInFile()
        METHOD,         // obj.method()
        OPERATOR,       // +-><=?!&|^ и другие
        NUMBER,         // 0.1 0x7 3 -8
        CONSTANT,       // true false null undefined
        IDENTIFIER,     // const function var let prototype
        KEYWORD,        // break continue switch case default try catch delete do while finally if else for each in instanceof new throw typeof with yield return import
        SPECIAL,        // math mappet this Math JSON
        OTHER           // всё остальное
    }

    public final String doubleQuoted = "\"(?:\\\\.|[^\"\\\\])*\"";
    public final String singleQuoted = "'(?:\\\\.|[^'\\\\])*'";
    public final String backtickQuoted = "`(?:\\\\.|[^`\\\\])*`";
    public final String comment = "//.*";
    public final String multiComments = "/\\*[^*\\n]*(?:\\*(?!/)[^*\\n]*)*\\*?/?";
    public final String number = "(?<!\\w)-?(?:0x[\\da-fA-F]+|\\d+(?:\\.\\d+)?)(?!\\w)";
    public final String constant = "\\b(?:true|false|null|undefined)\\b";
    public final String keyword = "\\b(?:break|continue|switch|case|default|try|catch|delete|do|while|finally|if|else|for|each|in|instanceof|new|throw|typeof|with|yield|return|import)\\b";
    public final String identifier = "\\b(?:const|function|var|let|prototype)\\b";
    public final String special = "\\b(?:Math|JSON|this|mappet|math)\\b";
    public final String method = "\\b\\w+\\.\\w+\\s*(?=\\()";
    public final String function = "\\b\\w+\\s*(?=\\()";
    public final String operator = "[+\\-*/=<>!&|%^~]+";

    private final Pattern pattern;

    private SyntaxStyle style;

    public SyntaxHighlighter() {
        this.style = new SyntaxStyle();

        // Собираем pattern в правильном порядке
        String combinedPattern =
                "(" + doubleQuoted + ")" + "|" +                  // group 1  — string
                        "(" + singleQuoted + ")" + "|" +          // group 2  — string
                        "(" + backtickQuoted + ")" + "|" +        // group 3  — string
                        "(" + comment + ")" + "|" +               // group 4  — comment
                        "(" + multiComments + ")" + "|" +         // group 5  — multiComments
                        "(" + number + ")" + "|" +                // group 6  — number
                        "(" + constant + ")" + "|" +              // group 7  — constant
                        "(" + keyword + ")" + "|" +               // group 8  — keyword
                        "(" + identifier + ")" + "|" +            // group 9  — identifier
                        "(" + special + ")" + "|" +               // group 10  — special
                        "(" + method + ")" + "|" +                // group 11  — method
                        "(" + function + ")" + "|" +              // group 12 — function
                        "(" + operator + ")";                     // group 13 — operator

        pattern = Pattern.compile(combinedPattern);
    }

    public SyntaxStyle getStyle() {
        return this.style;
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
            if (lastSegment.token == TOKENS.MULTI_COMMENTS && !lastSegment.text.trim().endsWith("*/")) {
                inComment = true;
                if (!line.contains("*/")) {
                    list.add(new TextSegment(TOKENS.MULTI_COMMENTS, line, style.comments, font.getStringWidth(line)));
                    return list;
                }
            }

            // Продолжение многострочной строки
            if (lastSegment.token == TOKENS.STRING && lastSegment.text.trim().endsWith("\\")) {
                inString = lastSegment.text.trim().charAt(0);
                if (!line.contains("" + inString)) {
                    list.add(new TextSegment(TOKENS.STRING, line, style.strings, font.getStringWidth(line)));
                    return list;
                }
            }
        }

        if (inComment) {
            int index = line.indexOf("*/");
            String str = line.substring(0, index + 2);
            line = line.substring(index + 2);
            list.add(new TextSegment(TOKENS.MULTI_COMMENTS, str, style.comments, font.getStringWidth(str)));
        }

        if (inString != '\0') {
            int index = line.indexOf(inString);
            String str = line.substring(0, index + 1);
            line = line.substring(index + 1);
            list.add(new TextSegment(TOKENS.STRING, str, style.strings, font.getStringWidth(str)));
        }

        Matcher matcher = pattern.matcher(line);
        int lastEnd = 0;

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            if (start > lastEnd) {
                String skipped = line.substring(lastEnd, start);
                int width = font.getStringWidth(skipped);
                list.add(new TextSegment(TOKENS.OTHER, skipped, style.other, width));
            }

            String match = matcher.group();
            TOKENS token;
            int color;

            if (matcher.group(1) != null) {
                token = TOKENS.STRING;
                color = style.strings;
            } else if (matcher.group(2) != null) {
                token = TOKENS.STRING;
                color = style.strings;
            } else if (matcher.group(3) != null) {
                token = TOKENS.STRING;
                color = style.strings;
            } else if (matcher.group(4) != null) {
                token = TOKENS.COMMENT;
                color = style.comments;
            } else if (matcher.group(5) != null) {
                token = TOKENS.MULTI_COMMENTS;
                color = style.comments;
            } else if (matcher.group(6) != null) {
                token = TOKENS.NUMBER;
                color = style.numbers;
            } else if (matcher.group(7) != null) {
                token = TOKENS.CONSTANT;
                color = style.constants;
            } else if (matcher.group(8) != null) {
                token = TOKENS.KEYWORD;
                color = style.keywords;
            } else if (matcher.group(9) != null) {
                token = TOKENS.IDENTIFIER;
                color = style.identifiers;
            } else if (matcher.group(10) != null) {
                token = TOKENS.SPECIAL;
                color = style.special;
            } else if (matcher.group(11) != null) {
                token = TOKENS.METHOD;
                color = style.methods;
            } else if (matcher.group(12) != null) {
                token = TOKENS.FUNCTION;
                color = style.functions;
            } else if (matcher.group(13) != null) {
                token = TOKENS.OPERATOR;
                color = style.operators;
            } else {
                token = TOKENS.OTHER;
                color = style.other;
            }

            list.add(new TextSegment(token, match, color, font.getStringWidth(match)));
            lastEnd = end;
        }

        // Добавить остаток строки (если что-то осталось)
        if (lastEnd < line.length()) {
            String tail = line.substring(lastEnd);
            int width = font.getStringWidth(tail);
            list.add(new TextSegment(TOKENS.OTHER, tail, style.other, width));
        }

        return list;
    }
}

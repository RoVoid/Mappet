package mchorse.mappet.client.gui.scripts.utils;

public class TextSegment {
    public TOKEN token;
    public String text;
    public int color;
    public int alpha;
    public int width;

    public TextSegment(TOKEN token, String text, int color, int width) {
        this(token, text, color, 0, width);
    }

    public TextSegment(TOKEN token, String text, int color, int alpha, int width) {
        this.token = token;
        this.text = text;
        this.color = color;
        this.alpha = alpha;
        this.width = width;
    }

    public boolean is(TOKEN token) {
        return this.token == token;
    }

    public enum TOKEN {
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
}
package mchorse.mappet.client.gui.scripts.analysis;

public class TextSegment {
    public Style style;
    public String text;
    public int color;
    public int alpha;
    public int width;

    public TextSegment(Style style, String text, int color, int width) {
        this(style, text, color, 0, width);
    }

    public TextSegment(Style style, String text, int color, int alpha, int width) {
        this.style = style;
        this.text = text;
        this.color = color;
        this.alpha = alpha;
        this.width = width;
    }

    public boolean is(Style style) {
        return this.style == style;
    }

    public enum Style {
        COMMENT,        // //
        MULTI_COMMENTS, // /* */
        STRING,         // '' "" ``
        FUNCTION,       // function func(); funcInFile()
        METHOD,         // obj.method()
        OPERATOR,       // +-><=?!&|^ и другие
        NUMBER,         // 0.1 0x7 3 -8
        CONSTANT,       // true false null undefined
        IDENTIFIER,     // const var let prototype function
        KEYWORD,        // break continue switch case default try catch delete do while finally if else for each in instanceof new throw typeof with yield return
        IMPORT,         // import
        PARAMETER,      // function(param)
        SPECIAL,        // math mappet this Math JSON Date Java
        OTHER           // всё остальное
    }
}
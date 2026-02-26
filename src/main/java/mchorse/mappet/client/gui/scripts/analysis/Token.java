package mchorse.mappet.client.gui.scripts.analysis;

public class Token {
    public final Type type;
    public final String text;
    public final int start;
    public final int end;

    public static final Token NULL = new Token(Type.EOF, "", 0, 0);

    public Token(Type type, String text, int start, int end) {
        this.type = type;
        this.text = text;
        this.start = start;
        this.end = end;
    }

    public int length() {
        return end - start;
    }

    public enum Type {
        WHITESPACE, COMMENT, MULTI_COMMENT, STRING, NUMBER, KEYWORD, CONSTANT, IDENTIFIER, OPERATOR, PUNCTUATION, EOF
    }
}


package mchorse.mappet.client.gui.scripts.analysis;

import java.util.*;

public class JsLexer {

    private static final Set<String> KEYWORDS = new HashSet<>(
            Arrays.asList("break", "continue", "switch", "case", "default", "try", "catch", "delete", "do", "while", "finally", "if",
                    "else", "for", "each", "in", "instanceof", "new", "throw", "typeof", "with", "yield", "return", "import", "var", "let",
                    "const", "function"));

    private static final Set<String> CONSTANTS = new HashSet<>(Arrays.asList("true", "false", "null", "undefined"));

    private String input;
    private int length;
    private int pos;
    private LexerState state;

    public List<Token> tokenize(String input, LexerState state) {
        this.input = input;
        length = input.length();
        pos = 0;
        this.state = state == null ? new LexerState() : state;

        List<Token> tokens = new ArrayList<>();

        if (this.state.inComment) tokens.add(readMultiComment(true));
        else if (this.state.inString != 0) tokens.add(readString(true));

        while (neof()) {
            char c = peek();

            if (Character.isWhitespace(c)) tokens.add(readWhitespace());
            else if (c == '/' && peekNext() == '/') tokens.add(readSingleComment());
            else if (c == '/' && peekNext() == '*') tokens.add(readMultiComment(false));
            else if (c == '"' || c == '\'' || c == '`') tokens.add(readString(false));
            else if (Character.isDigit(c)) tokens.add(readNumber());
            else if (isIdentifierStart(c)) tokens.add(readIdentifier());
            else if (isOperatorStart(c)) tokens.add(readOperator());
            else {
                int start = pos;
                next();
                tokens.add(token(Token.Type.PUNCTUATION, start));
            }
        }

        tokens.add(new Token(Token.Type.EOF, "", pos, pos));
        return tokens;
    }

    private Token readWhitespace() {
        int start = pos;
        while (neof() && Character.isWhitespace(peek())) next();
        return token(Token.Type.WHITESPACE, start);
    }

    private Token readSingleComment() {
        int start = pos;
        while (neof() && peek() != '\n') next();
        return token(Token.Type.COMMENT, start);
    }

    private Token readMultiComment(boolean continued) {
        int start = pos;
        if (!continued) {
            next();
            next(); // /*
        }

        while (neof()) {
            if (peek() == '*' && peekNext() == '/') {
                next();
                next();
                state.inComment = false;
                return token(Token.Type.MULTI_COMMENT, start);
            }
            next();
        }

        state.inComment = true;
        return token(Token.Type.MULTI_COMMENT, start);
    }

    private Token readString(boolean continued) {
        int start = pos;

        char quote = continued ? state.inString : next();
        if (!continued) state.inString = quote;

        while (neof()) {
            char c = next();
            if (c == '\\') {
                if (neof()) next();
            }
            else if (c == quote) {
                state.inString = 0;
                break;
            }
        }

        return token(Token.Type.STRING, start);
    }

    private Token readNumber() {
        int start = pos;

        if (peek() == '0' && (peekNext() == 'x' || peekNext() == 'X')) {
            next();
            do next(); while (neof() && isHex(peek()));
        }
        else {
            while (neof() && Character.isDigit(peek())) next();
            if (neof() && peek() == '.') do next(); while (neof() && Character.isDigit(peek()));
        }

        return token(Token.Type.NUMBER, start);
    }

    private Token readIdentifier() {
        int start = pos;
        while (neof() && isIdentifierPart(peek())) next();

        String text = input.substring(start, pos);
        if (KEYWORDS.contains(text)) return token(Token.Type.KEYWORD, start, text);
        if (CONSTANTS.contains(text)) return token(Token.Type.CONSTANT, start, text);
        return token(Token.Type.IDENTIFIER, start, text);
    }

    private Token readOperator() {
        int start = pos;
        while (neof() && isOperatorPart(peek())) next();
        return token(Token.Type.OPERATOR, start);
    }

    private Token token(Token.Type type, int start) {return token(type, start, start >= pos ? "" : input.substring(start, pos));}

    private Token token(Token.Type type, int start, String text) {return new Token(type, text, start, pos);}

    private boolean neof() {return pos < length;}

    private char peek() {return input.charAt(pos);}

    private char peekNext() {return pos + 1 < length ? input.charAt(pos + 1) : 0;}

    private char next() {return input.charAt(pos++);}

    private boolean isIdentifierStart(char c) {
        return Character.isLetter(c) || c == '_' || c == '$';
    }

    private boolean isIdentifierPart(char c) {
        return isIdentifierStart(c) || Character.isDigit(c);
    }

    private boolean isOperatorStart(char c) {
        return "+-*/=<>!&|%^~".indexOf(c) >= 0;
    }

    private boolean isOperatorPart(char c) {
        return isOperatorStart(c);
    }

    private boolean isHex(char c) {
        return Character.digit(c, 16) != -1;
    }
}


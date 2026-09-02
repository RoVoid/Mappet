package mchorse.mappet.client.gui.scripts.analysis;

import mchorse.mappet.Mappet;
import mchorse.mappet.client.gui.scripts.analysis.scope.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsParser {

    private List<Token> tokens;
    private int pos;

    private String sourceText;

    private Scope globalScope;
    private Scope currentScope;

    private FunctionType currentFunction;

    private TypeRegistry typeRegistry;

    public Scope parse(List<Token> tokens, TypeRegistry registry) {
        return parse(tokens, registry, null);
    }

    public Scope parse(List<Token> rawTokens, TypeRegistry registry, Map<String, Type> predefinedGlobals) {
        sourceText = reconstructSource(rawTokens);
        this.tokens = filterTrivia(rawTokens);
        pos = 0;
        typeRegistry = registry != null ? registry : new TypeRegistry();

        globalScope = new Scope(null);
        globalScope.start = 0;
        globalScope.end = Integer.MAX_VALUE;
        currentScope = globalScope;

        if (predefinedGlobals != null) {
            for (Map.Entry<String, Type> entry : predefinedGlobals.entrySet()) {
                currentScope.define(new Symbol(entry.getKey(), entry.getValue()));
            }
        }

        while (!check(Token.Type.EOF)) safeStatement();

        return globalScope;
    }

    private String reconstructSource(List<Token> raw) {
        if (raw.isEmpty()) return "";

        int length = raw.get(raw.size() - 1).end;
        StringBuilder sb = new StringBuilder(length);

        for (Token t : raw) {
            while (sb.length() < t.start) sb.append(' ');
            sb.append(t.text);
        }

        return sb.toString();
    }

    private Map<Integer, String> commentsByEnd;

    private List<Token> filterTrivia(List<Token> raw) {
        List<Token> out = new ArrayList<>(raw.size());
        commentsByEnd = new java.util.HashMap<>();

        for (Token t : raw) {
            if (t.type == Token.Type.MULTI_COMMENT) {
                commentsByEnd.put(t.end, t.text);
                continue;
            }
            if (t.type == Token.Type.WHITESPACE || t.type == Token.Type.COMMENT) continue;
            out.add(t);
        }

        if (out.isEmpty() || out.get(out.size() - 1).type != Token.Type.EOF) {
            int end = raw.isEmpty() ? 0 : raw.get(raw.size() - 1).end;
            out.add(new Token(Token.Type.EOF, "", end, end));
        }

        return out;
    }

    private void safeStatement() {
        try {
            parseStatement();
        }
        catch (RuntimeException e) {
            synchronize();
        }
    }

    private void synchronize() {
        if (!check(Token.Type.EOF)) pos++;

        int depth = 0;
        while (!check(Token.Type.EOF)) {
            String t = peek().text;

            if (t.equals("{")) {
                depth++;
            }
            else if (t.equals("}")) {
                if (depth == 0) return; // let the enclosing block consume its own closing brace
                depth--;
            }
            else if (t.equals(";") && depth == 0) {
                pos++;
                return;
            }

            pos++;
        }
    }

    private void parseStatement() {

        if (match(";")) return; // empty statement, also consumes the previous statement's trailing ';'

        if (matchKeyword("import")) {
            parseImport();
            return;
        }

        if (matchKeyword("function")) {
            parseFunction();
            return;
        }

        if (matchKeyword("var") || matchKeyword("let") || matchKeyword("const")) {
            parseVariable();
            return;
        }

        if (matchKeyword("return")) {
            parseReturn();
            return;
        }

        if (matchKeyword("for")) {
            parseFor();
            return;
        }

        if (match("{")) {
            enterScope();
            try {
                while (!check("}") && !check(Token.Type.EOF)) safeStatement();
                match("}");
            }
            finally {
                exitScope();
            }
            return;
        }

        parseExpression();
    }

    private void parseImport() {
        Token name = consume(Token.Type.IDENTIFIER);

        Type type = typeRegistry.resolve(name.text);
        if (type != null) currentScope.define(new Symbol(name.text, type));
    }

    private void parseVariable() {

        Token name = consume(Token.Type.IDENTIFIER);

        Type type = PrimitiveType.UNKNOWN;

        if (match("=")) type = parseExpression();

        currentScope.define(new Symbol(name.text, type));

        while (match(",")) {
            Token next = consume(Token.Type.IDENTIFIER);
            Type nextType = PrimitiveType.UNKNOWN;
            if (match("=")) nextType = parseExpression();
            currentScope.define(new Symbol(next.text, nextType));
        }
    }

    private void parseFunction() {

        Map<String, String> jsDocParamTypes = findPrecedingJsDocParams(previous().start);

        Token name = consume(Token.Type.IDENTIFIER);

        FunctionType fnType = new FunctionType();
        currentScope.define(new Symbol(name.text, fnType));

        consume("(");
        enterScope();

        FunctionType previousFunction = currentFunction;
        currentFunction = fnType;

        try {
            if (!check(")")) do {
                Token param = consume(Token.Type.IDENTIFIER);

                Type paramType = PrimitiveType.UNKNOWN;
                String jsDocType = jsDocParamTypes.get(param.text);
                if (jsDocType != null) {
                    Type resolved = typeRegistry.resolveTypeName(jsDocType);
                    if (resolved != null) paramType = resolved;
                }

                Symbol paramSymbol = new Symbol(param.text, paramType);
                currentScope.define(paramSymbol);
                fnType.parameterTypes.add(paramType);
                fnType.parameterNames.add(param.text);
            } while (match(","));

            consume(")");
            safeStatement();
        }
        finally {
            currentFunction = previousFunction;
            exitScope();
        }
    }

    private Map<String, String> findPrecedingJsDocParams(int functionKeywordStart) {
        String comment = null;
        int bestEnd = -1;

        for (Map.Entry<Integer, String> entry : commentsByEnd.entrySet()) {
            int end = entry.getKey();
            if (end > functionKeywordStart) continue;
            if (end > bestEnd) {
                bestEnd = end;
                comment = entry.getValue();
            }
        }

        Map<String, String> result = new java.util.LinkedHashMap<>();
        if (comment == null || !comment.startsWith("/**")) return result;
        if (bestEnd < 0 || !isOnlyWhitespaceBetween(bestEnd, functionKeywordStart)) return result;

        java.util.regex.Matcher m = JSDOC_PARAM.matcher(comment);
        while (m.find()) result.put(m.group(2), m.group(1));

        return result;
    }

    private static final java.util.regex.Pattern JSDOC_PARAM =
            java.util.regex.Pattern.compile("@param\\s*\\{([^}]+)\\}\\s*([A-Za-z_$][A-Za-z0-9_$]*)");

    private boolean isOnlyWhitespaceBetween(int start, int end) {
        if (sourceText == null) return true;
        for (int i = start; i < end && i < sourceText.length(); i++) {
            if (!Character.isWhitespace(sourceText.charAt(i))) return false;
        }
        return true;
    }

    private void parseReturn() {

        if (check(";") || check("}") || check(Token.Type.EOF)) return;

        Type returnType = parseExpression();

        if (currentFunction != null) currentFunction.returnType = merge(currentFunction.returnType, returnType);
    }

    private void parseFor() {

        boolean isEach = matchKeyword("each");

        consume("(");

        enterScope();
        try {
            matchKeyword("var");
            matchKeyword("let");
            matchKeyword("const");

            Token varName = consume(Token.Type.IDENTIFIER);

            consumeKeyword("in");

            Type iterableType = parseExpression();

            consume(")");

            Type loopType = PrimitiveType.UNKNOWN;

            if (!isEach) {
                if (iterableType instanceof ArrayType) loopType = PrimitiveType.NUMBER;
                else if (iterableType instanceof ObjectType) loopType = PrimitiveType.STRING;
            }
            else {
                Type element = elementType(iterableType);
                if (element != null) loopType = element;
            }

            currentScope.define(new Symbol(varName.text, loopType));

            safeStatement();
        }
        finally {
            exitScope();
        }
    }

    private Type parseExpression() {
        Type leftType = parseMember();
        if (match("=")) return parseExpression();
        return leftType;
    }

    private Type parseMember() {

        Type type = parsePrimary();

        while (true) {
            if (match(".")) {
                Token property = consume(Token.Type.IDENTIFIER);

                if (type instanceof ObjectType) {
                    ObjectType obj = (ObjectType) type;

                    if (obj.fields.containsKey(property.text)) type = obj.fields.get(property.text);
                    else if (obj.methods.containsKey(property.text)) type = new OverloadType(obj.methods.get(property.text));
                    else type = PrimitiveType.UNKNOWN;
                }
                else type = PrimitiveType.UNKNOWN;
            }
            else if (check("(") && (type instanceof FunctionType || type instanceof OverloadType)) {
                consume("(");

                int argCount = 0;
                if (!check(")")) do {
                    parseExpression();
                    argCount++;
                } while (match(","));

                consume(")");

                FunctionType fn = type instanceof OverloadType
                        ? TypeRegistry.pickOverload(((OverloadType) type).candidates, argCount)
                        : (FunctionType) type;

                type = fn != null ? fn.returnType : PrimitiveType.UNKNOWN;
            }
            else if (check("[")) {
                consume("[");
                if (!check("]")) parseExpression();
                consume("]");

                Type element = elementType(type);
                type = element != null ? element : PrimitiveType.UNKNOWN;
            }
            else break;
        }

        return type;
    }

    private Type parsePrimary() {
        if (match(Token.Type.NUMBER)) return PrimitiveType.NUMBER;
        if (match(Token.Type.STRING)) return PrimitiveType.STRING;
        if (match(Token.Type.CONSTANT)) return PrimitiveType.UNKNOWN;

        if (match("(")) {
            Type inner = check(")") ? PrimitiveType.UNKNOWN : parseExpression();
            consume(")");
            return inner;
        }

        if (match("[")) {
            Type elementType = PrimitiveType.UNKNOWN;

            if (!check("]")) {
                elementType = parseExpression();
                while (match(",")) parseExpression();
            }

            consume("]");
            return new ArrayType(elementType);
        }

        if (matchKeyword("new")) {
            Type type = parsePrimary();
            if (check("(")) {
                consume("(");
                if (!check(")")) do parseExpression(); while (match(","));
                consume(")");
            }
            return type;
        }

        if (match(Token.Type.IDENTIFIER)) {
            String name = previous().text;
            Symbol symbol = currentScope.resolve(name);
            return symbol != null ? symbol.type : PrimitiveType.UNKNOWN;
        }

        throw error("Unexpected token");
    }

    private Type elementType(Type type) {
        if (type instanceof ArrayType) return ((ArrayType) type).elementType;
        if (type instanceof ObjectType) return ((ObjectType) type).elementType;
        return null;
    }

    private void enterScope() {
        Scope s = new Scope(currentScope);
        s.start = peek().start;
        currentScope = s;
    }

    private void exitScope() {
        currentScope.end = previous().end;
        currentScope = currentScope.parent;
    }

    private boolean match(String text) {
        if (check(text)) {
            advance();
            return true;
        }
        return false;
    }

    private boolean match(Token.Type type) {
        if (check(type)) {
            advance();
            return true;
        }
        return false;
    }

    private boolean matchKeyword(String kw) {
        if (check(Token.Type.KEYWORD) && peek().text.equals(kw)) {
            advance();
            return true;
        }
        return false;
    }

    private boolean check(String text) {
        return peek().text.equals(text);
    }

    private boolean check(Token.Type type) {
        return peek().type == type;
    }

    private Token consume(String text) {
        if (!check(text)) throw error("Expected " + text);
        Token t = peek();
        advance();
        return t;
    }

    private Token consume(Token.Type type) {
        if (!check(type)) throw error("Expected " + type);
        Token t = peek();
        advance();
        return t;
    }

    private Token consumeKeyword(String kw) {
        if (!check(Token.Type.KEYWORD) || !peek().text.equals(kw)) throw error("Expected keyword " + kw);
        Token t = peek();
        advance();
        return t;
    }

    private void advance() {
        if (pos < tokens.size() - 1) pos++;
    }

    private Token peek() {
        return tokens.get(Math.min(pos, tokens.size() - 1));
    }

    private Token previous() {
        return tokens.get(Math.max(0, Math.min(pos - 1, tokens.size() - 1)));
    }

    private RuntimeException error(String msg) {
        return new RuntimeException(msg);
    }

    private Type merge(Type a, Type b) {
        if (a == PrimitiveType.UNKNOWN) return b;
        if (b == PrimitiveType.UNKNOWN) return a;
        return a.equals(b) ? a : PrimitiveType.UNKNOWN;
    }
}

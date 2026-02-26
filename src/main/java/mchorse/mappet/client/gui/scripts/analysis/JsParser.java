package mchorse.mappet.client.gui.scripts.analysis;

import mchorse.mappet.client.gui.scripts.analysis.scope.*;

import java.util.List;

public class JsParser {

    private List<Token> tokens;
    private int pos;

    private Scope globalScope;
    private Scope currentScope;

    private FunctionType currentFunction;

    private TypeRegistry typeRegistry;

    public Scope parse(List<Token> tokens, TypeRegistry registry) {
        this.tokens = tokens;
        pos = 0;
        typeRegistry = registry;

        globalScope = new Scope(null);
        currentScope = globalScope;

        while (!check(Token.Type.EOF)) parseStatement();

        return globalScope;
    }

    private void parseStatement() {

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
            while (!match("}")) parseStatement();
            exitScope();
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
    }

    private void parseFunction() {

        Token name = consume(Token.Type.IDENTIFIER);

        FunctionType fnType = new FunctionType();
        currentScope.define(new Symbol(name.text, fnType));

        consume("(");

        enterScope();

        FunctionType previousFunction = currentFunction;
        currentFunction = fnType;

        if (!check(")")) do {
            Token param = consume(Token.Type.IDENTIFIER);
            Symbol paramSymbol = new Symbol(param.text, PrimitiveType.UNKNOWN);
            currentScope.define(paramSymbol);
            fnType.parameterTypes.add(PrimitiveType.UNKNOWN);
        } while (match(","));

        consume(")");
        parseStatement(); // тело функции

        currentFunction = previousFunction;
        exitScope();
    }

    private void parseReturn() {

        Type returnType = parseExpression();

        if (currentFunction != null) currentFunction.returnType = merge(currentFunction.returnType, returnType);
    }

    private void parseFor() {

        boolean isEach = matchKeyword("each");

        consume("(");

        matchKeyword("var");
        matchKeyword("let");
        matchKeyword("const");

        Token varName = consume(Token.Type.IDENTIFIER);

        consumeKeyword("in");

        Type iterableType = parseExpression();

        consume(")");

        enterScope();

        Type loopType = PrimitiveType.UNKNOWN;

        // for each (x in obj)
        if (!isEach) {
            // for (x in obj)
            if (iterableType instanceof ArrayType) loopType = PrimitiveType.NUMBER;
            else if (iterableType instanceof ObjectType) loopType = PrimitiveType.STRING;
        }
        else if (iterableType instanceof ArrayType) loopType = ((ArrayType) iterableType).elementType;

        currentScope.define(new Symbol(varName.text, loopType));

        parseStatement();

        exitScope();
    }


    private Type parseExpression() {
        Type leftType = parseMember();
        if (match("=")) return parseExpression(); // rightType
        return leftType;
    }

    private Type parseMember() {

        Type type = parsePrimary();

        while (match(".")) {
            Token property = consume(Token.Type.IDENTIFIER);

            if (type instanceof ObjectType) {
                ObjectType obj = (ObjectType) type;

                if (obj.fields.containsKey(property.text)) type = obj.fields.get(property.text);
                else if (obj.methods.containsKey(property.text)) type = obj.methods.get(property.text);
                else type = PrimitiveType.UNKNOWN;
            }
        }

        if (match("(")) if (type instanceof FunctionType) {
            FunctionType fn = (FunctionType) type;

            if (!check(")")) do parseExpression(); while (match(","));

            consume(")");
            return fn.returnType;
        }

        return type;
    }

    private Type parsePrimary() {
        if (match(Token.Type.NUMBER)) return PrimitiveType.NUMBER;
        if (match(Token.Type.STRING)) return PrimitiveType.STRING;
        if (match("[")) {
            Type elementType = PrimitiveType.UNKNOWN;

            if (!check("]")) {
                elementType = parseExpression();
                while (match(",")) parseExpression();
            }

            consume("]");
            return new ArrayType(elementType);
        }

        if (match(Token.Type.IDENTIFIER)) {
            String name = previous().text;
            Symbol symbol = currentScope.resolve(name);
            return symbol != null ? symbol.type : PrimitiveType.UNKNOWN;
        }

        throw error("Unexpected token");
    }


    private void enterScope() {
        currentScope = new Scope(currentScope);
    }

    private void exitScope() {
        currentScope = currentScope.parent;
    }

    private boolean match(String text) {
        if (check(text)) {
            pos++;
            return true;
        }
        return false;
    }

    private boolean match(Token.Type type) {
        if (check(type)) {
            pos++;
            return true;
        }
        return false;
    }

    private boolean matchKeyword(String kw) {
        if (check(Token.Type.KEYWORD) && peek().text.equals(kw)) {
            pos++;
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
        return tokens.get(pos++);
    }

    private Token consume(Token.Type type) {
        if (!check(type)) throw error("Expected " + type);
        return tokens.get(pos++);
    }

    private Token consumeKeyword(String kw) {
        if (!check(Token.Type.KEYWORD) || !peek().text.equals(kw)) throw error("Expected keyword " + kw);
        return tokens.get(pos++);
    }

    private Token peek() {
        return tokens.get(pos);
    }

    private Token previous() {
        return tokens.get(pos - 1);
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

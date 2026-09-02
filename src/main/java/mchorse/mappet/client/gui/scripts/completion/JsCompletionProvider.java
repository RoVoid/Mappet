package mchorse.mappet.client.gui.scripts.completion;

import mchorse.mappet.api.scripts.code.ScriptFactory;
import mchorse.mappet.api.scripts.code.math.ScriptMath;
import mchorse.mappet.client.gui.scripts.analysis.JsLexer;
import mchorse.mappet.client.gui.scripts.analysis.JsParser;
import mchorse.mappet.client.gui.scripts.analysis.Token;
import mchorse.mappet.client.gui.scripts.analysis.TypeRegistry;
import mchorse.mappet.client.gui.scripts.analysis.scope.*;

import java.util.*;

public class JsCompletionProvider implements ICompletionProvider {
    private static final int MAX_SUGGESTIONS = 8;

    private static final String[] SYNTAX_KEYWORDS = {"let", "var", "const", "for", "function", "if", "else", "return", "import"};

    /** JS Array.prototype-style methods offered on ArrayType values (real array literals / T[] — not List<T>, which keeps its real reflected methods instead). {name, "(params)", "returnType"}, where "T" in the return type is substituted with the array's actual element type. */
    private static final String[][] ARRAY_METHODS = {
            {"push", "(item)", "number"},
            {"pop", "()", "T"},
            {"shift", "()", "T"},
            {"unshift", "(item)", "number"},
            {"slice", "(start, end)", "T[]"},
            {"splice", "(start, deleteCount)", "T[]"},
            {"indexOf", "(item)", "number"},
            {"includes", "(item)", "boolean"},
            {"join", "(separator)", "string"},
            {"forEach", "(callback)", "void"},
            {"map", "(callback)", "T[]"},
            {"filter", "(callback)", "T[]"},
            {"reduce", "(callback, initial)", "unknown"},
            {"find", "(callback)", "T"},
            {"sort", "(callback)", "T[]"},
            {"reverse", "()", "T[]"},
            {"concat", "(other)", "T[]"},
    };

    private final TypeRegistry typeRegistry;
    private final Map<String, Type> globals = new LinkedHashMap<>();

    private int cachedVersion = Integer.MIN_VALUE;
    private int cachedContentHash = 0;
    private Scope cachedRoot;

    public JsCompletionProvider() {
        this(new TypeRegistry());
        addGlobal("mappet", ScriptFactory.class);
        addGlobal("math", ScriptMath.class);
    }

    public JsCompletionProvider(TypeRegistry typeRegistry) {
        this.typeRegistry = typeRegistry != null ? typeRegistry : new TypeRegistry();
    }

    public TypeRegistry getTypeRegistry() {return typeRegistry;}

    public JsCompletionProvider addGlobal(String name, Class<?> clazz) {
        globals.put(name, typeRegistry.fromClass(clazz));
        invalidate();
        return this;
    }

    public JsCompletionProvider addGlobal(String name, Type type) {
        globals.put(name, type);
        invalidate();
        return this;
    }

    public void invalidate() {
        cachedVersion = Integer.MIN_VALUE;
        cachedRoot = null;
    }

    /* ================================================================
     * Document-aware API — what GuiCodeEditor actually calls.
     * ================================================================ */

    @Override
    public boolean isTriggered(ICompletionContext ctx) {
        String line = ctx.getLine(ctx.getCursorLine());
        int cursorOffset = ctx.getCursorOffset();

        if (isInsideString(line, cursorOffset)) return false;
        if (jsDocTypeSlot(line, cursorOffset) != null) return true;

        return extractChain(line, cursorOffset) != null;
    }

    @Override
    public List<CompletionSuggestion> getSuggestions(ICompletionContext ctx) {
        String line = ctx.getLine(ctx.getCursorLine());
        int cursorOffset = ctx.getCursorOffset();
        if (isInsideString(line, cursorOffset)) return Collections.emptyList();

        String jsDocType = jsDocTypeSlot(line, cursorOffset);
        if (jsDocType != null) return suggestTypeNames(jsDocType);

        Chain chain = extractChain(line, cursorOffset);
        if (chain == null) return Collections.emptyList();

        String before = line.substring(0, chain.chainStart);
        if (chain.parts.size() == 1 && !chain.hasDot && isImportContext(before)) {
            return suggestImports(chain.parts.get(0).toLowerCase());
        }

        Scope scope = scopeAt(ctx);
        return suggest(scope, chain, before);
    }

    @Override
    public String complete(ICompletionContext ctx, CompletionSuggestion suggestion, int[] outCursorOffset) {
        return applyCompletion(ctx.getLine(ctx.getCursorLine()), ctx.getCursorOffset(), suggestion, outCursorOffset);
    }

    /* ================================================================
     * Legacy single-line API, kept so this class still satisfies the
     * base interface standalone. No positional scope is available here
     * (only whatever the document-aware calls last cached).
     * ================================================================ */

    @Override
    public boolean isTriggered(String line, int cursorOffset) {
        if (isInsideString(line, cursorOffset)) return false;
        if (jsDocTypeSlot(line, cursorOffset) != null) return true;
        return extractChain(line, cursorOffset) != null;
    }

    @Override
    public List<CompletionSuggestion> getSuggestions(String line, int cursorOffset) {
        if (isInsideString(line, cursorOffset)) return Collections.emptyList();

        String jsDocType = jsDocTypeSlot(line, cursorOffset);
        if (jsDocType != null) return suggestTypeNames(jsDocType);

        Chain chain = extractChain(line, cursorOffset);
        if (chain == null) return Collections.emptyList();

        Scope scope = cachedRoot != null ? cachedRoot : new Scope(null);
        return suggest(scope, chain, line.substring(0, chain.chainStart));
    }

    @Override
    public String complete(String line, int cursorOffset, CompletionSuggestion suggestion, int[] outCursorOffset) {
        return applyCompletion(line, cursorOffset, suggestion, outCursorOffset);
    }

    /* ================================================================
     * Parsing / caching
     * ================================================================ */

    private Scope scopeAt(ICompletionContext ctx) {
        Scope root = parseDocument(ctx);
        Scope at = root.findScopeAt(absoluteOffset(ctx));
        return at != null ? at : root;
    }

    private Scope parseDocument(ICompletionContext ctx) {
        String source = joinLines(ctx);
        int contentHash = source.hashCode();

        if (cachedRoot != null && ctx.getVersion() == cachedVersion && contentHash == cachedContentHash) {
            return cachedRoot;
        }

        List<Token> tokens = new JsLexer().tokenize(source, null);

        Scope root;
        try {
            root = new JsParser().parse(tokens, typeRegistry, globals);
        }
        catch (Exception e) {
            root = new Scope(null);
        }

        cachedRoot = root;
        cachedVersion = ctx.getVersion();
        cachedContentHash = contentHash;
        return root;
    }

    private String joinLines(ICompletionContext ctx) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, n = ctx.getLineCount(); i < n; i++) {
            sb.append(ctx.getLine(i));
            if (i < n - 1) sb.append('\n');
        }
        return sb.toString();
    }

    private int absoluteOffset(ICompletionContext ctx) {
        int offset = 0;
        int cursorLine = ctx.getCursorLine();

        for (int i = 0; i < cursorLine; i++) offset += ctx.getLine(i).length() + 1;
        offset += Math.min(ctx.getCursorOffset(), ctx.getLine(cursorLine).length());
        return offset;
    }

    /* ================================================================
     * Chain extraction: turns "foo.bar.b" (cursor right after "b") into
     * parts ["foo", "bar", "b"], the last element being the partial
     * name currently being typed/completed. Each earlier part also
     * records whether it was called ("foo()") or indexed ("foo[0]"),
     * which lets member resolution see past a value-returning method
     * call in the middle of the chain (e.g. "vector().x").
     * ================================================================ */

    private static class Chain {
        final List<String> parts;
        final List<Boolean> called;
        final List<Boolean> indexed;
        final List<Integer> argCounts;
        final int chainStart;
        final boolean hasDot;

        Chain(List<String> parts, List<Boolean> called, List<Boolean> indexed, List<Integer> argCounts, int chainStart, boolean hasDot) {
            this.parts = parts;
            this.called = called;
            this.indexed = indexed;
            this.argCounts = argCounts;
            this.chainStart = chainStart;
            this.hasDot = hasDot;
        }
    }

    private Chain extractChain(String line, int cursorOffset) {
        int end = Math.max(0, Math.min(cursorOffset, line.length()));
        int pos = end;

        List<String> names = new ArrayList<>();
        List<Boolean> called = new ArrayList<>();
        List<Boolean> indexed = new ArrayList<>();
        List<Integer> argCounts = new ArrayList<>();

        int identStart = pos;
        while (identStart > 0 && isIdentPart(line.charAt(identStart - 1))) identStart--;

        names.add(line.substring(identStart, pos));
        called.add(false);
        indexed.add(false);
        argCounts.add(0);
        pos = identStart;

        boolean hasDot = false;

        while (pos > 0 && line.charAt(pos - 1) == '.') {
            hasDot = true;
            pos--; // consume '.'

            boolean segCalled = false;
            boolean segIndexed = false;
            int segArgCount = 0;

            if (pos > 0 && (line.charAt(pos - 1) == ')' || line.charAt(pos - 1) == ']')) {
                char close = line.charAt(pos - 1);
                char open = close == ')' ? '(' : '[';
                int closeIdx = pos - 1;
                int depth = 0;
                int j = closeIdx;
                int commas = 0;

                while (j >= 0) {
                    char c = line.charAt(j);
                    if (c == close) depth++;
                    else if (c == open) {
                        depth--;
                        if (depth == 0) break;
                    }
                    else if (c == ',' && depth == 1 && close == ')') commas++;
                    j--;
                }

                if (j < 0) return null; // unbalanced brackets, bail out rather than guess

                boolean emptyArgs = line.substring(j + 1, closeIdx).trim().isEmpty();
                if (close == ')') {
                    segCalled = true;
                    segArgCount = emptyArgs ? 0 : commas + 1;
                }
                else segIndexed = true;

                pos = j;
            }

            int nameEnd = pos;
            while (pos > 0 && isIdentPart(line.charAt(pos - 1))) pos--;

            if (pos == nameEnd) break; // dot not preceded by an identifier/call, chain ends here

            names.add(line.substring(pos, nameEnd));
            called.add(segCalled);
            indexed.add(segIndexed);
            argCounts.add(segArgCount);
        }

        if (names.size() == 1 && names.get(0).isEmpty()) return null;

        Collections.reverse(names);
        Collections.reverse(called);
        Collections.reverse(indexed);
        Collections.reverse(argCounts);

        return new Chain(names, called, indexed, argCounts, pos, hasDot);
    }

    private boolean isIdentPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '$';
    }

    /** Whether the cursor sits inside an open '/"/` string literal on this line (escapes respected). Doesn't track multi-line template literals — good enough for suppressing suggestions while typing string content. */
    private boolean isInsideString(String line, int offset) {
        int end = Math.max(0, Math.min(offset, line.length()));
        char quote = 0;
        boolean escaped = false;

        for (int i = 0; i < end; i++) {
            char c = line.charAt(i);

            if (quote != 0) {
                if (escaped) escaped = false;
                else if (c == '\\') escaped = true;
                else if (c == quote) quote = 0;
            }
            else if (c == '\'' || c == '"' || c == '`') quote = c;
        }

        return quote != 0;
    }

    private static final java.util.regex.Pattern JSDOC_TYPE_SLOT =
            java.util.regex.Pattern.compile(".*@(?:param|returns?|type)\\s*\\{([^{}]*)$");

    /** If the cursor is inside a still-open "{...}" right after "@param"/"@return"/"@type" on this line (e.g. "* @param {Scr"), returns what's been typed so far ("Scr"); null otherwise. */
    private String jsDocTypeSlot(String line, int offset) {
        int end = Math.max(0, Math.min(offset, line.length()));
        String beforeCursor = line.substring(0, end);

        if (!beforeCursor.contains("*") && !beforeCursor.contains("/**")) return null;

        java.util.regex.Matcher m = JSDOC_TYPE_SLOT.matcher(beforeCursor);
        return m.matches() ? m.group(1) : null;
    }

    /** Suggests primitive/registered type names for a JSDoc "{...}" type slot, matched against the last identifier-like word in what's typed so far (so "List<Scr" still matches on "Scr"). */
    private List<CompletionSuggestion> suggestTypeNames(String partial) {
        int cut = partial.length();
        while (cut > 0 && isIdentPart(partial.charAt(cut - 1))) cut--;
        String prefix = partial.substring(cut).toLowerCase();

        Map<String, CompletionSuggestion> byName = new LinkedHashMap<>();

        for (String prim : new String[] {"number", "string", "boolean"}) {
            if (prim.startsWith(prefix)) byName.put(prim, new CompletionSuggestion(prim, prim, false).withType("primitive"));
        }

        for (String name : typeRegistry.getNamedTypes().keySet()) {
            if (name.toLowerCase().startsWith(prefix)) byName.put(name, new CompletionSuggestion(name, name, false).withType("type"));
        }

        return finalize(byName.values());
    }

    /* ================================================================
     * Signature help: parameter hints while typing inside a call's "(...)"
     * ================================================================ */

    public static class SignatureHint {
        public final String methodName;
        public final String signature;
        public final String returnType;
        public final int activeParameter;

        SignatureHint(String methodName, String signature, String returnType, int activeParameter) {
            this.methodName = methodName;
            this.signature = signature;
            this.returnType = returnType;
            this.activeParameter = activeParameter;
        }
    }

    public SignatureHint getSignatureHint(ICompletionContext ctx) {
        String line = ctx.getLine(ctx.getCursorLine());
        int cursorOffset = Math.max(0, Math.min(ctx.getCursorOffset(), line.length()));

        if (isInsideString(line, cursorOffset)) return null;

        CallSite call = findEnclosingCall(line, cursorOffset);
        if (call == null) return null;

        Chain chain = extractChain(line, call.calleeEnd);
        if (chain == null) return null;

        List<FunctionType> overloads;

        if (chain.parts.size() == 1) {
            Scope scope = scopeAt(ctx);
            Symbol symbol = scope != null ? scope.resolve(chain.parts.get(0)) : null;
            overloads = symbol != null && symbol.type instanceof FunctionType
                    ? Collections.singletonList((FunctionType) symbol.type) : null;
        }
        else {
            Scope scope = scopeAt(ctx);
            Type ownerType = resolveChainType(scope, chain, chain.parts.size() - 1);
            String methodName = chain.parts.get(chain.parts.size() - 1);
            overloads = ownerType instanceof ObjectType ? ((ObjectType) ownerType).methods.get(methodName) : null;
        }

        if (overloads == null || overloads.isEmpty()) return null;

        FunctionType fn = TypeRegistry.pickOverloadForParameter(overloads, call.activeParameter);
        if (fn == null) return null;

        String methodName = chain.parts.get(chain.parts.size() - 1);
        return new SignatureHint(methodName, TypeRegistry.describeParameters(fn), TypeRegistry.describe(fn.returnType), call.activeParameter);
    }

    private static class CallSite {
        final int calleeEnd;
        final int activeParameter;

        CallSite(int calleeEnd, int activeParameter) {
            this.calleeEnd = calleeEnd;
            this.activeParameter = activeParameter;
        }
    }

    /** Scans left from the cursor for the innermost unclosed "(" that follows an identifier/chain (i.e. a call, not a grouping paren), counting commas at depth 0 to find the active argument. */
    private CallSite findEnclosingCall(String line, int cursorOffset) {
        int depth = 0;
        int commas = 0;

        for (int i = cursorOffset - 1; i >= 0; i--) {
            char c = line.charAt(i);

            if (c == ')') depth++;
            else if (c == '(') {
                if (depth > 0) {depth--; continue;}

                if (i == 0 || !isIdentPart(line.charAt(i - 1))) return null;
                return new CallSite(i, commas);
            }
            else if (c == ',' && depth == 0) commas++;
            else if (c == ';' && depth == 0) return null;
        }

        return null;
    }

    /* ================================================================
     * Suggestion generation
     * ================================================================ */

    private List<CompletionSuggestion> suggest(Scope scope, Chain chain, String before) {
        String last = chain.parts.get(chain.parts.size() - 1).toLowerCase();

        if (chain.parts.size() == 1) return suggestScopeSymbols(scope, last, before);

        Type type = resolveChainType(scope, chain, chain.parts.size() - 1);

        if (type instanceof ArrayType) return suggestArrayMembers((ArrayType) type, last);
        if (!(type instanceof ObjectType)) return Collections.emptyList();

        return suggestMembers((ObjectType) type, last);
    }

    /** Resolves the type of a chain prefix (indices [0, uptoExclusive)), unwrapping through calls ("foo()") and indexing ("foo[0]") as it goes so a value-returning method mid-chain doesn't block resolution of what follows it. */
    private Type resolveChainType(Scope scope, Chain chain, int uptoExclusive) {
        if (uptoExclusive <= 0 || scope == null) return null;

        Symbol root = scope.resolve(chain.parts.get(0));
        Type type = root != null ? root.type : null;
        type = applySegmentOp(type, chain, 0);

        for (int i = 1; i < uptoExclusive && type != null; i++) {
            type = memberType(type, chain.parts.get(i));
            type = applySegmentOp(type, chain, i);
        }

        return type;
    }

    private Type applySegmentOp(Type type, Chain chain, int index) {
        if (type == null) return null;
        if (chain.called.get(index)) return invoke(type, chain.argCounts.get(index));
        if (chain.indexed.get(index)) return type instanceof ArrayType ? ((ArrayType) type).elementType : null;
        return type;
    }

    private Type invoke(Type type, int argCount) {
        if (type instanceof OverloadType) {
            FunctionType fn = TypeRegistry.pickOverload(((OverloadType) type).candidates, argCount);
            return fn != null ? fn.returnType : null;
        }
        if (type instanceof FunctionType) return ((FunctionType) type).returnType;
        return type;
    }

    private Type memberType(Type type, String member) {
        if (type instanceof ArrayType) return member.equals("length") ? PrimitiveType.NUMBER : null;
        if (!(type instanceof ObjectType)) return null;

        ObjectType obj = (ObjectType) type;
        if (obj.fields.containsKey(member)) return obj.fields.get(member);
        if (obj.methods.containsKey(member)) return new OverloadType(obj.methods.get(member));
        return null;
    }

    private List<CompletionSuggestion> suggestScopeSymbols(Scope scope, String prefix, String before) {
        Map<String, CompletionSuggestion> byName = new LinkedHashMap<>();

        for (Scope s = scope; s != null; s = s.parent) {
            for (Symbol symbol : s.getOwnSymbols()) {
                if (byName.containsKey(symbol.name)) continue;
                if (!symbol.name.toLowerCase().startsWith(prefix)) continue;

                boolean isFn = symbol.type instanceof FunctionType;
                CompletionSuggestion suggestion = new CompletionSuggestion(symbol.name, isFn ? symbol.name + "()" : symbol.name, isFn);
                annotate(suggestion, symbol.type);
                byName.put(symbol.name, suggestion);
            }
        }

        for (String keyword : SYNTAX_KEYWORDS) {
            if (byName.containsKey(keyword)) continue;
            if (!keyword.startsWith(prefix)) continue;
            byName.put(keyword, new CompletionSuggestion(keyword, keyword, false).withType("keyword"));
        }

        if (endsWithKeyword(before, "for") && "each".startsWith(prefix)) {
            byName.put("each", new CompletionSuggestion("each", "each (var x in list) {...}", false).withType("keyword"));
        }

        return finalize(byName.values());
    }

    /** Whether {@code text}, ignoring trailing whitespace, ends with {@code keyword} as a whole word (not as a suffix of a longer identifier). */
    private boolean endsWithKeyword(String text, String keyword) {
        String trimmed = text.trim();
        if (!trimmed.endsWith(keyword)) return false;

        int idx = trimmed.length() - keyword.length();
        return idx == 0 || !isIdentPart(trimmed.charAt(idx - 1));
    }

    private List<CompletionSuggestion> suggestArrayMembers(ArrayType type, String prefix) {
        Map<String, CompletionSuggestion> byName = new LinkedHashMap<>();
        String elem = TypeRegistry.describe(type.elementType);

        if ("length".startsWith(prefix)) {
            byName.put("length", new CompletionSuggestion("length", "length", false).withType("number"));
        }

        for (String[] m : ARRAY_METHODS) {
            String name = m[0];
            if (!name.toLowerCase().startsWith(prefix)) continue;

            String returnType = m[2].replace("T", elem);
            byName.put(name, new CompletionSuggestion(name, name + "()", true).withType(returnType).withSignature(m[1]));
        }

        return finalize(byName.values());
    }

    private List<CompletionSuggestion> suggestMembers(ObjectType type, String prefix) {
        Map<String, CompletionSuggestion> byName = new LinkedHashMap<>();

        for (Map.Entry<String, Type> field : type.fields.entrySet()) {
            String name = field.getKey();
            if (!name.toLowerCase().startsWith(prefix)) continue;
            byName.put(name, new CompletionSuggestion(name, name, false).withType(TypeRegistry.describe(field.getValue())));
        }

        for (Map.Entry<String, List<FunctionType>> method : type.methods.entrySet()) {
            String name = method.getKey();
            if (!name.toLowerCase().startsWith(prefix)) continue;

            List<FunctionType> overloads = method.getValue();
            FunctionType primary = overloads.get(0);

            CompletionSuggestion suggestion = new CompletionSuggestion(name, name + "()", true)
                    .withType(TypeRegistry.describe(primary.returnType))
                    .withSignature(TypeRegistry.describeOverloads(overloads));
            byName.put(name, suggestion);
        }

        return finalize(byName.values());
    }

    private void annotate(CompletionSuggestion suggestion, Type type) {
        if (type instanceof FunctionType) {
            FunctionType fn = (FunctionType) type;
            suggestion.withType(TypeRegistry.describe(fn.returnType));
            suggestion.withSignature(TypeRegistry.describeParameters(fn));
        }
        else {
            suggestion.withType(TypeRegistry.describe(type));
        }
    }

    private boolean isImportContext(String beforeChain) {
        return beforeChain.trim().equals("import");
    }

    private List<CompletionSuggestion> suggestImports(String prefix) {
        Map<String, CompletionSuggestion> byName = new LinkedHashMap<>();

        for (String name : typeRegistry.getNamedTypes().keySet()) {
            if (name.toLowerCase().startsWith(prefix)) byName.put(name, new CompletionSuggestion(name, name, false));
        }

        return finalize(byName.values());
    }

    private List<CompletionSuggestion> finalize(Collection<CompletionSuggestion> values) {
        List<CompletionSuggestion> result = new ArrayList<>(values);
        result.sort(Comparator.comparing(s -> s.name.toLowerCase()));
        if (result.size() > MAX_SUGGESTIONS) result = result.subList(0, MAX_SUGGESTIONS);
        return result;
    }

    /* ================================================================
     * Applying a suggestion
     * ================================================================ */

    private String applyCompletion(String line, int cursorOffset, CompletionSuggestion suggestion, int[] outCursorOffset) {
        int end = Math.max(0, Math.min(cursorOffset, line.length()));
        int start = end;

        while (start > 0 && isIdentPart(line.charAt(start - 1))) start--;

        String left = line.substring(0, start);
        String right = line.substring(end);

        if (suggestion.name.equals("each")) {
            String head = "each (var ";
            String tail = " in ) {}";
            outCursorOffset[0] = left.length() + head.length();
            return left + head + tail + right;
        }

        String insertion = suggestion.name;
        boolean alreadyCalled = right.startsWith("(");
        if (suggestion.isMethod && !alreadyCalled) insertion += "()";

        outCursorOffset[0] = left.length() + insertion.length();
        return left + insertion + right;
    }
}

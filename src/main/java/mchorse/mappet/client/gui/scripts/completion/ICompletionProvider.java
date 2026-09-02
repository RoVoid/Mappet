package mchorse.mappet.client.gui.scripts.completion;

import java.util.List;

/**
 * A pluggable source of autocomplete suggestions for {@link mchorse.mappet.client.gui.scripts.GuiCodeEditor}.
 *
 * One implementation exists per supported language (e.g. {@link JsCompletionProvider} for
 * JavaScript). GuiCodeEditor talks only to this interface, so a new language just needs a new
 * implementation — no changes to the editor itself.
 */
public interface ICompletionProvider {
    /**
     * Whether this provider has anything to suggest for the text right before the cursor
     * (e.g. text after the last unfinished "." in JS). Returning false skips suggestion lookup
     * and the overlay entirely.
     *
     * @param line           the full text of the current line
     * @param cursorOffset   cursor offset within {@code line}
     */
    boolean isTriggered(String line, int cursorOffset);

    /**
     * Returns the ranked list of suggestions for the current cursor position. Empty list = no
     * overlay is drawn. Implementations should keep this list short (a handful of entries).
     */
    List<CompletionSuggestion> getSuggestions(String line, int cursorOffset);

    /**
     * Applies the given suggestion to {@code line}, returning the resulting line. Also fills in
     * {@code outCursorOffset[0]} with where the cursor should land afterwards.
     */
    String complete(String line, int cursorOffset, CompletionSuggestion suggestion, int[] outCursorOffset);

    /* ================================================================
     * Document-aware variants. GuiCodeEditor calls these. Override them
     * for scope/type-aware completion (see JsCompletionProvider); the
     * default implementations just fall back to the line-only variants
     * above, so existing single-line providers keep working unchanged.
     * ================================================================ */

    default boolean isTriggered(ICompletionContext ctx) {
        return isTriggered(ctx.getLine(ctx.getCursorLine()), ctx.getCursorOffset());
    }

    default List<CompletionSuggestion> getSuggestions(ICompletionContext ctx) {
        return getSuggestions(ctx.getLine(ctx.getCursorLine()), ctx.getCursorOffset());
    }

    default String complete(ICompletionContext ctx, CompletionSuggestion suggestion, int[] outCursorOffset) {
        return complete(ctx.getLine(ctx.getCursorLine()), ctx.getCursorOffset(), suggestion, outCursorOffset);
    }
}

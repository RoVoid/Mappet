package mchorse.mappet.client.gui.scripts.completion;

/**
 * Read-only view of the editor's document and cursor, given to {@link ICompletionProvider} so
 * it can build scope-aware suggestions instead of looking only at the current line.
 *
 * Implemented directly by {@link mchorse.mappet.client.gui.scripts.GuiCodeEditor} — no copying
 * of the document is needed to satisfy it.
 */
public interface ICompletionContext {
    int getLineCount();

    String getLine(int index);

    int getCursorLine();

    int getCursorOffset();

    /**
     * Monotonically increasing counter bumped on every edit. Lets providers cache their parse
     * of the document and skip re-parsing on frames where nothing changed (this is queried every
     * frame while the cursor sits on a triggerable position, so caching matters).
     */
    int getVersion();
}

package mchorse.mappet.client.gui.scripts.search;

import mchorse.mappet.client.gui.utils.text.GuiMultiTextElement;
import mchorse.mclib.utils.undo.UndoManager;

import java.util.regex.Pattern;

// he just copied it from MappetExtra
public interface GuiTextEditorSearchable
{
    void setSearching(boolean searching);

    boolean isSearching();

    Pattern getPattern();

    void setPattern(Pattern pattern);

    int refreshSearchResults(boolean jumpToFirst);

    boolean navigateMatch(boolean backwards);

    int getMatchCount();

    int getCurrentMatchIndex();

    boolean replaceCurrentMatch(Pattern pattern, String replacement);

    int replaceAllMatches(Pattern pattern, String replacement);

    UndoManager<GuiMultiTextElement> getUndo();
}

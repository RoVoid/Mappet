package mchorse.mappet.client.gui.scripts.highlights;

import com.google.common.collect.ImmutableSet;
import mchorse.mappet.ClientProxy;
import mchorse.mappet.client.gui.scripts.utils.SyntaxHighlighter;
import mchorse.mappet.utils.NBTToJsonLike;

import java.io.File;
import java.util.regex.Pattern;

public class Highlighters
{
    private static File editorHighlighters;

    private static SyntaxHighlighter defaultHighlighter;

    public static File highlighterFile(String name)
    {
        if (!name.endsWith(".json"))
        {
            name += ".json";
        }

        return new File(editorHighlighters, name);
    }

    public static SyntaxHighlighter readHighlighter(File file)
    {
        try
        {
            return new SyntaxHighlighter(NBTToJsonLike.read(file));
        }
        catch (Exception ignored)
        {}

        return defaultHighlighter;
    }

    public static void writeHighlighter(File file, SyntaxHighlighter highlighter)
    {
        try
        {
            NBTToJsonLike.write(file, highlighter.toNBT());
        }
        catch (Exception ignored)
        {}
    }

    public static void initiate()
    {
        if (editorHighlighters != null)
        {
            return;
        }

        editorHighlighters = new File(ClientProxy.configFolder, "highlights");
        editorHighlighters.mkdirs();

        File js = new File(editorHighlighters, "js.json");
        File kts = new File(editorHighlighters, "kts.json");

        if (!js.isFile())
        {
            SyntaxHighlighter jsHighlighter = new SyntaxHighlighter();
            jsHighlighter.operators = ImmutableSet.of("+", "-", "=", "/", "*", "<", ">", "~", "&", "|", "!");
            jsHighlighter.primaryKeywords = ImmutableSet.of(
                    "break", "continue", "switch", "case", "default", "try",
                    "catch", "delete", "do", "while", "finally", "if",
                    "else", "for", "each", "in", "instanceof",
                    "new", "throw", "typeof", "with", "yield", "return"
            );
            jsHighlighter.secondaryKeywords = ImmutableSet.of("const", "function", "var", "let", "prototype", "Math", "JSON", "mappet", "math");
            jsHighlighter.special = ImmutableSet.of("this", "arguments");
            jsHighlighter.typeKeywords = ImmutableSet.of("true", "false", "null", "undefined");
            jsHighlighter.functionName = Pattern.compile("[\\w_]", Pattern.CASE_INSENSITIVE);

            writeHighlighter(js, jsHighlighter);
            Highlighters.defaultHighlighter = jsHighlighter;
        }
        else
        {
            Highlighters.defaultHighlighter = Highlighters.readHighlighter(Highlighters.highlighterFile("js.json"));
        }

        if (!kts.isFile())
        {
            SyntaxHighlighter ktsHighlighter = new SyntaxHighlighter();
            ktsHighlighter.operators = ImmutableSet.of("+", "-", "=", "/", "*", "<", ">", "~", "&", "|", "!", "..", "->");
            ktsHighlighter.primaryKeywords = ImmutableSet.of(
                    "break", "continue", "switch", "case", "try",
                    "catch", "delete", "do", "while", "else", "finally", "if",
                    "for", "is", "as", "in", "instanceof",
                    "new", "throw", "typeof", "with", "yield", "when", "return",
                    "by", "constructor", "delegate", "dynamic", "field", "get", "set", "init", "value",
                    "where", "actual", "annotation", "companion", "external", "infix", "inline", "inner", "internal",
                    "open", "operator", "out", "override", "suspend", "vararg"
            );
            ktsHighlighter.secondaryKeywords = ImmutableSet.of(
                    "abstract", "extends", "final", "implements", "interface", "super", "throws",
                    "data", "class", "fun", "var", "val", "import", "Java", "JSON", "mappet", "math"
            );
            ktsHighlighter.special = ImmutableSet.of("this", "it");
            ktsHighlighter.typeKeywords = ImmutableSet.of("true", "false", "null", "undefined", "enum");
            ktsHighlighter.functionName = Pattern.compile("[\\w_]+", Pattern.CASE_INSENSITIVE);


            writeHighlighter(kts, ktsHighlighter);
        }
    }
}

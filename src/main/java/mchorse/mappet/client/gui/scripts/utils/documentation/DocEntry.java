package mchorse.mappet.client.gui.scripts.utils.documentation;

import joptsimple.internal.Strings;
import mchorse.mappet.Mappet;
import mchorse.mappet.client.gui.scripts.GuiTextEditor;
import mchorse.mappet.client.gui.utils.text.GuiText;
import mchorse.mclib.client.gui.framework.elements.GuiScrollElement;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DocEntry {
    public DocEntry parent;

    public String name = "";
    public String displayName = "";
    public String doc = "";
    public String source = "Mappet";

    public List<DocEntry> entries = new ArrayList<>();

    public DocEntry() {

    }

    public DocEntry(String name) {
        this.name = name;

        int index = name.lastIndexOf(".");
        displayName = index < 0 ? name : name.substring(index + 1);
    }

    public String appendCode(String code) {
        List<String> strings = new ArrayList<>(Arrays.asList(code.split("\n")));
        int first = 0;
        for (String string : strings) {
            if (!string.trim().isEmpty()) break;
            first += 1;
        }

        String firstLine = strings.get(first);
        int indent = 0;
        for (int i = 0; i < firstLine.length(); i++) {
            if (firstLine.charAt(i) != ' ') break;
            indent += 1;
        }

        strings.remove(strings.size() - 1);

        if (indent > 0) {
            for (int i = 0; i < strings.size(); i++) {
                String string = strings.get(i);
                if (string.length() > indent) strings.set(i, string.substring(indent));
            }
        }

        return Strings.join(strings, "\n").trim();
    }

    public void append(Minecraft mc, GuiScrollElement target) {
        String[] splits = doc.split("\n{2,}");
        boolean parsing = false;
        StringBuilder code = new StringBuilder();

        for (String line : splits) {
            if (line.trim().startsWith("<pre>{@code")) {
                parsing = true;
                line = line.trim().substring("<pre>{@code".length() + 1);
            }

            if (parsing) code.append("\n\n").append(line);
            else {
                line = line.replaceAll("\n", "").trim();
                line = line.replaceAll("<b>", TextFormatting.BOLD.toString());
                line = line.replaceAll("<i>", TextFormatting.ITALIC.toString());
                line = line.replaceAll("<s>", TextFormatting.STRIKETHROUGH.toString());
                line = line.replaceAll("<code>", TextFormatting.GRAY.toString());
                line = line.replaceAll("<li> *", "\n- ");
                line = line.replaceAll("</(b|i|s|code|ul|li)>", TextFormatting.RESET.toString());
                line = line.replaceAll("</?(p|ul|li)>", "");
                line = line.replaceAll("\\{@link +[^}]+\\.([^}]+)}", TextFormatting.GOLD + "$1" + TextFormatting.RESET);
                line = line.replaceAll("\\{@link +([^}]*)#([^}]+)}", TextFormatting.GOLD + "$1" + TextFormatting.RESET + "." + TextFormatting.GRAY + "$2" + TextFormatting.RESET);
                line = line.replaceAll("\\{@link ([^}]+)}", TextFormatting.GOLD + "$1" + TextFormatting.RESET);
                line = line.replaceAll("&lt;", "<");
                line = line.replaceAll("&gt;", ">");
                line = line.replaceAll("&amp;", "&");

                GuiText text = new GuiText(mc).text(line.trim().replaceAll(" {2,}", " "));
                if (line.trim().startsWith("<p>")) text.marginTop(12);
                target.add(text);
            }

            if (line.trim().endsWith("}</pre>")) {
                GuiTextEditor editor = new GuiTextEditor(mc, null);
                String text = appendCode(code.toString()).replaceAll("§", "\\\\u00A7");

                editor.setText(text);
                editor.background().flex().h(editor.getLines().size() * 12 + 20);
                editor.getHighlighter().setStyle(Mappet.scriptEditorSyntaxStyle.get());
                target.add(editor);

                parsing = false;
                code = new StringBuilder();
            }
        }
    }

    public void render(Minecraft mc, GuiScrollElement target) {
        target.add(new GuiText(mc).text(IKey.format("mappet.gui.scripts.documentation.source", source)));
        append(mc, target);
    }

    public String getName() {
        return displayName;
    }

    public DocEntry getEntry() {
        return this;
    }

    public List<DocEntry> getEntries() {
        return entries;
    }

    public void setParent(DocEntry parent) {
        parent.entries.add(this);
        this.parent = parent;
    }

    public void addChildren(DocEntry... children) {
        for(DocEntry child : children) child.setParent(this);
    }
}
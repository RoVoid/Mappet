package mchorse.mappet.client.gui.scripts.utils.documentation;

import mchorse.mappet.Mappet;
import mchorse.mappet.client.gui.scripts.GuiTextEditor;
import mchorse.mappet.client.gui.utils.text.GuiText;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.GuiScrollElement;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DocMethodVariant extends DocEntry {
    public List<DocVariable> params = new ArrayList<>();
    public List<String> annotations = new ArrayList<>();
    public DocVariable returns;

    public DocMethodVariant(String name) {
        super(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<DocEntry> getEntries() {
        return null;
    }

    public String getNameWithParams() {
        StringBuilder str = new StringBuilder(getName());
        str.append('(');
        for (DocVariable param : params) str.append(param.getType()).append(' ').append(param.getName()).append(", ");
        if (!params.isEmpty()) str.delete(str.length() - 2, str.length());
        str.append(')');
        return str.toString();
    }

    @Override
    public void render(Minecraft mc, GuiScrollElement target) {
        super.render(mc, target);

        GuiTextEditor preview = new GuiTextEditor(mc, null);

        preview.marginTop(8).marginBottom(8);
        preview.setText(getNameWithParams());
        preview.background().flex().h(preview.getLines().size() + 20);
        preview.getHighlighter().setStyle(Mappet.scriptEditorSyntaxStyle.get());
        target.add(preview);

        for (DocVariable param : params) {
            GuiText text = new GuiText(mc).text(TextFormatting.GOLD + param.getType() + TextFormatting.RESET + " " + param.name);
            target.add(text);

            if (param.doc.isEmpty()) continue;
            param.append(mc, target);
            ((GuiElement) target.getChildren().get(target.getChildren().size() - 1)).marginBottom(8);
        }

        // TODO Можно пересмотреть
        List<String> annotations = this.annotations.stream()
                .map(annotation -> "@" + annotation.substring(annotation.lastIndexOf(".") + 1))
                .filter(annotation -> !annotation.equals("@Override"))
                .collect(Collectors.toList());
        if (!annotations.isEmpty()) {
            String annotationsText = String.join(", ", annotations);
            target.add(new GuiText(mc).text(String.valueOf(TextFormatting.GRAY) + TextFormatting.BOLD + annotationsText).marginTop(8));
        }

        target.add(new GuiText(mc).text("Returns " + TextFormatting.GOLD + returns.getType()).marginTop(8));
        if (!returns.doc.isEmpty()) returns.append(mc, target);
    }
}
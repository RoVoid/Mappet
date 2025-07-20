package mchorse.mappet.client.gui.scripts.utils.documentation;

import mchorse.mappet.client.gui.utils.text.GuiText;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.GuiScrollElement;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DocMethodVariant extends DocEntry {
    public boolean isDeprecated = false;

    public List<DocVariable> params = new ArrayList<>();
    public List<String> annotations = new ArrayList<>();
    public DocVariable returns = new DocVariable();

    public DocMethodVariant(String name) {
        super(name);
    }

    @Override
    public String getName() {
        String defaultColor = (isDeprecated ? TextFormatting.DARK_GRAY : TextFormatting.RESET).toString();
        StringBuilder str = new StringBuilder(defaultColor);
        str.append(name).append('(');
        for (DocVariable param : params)
            str.append(TextFormatting.GOLD).append(param.getType()).append(defaultColor).append(' ').append(param.getName()).append(", ");
        if (!params.isEmpty()) str.delete(str.length() - 2, str.length());
        str.append(')');
        return str.toString();
    }

    @Override
    public List<DocEntry> getEntries() {
        return null;
    }

    @Override
    public void append(Minecraft mc, GuiScrollElement target) {
        super.append(mc, target);

        if (!params.isEmpty()) {
            GuiText offset = new GuiText(mc);
            offset.marginTop(2);
            target.add(offset);
        }
        for (DocVariable param : params) {
            GuiText text = new GuiText(mc).text(TextFormatting.GOLD + param.getType() + TextFormatting.RESET + " " + param.name);
            target.add(text);

            if (param.doc.isEmpty()) continue;
            param.append(mc, target);
            ((GuiElement) target.getChildren().get(target.getChildren().size() - 1)).marginBottom(5);
        }

        List<String> annotations = this.annotations.stream()
                .map(annotation -> "@" + annotation.substring(annotation.lastIndexOf(".") + 1))
                .filter(annotation -> !annotation.equals("@Override"))
                .collect(Collectors.toList());
        if (!annotations.isEmpty()) {
            String annotationsText = String.join(", ", annotations);
            target.add(new GuiText(mc).text(TextFormatting.GRAY.toString() + TextFormatting.BOLD + annotationsText).marginTop(8));
        }

        target.add(new GuiText(mc).text("Returns " + TextFormatting.GOLD + returns.getType()).marginTop(8));
        if (!returns.doc.isEmpty()) returns.append(mc, target);
    }
}
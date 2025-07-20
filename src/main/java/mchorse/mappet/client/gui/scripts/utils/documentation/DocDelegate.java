package mchorse.mappet.client.gui.scripts.utils.documentation;

import mchorse.mclib.client.gui.framework.elements.GuiScrollElement;
import net.minecraft.client.Minecraft;

import java.util.List;

public class DocDelegate extends DocEntry {
    public DocDelegate() {

    }

    public DocDelegate(String name) {

    }

    public DocDelegate(DocEntry parent) {
        this.parent = parent;
    }

    @Override
    public void addChildren(DocEntry... children) {

    }

    @Override
    public void append(Minecraft mc, GuiScrollElement target) {
    }

    @Override
    public String parseCode(String code) {
        return "";
    }

    @Override
    public List<DocEntry> getEntries() {
        return null;
    }

    @Override
    public DocEntry getEntry() {
        return parent;
    }

    @Override
    public String getName() {
        return "../";
    }

    @Override
    public void render(Minecraft mc, GuiScrollElement target) {

    }
}

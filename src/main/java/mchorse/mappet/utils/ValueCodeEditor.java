package mchorse.mappet.utils;

import mchorse.mappet.client.gui.scripts.GuiCodeEditor;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.config.gui.GuiConfigPanel;
import mchorse.mclib.config.values.IConfigGuiProvider;
import mchorse.mclib.config.values.ValueString;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.List;

public class ValueCodeEditor extends ValueString implements IConfigGuiProvider {
    protected GuiCodeEditor code;
    public ValueCodeEditor(String id) {
        this(id, "function main(event) {\n    var subject = event.getSubject();\n    var states = event.getServer().getStates();\n}");
    }

    public ValueCodeEditor(String id, String defaultValue) {
        super(id, defaultValue);
        clientSide();
    }

    public void updateStyle(){
        code.getHighlighter().setStyle(null);
        code.resetHighlight();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public List<GuiElement> getFields(Minecraft mc, GuiConfigPanel gui) {
        code = new GuiCodeEditor(mc, this::set);
        code.background();
        code.setText(get());
        code.flex().relative(gui).wh(0.45F, 0.25F);
        return Collections.singletonList(code);
    }
}

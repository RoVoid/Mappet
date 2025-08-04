package mchorse.mappet.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import mchorse.mappet.client.gui.scripts.style.SyntaxStyle;
import mchorse.mappet.client.gui.scripts.themes.GuiThemeEditorOverlayPanel;
import mchorse.mappet.client.gui.scripts.themes.Themes;
import mchorse.mappet.client.gui.utils.overlays.GuiOverlay;
import mchorse.mclib.client.gui.framework.GuiBase;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.config.gui.GuiConfigPanel;
import mchorse.mclib.config.values.IConfigGuiProvider;
import mchorse.mclib.config.values.Value;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ValueSyntaxStyle extends Value implements IConfigGuiProvider {
    private SyntaxStyle style = new SyntaxStyle();
    private String fileName = "monokai.json";

    public ValueSyntaxStyle(String id) {
        super(id);
        clientSide();
    }

    public SyntaxStyle get() {
        return style;
    }

    public String getFileName() {
        return fileName;
    }

    public void set(String fileName, SyntaxStyle style) {
        this.fileName = fileName;
        this.style = new SyntaxStyle(style.toNBT());
        saveLater();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public List<GuiElement> getFields(Minecraft mc, GuiConfigPanel config) {
        GuiButtonElement button = new GuiButtonElement(mc, IKey.lang("mappet.gui.syntax_theme.edit"), (t) ->
                GuiOverlay.addOverlay(GuiBase.getCurrent(), new GuiThemeEditorOverlayPanel(mc), 0.6F, 0.95F));
        return Collections.singletonList(button.tooltip(IKey.lang(getCommentKey())));
    }

    @Override
    public void valueFromJSON(JsonElement element) {
        String fileName = element.getAsString();
        SyntaxStyle style = Themes.readTheme(Themes.getThemeFile(fileName));

        if (style != null) {
            this.style = style;
            this.fileName = fileName;
        }
    }

    @Override
    public JsonElement valueToJSON() {
        return new JsonPrimitive(fileName);
    }

    @Override
    public void copy(Value value) {
        if (value instanceof ValueSyntaxStyle) {
            ValueSyntaxStyle config = (ValueSyntaxStyle) value;
            fileName = config.fileName;
            style = new SyntaxStyle(config.style.toNBT());
        }
    }
}
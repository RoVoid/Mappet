package mchorse.mappet.config;

import mchorse.mappet.client.gui.utils.GuiEnumElement;
import mchorse.mappet.utils.PlayerUtils;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiLabel;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.config.gui.GuiConfigPanel;
import mchorse.mclib.config.values.IConfigGuiProvider;
import mchorse.mclib.config.values.IServerValue;
import mchorse.mclib.config.values.ValueInt;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Collections;
import java.util.List;

public class ValueRights extends ValueInt implements IServerValue, IConfigGuiProvider {

    public ValueRights(String id, Right defaultValue) {
        super(id, defaultValue.ordinal());
    }

    public Right right() {
        return Right.values()[get() % Right.values().length];
    }

    @Override
    public List<GuiElement> getFields(Minecraft minecraft, GuiConfigPanel panel) {
        GuiLabel label = Elements.label(IKey.lang(id), 0).anchor(0.0F, 0.5F);

        GuiEnumElement<Right> button = new GuiEnumElement<>(minecraft, right(), b -> set(b.ordinal()));
        button.bakeLabels("mappet.rights");

        GuiElement element = new GuiElement(minecraft);
        element.flex().row(0).preferred(0).height(20);
        element.add(label, button);
        return Collections.singletonList(element);
    }

    public boolean check(EntityPlayer player) {
        switch (right()) {
            case CREATIVE:
                return player.isCreative();
            case OPERATOR:
                return PlayerUtils.isOperator(player);
            case ALL:
                return true;
            default:
                return false;
        }
    }

    public enum Right {
        OPERATOR, CREATIVE, ALL
    }
}
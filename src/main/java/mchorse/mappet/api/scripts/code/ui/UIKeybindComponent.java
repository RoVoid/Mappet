package mchorse.mappet.api.scripts.code.ui;

import mchorse.mappet.api.ui.UIContext;
import mchorse.mappet.api.ui.utils.DiscardMethod;
import mchorse.mappet.client.gui.utils.GuiExtendedKeybindElement;
import mchorse.mclib.McLib;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

public class UIKeybindComponent extends UIComponent {
    public Integer keycode = 0;

    public Integer background = 0;
    public Integer waitBackground = McLib.primaryColor.get();
    public Integer color = 16777215;

    public UIKeybindComponent() {
    }

    public UIKeybindComponent background(int background) {
        change("Background");
        this.background = background;
        return this;
    }

    public UIKeybindComponent waitBackground(int waitBackground) {
        change("WaitBackground");
        this.waitBackground = waitBackground;
        return this;
    }

    public UIKeybindComponent color(int color) {
        change("Color");
        this.color = color;
        return this;
    }

    @Override
    @DiscardMethod
    @SideOnly(Side.CLIENT)
    protected boolean isDataReserved() {
        return true;
    }

    @Override
    @DiscardMethod
    @SideOnly(Side.CLIENT)
    protected void applyProperty(UIContext context, String key, GuiElement element) {
        super.applyProperty(context, key, element);

        GuiExtendedKeybindElement button = (GuiExtendedKeybindElement) element;

        switch (key) {
            case "Background":
                if (background != null && background >= 0) button.background = background;
                else button.background = 0;
                break;
            case "WaitBackground":
                if (waitBackground != null && waitBackground >= 0) button.waitBackground = waitBackground;
                else button.waitBackground = McLib.primaryColor.get();
                break;
            case "Color":
                if (color != null && color >= 0) button.color = color;
                else button.color = 0xFFFFFF;
                break;
        }
    }


    @Override
    @DiscardMethod
    @SideOnly(Side.CLIENT)
    public GuiElement create(Minecraft mc, UIContext context) {
        GuiExtendedKeybindElement keybind = new GuiExtendedKeybindElement(mc, (k) -> {
            if (!id.isEmpty()) {
                context.data.setInteger(id, k == Keyboard.KEY_ESCAPE ? 0 : k);
                context.dirty(id, updateDelay);
            }
        });
        keybind.setKeybind(keycode);

        if (background != null && background >= 0) {
            keybind.background = background;
        }

        if (waitBackground != null && waitBackground >= 0) {
            keybind.waitBackground = waitBackground;
        }

        if (color != null && color >= 0) {
            keybind.color = color;
        }

        return apply(keybind, context);
    }


    @Override
    @DiscardMethod
    public void populateData(NBTTagCompound tag) {
        super.populateData(tag);

        if (!id.isEmpty()) {
            tag.setInteger(id, keycode);
        }
    }

    @Override
    public void serializeNBT(NBTTagCompound tag) {
        super.serializeNBT(tag);

        if (background != null) {
            tag.setInteger("Background", background);
        }
        if (waitBackground != null) {
            tag.setInteger("WaitBackground", waitBackground);
        }
        if (color != null) {
            tag.setInteger("Color", color);
        }
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        super.deserializeNBT(tag);

        if (tag.hasKey("Background")) {
            background = tag.getInteger("Background");
        }
        if (tag.hasKey("WaitBackground")) {
            waitBackground = tag.getInteger("WaitBackground");
        }
        if (tag.hasKey("Color")) {
            color = tag.getInteger("Color");
        }
    }
}

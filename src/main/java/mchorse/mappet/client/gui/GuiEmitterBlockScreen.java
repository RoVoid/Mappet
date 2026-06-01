package mchorse.mappet.client.gui;

import mchorse.mappet.client.gui.conditions.GuiOpenConditionButtonElement;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.packets.blocks.PacketEditEmitter;
import mchorse.mclib.client.gui.framework.GuiBase;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;

import java.util.function.Consumer;

public class GuiEmitterBlockScreen extends GuiBase
{
    public GuiOpenConditionButtonElement conditionButtonElement;
    public GuiTrackpadElement radius;
    public GuiTrackpadElement update;
    public GuiToggleElement disable;

    private final BlockPos pos;

    public GuiEmitterBlockScreen(PacketEditEmitter message)
    {
        super();

        pos = message.pos;

        Minecraft mc = Minecraft.getMinecraft();

        conditionButtonElement = new GuiOpenConditionButtonElement(mc, message.createChecker());

        radius = new GuiTrackpadElement(mc, (Consumer<Double>) null);
        radius.limit(0).setValue(message.radius);

        update = new GuiTrackpadElement(mc, (Consumer<Double>) null);
        update.limit(1).integer().setValue(message.update);

        disable = new GuiToggleElement(mc, IKey.lang("mappet.gui.emitter_block.disable"), null);
        disable.toggled(message.resets);
        disable.tooltip(IKey.lang("mappet.gui.emitter_block.disable_tootlip"));

        GuiElement frame = Elements.column(mc, 5,
                Elements.label(IKey.lang("mappet.gui.emitter_block.condition")),
                conditionButtonElement,
                Elements.row(mc, 5,
                        Elements.column(mc, 5, Elements.label(IKey.lang("mappet.gui.emitter_block.radius")), radius),
                        Elements.column(mc, 5, Elements.label(IKey.lang("mappet.gui.emitter_block.update")), update)
                ).marginTop(12),
                disable
        );

        frame.flex().relative(viewport).xy(0.5F, 0.5F).w(0.5F).anchor(0.5F, 0.5F);

        root.add(frame);
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    protected void closeScreen()
    {
        super.closeScreen();
        Dispatcher.sendToServer(new PacketEditEmitter(pos, conditionButtonElement.toNBT(), (float) radius.value, (int) update.value, disable.isToggled()));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
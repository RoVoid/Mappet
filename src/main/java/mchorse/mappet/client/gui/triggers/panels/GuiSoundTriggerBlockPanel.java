package mchorse.mappet.client.gui.triggers.panels;

import mchorse.mappet.api.triggers.blocks.SoundTriggerBlock;
import mchorse.mappet.api.utils.TargetMode;
import mchorse.mappet.api.utils.content.IContentTypeBase;
import mchorse.mappet.client.gui.triggers.GuiTriggerOverlayPanel;
import mchorse.mappet.client.gui.utils.GuiEnumElement;
import mchorse.mappet.client.gui.utils.GuiMappetUtils;
import mchorse.mappet.client.gui.utils.overlays.GuiOverlay;
import mchorse.mappet.client.gui.utils.overlays.GuiResourceLocationOverlayPanel;
import mchorse.mappet.client.gui.utils.overlays.GuiSoundOverlayPanel;
import mchorse.mclib.client.gui.framework.GuiBase;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class GuiSoundTriggerBlockPanel extends GuiStringTriggerBlockPanel<SoundTriggerBlock> {
    public GuiEnumElement<TargetMode> target;

    public GuiSoundTriggerBlockPanel(Minecraft mc, GuiTriggerOverlayPanel overlay, SoundTriggerBlock block) {
        super(mc, overlay, block);

        target = GuiMappetUtils.createTargetCirculate(mc, block.target, (target) -> this.block.target = target);

        for (TargetMode mode : TargetMode.values())
            if (!(mode == TargetMode.PLAYER || mode == TargetMode.GLOBAL)) target.disable(mode.ordinal());

        addPicker();
        add(Elements.label(IKey.lang("mappet.gui.conditions.target")).marginTop(12), target);
        addDelay();
    }

    @Override
    protected IKey getLabel() {
        return IKey.lang("mappet.gui.overlays.sounds.main");
    }

    @Override
    protected IContentTypeBase getType() {
        return null;
    }

    @Override
    protected void openOverlay() {
        GuiResourceLocationOverlayPanel overlay = new GuiSoundOverlayPanel(mc, this::setSound).set(block.string);
        GuiOverlay.addOverlay(GuiBase.getCurrent(), overlay, 0.5F, 0.9F);
    }

    private void setSound(ResourceLocation location) {
        block.string = location == null ? "" : location.toString();
    }
}
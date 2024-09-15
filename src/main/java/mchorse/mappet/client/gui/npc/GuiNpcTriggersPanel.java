package mchorse.mappet.client.gui.npc;

import mchorse.mappet.api.npcs.NpcState;
import mchorse.mappet.client.gui.triggers.GuiTriggerElement;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

public class GuiNpcTriggersPanel extends GuiNpcPanel {
    public GuiTriggerElement triggerDied;
    public GuiTriggerElement triggerDamaged;
    public GuiTriggerElement triggerInteract;
    public GuiTriggerElement triggerTick;
    public GuiTriggerElement triggerTarget;
    public GuiTriggerElement triggerInitialize;
    public GuiTriggerElement triggerEntityCollision;

    public GuiNpcTriggersPanel(Minecraft mc) {
        super(mc, IKey.lang("mappet.gui.npcs.triggers.title"));

        this.triggerDied = new GuiTriggerElement(mc);
        this.triggerDamaged = new GuiTriggerElement(mc);
        this.triggerInteract = new GuiTriggerElement(mc);
        this.triggerTick = new GuiTriggerElement(mc);
        this.triggerTarget = new GuiTriggerElement(mc);
        this.triggerEntityCollision = new GuiTriggerElement(mc);
        this.triggerInitialize = new GuiTriggerElement(mc);

        this.add(Elements.label(IKey.lang("mappet.gui.npcs.behavior.initialize")).background().marginTop(12).marginBottom(5), this.triggerInitialize);
        this.add(Elements.label(IKey.lang("mappet.gui.npcs.behavior.interact")).background().marginTop(12).marginBottom(5), this.triggerInteract);
        this.add(Elements.label(IKey.lang("mappet.gui.npcs.behavior.damaged")).background().marginTop(12).marginBottom(5), this.triggerDamaged);
        this.add(Elements.label(IKey.lang("mappet.gui.npcs.behavior.died")).background().marginTop(12).marginBottom(5), this.triggerDied);
        this.add(Elements.label(IKey.lang("mappet.gui.npcs.behavior.tick")).background().marginTop(12).marginBottom(5), this.triggerTick);
        this.add(Elements.label(IKey.lang("mappet.gui.npcs.behavior.target")).background().marginTop(12).marginBottom(5), this.triggerTarget);
        this.add(Elements.label(IKey.lang("mappet.gui.npcs.behavior.collision")).background().marginTop(12).marginBottom(5), this.triggerEntityCollision);
    }

    @Override
    public void set(NpcState state) {
        super.set(state);

        this.triggerDied.set(state.triggerDied);
        this.triggerDamaged.set(state.triggerDamaged);
        this.triggerInteract.set(state.triggerInteract);
        this.triggerTick.set(state.triggerTick);
        this.triggerTarget.set(state.triggerTarget);
        this.triggerEntityCollision.set(state.triggerEntityCollision);
        this.triggerInitialize.set(state.triggerInitialize);
    }
}
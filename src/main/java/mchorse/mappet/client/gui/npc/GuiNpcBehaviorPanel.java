package mchorse.mappet.client.gui.npc;

import mchorse.mappet.api.npcs.NpcState;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

public class GuiNpcBehaviorPanel extends GuiNpcPanel {
    public GuiToggleElement lookAtPlayer;
    public GuiToggleElement lookAround;
    public GuiToggleElement wander;
    public GuiToggleElement alwaysWander;
    public GuiToggleElement canFly;
    public GuiToggleElement canPickUpLoot;
    public GuiToggleElement canBeSteered;

    public GuiNpcBehaviorPanel(Minecraft mc) {
        super(mc, IKey.lang("mappet.gui.npcs.behavior.title"));

        this.lookAtPlayer = new GuiToggleElement(mc, IKey.lang("mappet.gui.npcs.behavior.look_at_player"), (b) -> this.state.lookAtPlayer.set(b.isToggled()));
        this.lookAround = new GuiToggleElement(mc, IKey.lang("mappet.gui.npcs.behavior.look_around"), (b) -> this.state.lookAround.set(b.isToggled()));
        this.wander = new GuiToggleElement(mc, IKey.lang("mappet.gui.npcs.behavior.wander"), (b) -> this.state.wander.set(b.isToggled()));
        this.alwaysWander = new GuiToggleElement(mc, IKey.lang("mappet.gui.npcs.behavior.always_wander"), (b) -> this.state.alwaysWander.set(b.isToggled()));
        this.canFly = new GuiToggleElement(mc, IKey.lang("mappet.gui.npcs.behavior.can_fly"), (b) -> this.state.canFly.set(b.isToggled()));
        this.canPickUpLoot = new GuiToggleElement(mc, IKey.lang("mappet.gui.npcs.behavior.can_pick_up_loot"), (b) -> this.state.canPickUpLoot.set(b.isToggled()));
        this.canBeSteered = new GuiToggleElement(mc, IKey.lang("mappet.gui.npcs.behavior.can_be_steered"), (b) -> this.state.canBeSteered.set(b.isToggled()));

        this.add(this.lookAtPlayer, this.lookAround, this.wander, this.alwaysWander, this.canFly, this.canPickUpLoot, this.canBeSteered);
    }

    @Override
    public void set(NpcState state) {
        super.set(state);

        this.lookAtPlayer.toggled(state.lookAtPlayer.get());
        this.lookAround.toggled(state.lookAround.get());
        this.wander.toggled(state.wander.get());
        this.alwaysWander.toggled(state.alwaysWander.get());
        this.canFly.toggled(state.canFly.get());
        this.canPickUpLoot.toggled(state.canPickUpLoot.get());
        this.canBeSteered.toggled(state.canBeSteered.get());
    }
}
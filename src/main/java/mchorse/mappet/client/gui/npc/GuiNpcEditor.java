package mchorse.mappet.client.gui.npc;

import mchorse.mappet.api.npcs.NpcState;
import mchorse.mclib.client.gui.framework.elements.GuiScrollElement;
import mchorse.mclib.client.gui.utils.ScrollDirection;
import net.minecraft.client.Minecraft;

public class GuiNpcEditor extends GuiScrollElement {
    private NpcState state;

    private final GuiNpcMetaPanel meta;
    private final GuiNpcGeneralPanel general;
    private final GuiNpcHealthPanel health;
    private final GuiNpcDamagePanel damage;
    private final GuiNpcMovementPanel movement;
    private final GuiNpcBehaviorPanel behavior;
    private final GuiNpcTriggersPanel triggers;
    private final GuiNpcRespawnPanel respawn;

    public GuiNpcEditor(Minecraft mc, boolean id) {
        super(mc, ScrollDirection.HORIZONTAL);

        this.scroll.scrollSpeed *= 2;

        this.meta = new GuiNpcMetaPanel(mc, id);
        this.general = new GuiNpcGeneralPanel(mc);
        this.health = new GuiNpcHealthPanel(mc);
        this.damage = new GuiNpcDamagePanel(mc);
        this.movement = new GuiNpcMovementPanel(mc);
        this.behavior = new GuiNpcBehaviorPanel(mc);
        this.triggers = new GuiNpcTriggersPanel(mc);
        this.respawn = new GuiNpcRespawnPanel(mc);

        this.flex().column(5).scroll().width(180).padding(15);

        add(meta);
        add(general);
        add(health);
        add(damage);
        add(movement);
        add(behavior);
        add(triggers);
        add(respawn);
    }

    public void set(NpcState state) {
        this.state = state;

        this.meta.set(state);
        this.general.set(state);
        this.health.set(state);
        this.damage.set(state);
        this.movement.set(state);
        this.behavior.set(state);
        this.triggers.set(state);
        this.respawn.set(state);

        this.resize();
    }

    public NpcState get() {
        return this.state;
    }
}
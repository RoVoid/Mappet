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

        float width = 0.2f;
        this.meta.flex().relative(this).w(width).h(1F);
        this.general.flex().relative(this).w(width).h(0.625F);
        this.health.flex().relative(this).w(width).h(1F);
        this.damage.flex().relative(this).w(width).h(0.525F);
        this.movement.flex().relative(this).w(width).h(1F);
        this.behavior.flex().relative(this).w(width).h(1F);
        this.triggers.flex().relative(this).w(width).h(1F);
        this.respawn.flex().relative(this).w(width).h(0.6F);

        this.flex().column(5).scroll().width(180).padding(15);

        meta.add(general);
        health.add(damage);
        behavior.add(respawn);
        add(meta, health, movement, behavior, triggers);
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
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

        scroll.scrollSpeed *= 2;

        meta = new GuiNpcMetaPanel(mc, id);
        general = new GuiNpcGeneralPanel(mc);
        health = new GuiNpcHealthPanel(mc);
        damage = new GuiNpcDamagePanel(mc);
        movement = new GuiNpcMovementPanel(mc);
        behavior = new GuiNpcBehaviorPanel(mc);
        triggers = new GuiNpcTriggersPanel(mc);
        respawn = new GuiNpcRespawnPanel(mc);

        float width = 0.2f;
        meta.flex().relative(this).w(width).h(1F);
        general.flex().relative(this).w(width).h(0.625F);
        health.flex().relative(this).w(width).h(1F);
        damage.flex().relative(this).w(width).h(0.525F);
        movement.flex().relative(this).w(width).h(1F);
        behavior.flex().relative(this).w(width).h(1F);
        triggers.flex().relative(this).w(width).h(1F);
        respawn.flex().relative(this).w(width).h(0.6F);

        flex().column(5).scroll().width(180).padding(15);

        meta.add(general);
        health.add(damage);
        behavior.add(respawn);
        add(meta, health, movement, behavior, triggers);
    }

    public void set(NpcState state) {
        this.state = state;

        meta.set(state);
        general.set(state);
        health.set(state);
        damage.set(state);
        movement.set(state);
        behavior.set(state);
        triggers.set(state);
        respawn.set(state);

        resize();
    }

    public NpcState get() {
        return state;
    }
}
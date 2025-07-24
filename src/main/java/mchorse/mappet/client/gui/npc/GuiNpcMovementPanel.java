package mchorse.mappet.client.gui.npc;

import mchorse.mappet.api.npcs.NpcState;
import mchorse.mappet.client.gui.utils.GuiBlockPosAndTriggerList;
import mchorse.mappet.client.gui.utils.GuiBlockPosElement;
import mchorse.mappet.client.gui.utils.GuiBlockPosList;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiLabel;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

public class GuiNpcMovementPanel extends GuiNpcPanel
{
    public GuiTrackpadElement speed;
    public GuiTrackpadElement flightMaxHeight;
    public GuiTrackpadElement flightMinHeight;
    public GuiTrackpadElement jumpPower;
    public GuiBlockPosList steeringOffset;
    public GuiToggleElement canSwim;
    public GuiToggleElement immovable;
    public GuiToggleElement collision;
    public GuiToggleElement hasPost;
    public GuiBlockPosElement postPosition;
    public GuiTrackpadElement postRadius;
    public GuiToggleElement patrolCirculate;
    public GuiBlockPosAndTriggerList patrol;
    public GuiTextElement follow;

    public GuiNpcMovementPanel(Minecraft mc)
    {
        super(mc, IKey.lang("mappet.gui.npcs.movement.title"));

        speed = new GuiTrackpadElement(mc, (v) -> state.speed.set(v.floatValue()));
        flightMaxHeight = new GuiTrackpadElement(mc, (v) -> state.flightMaxHeight.set(v));
        flightMinHeight = new GuiTrackpadElement(mc, (v) -> state.flightMinHeight.set(v));
        jumpPower = new GuiTrackpadElement(mc, (v) -> state.jumpPower.set(v.floatValue()));
        steeringOffset = new GuiBlockPosList(mc);

        GuiLabel steeringOffsetLabel = Elements.label(IKey.lang("mappet.gui.npcs.movement.steering_offset")).background();
        GuiIconElement addSteeringOffset = new GuiIconElement(mc, Icons.ADD, (b) -> steeringOffset.addBlockPos());

        addSteeringOffset.flex().relative(steeringOffsetLabel).xy(1F, 0.5F).w(10).anchor(1F, 0.5F);
        steeringOffsetLabel.add(addSteeringOffset);

        canSwim = new GuiToggleElement(mc, IKey.lang("mappet.gui.npcs.movement.can_swim"), (b) -> state.canSwim.set(b.isToggled()));
        immovable = new GuiToggleElement(mc, IKey.lang("mappet.gui.npcs.movement.immovable"), (b) -> state.immovable.set(b.isToggled()));
        collision = new GuiToggleElement(mc, IKey.lang("mappet.gui.npcs.movement.collision"), (b) -> state.collision.set(b.isToggled()));
        hasPost = new GuiToggleElement(mc, IKey.lang("mappet.gui.npcs.movement.post"), (b) -> state.hasPost.set(b.isToggled()));
        postPosition = new GuiBlockPosElement(mc, (pos) -> state.postPosition = pos);
        postRadius = new GuiTrackpadElement(mc, (v) -> state.postRadius.set(v.floatValue()));
        patrolCirculate = new GuiToggleElement(mc, IKey.lang("mappet.gui.npcs.movement.patrol_circulate"), (b) -> state.patrolCirculate.set(b.isToggled()));
        patrol = new GuiBlockPosAndTriggerList(mc);
        follow = new GuiTextElement(mc, 1000, (t) -> state.follow.set(t));

        GuiLabel patrolLabel = Elements.label(IKey.lang("mappet.gui.npcs.movement.patrol_points")).background();
        GuiIconElement addPatrol = new GuiIconElement(mc, Icons.ADD, (b) -> patrol.addBlockPos());
        addPatrol.flex().relative(patrolLabel).xy(1F, 0.5F).w(10).anchor(1F, 0.5F);
        patrolLabel.add(addPatrol);

        add(Elements.label(IKey.lang("mappet.gui.npcs.movement.speed")), speed);
        add(Elements.label(IKey.lang("mappet.gui.npcs.movement.flight_max_height")), flightMaxHeight);
        add(Elements.label(IKey.lang("mappet.gui.npcs.movement.flight_min_height")), flightMinHeight);
        add(Elements.label(IKey.lang("mappet.gui.npcs.movement.jump_power")), jumpPower);
        add(canSwim, immovable, collision);
        add(hasPost.marginTop(12), postPosition, postRadius);
        add(Elements.label(IKey.lang("mappet.gui.npcs.movement.follow")).marginTop(12), follow);

        GuiElement patrolArea = new GuiElement(mc);
        patrolArea.add(patrolLabel, patrol, patrolCirculate);
        patrolArea.flex().column(5).vertical().stretch();
        add(patrolArea.marginTop(12));

        GuiElement steeringOffsetArea = new GuiElement(mc);
        steeringOffsetArea.add(steeringOffsetLabel, steeringOffset);
        steeringOffsetArea.flex().column(5).vertical().stretch();
        add(steeringOffsetArea.marginTop(12));
    }

    @Override
    public void set(NpcState state)
    {
        super.set(state);

        speed.setValue(state.speed.get());
        flightMaxHeight.setValue(state.flightMaxHeight.get());
        flightMinHeight.setValue(state.flightMinHeight.get());
        jumpPower.setValue(state.jumpPower.get());
        steeringOffset.set(state.steeringOffset);
        canSwim.toggled(state.canSwim.get());
        immovable.toggled(state.immovable.get());
        collision.toggled(state.collision.get());
        hasPost.toggled(state.hasPost.get());
        postPosition.set(state.postPosition);
        postRadius.setValue(state.postRadius.get());
        patrolCirculate.toggled(state.patrolCirculate.get());
        patrol.set(state.patrol, state.patrolTriggers);
        follow.setText(state.follow.get());
    }
}
package mchorse.mappet.api.scripts.code.entities;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.npcs.Npc;
import mchorse.mappet.api.npcs.NpcState;
import mchorse.mappet.api.scripts.code.data.ScriptVector;
import mchorse.mappet.api.scripts.user.entities.IScriptNpc;
import mchorse.mappet.api.triggers.Trigger;
import mchorse.mappet.entities.EntityNpc;
import mchorse.metamorph.api.MorphUtils;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class ScriptNpc extends ScriptEntity<EntityNpc> implements IScriptNpc {
    public ScriptNpc(EntityNpc entity) {
        super(entity);
    }

    @Override
    public EntityNpc asMinecraft() {
        return entity;
    }

    @Override
    public EntityNpc getMappetNpc() {
        return asMinecraft();
    }

    @Override
    public String getId() {
        return entity.getId();
    }

    @Override
    public String getNpcId() {
        return getId();
    }

    @Override
    public boolean setMorph(AbstractMorph morph) {
        entity.getState().morph = MorphUtils.copy(morph);
        entity.setMorph(entity.getState().morph);
        entity.sendNpcStateChangePacket();
        return true;
    }

    @Override
    public String getNpcState() {
        return entity.getState().stateName.get();
    }

    @Override
    public void setNpcState(String stateId) {
        String npcId = entity.getId();
        Npc npc = Mappet.npcs.load(npcId);
        NpcState state = npc == null ? null : npc.states.get(stateId);

        if (npc != null && state == null && npc.states.containsKey("default")) {
            state = npc.states.get("default");
        }

        if (state != null) {
            entity.setNpc(npc, state);
            if (!npc.serializeNBT().getString("StateName").equals("default")) {
                entity.setStringInData("StateName", stateId);
            }
        }

        entity.sendNpcStateChangePacket();
    }

    @Override
    public void canPickUpLoot(boolean canPickUpLoot) {
        entity.setCanPickUpLoot(canPickUpLoot);
    }

    @Override
    public void follow(String target) {
        NpcState state = entity.getState();
        state.follow.set(target);
        entity.setState(state, false);
    }

    @Override
    public String getFaction() {
        return entity.getState().faction.get();
    }

    @Override
    public void setCanBeSteered(boolean enabled) {
        NpcState state = entity.getState();
        state.canBeSteered.set(enabled);
        entity.sendNpcStateChangePacket();
    }

    @Override
    public boolean canBeSteered() {
        return entity.getState().canBeSteered.get();
    }

    @Override
    public void setSteeringOffset(int index, float x, float y, float z) {
        NpcState state = entity.getState();
        if (index >= 0 && index < state.steeringOffset.size()) {
            state.steeringOffset.set(index, new BlockPos(x, y, z));
        } else Mappet.logger.error("Invalid index: " + index);
        entity.sendNpcStateChangePacket();
    }

    @Override
    public void addSteeringOffset(float x, float y, float z) {
        NpcState state = entity.getState();
        state.steeringOffset.add(new BlockPos(x, y, z));
        entity.sendNpcStateChangePacket();
    }
    
    @Override
    public List<ScriptVector> getSteeringOffsets() {
        NpcState state = entity.getState();
        List<ScriptVector> steeringOffsets = new ArrayList<>();
        for (BlockPos pos : state.steeringOffset) {
            steeringOffsets.add(new ScriptVector(pos.getX(), pos.getY(), pos.getZ()));
        }
        return steeringOffsets;
    }

    @Override
    public void setNpcSpeed(float speed) {
        NpcState state = entity.getState();
        state.speed.set(speed);
        entity.sendNpcStateChangePacket();
    }

    @Override
    public float getNpcSpeed() {
        return entity.getState().speed.get();
    }

    @Override
    public void setJumpPower(float jumpHeight) {
        NpcState state = entity.getState();
        state.jumpPower.set(jumpHeight);
        entity.sendNpcStateChangePacket();
    }

    @Override
    public float getJumpPower() {
        return entity.getState().jumpPower.get();
    }

    @Override
    public void setInvincible(boolean invincible) {
        NpcState state = entity.getState();
        state.invincible.set(invincible);
        entity.sendNpcStateChangePacket();
    }

    @Override
    public boolean isInvincible() {
        return entity.getState().invincible.get();
    }

    @Override
    public void setCanSwim(boolean canSwim) {
        NpcState state = entity.getState();
        state.canSwim.set(canSwim);
        entity.sendNpcStateChangePacket();
    }

    @Override
    public boolean canSwim() {
        return entity.getState().canSwim.get();
    }

    @Override
    public void setImmovable(boolean immovable) {
        NpcState state = entity.getState();
        state.immovable.set(immovable);
        entity.sendNpcStateChangePacket();
    }

    @Override
    public boolean isImmovable() {
        return entity.getState().immovable.get();
    }

    @Override
    public void setShadowSize(float size) {
        NpcState state = entity.getState();
        state.shadowSize.set(size);
        entity.sendNpcStateChangePacket();
    }

    @Override
    public float getShadowSize() {
        return entity.getState().shadowSize.get();
    }

    @Override
    public float setXpValue(int xp) {
        NpcState state = entity.getState();
        state.xp.set(xp);
        entity.sendNpcStateChangePacket();
        return xp;
    }

    @Override
    public int getXpValue() {
        return entity.getState().xp.get();
    }

    @Override
    public float getPathDistance() {
        NpcState state = entity.getState();
        return state.pathDistance.get();
    }

    @Override
    public void setPathDistance(float sightRadius) {
        NpcState state = entity.getState();
        state.pathDistance.set(sightRadius);
        entity.sendNpcStateChangePacket();
    }

    @Override
    public void setAttackRange(float sightDistance) {
        NpcState state = entity.getState();
        state.sightDistance.set(sightDistance);
        entity.sendNpcStateChangePacket();
    }

    @Override
    public float getAttackRange() {
        return entity.getState().sightDistance.get();
    }

    @Override
    public void setKillable(boolean killable) {
        NpcState state = entity.getState();
        state.killable.set(killable);
        entity.sendNpcStateChangePacket();
    }

    @Override
    public boolean isKillable() {
        return entity.getState().killable.get();
    }

    @Override
    public boolean canGetBurned() {
        return entity.getState().canGetBurned.get();
    }

    @Override
    public void canGetBurned(boolean canGetBurned) {
        NpcState state = entity.getState();
        state.canGetBurned.set(canGetBurned);
        entity.sendNpcStateChangePacket();
    }

    @Override
    public boolean canFallDamage() {
        return entity.getState().canFallDamage.get();
    }

    @Override
    public void canFallDamage(boolean canFallDamage) {
        NpcState state = entity.getState();
        state.canFallDamage.set(canFallDamage);
        entity.sendNpcStateChangePacket();
    }

    @Override
    public float getDamage() {
        return entity.getState().damage.get();
    }

    @Override
    public void setDamage(float damage) {
        NpcState state = entity.getState();
        state.damage.set(damage);
        entity.sendNpcStateChangePacket();
    }

    @Override
    public int getDamageDelay() {
        return entity.getState().damageDelay.get();
    }

    @Override
    public void setDamageDelay(int damageDelay) {
        NpcState state = entity.getState();
        state.damageDelay.set(damageDelay);
        entity.sendNpcStateChangePacket();
    }

    @Override
    public boolean doesWander() {
        return entity.getState().wander.get();
    }

    @Override
    public void setWander(boolean wander) {
        NpcState state = entity.getState();
        state.wander.set(wander);
        entity.sendNpcStateChangePacket();
    }

    @Override
    public boolean doesLookAround() {
        return entity.getState().lookAround.get();
    }

    @Override
    public void setLookAround(boolean lookAround) {
        NpcState state = entity.getState();
        state.lookAround.set(lookAround);
        entity.sendNpcStateChangePacket();
    }

    @Override
    public boolean doesLookAtPlayer() {
        return entity.getState().lookAtPlayer.get();
    }

    @Override
    public void setLookAtPlayer(boolean lookAtPlayer) {
        NpcState state = entity.getState();
        state.lookAtPlayer.set(lookAtPlayer);
        entity.sendNpcStateChangePacket();
    }

    /* Triggers */

    @Override
    public void clearPatrolPoints() {
        NpcState state = entity.getState();
        state.patrol.clear();
        entity.setState(state, false);
    }

    public void addPatrolPoints(float x, float y, float z) {
        NpcState npcState = entity.getState();
        npcState.patrol.add(new BlockPos(x, y, z));
        npcState.patrolTriggers.add(new Trigger());
        entity.setState(npcState, true);
    }

    @Override
    public void removePatrolPoint(int index) {
        NpcState state = entity.getState();

        if (index < state.patrol.size()) {
            state.patrol.remove(index);
            state.patrolTriggers.remove(index);
        }

        entity.setState(state, false);
    }

    @Override
    public void removePatrolPoint(int x, int y, int z) {
        NpcState state = entity.getState();

        state.patrol.stream().filter(p -> p.getX() == x && p.getY() == y && p.getZ() == z).forEach(p -> {
            int index = state.patrol.indexOf(p);

            state.patrol.remove(index);
            state.patrolTriggers.remove(index);
        });

        entity.setState(state, false);
    }
}
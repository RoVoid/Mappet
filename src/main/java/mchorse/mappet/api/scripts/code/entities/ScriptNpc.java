package mchorse.mappet.api.scripts.code.entities;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.npcs.Npc;
import mchorse.mappet.api.npcs.NpcState;
import mchorse.mappet.api.scripts.code.math.ScriptVector;
import mchorse.mappet.api.triggers.Trigger;
import mchorse.mappet.api.triggers.blocks.ScriptTriggerBlock;
import mchorse.mappet.entities.EntityNpc;
import mchorse.metamorph.api.MorphUtils;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScriptNpc extends ScriptEntity<EntityNpc> {
    public ScriptNpc(EntityNpc entity) {
        super(entity);
    }

    public EntityNpc asMinecraft() {
        return entity;
    }

    public EntityNpc getMappetNpc() {
        return asMinecraft();
    }

    public String getId() {
        return entity.getId();
    }

    public String getNpcId() {
        return getId();
    }

    public boolean setMorph(AbstractMorph morph) {
        entity.getState().morph = MorphUtils.copy(morph);
        entity.setMorph(entity.getState().morph);
        entity.sendNpcStateChangePacket();
        return true;
    }

    public String getNpcState() {
        return entity.getState().stateName.get();
    }

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

    public void canPickUpLoot(boolean canPickUpLoot) {
        entity.setCanPickUpLoot(canPickUpLoot);
    }

    public void follow(String target) {
        NpcState state = entity.getState();
        state.follow.set(target);
        entity.setState(state, false);
    }

    public String getFaction() {
        return entity.getState().faction.get();
    }

    public void setCanBeSteered(boolean enabled) {
        NpcState state = entity.getState();
        state.canBeSteered.set(enabled);
        entity.sendNpcStateChangePacket();
    }

    public boolean canBeSteered() {
        return entity.getState().canBeSteered.get();
    }

    public void setSteeringOffset(int index, float x, float y, float z) {
        NpcState state = entity.getState();
        if (index >= 0 && index < state.steeringOffset.size()) {
            state.steeringOffset.set(index, new BlockPos(x, y, z));
        }
        else Mappet.logger.error("Invalid index: " + index);
        entity.sendNpcStateChangePacket();
    }

    public void addSteeringOffset(float x, float y, float z) {
        NpcState state = entity.getState();
        state.steeringOffset.add(new BlockPos(x, y, z));
        entity.sendNpcStateChangePacket();
    }

    public List<ScriptVector> getSteeringOffsets() {
        NpcState state = entity.getState();
        List<ScriptVector> steeringOffsets = new ArrayList<>();
        for (BlockPos pos : state.steeringOffset) {
            steeringOffsets.add(new ScriptVector(pos.getX(), pos.getY(), pos.getZ()));
        }
        return steeringOffsets;
    }

    public void setNpcSpeed(float speed) {
        NpcState state = entity.getState();
        state.speed.set(speed);
        entity.sendNpcStateChangePacket();
    }

    public float getNpcSpeed() {
        return entity.getState().speed.get();
    }

    public void setJumpPower(float jumpHeight) {
        NpcState state = entity.getState();
        state.jumpPower.set(jumpHeight);
        entity.sendNpcStateChangePacket();
    }

    public float getJumpPower() {
        return entity.getState().jumpPower.get();
    }

    public void setInvincible(boolean invincible) {
        NpcState state = entity.getState();
        state.invincible.set(invincible);
        entity.sendNpcStateChangePacket();
    }

    public boolean isInvincible() {
        return entity.getState().invincible.get();
    }

    public void setCanSwim(boolean canSwim) {
        NpcState state = entity.getState();
        state.canSwim.set(canSwim);
        entity.sendNpcStateChangePacket();
    }

    public boolean canSwim() {
        return entity.getState().canSwim.get();
    }

    public void setImmovable(boolean immovable) {
        NpcState state = entity.getState();
        state.immovable.set(immovable);
        entity.sendNpcStateChangePacket();
    }

    public boolean isImmovable() {
        return entity.getState().immovable.get();
    }

    public void setCollision(boolean enabled) {
        NpcState state = entity.getState();
        state.collision.set(enabled);
        entity.sendNpcStateChangePacket();
    }

    public boolean hasCollision() {
        return entity.getState().collision.get();
    }

    public void setShadowSize(float size) {
        NpcState state = entity.getState();
        state.shadowSize.set(size);
        entity.sendNpcStateChangePacket();
    }

    public float getShadowSize() {
        return entity.getState().shadowSize.get();
    }

    public float setXpValue(int xp) {
        NpcState state = entity.getState();
        state.xp.set(xp);
        entity.sendNpcStateChangePacket();
        return xp;
    }

    public int getXpValue() {
        return entity.getState().xp.get();
    }

    public float getPathDistance() {
        NpcState state = entity.getState();
        return state.pathDistance.get();
    }

    public void setPathDistance(float sightRadius) {
        NpcState state = entity.getState();
        state.pathDistance.set(sightRadius);
        entity.sendNpcStateChangePacket();
    }

    public void setAttackRange(float sightDistance) {
        NpcState state = entity.getState();
        state.sightDistance.set(sightDistance);
        entity.sendNpcStateChangePacket();
    }

    public float getAttackRange() {
        return entity.getState().sightDistance.get();
    }

    public void setKillable(boolean killable) {
        NpcState state = entity.getState();
        state.killable.set(killable);
        entity.sendNpcStateChangePacket();
    }

    public boolean isKillable() {
        return entity.getState().killable.get();
    }

    public boolean canGetBurned() {
        return entity.getState().canGetBurned.get();
    }

    public void canGetBurned(boolean canGetBurned) {
        NpcState state = entity.getState();
        state.canGetBurned.set(canGetBurned);
        entity.sendNpcStateChangePacket();
    }

    public boolean canFallDamage() {
        return entity.getState().canFallDamage.get();
    }

    public void canFallDamage(boolean canFallDamage) {
        NpcState state = entity.getState();
        state.canFallDamage.set(canFallDamage);
        entity.sendNpcStateChangePacket();
    }

    public float getDamage() {
        return entity.getState().damage.get();
    }

    public void setDamage(float damage) {
        NpcState state = entity.getState();
        state.damage.set(damage);
        entity.sendNpcStateChangePacket();
    }

    public int getDamageDelay() {
        return entity.getState().damageDelay.get();
    }

    public void setDamageDelay(int damageDelay) {
        NpcState state = entity.getState();
        state.damageDelay.set(damageDelay);
        entity.sendNpcStateChangePacket();
    }

    public boolean doesWander() {
        return entity.getState().wander.get();
    }

    public void setWander(boolean wander) {
        NpcState state = entity.getState();
        state.wander.set(wander);
        entity.sendNpcStateChangePacket();
    }

    public boolean doesLookAround() {
        return entity.getState().lookAround.get();
    }

    public void setLookAround(boolean lookAround) {
        NpcState state = entity.getState();
        state.lookAround.set(lookAround);
        entity.sendNpcStateChangePacket();
    }

    public boolean doesLookAtPlayer() {
        return entity.getState().lookAtPlayer.get();
    }

    public void setLookAtPlayer(boolean lookAtPlayer) {
        NpcState state = entity.getState();
        state.lookAtPlayer.set(lookAtPlayer);
        entity.sendNpcStateChangePacket();
    }

    /* Triggers */

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

    public void addPatrolPoints(float x, float y, float z, String script) {
        addPatrolPoints(x, y, z, script, "main");
    }

    public void addPatrolPoints(float x, float y, float z, String script, String function) {
        NpcState npcState = entity.getState();
        npcState.patrol.add(new BlockPos(x, y, z));
        npcState.patrolTriggers.add(new Trigger(Collections.singletonList(new ScriptTriggerBlock(script.trim(), function.trim()))));
        entity.setState(npcState, true);
    }

    public void removePatrolPoint(int index) {
        NpcState state = entity.getState();

        if (index < state.patrol.size()) {
            state.patrol.remove(index);
            state.patrolTriggers.remove(index);
        }

        entity.setState(state, false);
    }

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
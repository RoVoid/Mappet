package mchorse.mappet.api.scripts.code.entities.ai.rotations;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIRotations extends EntityAIBase {
    private final EntityLiving entity;
    private final float yaw;
    private final float pitch;
    private final float yawHead;
    private final float chance;

    public EntityAIRotations(EntityLiving entity, float yaw, float pitch, float yawHead, float chance) {
        this.entity = entity;
        this.yaw = yaw;
        this.pitch = pitch;
        this.chance = chance;
        this.yawHead = yawHead;
        setMutexBits(2);
    }

    @Override
    public boolean shouldExecute() {
        return entity.getRNG().nextFloat() < chance;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return false;
    }

    @Override
    public void startExecuting() {
        entity.rotationYaw = yaw;
        entity.rotationPitch = pitch;
        entity.rotationYawHead = yawHead;
    }
}
package mchorse.mappet.api.scripts.code.entities.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAILookAtTarget extends EntityAIBase {
    private final EntityLiving entity;

    private final Entity target;

    private final float chance;

    private int lookTime;

    public EntityAILookAtTarget(EntityLiving entity, Entity target, float chance) {
        this.entity = entity;
        this.target = target;
        this.chance = chance;

        setMutexBits(2);
    }

    @Override
    public boolean shouldExecute() {
        return !(entity.getRNG().nextFloat() >= chance);
    }

    @Override
    public boolean shouldContinueExecuting() {
        return target.isEntityAlive() && lookTime > 0;
    }

    @Override
    public void startExecuting() {
        lookTime = 40 + entity.getRNG().nextInt(40);
    }

    @Override
    public void resetTask() {
        lookTime = 0;
    }

    @Override
    public void updateTask() {
        entity.getLookHelper().setLookPosition(target.posX, target.posY + (double) target.getEyeHeight(), target.posZ, (float) entity.getHorizontalFaceSpeed(), (float) entity.getVerticalFaceSpeed());
        lookTime--;
    }

    public Entity getTarget() {
        return target;
    }
}
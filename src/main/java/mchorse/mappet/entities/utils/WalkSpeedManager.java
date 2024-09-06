package mchorse.mappet.entities.utils;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import java.util.UUID;

public class WalkSpeedManager {

    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("a053b1d3-4c27-4e8e-9f38-df029c5f5875");
    private static final String SPEED_MODIFIER_NAME = "mappet-speed-adjustment";

    public static void setWalkSpeed(EntityPlayer entity, float speed) {
        if (!entity.world.isRemote) {
            entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(SPEED_MODIFIER_UUID);
            entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).applyModifier(new AttributeModifier(SPEED_MODIFIER_UUID, SPEED_MODIFIER_NAME, speed - entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue(), 0));
        }
    }

    public static void resetWalkSpeed(EntityPlayer entity) {
        if (!entity.world.isRemote)
            entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(SPEED_MODIFIER_UUID);
    }

    public static float getWalkSpeed(EntityPlayer entity) {
        return (float) entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
    }
}

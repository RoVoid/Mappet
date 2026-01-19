package mchorse.mappet.entities.ai;

import mchorse.mappet.entities.EntityNpc;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityAIFollowTarget extends EntityAIBase {
    private static final double TELEPORT_DISTANCE = 144;

    private final EntityNpc target;
    private EntityLivingBase follow;
    private final double speed;
    private int timer;
    private float max;
    private float min;
    private float prevWaterFactor;

    public EntityAIFollowTarget(EntityNpc target, double speed, float min, float max) {
        this.target = target;
        this.speed = speed;
        this.min = min;
        this.max = max;

        setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        EntityLivingBase target = this.target.getFollowTarget();

        if (target == null) return false;
        if (target instanceof EntityPlayer && ((EntityPlayer) target).isSpectator()) return false;
        if (this.target.getDistanceSq(target) < min * min) return false;

        follow = target;
        return true;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !target.getNavigator().noPath() && target.getDistanceSq(follow) > min * min;
    }

    @Override
    public void startExecuting() {
        timer = 0;
        prevWaterFactor = target.getPathPriority(PathNodeType.WATER);
        target.setPathPriority(PathNodeType.WATER, 0);
    }

    @Override
    public void resetTask() {
        follow = null;
        target.getNavigator().clearPath();

        target.setPathPriority(PathNodeType.WATER, prevWaterFactor);
    }

    @Override
    public void updateTask() {
        target.getLookHelper().setLookPositionWithEntity(follow, 10, target.getVerticalFaceSpeed());

        if (timer > 0) {
            timer--;
            return;
        }

        timer = 10;

        if (!target.getNavigator()
                .tryMoveToEntityLiving(follow,
                        speed) && !target.getLeashed() && !target.isRiding() || target.getDistanceSq(follow) >= TELEPORT_DISTANCE) {
            int x = MathHelper.floor(follow.posX) - 2;
            int z = MathHelper.floor(follow.posZ) - 2;
            int y = MathHelper.floor(follow.getEntityBoundingBox().minY);

            for (int bx = 0; bx <= 4; ++bx) {
                for (int bz = 0; bz <= 4; ++bz) {
                    if ((bx < 1 || bz < 1 || bx > 3 || bz > 3) && canTeleport(x, z, y, bx, bz)) {
                        target.setLocationAndAngles((float) (x + bx) + 0.5F,
                                y,
                                (float) (z + bz) + 0.5F,
                                target.rotationYaw,
                                target.rotationPitch);
                        target.getNavigator().clearPath();

                        return;
                    }
                }
            }
        }
    }

    private boolean canTeleport(int x, int z, int y, int offsetX, int offsetY) {
        World world = target.world;
        BlockPos pos = new BlockPos(x + offsetX, y - 1, z + offsetY);
        IBlockState state = world.getBlockState(pos);

        return state.getBlockFaceShape(world,
                pos,
                EnumFacing.DOWN) == BlockFaceShape.SOLID && state.canEntitySpawn(target) && world.isAirBlock(pos.up()) && world.isAirBlock(
                pos.up(2));
    }
}

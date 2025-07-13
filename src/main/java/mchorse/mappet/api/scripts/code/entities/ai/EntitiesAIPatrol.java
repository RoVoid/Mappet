package mchorse.mappet.api.scripts.code.entities.ai;

import mchorse.mclib.utils.MathUtils;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.ArrayUtils;

public class EntitiesAIPatrol extends EntityAIBase {
    private final EntityLiving target;

    private final double speed;

    private int timer;

    private float prevWaterFactor;

    private int index;

    private int direction = 1;

    private BlockPos[] patrolPoints;

    private boolean[] shouldCirculate;

    private String[] executeCommandOnArrival;

    public EntitiesAIPatrol(EntityLiving target, double speed, BlockPos[] patrolPoints, boolean[] shouldCirculate, String[] executeCommandOnArrival) {
        this.target = target;
        this.speed = speed;
        this.patrolPoints = patrolPoints;
        this.shouldCirculate = shouldCirculate;
        this.executeCommandOnArrival = executeCommandOnArrival;

        setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        return this.target.getAttackingEntity() == null && patrolPoints.length > 0;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return target.getAttackingEntity() == null && patrolPoints.length > 0;
    }

    @Override
    public void startExecuting() {
        timer = 0;
        prevWaterFactor = target.getPathPriority(PathNodeType.WATER);

        target.setPathPriority(PathNodeType.WATER, 0);
    }

    @Override
    public void resetTask() {
        target.getNavigator().clearPath();

        target.setPathPriority(PathNodeType.WATER, prevWaterFactor);
        target.rotationPitch = 0;
    }

    @Override
    public void updateTask() {
        if (index < 0 || index >= patrolPoints.length) return;

        BlockPos pos = patrolPoints[index];

        if (target.getDistanceSq(pos) < 2) {
            int next = index + direction;

            if (shouldCirculate[index]) {
                index = MathUtils.cycler(index + direction, 0, patrolPoints.length - 1);
            } else {
                if (next < 0 || next >= patrolPoints.length) direction = -direction;
                index += direction;
            }

            // Execute command on arrival if specified
            if (executeCommandOnArrival[index] != null) {
                MinecraftServer server = target.getServer();
                if (server != null)
                    server.getCommandManager().executeCommand(target, executeCommandOnArrival[index]);
            }
            timer = 0;
        }

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        target.getLookHelper().setLookPosition(x, y + target.height, z, 10, target.getVerticalFaceSpeed());

        if (--timer <= 0) {
            timer = 10;
            Path path = target.getNavigator().getPathToXYZ(x, y, z);
            if (path != null) target.getNavigator().setPath(path, speed);
        }
    }

    public BlockPos[] getPatrolPoints() {
        return patrolPoints;
    }

    public boolean[] getShouldCirculate() {
        return shouldCirculate;
    }

    public String[] getExecuteCommandOnArrival() {
        return executeCommandOnArrival;
    }

    public void addPatrolPoint(BlockPos point, boolean shouldCirculate, String executeCommandOnArrival) {
        this.patrolPoints = ArrayUtils.add(this.patrolPoints, point);
        this.shouldCirculate = ArrayUtils.add(this.shouldCirculate, shouldCirculate);
        this.executeCommandOnArrival = ArrayUtils.add(this.executeCommandOnArrival, executeCommandOnArrival);
    }
}

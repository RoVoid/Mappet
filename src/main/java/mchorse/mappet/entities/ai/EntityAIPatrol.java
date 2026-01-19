package mchorse.mappet.entities.ai;

import mchorse.mappet.api.npcs.NpcState;
import mchorse.mappet.api.triggers.Trigger;
import mchorse.mappet.api.utils.DataContext;
import mchorse.mappet.entities.EntityNpc;
import mchorse.mclib.utils.MathUtils;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.BlockPos;

public class EntityAIPatrol extends EntityAIBase {
    private final EntityNpc target;
    private double speed;
    private int cooldown;
    private float prevWaterFactor;

    private int index;
    private int direction = 1;

    public EntityAIPatrol(EntityNpc target) {
        this.target = target;
        speed = target.getState().speed.get();

        setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        return target.getAttackTarget() == null && !target.getState().patrol.isEmpty();
    }

    @Override
    public boolean shouldContinueExecuting() {
        speed = target.getState().speed.get();
        return target.getAttackTarget() == null && !target.getState().patrol.isEmpty();
    }

    @Override
    public void startExecuting() {
        cooldown = 0;
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
        NpcState state = target.getState();

        if (index < 0 || index >= state.patrol.size()) return;

        BlockPos pos = state.patrol.get(index);

        if (target.getDistanceSq(pos) < 3) {
            if (index >= 0 && index < state.patrolTriggers.size()) {
                Trigger triggerPatrol = state.patrolTriggers.get(index);
                DataContext context = new DataContext(target)
                        .set("last", index == state.patrol.size() - 1 ? 1 : 0)
                        .set("index", index)
                        .set("count", state.patrol.size());

                triggerPatrol.trigger(context);
            }

            if (state.patrolCirculate.get()) {
                index = MathUtils.cycler(index + direction, 0, state.patrol.size() - 1);
            } else {
                if (index + direction < 0 || index + direction >= target.getState().patrol.size()) direction = -direction;
                index += direction;
            }

            cooldown = 0;
        }

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        target.getLookHelper().setLookPosition(x, y + target.height, z, 10, target.getVerticalFaceSpeed());

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        PathNavigate navigator = target.getNavigator();
        Path path = navigator.getPathToXYZ(x, y, z);

        navigator.setPath(path, speed);
        cooldown = 10;
    }
}

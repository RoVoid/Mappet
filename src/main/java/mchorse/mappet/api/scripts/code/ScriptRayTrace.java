package mchorse.mappet.api.scripts.code;

import mchorse.mappet.api.scripts.code.entities.ScriptEntity;
import mchorse.mappet.api.scripts.user.IScriptRayTrace;
import mchorse.mappet.api.scripts.code.math.ScriptVector;
import mchorse.mappet.api.scripts.user.entities.IScriptEntity;
import net.minecraft.util.math.RayTraceResult;

public class ScriptRayTrace implements IScriptRayTrace {
    private final RayTraceResult result;
    private IScriptEntity entity;

    public ScriptRayTrace(RayTraceResult result) {
        this.result = result;
    }

    @Override
    public RayTraceResult getMinecraftRayTraceResult() {
        return result;
    }

    @Override
    public boolean isMissed() {
        return result.typeOfHit == RayTraceResult.Type.MISS;
    }

    @Override
    public boolean isBlock() {
        return result.typeOfHit == RayTraceResult.Type.BLOCK;
    }

    @Override
    public boolean isEntity() {
        return result.typeOfHit == RayTraceResult.Type.ENTITY;
    }

    @Override
    public IScriptEntity getEntity() {
        if (result.entityHit == null) return null;
        if (entity == null) entity = ScriptEntity.create(result.entityHit);
        return entity;
    }

    @Override
    public ScriptVector getBlock() {
        return new ScriptVector(result.getBlockPos());
    }

    @Override
    public ScriptVector getHitPosition() {
        return new ScriptVector(result.hitVec);
    }
}
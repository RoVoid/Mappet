package mchorse.mappet.api.scripts.code;

import mchorse.mappet.api.scripts.code.entities.ScriptEntity;
import mchorse.mappet.api.scripts.code.math.ScriptVector;
import mchorse.mappet.api.scripts.code.entities.ScriptEntity;
import net.minecraft.util.math.RayTraceResult;

public class ScriptRayTrace {
    private final RayTraceResult result;
    private ScriptEntity entity;

    public ScriptRayTrace(RayTraceResult result) {
        this.result = result;
    }

    public RayTraceResult getMinecraftRayTraceResult() {
        return result;
    }

    public boolean isMissed() {
        return result.typeOfHit == RayTraceResult.Type.MISS;
    }

    public boolean isBlock() {
        return result.typeOfHit == RayTraceResult.Type.BLOCK;
    }

    public boolean isEntity() {
        return result.typeOfHit == RayTraceResult.Type.ENTITY;
    }

    public ScriptEntity getEntity() {
        if (result.entityHit == null) return null;
        if (entity == null) entity = ScriptEntity.create(result.entityHit);
        return entity;
    }

    public ScriptVector getBlock() {
        return new ScriptVector(result.getBlockPos());
    }

    public ScriptVector getHitPosition() {
        return new ScriptVector(result.hitVec);
    }
}
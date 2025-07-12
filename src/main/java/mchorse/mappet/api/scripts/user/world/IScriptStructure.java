package mchorse.mappet.api.scripts.user.world;

import mchorse.mappet.api.scripts.code.data.ScriptVector;
import net.minecraft.world.gen.structure.template.Template;

public interface IScriptStructure {
    void takeBlocks(ScriptVector start, ScriptVector end, boolean withEntities);

    void takeBlocks(int x1, int y1, int z1, int x2, int y2, int z2, boolean withEntities);

    void placeBlocks(ScriptVector pos);

    void placeBlocks(int x, int y, int z);

    void placeBlocks(ScriptVector pos, int rotation, int mirror, boolean withEntities);

    void placeBlocks(int x, int y, int z, int rotation, int mirror, boolean withEntities);

    ScriptVector getSize();

    void save();

    void save(String name);

    int getDimensionId();

    boolean isValid();

    Template getMinecraftStructure();
}

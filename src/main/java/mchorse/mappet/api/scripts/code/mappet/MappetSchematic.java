package mchorse.mappet.api.scripts.code.mappet;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.schematics.Schematic;
import mchorse.mappet.api.scripts.code.world.ScriptWorld;

public class MappetSchematic {
    private Schematic schematic;

    private final ScriptWorld world;

    public static MappetSchematic create(ScriptWorld world)
    {
        return new MappetSchematic(world);
    }

    public MappetSchematic(ScriptWorld world)
    {
        this.schematic = new Schematic();
        this.world = world;
    }

    public MappetSchematic loadFromWorld(int x1, int y1, int z1, int x2, int y2, int z2)
    {
        this.schematic.loadFromWorld(this.world.asMinecraft(), x1, y1, z1, x2, y2, z2);
        return this;
    }

    public MappetSchematic place(int x, int y, int z, boolean replaceBlocks, boolean placeAir)
    {
        this.schematic.place(this.world.asMinecraft(), x, y, z, replaceBlocks, placeAir);
        return this;
    }

    public MappetSchematic place(int x, int y, int z, boolean replaceBlocks)
    {
        return this.place(x, y, z, replaceBlocks, true);
    }

    public MappetSchematic place(int x, int y, int z)
    {
        return this.place(x, y, z, true, true);
    }

    public MappetSchematic saveToFile(String name)
    {
        Mappet.schematics.save(name, this.schematic);
        return this;
    }

    public MappetSchematic loadFromFile(String name)
    {
        this.schematic = Mappet.schematics.load(name);
        return this;
    }
}

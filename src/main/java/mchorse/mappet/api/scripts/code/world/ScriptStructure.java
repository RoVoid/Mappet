package mchorse.mappet.api.scripts.code.world;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.scripts.code.data.ScriptVector;
import mchorse.mappet.api.scripts.user.world.IScriptStructure;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class ScriptStructure implements IScriptStructure {
    String name;
    WorldServer world;
    Template structure;

    public ScriptStructure(ScriptWorld world, String name) {
        if (!(world.getMinecraftWorld() instanceof WorldServer)) return;
        this.world = ((WorldServer) world.getMinecraftWorld());
        this.name = name;
        TemplateManager manager = this.world.getStructureTemplateManager();
        structure = manager.get(this.world.getMinecraftServer(), new ResourceLocation(name));
        if (structure == null) structure = new Template();
    }

    public void takeBlocks(BlockPos pos, BlockPos size, boolean withEntities) {
        structure.takeBlocksFromWorld(world, pos, size, withEntities, Blocks.STRUCTURE_VOID);
    }

    @Override
    public void takeBlocks(ScriptVector start, ScriptVector end, boolean withEntities) {
        takeBlocks(start.toBlockPos(), end.toBlockPos().subtract(start.toBlockPos()).add(1, 1, 1), withEntities);
    }

    @Override
    public void takeBlocks(int x1, int y1, int z1, int x2, int y2, int z2, boolean withEntities) {
        takeBlocks(new BlockPos(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2)), new BlockPos(Math.abs(x2 - x1) + 1, Math.abs(y2 - y1) + 1, Math.abs(z2 - z1) + 1), withEntities);
    }

    public void placeBlocks(BlockPos pos, int rotation, int mirror, boolean withEntities) {
        PlacementSettings settings = new PlacementSettings();
        settings.setIntegrity(1.0F);
        settings.setSeed(new Random().nextLong());
        settings.setRotation(Rotation.values()[rotation % 4]);
        settings.setMirror(Mirror.values()[mirror % 3]);
        settings.setIgnoreEntities(!withEntities);
        structure.addBlocksToWorld(world, pos, settings);
    }

    @Override
    public void placeBlocks(ScriptVector pos) {
        placeBlocks(pos.toBlockPos(), 0, 0, false);
    }

    @Override
    public void placeBlocks(int x, int y, int z) {
        placeBlocks(new BlockPos(x, y, z), 0, 0, false);
    }

    @Override
    public void placeBlocks(ScriptVector pos, int rotation, int mirror, boolean withEntities) {
        placeBlocks(pos.toBlockPos(), rotation, mirror, withEntities);
    }

    @Override
    public void placeBlocks(int x, int y, int z, int rotation, int mirror, boolean withEntities) {
        placeBlocks(new BlockPos(x, y, z), rotation, mirror, withEntities);
    }

    @Override
    public ScriptVector getSize() {
        return new ScriptVector(structure.getSize());
    }

    @Override
    public void save() {
        save(name);
    }

    @Override
    public void save(String name) {
        File file = new File(world.getSaveHandler().getWorldDirectory(), "structures/" + name + ".nbt");
        NBTTagCompound nbt = new NBTTagCompound();
        structure.writeToNBT(nbt);
        try {
            CompressedStreamTools.write(nbt, file);
        } catch (IOException e) {
            Mappet.logger.error(e.getMessage());
        }
    }

    @Override
    public int getDimensionId() {
        return world.provider.getDimension();
    }

    @Override
    public boolean isValid() {
        return structure != null && world != null;
    }

    @Override
    public Template getMinecraftStructure() {
        return structure;
    }
}

package mchorse.mappet.api.scripts.code.blocks;

import mchorse.mappet.api.scripts.code.items.ScriptInventory;
import mchorse.mappet.api.scripts.code.math.ScriptVector;
import mchorse.mappet.api.scripts.code.nbt.ScriptNBTCompound;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class ScriptTileEntity {
    private final TileEntity tile;

    public ScriptTileEntity(TileEntity tile) {
        this.tile = tile;
    }

    @Deprecated
    public TileEntity getMinecraftTileEntity() {
        return tile;
    }

    public TileEntity asMinecraft() {
        return tile;
    }

    public String getId() {
        ResourceLocation key = TileEntity.getKey(tile.getClass());
        return key == null ? "" : key.toString();
    }

    public boolean isInvalid() {
        return tile.isInvalid();
    }

    public ScriptNBTCompound getData() {
        return new ScriptNBTCompound(tile.serializeNBT());
    }

    public void setData(ScriptNBTCompound compound) {
        tile.readFromNBT(compound.asMinecraft());
        tile.markDirty();
    }

    public ScriptNBTCompound getTileData() {
        return new ScriptNBTCompound(tile.getTileData());
    }

    public boolean hasInventory(int x, int y, int z) {
        return tile instanceof IInventory;
    }

    public boolean hasInventory(ScriptVector pos) {
        return hasInventory(pos.floorX(), pos.floorY(), pos.floorZ());
    }

    public ScriptInventory getInventory(int x, int y, int z) {
        return tile instanceof IInventory ? new ScriptInventory((IInventory) tile) : null;
    }
}
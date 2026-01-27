package mchorse.mappet.api.scripts.code.world;

import io.netty.buffer.Unpooled;
import mchorse.blockbuster.common.GunProps;
import mchorse.blockbuster.common.entity.EntityGunProjectile;
import mchorse.mappet.Mappet;
import mchorse.mappet.api.npcs.Npc;
import mchorse.mappet.api.npcs.NpcState;
import mchorse.mappet.api.scripts.code.ScriptFactory;
import mchorse.mappet.api.scripts.code.ScriptRayTrace;
import mchorse.mappet.api.scripts.code.blocks.ScriptBlockState;
import mchorse.mappet.api.scripts.code.blocks.ScriptTileEntity;
import mchorse.mappet.api.scripts.code.entities.ScriptEntity;
import mchorse.mappet.api.scripts.code.entities.ScriptNpc;
import mchorse.mappet.api.scripts.code.items.ScriptInventory;
import mchorse.mappet.api.scripts.code.items.ScriptItemStack;
import mchorse.mappet.api.scripts.code.mappet.MappetSchematic;
import mchorse.mappet.api.scripts.code.math.ScriptBox;
import mchorse.mappet.api.scripts.code.math.ScriptVector;
import mchorse.mappet.api.scripts.code.nbt.ScriptNBTCompound;
import mchorse.mappet.api.scripts.user.IScriptRayTrace;
import mchorse.mappet.api.scripts.user.blocks.IScriptBlockState;
import mchorse.mappet.api.scripts.user.blocks.IScriptTileEntity;
import mchorse.mappet.api.scripts.user.entities.IScriptEntity;
import mchorse.mappet.api.scripts.user.entities.IScriptEntityItem;
import mchorse.mappet.api.scripts.user.entities.IScriptNpc;
import mchorse.mappet.api.scripts.user.entities.player.IScriptPlayer;
import mchorse.mappet.api.scripts.user.items.IScriptInventory;
import mchorse.mappet.api.scripts.user.items.IScriptItemStack;
import mchorse.mappet.api.scripts.user.nbt.INBTCompound;
import mchorse.mappet.api.scripts.user.world.IScriptWorld;
import mchorse.mappet.api.utils.RayTracing;
import mchorse.mappet.client.gui.scripts.scriptedItem.util.Pair;
import mchorse.mappet.client.morphs.WorldMorph;
import mchorse.mappet.entities.EntityNpc;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.packets.scripts.PacketWorldMorph;
import mchorse.mappet.utils.WorldUtils;
import mchorse.mclib.utils.MathUtils;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ScriptWorld implements IScriptWorld {
    public static final int MAX_VOLUME = 100;

    private final World world;
    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    public ScriptWorld(World world) {
        this.world = world;
    }

    @Override
    @Deprecated
    public World getMinecraftWorld() {
        return world;
    }

    @Override
    public World asMinecraft() {
        return world;
    }

    @Override
    public void setGameRule(String name, Object value) {
        if (value instanceof Boolean || value instanceof String || value instanceof Integer) {
            world.getGameRules().setOrCreateGameRule(name, String.valueOf(value));
        }
        else Mappet.logger.error("Unsupported game rule value type: " + value.getClass());
    }

    @Override
    public Object getGameRule(String name) {
        if (!world.getGameRules().hasRule(name)) return null;
        String value = world.getGameRules().getString(name);

        if (value.equals("true") || value.equals("false")) return Boolean.parseBoolean(value);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return value;
        }
    }

    @Override
    public boolean isBlockLoaded(int x, int y, int z) {
        return world.isBlockLoaded(pos.setPos(x, y, z));
    }

    @Override
    public boolean isBlockLoaded(ScriptVector newPos) {
        return world.isBlockLoaded(pos.setPos(newPos.floorX(), newPos.floorY(), newPos.floorZ()));
    }

    @Override
    public void setBlock(IScriptBlockState block, int x, int y, int z) {
        if (isBlockLoaded(x, y, z)) world.setBlockState(pos, block.asMinecraft(), 2 | 4);
    }

    @Override
    public void setBlock(IScriptBlockState block, ScriptVector pos) {
        setBlock(block, pos.floorX(), pos.floorY(), pos.floorZ());
    }

    @Override
    public void removeBlock(int x, int y, int z) {
        if (isBlockLoaded(x, y, z)) world.setBlockToAir(pos);
    }

    @Override
    public void removeBlock(ScriptVector pos) {
        removeBlock(pos.floorX(), pos.floorY(), pos.floorZ());
    }

    @Override
    public IScriptBlockState getBlock(int x, int y, int z) {
        if (isBlockLoaded(x, y, z)) {
            return ScriptBlockState.create(world.getBlockState(pos).getActualState(world, pos));
        }
        return ScriptBlockState.AIR;
    }

    @Override
    public IScriptBlockState getBlock(ScriptVector pos) {
        return getBlock(pos.floorX(), pos.floorY(), pos.floorZ());
    }

    @Override
    public boolean hasTileEntity(int x, int y, int z) {
        return isBlockLoaded(x, y, z) && world.getTileEntity(pos) != null;
    }

    @Override
    public boolean hasTileEntity(ScriptVector pos) {
        return hasTileEntity(pos.floorX(), pos.floorY(), pos.floorZ());
    }

    @Override
    public void setTileEntity(IScriptBlockState block, int x, int y, int z, INBTCompound tileData) {
        setBlock(block, x, y, z);
        if (tileData == null) tileData = new ScriptNBTCompound(null);
        tileData.setInt("x", x);
        tileData.setInt("y", y);
        tileData.setInt("z", z);
        getTileEntity(x, y, z).setData(tileData);
    }

    @Override
    public void setTileEntity(IScriptBlockState block, ScriptVector pos, INBTCompound tileData) {
        setTileEntity(block, pos.floorX(), pos.floorY(), pos.floorZ(), tileData);
    }

    @Override
    public IScriptTileEntity getTileEntity(int x, int y, int z) {
        if (!hasTileEntity(x, y, z)) return null;
        return new ScriptTileEntity(world.getTileEntity(pos.setPos(x, y, z)));
    }

    @Override
    public IScriptTileEntity getTileEntity(ScriptVector pos) {
        return getTileEntity(pos.floorX(), pos.floorY(), pos.floorZ());
    }

    @Override
    public void replaceBlocks(IScriptBlockState block, IScriptBlockState newBlock, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        replaceBlocks(block, newBlock, new ScriptBox(minX, minY, minZ, maxX, maxY, maxZ));
    }

    @Override
    public void replaceBlocks(IScriptBlockState block, IScriptBlockState newBlock, ScriptVector start, ScriptVector end) {
        replaceBlocks(block, newBlock, new ScriptBox(start.x, start.y, start.z, end.x, end.y, end.z));
    }

    @Override
    public void replaceBlocks(IScriptBlockState block, IScriptBlockState newBlock, ScriptBox box) {
        for (int x = (int) Math.floor(box.minX); x <= (int) box.maxX; x++) {
            for (int y = (int) Math.floor(box.minY); y <= (int) box.maxY; y++) {
                for (int z = (int) Math.floor(box.minZ); z <= (int) box.maxZ; z++) {
                    if (isBlockLoaded(x, y, z) && getBlock(x, y, z).isSame(block)) setBlock(newBlock, x, y, z);
                }
            }
        }
    }

    @Override
    public void replaceBlocks(IScriptBlockState block, IScriptBlockState newBlock, INBTCompound tileData, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        replaceBlocks(block, newBlock, tileData, new ScriptBox(minX, minY, minZ, maxX, maxY, maxZ));
    }

    @Override
    public void replaceBlocks(IScriptBlockState block, IScriptBlockState newBlock, INBTCompound tileData, ScriptVector start, ScriptVector end) {
        replaceBlocks(block, newBlock, tileData, new ScriptBox(start.x, start.y, start.z, end.x, end.y, end.z));
    }

    @Override
    public void replaceBlocks(IScriptBlockState block, IScriptBlockState newBlock, INBTCompound tileData, ScriptBox box) {
        for (int x = (int) Math.floor(box.minX); x <= (int) box.maxX; x++) {
            for (int y = (int) Math.floor(box.minY); y <= (int) box.maxY; y++) {
                for (int z = (int) Math.floor(box.minZ); z <= (int) box.maxZ; z++) {
                    if (isBlockLoaded(x, y, z) && getBlock(x, y, z).isSame(block)) setTileEntity(newBlock, x, y, z, tileData);
                }
            }
        }
    }

    @Override
    public boolean hasInventory(int x, int y, int z) {
        return isBlockLoaded(x, y, z) && world.getTileEntity(pos) instanceof IInventory;
    }

    @Override
    public boolean hasInventory(ScriptVector pos) {
        return hasInventory(pos.floorX(), pos.floorY(), pos.floorZ());
    }

    @Override
    public IScriptInventory getInventory(int x, int y, int z) {
        if (isBlockLoaded(x, y, z)) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof IInventory) return new ScriptInventory((IInventory) tile);
        }
        return null;
    }

    @Override
    public boolean isRaining() {
        return world.getWorldInfo().isRaining();
    }

    @Override
    public void setRaining(boolean raining) {
        world.getWorldInfo().setRaining(raining);
    }

    @Override
    public long getTime() {
        return world.getWorldTime();
    }

    @Override
    public void setTime(long time) {
        world.setWorldTime(time);
    }

    @Override
    public long getTotalTime() {
        return world.getTotalWorldTime();
    }

    @Override
    public int getDimensionId() {
        MinecraftServer server = world.getMinecraftServer();
        if (server != null) for (int id : DimensionManager.getIDs()) if (server.getWorld(id) == world) return id;
        return -1;
    }

    @Override
    public void spawnParticles(EnumParticleTypes type, boolean longDistance, double x, double y, double z, int number, double dx, double dy, double dz, double speed, int... args) {
        ((WorldServer) world).spawnParticle(type, longDistance, x, y, z, number, dx, dy, dz, speed, args);
    }

    @Override
    public void spawnParticles(EnumParticleTypes type, boolean longDistance, ScriptVector pos, int number, ScriptVector offset, double speed, int... args) {
        ((WorldServer) world).spawnParticle(type, longDistance, pos.x, pos.y, pos.z, number, offset.x, offset.y, offset.z, speed, args);
    }

    @Override
    public void spawnParticles(IScriptPlayer entity, EnumParticleTypes type, boolean longDistance, double x, double y, double z, int number, double dx, double dy, double dz, double speed, int... args) {
        if (entity == null) return;
        ((WorldServer) world).spawnParticle(entity.asMinecraft(), type, longDistance, x, y, z, number, dx, dy, dz, speed, args);
    }

    public void spawnParticles(IScriptPlayer entity, EnumParticleTypes type, boolean longDistance, ScriptVector pos, int number, ScriptVector offset, double speed, int... args) {
        if (entity == null) return;
        ((WorldServer) world).spawnParticle(entity.asMinecraft(),
                type,
                longDistance,
                pos.x,
                pos.y,
                pos.z,
                number,
                offset.x,
                offset.y,
                offset.z,
                speed,
                args);
    }

    @Override
    public IScriptEntity spawnEntity(String id, double x, double y, double z, INBTCompound compound) {
        if (!isBlockLoaded((int) x, (int) y, (int) z)) return null;
        NBTTagCompound tag = new NBTTagCompound();
        if (compound != null) tag.merge(compound.asMinecraft());
        tag.setString("id", id);

        Entity entity = AnvilChunkLoader.readWorldEntityPos(tag, world, x, y, z, true);
        return entity == null ? null : ScriptEntity.create(entity);
    }

    @Override
    public IScriptEntity spawnEntity(String id, ScriptVector pos, INBTCompound compound) {
        return spawnEntity(id, pos.x, pos.y, pos.z, compound);
    }

    @Override
    public IScriptNpc spawnNpc(String id, String state, double x, double y, double z) {
        return spawnNpc(id, state, x, y, z, 0, 0, 0);
    }

    @Override
    public IScriptNpc spawnNpc(String id, String state, double x, double y, double z, float yaw, float pitch, float headYaw) {
        Npc npc = Mappet.npcs.load(id);
        if (npc == null) return null;

        NpcState npcState = npc.states.get(state);
        if (npcState == null) return null;

        EntityNpc entity = new EntityNpc(world);

        entity.setPositionAndRotation(x, y, z, yaw, pitch);
        entity.setRotationYawHead(headYaw);
        entity.setNpc(npc, npcState);

        entity.world.spawnEntity(entity);
        entity.initialize();

        if (!npc.serializeNBT().getString("StateName").equals("default")) {
            entity.setStringInData("StateName", state);
        }

        return new ScriptNpc(entity);
    }

    @Override
    public IScriptNpc spawnNpc(String id, String state, ScriptVector pos) {
        return spawnNpc(id, state, pos.x, pos.y, pos.z, 0, 0, 0);
    }

    @Override
    public IScriptNpc spawnNpc(String id, String state, ScriptVector pos, ScriptVector rot) {
        return spawnNpc(id, state, pos.x, pos.y, pos.z, (float) rot.x, (float) rot.y, (float) rot.z);
    }

    @Override
    public List<IScriptEntity> getEntities(double x1, double y1, double z1, double x2, double y2, double z2) {
        return getEntities(x1, y1, z1, x2, y2, z2, false);
    }

    @Override
    public List<IScriptEntity> getEntities(ScriptBox box) {
        return getEntities(box, false);
    }

    @Override
    public List<IScriptEntity> getEntities(double x1, double y1, double z1, double x2, double y2, double z2, boolean ignoreVolumeLimit) {
        return getEntities(new ScriptBox(x1, y1, z1, x2, y2, z2), ignoreVolumeLimit);
    }

    @Override
    public List<IScriptEntity> getEntities(ScriptBox box, boolean ignoreVolumeLimit) {
        List<IScriptEntity> entities = new ArrayList<>();
        if (!ignoreVolumeLimit && (box.maxX - box.minX > MAX_VOLUME || box.maxY - box.minY > MAX_VOLUME || box.maxZ - box.minZ > MAX_VOLUME)) {
            return entities;
        }

        int minChunkX = ((int) Math.floor(box.minX)) >> 4;
        int minChunkZ = ((int) Math.floor(box.minZ)) >> 4;
        int maxChunkX = ((int) box.maxX) >> 4;
        int maxChunkZ = ((int) box.maxZ) >> 4;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (!world.isChunkGeneratedAt(chunkX, chunkZ)) continue;
                Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
                AxisAlignedBB chunkAABB = new AxisAlignedBB(Math.max(chunk.getPos().getXStart(), box.minX),
                        box.minY,
                        Math.max(chunk.getPos().getZStart(), box.minZ),
                        Math.min(chunk.getPos().getXEnd(), box.maxX),
                        box.maxY,
                        Math.min(chunk.getPos().getZEnd(), box.maxZ));

                List<Entity> chunkEntities = new ArrayList<>();
                chunk.getEntitiesWithinAABBForEntity(null, chunkAABB, chunkEntities, Objects::nonNull);
                for (Entity entity : chunkEntities) entities.add(ScriptEntity.create(entity));
            }
        }

        return entities;
    }

    @Override
    public void playSound(String event, double x, double y, double z, float volume, float pitch) {
        MinecraftServer server = world.getMinecraftServer();
        if (server == null) return;
        for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            WorldUtils.playSound(player, event, x, y, z, volume, pitch);
        }
    }

    @Override
    public void playSound(String event, ScriptVector pos, float volume, float pitch) {
        playSound(event, pos.x, pos.y, pos.z, volume, pitch);
    }

    @Override
    public void stopSound(String event, String category) {
        MinecraftServer server = world.getMinecraftServer();
        if (server == null) return;
        PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());

        packetbuffer.writeString(category);
        packetbuffer.writeString(event);

        for (EntityPlayerMP player : world.getMinecraftServer().getPlayerList().getPlayers()) {
            player.connection.sendPacket(new SPacketCustomPayload("MC|StopSound", packetbuffer));
        }
    }

    @Override
    public IScriptEntityItem dropItemStack(IScriptItemStack stack, double x, double y, double z, double mx, double my, double mz) {
        if (stack == null || stack.isEmpty()) return null;

        EntityItem item = new EntityItem(world, x, y, z, stack.asMinecraft().copy());

        item.motionX = mx;
        item.motionY = my;
        item.motionZ = mz;

        world.spawnEntity(item);

        return (IScriptEntityItem) ScriptEntity.create(item);
    }

    @Override
    public void explode(IScriptEntity exploder, double x, double y, double z, float distance, boolean blazeGround, boolean destroyTerrain) {
        world.newExplosion(exploder == null ? null : exploder.asMinecraft(), x, y, z, distance, blazeGround, destroyTerrain);
    }

    @Override
    public void explode(IScriptEntity exploder, ScriptVector pos, float distance, boolean blazeGround, boolean destroyTerrain) {
        world.newExplosion(exploder == null ? null : exploder.asMinecraft(), pos.x, pos.y, pos.z, distance, blazeGround, destroyTerrain);
    }

    @Override
    public IScriptRayTrace rayTrace(double x1, double y1, double z1, double x2, double y2, double z2) {
        return new ScriptRayTrace(RayTracing.rayTraceWithEntity(world, x1, y1, z1, x2, y2, z2));
    }

    @Override
    public IScriptRayTrace rayTraceBlock(double x1, double y1, double z1, double x2, double y2, double z2) {
        return new ScriptRayTrace(RayTracing.rayTrace(world, x1, y1, z1, x2, y2, z2));
    }

    @Override
    public boolean isActive(int x, int y, int z) {
        return world.isBlockPowered(new BlockPos(x, y, z));
    }

    @Override
    public boolean isActive(ScriptVector pos) {
        return world.isBlockPowered(pos.toBlockPos());
    }

    @Override
    public boolean testForBlock(ScriptBlockState block, int x, int y, int z) {
        return block != null && getBlock(x, y, z).isSame(block);
    }

    @Override
    public boolean testForBlock(ScriptBlockState block, ScriptVector pos) {
        return block != null && getBlock(pos).isSame(block);
    }

    @Override
    public void fill(IScriptBlockState block, int x1, int y1, int z1, int x2, int y2, int z2) {
        fill(block, new ScriptBox(x1, y1, z1, x2, y2, z2));
    }

    @Override
    public void fill(IScriptBlockState block, ScriptVector start, ScriptVector end) {
        fill(block, new ScriptBox(start.x, start.y, start.z, end.x, end.y, end.z));
    }

    @Override
    public void fill(IScriptBlockState block, ScriptBox box) {
        for (int x = (int) Math.floor(box.minX); x <= box.maxX; x++) {
            for (int y = (int) Math.floor(box.minY); y <= box.maxY; y++) {
                for (int z = (int) Math.floor(box.minZ); z <= box.maxZ; z++) setBlock(block, x, y, z);
            }
        }
    }

    @Override
    public IScriptEntity spawnFallingBlock(IScriptBlockState block, double x, double y, double z) {
        NBTTagCompound nbt = new NBTTagCompound();

        nbt.setString("Block", block.getId());
        nbt.setInteger("Data", block.getMeta());
        nbt.setInteger("Time", 1);

        return spawnEntity("minecraft:falling_block", x, y, z, new ScriptNBTCompound(nbt));
    }

    @Override
    public IScriptEntity spawnFallingBlock(IScriptBlockState block, ScriptVector pos) {
        return spawnFallingBlock(block, pos.x, pos.y, pos.z);
    }

    @Override
    @Deprecated
    public IScriptEntity setFallingBlock(int x, int y, int z) {
        return makeBlockFall(x, y, z);
    }

    @Override
    public IScriptEntity makeBlockFall(int x, int y, int z) {
        IScriptBlockState block = getBlock(x, y, z);
        if (block == null || block.isAir()) return null;

        NBTTagCompound nbt = new NBTTagCompound();

        nbt.setString("Block", block.getId());
        nbt.setInteger("Data", block.getMeta());
        nbt.setInteger("Time", 1);

        for (IProperty<?> property : block.asMinecraft().getProperties().keySet()) {
            nbt.setString(property.getName(), block.asMinecraft().getValue(property).toString());
        }

        if (hasTileEntity(x, y, z)) {
            nbt.setTag("TileEntityData", getTileEntity(x, y, z).getData().asMinecraft());
            world.removeTileEntity(pos);
        }

        world.setBlockToAir(pos);

        return spawnEntity("minecraft:falling_block", x + 0.5, y + 0.5, z + 0.5, new ScriptNBTCompound(nbt));
    }

    @Override
    public IScriptEntity makeBlockFall(ScriptVector pos) {
        return makeBlockFall(pos.floorX(), pos.floorY(), pos.floorZ());
    }

    @Override
    @Deprecated
    public void fillTileEntities(int x1, int y1, int z1, int x2, int y2, int z2, IScriptBlockState block, INBTCompound tileData) {
        int xMin = Math.min(x1, x2);
        int xMax = Math.max(x1, x2);
        int yMin = Math.min(y1, y2);
        int yMax = Math.max(y1, y2);
        int zMin = Math.min(z1, z2);
        int zMax = Math.max(z1, z2);

        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                for (int z = zMin; z <= zMax; z++) {
                    setTileEntity(block, x, y, z, tileData);
                }
            }
        }
    }

    @Override
    public void clone(int x, int y, int z, int xNew, int yNew, int zNew) {
        IScriptBlockState block = getBlock(x, y, z);
        if (block.isAir()) return;

        setBlock(block, xNew, yNew, zNew);
        if (!hasTileEntity(x, y, z)) return;

        INBTCompound tile = getTileEntity(x, y, z).getData();
        tile.setInt("x", xNew);
        tile.setInt("y", yNew);
        tile.setInt("z", zNew);
        getTileEntity(xNew, yNew, zNew).setData(tile);
    }

    @Override
    public void clone(int x1, int y1, int z1, int x2, int y2, int z2, int xNew, int yNew, int zNew) {
    }


    @Override
    public void clone(ScriptBox box, int xNew, int yNew, int zNew) {
        List<Pair<IScriptBlockState, INBTCompound>> blocks = new ArrayList<>();

        int minX = (int) Math.floor(box.minX);
        int minY = (int) Math.floor(box.minY);
        int minZ = (int) Math.floor(box.minZ);

        int dx = xNew - minX;
        int dy = yNew - minY;
        int dz = zNew - minZ;

        for (int x = minX; x <= box.maxX; x++) {
            for (int y = minY; y <= box.maxY; y++) {
                for (int z = minZ; z <= box.maxZ; z++) {
                    IScriptTileEntity tileEntity = getTileEntity(x, y, z);
                    blocks.add(new Pair<>(getBlock(x, y, z), tileEntity == null ? null : tileEntity.getData()));
                }
            }
        }

        int i = 0;
        for (int x = minX; x <= box.maxX; x++) {
            for (int y = minY; y <= box.maxY; y++) {
                for (int z = minZ; z <= box.maxZ; z++) {
                    Pair<IScriptBlockState, INBTCompound> pair = blocks.get(i++);
                    setTileEntity(pair.a, x + dx, y + dy, z + dz, pair.b);
                }
            }
        }
    }

    @Override
    public IScriptItemStack getBlockItem(int x, int y, int z) {
        if (!isBlockLoaded(x, y, z)) return ScriptItemStack.EMPTY;

        IBlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();

        if (block == Blocks.AIR) return ScriptItemStack.EMPTY;

        ItemStack itemStack;
        Item blockItem = Item.getItemFromBlock(block);

        if (blockItem == Items.AIR) return ScriptItemStack.EMPTY;
        itemStack = new ItemStack(blockItem, 1, block.getMetaFromState(blockState));

        TileEntity tileEntity = world.getTileEntity(pos);

        if (tileEntity != null) {
            NBTTagCompound tileEntityNBT = new NBTTagCompound();

            tileEntity.writeToNBT(tileEntityNBT);

            NBTTagCompound itemStackNBT = new NBTTagCompound();

            itemStackNBT.setTag("BlockEntityTag", tileEntityNBT);
            itemStack.setTagCompound(itemStackNBT);
        }

        return ScriptItemStack.create(itemStack);
    }

    @Override
    public ScriptWorldBorder getBorder() {
        return new ScriptWorldBorder(world.getWorldBorder());
    }

    @Override
    public int getLight(int x, int y, int z) {
        return world.getLight(pos.setPos(x, y, z));
    }

    /* Mappet stuff */

    @Override
    public void displayMorph(AbstractMorph morph, int expiration, double x, double y, double z, float yaw, float pitch, int range, IScriptPlayer player) {
        if (morph == null) return;

        WorldMorph worldMorph = new WorldMorph();

        worldMorph.morph = morph;
        worldMorph.expiration = expiration;
        worldMorph.x = x;
        worldMorph.y = y;
        worldMorph.z = z;
        worldMorph.yaw = yaw;
        worldMorph.pitch = pitch;

        if (player == null) {
            NetworkRegistry.TargetPoint point = new NetworkRegistry.TargetPoint(world.provider.getDimension(),
                    x,
                    y,
                    z,
                    MathUtils.clamp(range, 1, 256));
            Dispatcher.sendToAllAround(new PacketWorldMorph(worldMorph), point);
        }
        else Dispatcher.sendTo(new PacketWorldMorph(worldMorph), player.asMinecraft());
    }

    @Override
    public MappetSchematic createSchematic() {
        return MappetSchematic.create(this);
    }

    @Override
    public ScriptStructure loadStructure(String name) {
        return new ScriptStructure(this, name);
    }

//    public TemplateManager getStructureManager(){
//        if(!(world instanceof WorldServer)) return null;
//        return ((WorldServer) world).getStructureTemplateManager();
//    }


    /* BlockBuster stuff */

    @Override
    public IScriptEntity shootBBGunProjectile(IScriptEntity shooter, double x, double y, double z, double yaw, double pitch, String gunPropsNbtString) {
        if (shooter.asMinecraft() instanceof EntityLivingBase && Loader.isModLoaded("blockbuster")) {
            try {
                return shootBBGunProjectileMethod(shooter, x, y, z, yaw, pitch, gunPropsNbtString);
            } catch (Exception e) {
                Mappet.logger.error(e.getMessage());
            }
        }
        return null;
    }

    @Optional.Method(modid = "blockbuster")
    private IScriptEntity shootBBGunProjectileMethod(IScriptEntity shooter, double x, double y, double z, double yaw, double pitch, String gunPropsNbtString) {
        ScriptFactory factory = new ScriptFactory();

        EntityLivingBase entityLivingBase = (EntityLivingBase) shooter.asMinecraft();
        GunProps gunProps = new GunProps((factory.createCompound(gunPropsNbtString)).getCompound("Gun")
                .getCompound("Projectile")
                .asMinecraft());

        gunProps.fromNBT(factory.createCompound(gunPropsNbtString).getCompound("Gun").asMinecraft());

        EntityGunProjectile projectile = new EntityGunProjectile(entityLivingBase.world, gunProps, gunProps.projectileMorph);

        projectile.setPosition(x, y, z);
        projectile.shoot(entityLivingBase, (float) pitch, (float) yaw, 0, gunProps.speed, 0);
        projectile.setInitialMotion();
        entityLivingBase.world.spawnEntity(projectile);

        return ScriptEntity.create(projectile);
    }
}
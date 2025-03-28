package mchorse.mappet.api.scripts.code.world;

import com.google.common.collect.ImmutableList;
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
import mchorse.mappet.api.scripts.code.nbt.ScriptNBTCompound;
import mchorse.mappet.api.scripts.user.IScriptRayTrace;
import mchorse.mappet.api.scripts.user.world.IScriptWorld;
import mchorse.mappet.api.scripts.user.blocks.IScriptBlockState;
import mchorse.mappet.api.scripts.user.blocks.IScriptTileEntity;
import mchorse.mappet.api.scripts.user.data.ScriptVector;
import mchorse.mappet.api.scripts.user.entities.IScriptEntity;
import mchorse.mappet.api.scripts.user.entities.IScriptEntityItem;
import mchorse.mappet.api.scripts.user.entities.IScriptNpc;
import mchorse.mappet.api.scripts.user.entities.IScriptPlayer;
import mchorse.mappet.api.scripts.user.items.IScriptInventory;
import mchorse.mappet.api.scripts.user.items.IScriptItemStack;
import mchorse.mappet.api.scripts.user.nbt.INBTCompound;
import mchorse.mappet.api.utils.RayTracing;
import mchorse.mappet.client.morphs.WorldMorph;
import mchorse.mappet.entities.EntityNpc;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.common.scripts.PacketWorldMorph;
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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
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
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class ScriptWorld implements IScriptWorld {
    public static final int MAX_VOLUME = 100;

    private final World world;
    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    public ScriptWorld(World world) {
        this.world = world;
    }

    @Override
    public World getMinecraftWorld() {
        return this.world;
    }

    @Override
    public void setGameRule(String gameRule, Object value) {
        if (value instanceof Boolean || value instanceof String || value instanceof Integer) {
            this.world.getGameRules().setOrCreateGameRule(gameRule, String.valueOf(value));
        } else {
            throw new IllegalArgumentException("Unsupported game rule value type: " + value.getClass());
        }
    }

    @Override
    public Object getGameRule(String gameRule) {
        if (this.world.getGameRules().hasRule(gameRule)) {
            String value = this.world.getGameRules().getString(gameRule);

            if (value.equals("true") || value.equals("false")) {
                return Boolean.valueOf(value);
            } else {
                try {
                    return Integer.valueOf(value);
                } catch (NumberFormatException e) {
                    return value;
                }
            }
        } else {
            throw new IllegalArgumentException("No such game rule: " + gameRule);
        }
    }

    @Override
    public void setBlock(IScriptBlockState state, ScriptVector pos) {
        setBlock(state, (int) pos.x, (int) pos.y, (int) pos.z);
    }

    @Override
    public void setBlock(IScriptBlockState state, int x, int y, int z) {
        if (!this.world.isBlockLoaded(this.pos.setPos(x, y, z))) {
            return;
        }

        this.world.setBlockState(this.pos, state.getMinecraftBlockState(), 2 | 4);
    }

    @Override
    public void removeBlock(int x, int y, int z) {
        if (!this.world.isBlockLoaded(this.pos.setPos(x, y, z))) {
            return;
        }

        this.world.setBlockToAir(this.pos);
    }

    @Override
    public IScriptBlockState getBlock(int x, int y, int z) {
        if (!this.world.isBlockLoaded(this.pos.setPos(x, y, z))) {
            return ScriptBlockState.AIR;
        }

        IBlockState blockState = this.world.getBlockState(this.pos);

        return ScriptBlockState.create(blockState.getActualState(this.world, this.pos));
    }

    @Override
    public IScriptBlockState getBlock(ScriptVector pos) {
        return getBlock((int) pos.x, (int) pos.y, (int) pos.z);
    }

    @Override
    public boolean hasTileEntity(int x, int y, int z) {
        if (!this.world.isBlockLoaded(this.pos.setPos(x, y, z))) {
            return false;
        }

        return this.world.getTileEntity(this.pos) != null;
    }


    //@Override
    public void replaceBlocks(IScriptBlockState blockToBeReplaced, IScriptBlockState newBlock, Vector3d pos, int radius) {
        processBlocksInRegion(pos, radius, (x, y, z) ->
        {
            IScriptBlockState currentBlock = getBlock(x, y, z);

            if (currentBlock.isSame(blockToBeReplaced)) {
                setBlock(newBlock, x, y, z);
            }
        });
    }

    //@Override
    public void replaceBlocks(IScriptBlockState blockToBeReplaced, IScriptBlockState newBlock, INBTCompound tileData, Vector3d pos, int radius) {
        processBlocksInRegion(pos, radius, (x, y, z) ->
        {
            IScriptBlockState currentBlock = getBlock(x, y, z);

            if (currentBlock.isSame(blockToBeReplaced)) {
                setTileEntity(x, y, z, newBlock, tileData);
            }
        });
    }

    private void processBlocksInRegion(Vector3d pos, int radius, BlockPosConsumer consumer) {
        int minX = (int) Math.floor(pos.x - radius);
        int maxX = (int) Math.ceil(pos.x + radius);
        int minY = (int) Math.floor(pos.y - radius);
        int maxY = (int) Math.ceil(pos.y + radius);
        int minZ = (int) Math.floor(pos.z - radius);
        int maxZ = (int) Math.ceil(pos.z + radius);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (!this.world.isBlockLoaded(this.pos.setPos(x, y, z))) {
                        continue;
                    }

                    double dx = pos.x - (x + 0.5);
                    double dy = pos.y - (y + 0.5);
                    double dz = pos.z - (z + 0.5);
                    double distanceSquared = dx * dx + dy * dy + dz * dz;

                    if (distanceSquared <= (radius * radius)) {
                        consumer.accept(x, y, z);
                    }
                }
            }
        }
    }

    @Override
    public void replaceBlocks(IScriptBlockState blockToBeReplaced, IScriptBlockState newBlock, Vector3d pos1, Vector3d pos2) {
        processBlocksInRegion(pos1, pos2, (x, y, z) ->
        {
            IScriptBlockState currentBlock = getBlock(x, y, z);

            if (currentBlock.isSame(blockToBeReplaced)) {
                setBlock(newBlock, x, y, z);
            }
        });
    }

    @Override
    public void replaceBlocks(IScriptBlockState blockToBeReplaced, IScriptBlockState newBlock, INBTCompound tileData, Vector3d pos1, Vector3d pos2) {
        processBlocksInRegion(pos1, pos2, (x, y, z) ->
        {
            IScriptBlockState currentBlock = getBlock(x, y, z);

            if (currentBlock.isSame(blockToBeReplaced)) {
                setTileEntity(x, y, z, newBlock, tileData);
            }
        });
    }

    private void processBlocksInRegion(Vector3d pos1, Vector3d pos2, BlockPosConsumer consumer) {
        for (int x = (int) Math.min(pos1.x, pos2.x); x <= Math.max(pos1.x, pos2.x); x++) {
            for (int y = (int) Math.min(pos1.y, pos2.y); y <= Math.max(pos1.y, pos2.y); y++) {
                for (int z = (int) Math.min(pos1.z, pos2.z); z <= Math.max(pos1.z, pos2.z); z++) {
                    if (!this.world.isBlockLoaded(this.pos.setPos(x, y, z))) {
                        continue;
                    }

                    consumer.accept(x, y, z);
                }
            }
        }
    }

    @FunctionalInterface
    private interface BlockPosConsumer {
        void accept(int x, int y, int z);
    }

    @Override
    public IScriptTileEntity getTileEntity(int x, int y, int z) {
        if (!this.hasTileEntity(x, y, z)) {
            return null;
        }

        return new ScriptTileEntity(this.world.getTileEntity(this.pos.setPos(x, y, z)));
    }

    @Override
    public boolean hasInventory(int x, int y, int z) {
        this.pos.setPos(x, y, z);

        return this.world.isBlockLoaded(this.pos) && this.world.getTileEntity(this.pos) instanceof IInventory;
    }

    @Override
    public IScriptInventory getInventory(int x, int y, int z) {
        if (this.world.isBlockLoaded(this.pos.setPos(x, y, z))) {
            TileEntity tile = this.world.getTileEntity(this.pos);

            if (tile instanceof IInventory) {
                return new ScriptInventory((IInventory) tile);
            }
        }

        return null;
    }

    @Override
    public boolean isRaining() {
        return this.world.getWorldInfo().isRaining();
    }

    @Override
    public void setRaining(boolean raining) {
        this.world.getWorldInfo().setRaining(raining);
    }

    @Override
    public long getTime() {
        return this.world.getWorldTime();
    }

    @Override
    public void setTime(long time) {
        this.world.setWorldTime(time);
    }

    @Override
    public long getTotalTime() {
        return this.world.getTotalWorldTime();
    }

    @Override
    public int getDimensionId() {
        Integer[] ids = DimensionManager.getIDs();
        MinecraftServer server = this.world.getMinecraftServer();
        if (server == null) return 0;
        for (int id : ids) if (server.getWorld(id) == this.world) return id;
        return 0;
    }

    @Override
    public void spawnParticles(EnumParticleTypes type, boolean longDistance, double x, double y, double z, int n, double dx, double dy, double dz, double speed, int... args) {
        ((WorldServer) this.world).spawnParticle(type, longDistance, x, y, z, n, dx, dy, dz, speed, args);
    }

    @Override
    public void spawnParticles(IScriptPlayer entity, EnumParticleTypes type, boolean longDistance, double x, double y, double z, int n, double dx, double dy, double dz, double speed, int... args) {
        if (entity == null) {
            return;
        }

        ((WorldServer) this.world).spawnParticle(entity.getMinecraftPlayer(), type, longDistance, x, y, z, n, dx, dy, dz, speed, args);
    }

    @Override
    public IScriptEntity spawnEntity(String id, double x, double y, double z, INBTCompound compound) {
        if (!this.world.isBlockLoaded(this.pos.setPos(x, y, z))) {
            return null;
        }

        NBTTagCompound tag = new NBTTagCompound();

        if (compound != null) {
            tag.merge(compound.getNBTTagCompound());
        }

        tag.setString("id", id);

        Entity entity = AnvilChunkLoader.readWorldEntityPos(tag, this.world, x, y, z, true);

        return entity == null ? null : ScriptEntity.create(entity);
    }

    @Override
    public IScriptNpc spawnNpc(String id, String state, double x, double y, double z) {
        Npc npc = Mappet.npcs.load(id);

        if (npc == null) {
            return null;
        }

        NpcState npcState = npc.states.get(state);

        if (npcState == null) {
            return null;
        }

        EntityNpc entity = new EntityNpc(this.world);

        entity.setPosition(x, y, z);
        entity.setNpc(npc, npcState);

        entity.world.spawnEntity(entity);
        entity.initialize();

        if (!npc.serializeNBT().getString("StateName").equals("default")) {
            entity.setStringInData("StateName", state);
        }

        return new ScriptNpc(entity);
    }

    @Override
    public IScriptNpc spawnNpc(String id, String state, double x, double y, double z, float yaw, float pitch, float headYaw) {
        Npc npc = Mappet.npcs.load(id);

        if (npc == null) {
            return null;
        }

        NpcState npcState = npc.states.get(state);

        if (npcState == null) {
            return null;
        }

        EntityNpc entity = new EntityNpc(this.world);

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
    public List<IScriptEntity> getEntities(double x1, double y1, double z1, double x2, double y2, double z2) {
        return getEntities(x1, y1, z1, x2, y2, z2, false);
    }

    @Override
    public List<IScriptEntity> getEntities(double x1, double y1, double z1, double x2, double y2, double z2, boolean ignoreVolumeLimit) {
        List<IScriptEntity> entities = new ArrayList<>();

        double minX = Math.min(x1, x2);
        double minY = Math.min(y1, y2);
        double minZ = Math.min(z1, z2);
        double maxX = Math.max(x1, x2);
        double maxY = Math.max(y1, y2);
        double maxZ = Math.max(z1, z2);

        if (!ignoreVolumeLimit && (maxX - minX > MAX_VOLUME || maxY - minY > MAX_VOLUME || maxZ - minZ > MAX_VOLUME)) {
            return entities;
        }

        int minChunkX = ((int) minX) >> 4;
        int minChunkZ = ((int) minZ) >> 4;
        int maxChunkX = ((int) maxX) >> 4;
        int maxChunkZ = ((int) maxZ) >> 4;

        Predicate<Entity> filter = Objects::nonNull;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (this.world.isChunkGeneratedAt(chunkX, chunkZ)) {
                    Chunk chunk = this.world.getChunkFromChunkCoords(chunkX, chunkZ);
                    AxisAlignedBB chunkAABB = new AxisAlignedBB(
                            Math.max(chunk.getPos().getXStart(), minX),
                            minY,
                            Math.max(chunk.getPos().getZStart(), minZ),
                            Math.min(chunk.getPos().getXEnd(), maxX),
                            maxY,
                            Math.min(chunk.getPos().getZEnd(), maxZ)
                    );

                    List<Entity> chunkEntities = new ArrayList<>();
                    chunk.getEntitiesWithinAABBForEntity(null, chunkAABB, chunkEntities, filter::test);
                    for (Entity entity : chunkEntities) {
                        entities.add(ScriptEntity.create(entity));
                    }
                }
            }
        }

        return entities;
    }

    @Override
    public List<IScriptEntity> getEntities(double x, double y, double z, double radius) {
        radius = Math.abs(radius);
        List<IScriptEntity> entities = new ArrayList<>();

        if (radius > (double) MAX_VOLUME / 2) {
            return entities;
        }

        double minX = x - radius;
        double minY = y - radius;
        double minZ = z - radius;
        double maxX = x + radius;
        double maxY = y + radius;
        double maxZ = z + radius;

        if (!this.world.isBlockLoaded(this.pos.setPos(minX, minY, minZ)) || !this.world.isBlockLoaded(this.pos.setPos(maxX, maxY, maxZ))) {
            return entities;
        }

        for (Entity entity : this.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ))) {
            AxisAlignedBB box = entity.getEntityBoundingBox();
            double eX = (box.minX + box.maxX) / 2D;
            double eY = (box.minY + box.maxY) / 2D;
            double eZ = (box.minZ + box.maxZ) / 2D;

            double dX = x - eX;
            double dY = y - eY;
            double dZ = z - eZ;

            if (dX * dX + dY * dY + dZ * dZ < radius * radius) {
                entities.add(ScriptEntity.create(entity));
            }
        }

        return entities;
    }

    @Override
    public void playSound(String event, double x, double y, double z, float volume, float pitch) {
        MinecraftServer server = this.world.getMinecraftServer();
        if (server == null) return;
        for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            WorldUtils.playSound(player, event, x, y, z, volume, pitch);
        }
    }

    @Override
    public void stopSound(String event, String category) {
        MinecraftServer server = this.world.getMinecraftServer();
        if (server == null) return;
        PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());

        packetbuffer.writeString(category);
        packetbuffer.writeString(event);

        for (EntityPlayerMP player : this.world.getMinecraftServer().getPlayerList().getPlayers()) {
            player.connection.sendPacket(new SPacketCustomPayload("MC|StopSound", packetbuffer));
        }
    }

    @Override
    public IScriptEntityItem dropItemStack(IScriptItemStack stack, double x, double y, double z, double mx, double my, double mz) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }

        EntityItem item = new EntityItem(this.world, x, y, z, stack.getMinecraftItemStack().copy());

        item.motionX = mx;
        item.motionY = my;
        item.motionZ = mz;

        this.world.spawnEntity(item);

        return (IScriptEntityItem) ScriptEntity.create(item);
    }

    @Override
    public void explode(IScriptEntity exploder, double x, double y, double z, float distance, boolean blazeGround, boolean destroyTerrain) {
        this.world.newExplosion(exploder == null ? null : exploder.getMinecraftEntity(), x, y, z, distance, blazeGround, destroyTerrain);
    }

    @Override
    public IScriptRayTrace rayTrace(double x1, double y1, double z1, double x2, double y2, double z2) {
        return new ScriptRayTrace(RayTracing.rayTraceWithEntity(this.world, x1, y1, z1, x2, y2, z2));
    }

    @Override
    public IScriptRayTrace rayTraceBlock(double x1, double y1, double z1, double x2, double y2, double z2) {
        return new ScriptRayTrace(RayTracing.rayTrace(this.world, x1, y1, z1, x2, y2, z2));
    }

    @Override
    public boolean isActive(int x, int y, int z) {
        return this.world.getRedstonePower(new BlockPos(x, y, z), EnumFacing.UP) > 0;
    }

    @Override
    public boolean testForBlock(int x, int y, int z, String blockId, int meta) {
        Block value = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
        if (value == null) return false;
        ImmutableList<IBlockState> validStates = value.getBlockState().getValidStates();
        IBlockState state = meta >= 0 && meta < validStates.size() ? validStates.get(meta) : value.getDefaultState();

        return this.getBlock(x, y, z).isSame(ScriptBlockState.create(state));
    }

    @Override
    public void fill(IScriptBlockState state, int x1, int y1, int z1, int x2, int y2, int z2) {
        int xMin = Math.min(x1, x2);
        int xMax = Math.max(x1, x2);
        int yMin = Math.min(y1, y2);
        int yMax = Math.max(y1, y2);
        int zMin = Math.min(z1, z2);
        int zMax = Math.max(z1, z2);

        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                for (int z = zMin; z <= zMax; z++) {
                    this.setBlock(state, x, y, z);
                }
            }
        }
    }

    @Override
    public IScriptEntity summonFallingBlock(double x, double y, double z, String blockId, int meta) {
        NBTTagCompound nbt = new NBTTagCompound();

        nbt.setString("Block", blockId);
        nbt.setInteger("Data", meta);
        nbt.setInteger("Time", 1);

        return this.spawnEntity("minecraft:falling_block", x, y, z, new ScriptNBTCompound(nbt));
    }

    @Override
    public IScriptEntity setFallingBlock(int x, int y, int z) {
        IScriptBlockState state = getBlock(x, y, z);

        if (state.isAir()) {
            return null;
        }

        NBTTagCompound nbt = new NBTTagCompound();

        nbt.setString("Block", state.getBlockId());
        nbt.setInteger("Data", state.getMeta());
        nbt.setInteger("Time", 1);

        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(state.getBlockId()));

        if (block != null) {
            IBlockState blockState = block.getStateFromMeta(state.getMeta());

            for (IProperty<?> property : blockState.getProperties().keySet()) {
                nbt.setString(property.getName(), blockState.getValue(property).toString());
            }
        }

        if (hasTileEntity(x, y, z)) {
            nbt.setTag("TileEntityData", this.getTileEntity(x, y, z).getData().getNBTTagCompound());
        }

        this.world.removeTileEntity(this.pos.setPos(x, y, z));
        this.world.setBlockState(this.pos.setPos(x, y, z), Blocks.AIR.getDefaultState(), 3);

        return this.spawnEntity("minecraft:falling_block", x + 0.5, y + 0.5, z + 0.5, new ScriptNBTCompound(nbt));
    }

    @Override
    public void setTileEntity(int x, int y, int z, IScriptBlockState blockState, INBTCompound tileData) {
        setBlock(blockState, x, y, z);
        tileData.setInt("x", x);
        tileData.setInt("y", y);
        tileData.setInt("z", z);
        getTileEntity(x, y, z).setData(tileData);
    }

    @Override
    public void fillTileEntities(int x1, int y1, int z1, int x2, int y2, int z2, IScriptBlockState blockState, INBTCompound tileData) {
        int xMin = Math.min(x1, x2);
        int xMax = Math.max(x1, x2);
        int yMin = Math.min(y1, y2);
        int yMax = Math.max(y1, y2);
        int zMin = Math.min(z1, z2);
        int zMax = Math.max(z1, z2);

        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                for (int z = zMin; z <= zMax; z++) {
                    setTileEntity(x, y, z, blockState, tileData);
                }
            }
        }
    }

    @Override
    public void clone(int x, int y, int z, int xNew, int yNew, int zNew) {
        IScriptBlockState state = getBlock(x, y, z);

        if (!state.getBlockId().equals("minecraft:air")) {
            setBlock(state, xNew, yNew, zNew);

            if (getTileEntity(x, y, z) != null) {
                INBTCompound tile = getTileEntity(x, y, z).getData();
                tile.setInt("x", xNew);
                tile.setInt("y", yNew);
                tile.setInt("z", zNew);
                setBlock(state, x, y, z);
                getTileEntity(xNew, yNew, zNew).setData(tile);
            }
        }
    }

    @Override
    public void clone(int x1, int y1, int z1, int x2, int y2, int z2, int xNew, int yNew, int zNew) {
        int xMin = Math.min(x1, x2);
        int xMax = Math.max(x1, x2);
        int yMin = Math.min(y1, y2);
        int yMax = Math.max(y1, y2);
        int zMin = Math.min(z1, z2);
        int zMax = Math.max(z1, z2);
        int xCentre = (xMin + xMax) / 2;
        int yCentre = (yMin + yMax) / 2;
        int zCentre = (zMin + zMax) / 2;

        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                for (int z = zMin; z <= zMax; z++) {
                    clone(x, y, z, xNew + x - xCentre, yNew + y - yCentre, zNew + z - zCentre);
                }
            }
        }
    }

    @Override
    public IScriptItemStack getBlockStackWithTile(int x, int y, int z) {
        if (!this.world.isBlockLoaded(this.pos.setPos(x, y, z))) {
            return ScriptItemStack.EMPTY;
        }

        IBlockState blockState = this.world.getBlockState(this.pos);
        Block block = blockState.getBlock();

        if (block == Blocks.AIR) {
            return ScriptItemStack.EMPTY;
        }

        ItemStack itemStack;
        Item itemFromBlock = Item.getItemFromBlock(block);

        if (itemFromBlock == Items.AIR) {
            itemStack = block.getItem(this.world, this.pos, blockState);
            if (itemStack.isEmpty()) return ScriptItemStack.EMPTY;
        } else {
            itemStack = new ItemStack(itemFromBlock, 1, block.getMetaFromState(blockState));
        }

        TileEntity tileEntity = this.world.getTileEntity(this.pos);

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
        return world.getLight(new BlockPos(x, y, z));
    }

    /* Mappet stuff */

    @Override
    public void displayMorph(AbstractMorph morph, int expiration, double x, double y, double z, float yaw, float pitch, int range, IScriptPlayer player) {
        if (morph == null) {
            return;
        }

        WorldMorph worldMorph = new WorldMorph();

        worldMorph.morph = morph;
        worldMorph.expiration = expiration;
        worldMorph.x = x;
        worldMorph.y = y;
        worldMorph.z = z;
        worldMorph.yaw = yaw;
        worldMorph.pitch = pitch;

        int dimension = this.world.provider.getDimension();

        if (player == null) {
            NetworkRegistry.TargetPoint point = new NetworkRegistry.TargetPoint(dimension, x, y, z, MathUtils.clamp(range, 1, 256));

            Dispatcher.DISPATCHER.get().sendToAllAround(new PacketWorldMorph(worldMorph), point);
        } else {
            Dispatcher.sendTo(new PacketWorldMorph(worldMorph), player.getMinecraftPlayer());
        }
    }

    @Override
    public MappetSchematic createSchematic() {
        return MappetSchematic.create(this);
    }

    @Override
    public ScriptStructure loadStructure(String name){
        return new ScriptStructure(this, name);
    }

//    public TemplateManager getStructureManager(){
//        if(!(world instanceof WorldServer)) return null;
//        return ((WorldServer) world).getStructureTemplateManager();
//    }


    /* BlockBuster stuff */

    @Override
    public IScriptEntity shootBBGunProjectile(IScriptEntity shooter, double x, double y, double z, double yaw, double pitch, String gunPropsNbtString) {
        if (shooter.getMinecraftEntity() instanceof EntityLivingBase && Loader.isModLoaded("blockbuster")) {
            try {
                return this.shootBBGunProjectileMethod(shooter, x, y, z, yaw, pitch, gunPropsNbtString);
            } catch (Exception e) {
                Mappet.logger.error(e.getMessage());
            }
        }
        return null;
    }

    @Optional.Method(modid = "blockbuster")
    private IScriptEntity shootBBGunProjectileMethod(IScriptEntity shooter, double x, double y, double z, double yaw, double pitch, String gunPropsNbtString) {
        ScriptFactory factory = new ScriptFactory();

        EntityLivingBase entityLivingBase = (EntityLivingBase) shooter.getMinecraftEntity();
        GunProps gunProps = new GunProps((factory.createCompound(gunPropsNbtString)).getCompound("Gun").getCompound("Projectile").getNBTTagCompound());

        gunProps.fromNBT(factory.createCompound(gunPropsNbtString).getCompound("Gun").getNBTTagCompound());

        EntityGunProjectile projectile = new EntityGunProjectile(entityLivingBase.world, gunProps, gunProps.projectileMorph);

        projectile.setPosition(x, y, z);
        projectile.shoot(entityLivingBase, (float) pitch, (float) yaw, 0, gunProps.speed, 0);
        projectile.setInitialMotion();
        entityLivingBase.world.spawnEntity(projectile);

        return ScriptEntity.create(projectile);
    }
}
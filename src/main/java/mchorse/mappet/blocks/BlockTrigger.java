package mchorse.mappet.blocks;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.utils.DataContext;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.common.blocks.PacketEditTrigger;
import mchorse.mappet.tile.TileTrigger;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class BlockTrigger extends Block implements ITileEntityProvider {
    public static final PropertyBool COLLIDABLE = PropertyBool.create("collidable");

    public BlockTrigger() {
        super(Material.ROCK);
        this.setCreativeTab(Mappet.creativeTab);
        this.setBlockUnbreakable();
        this.setResistance(6000000.0F);
        this.setRegistryName(new ResourceLocation(Mappet.MOD_ID, "trigger"));
        this.setUnlocalizedName(Mappet.MOD_ID + ".trigger");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add(I18n.format("tile.mappet.trigger.tooltip"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
        return true;
    }

    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn) {
        if (!worldIn.isRemote && !playerIn.isCreative()) {
            TileEntity tile = worldIn.getTileEntity(pos);

            if (tile instanceof TileTrigger) {
                ((TileTrigger) tile).leftClick.trigger(new DataContext(playerIn)
                        .set("x", pos.getX())
                        .set("y", pos.getY())
                        .set("z", pos.getZ()));
            }
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) return true;

        TileEntity tile = worldIn.getTileEntity(pos);

        if (!(tile instanceof TileTrigger)) return true;
        TileTrigger trigger = (TileTrigger) tile;

        MinecraftServer server = playerIn.getServer();
        if (server != null && server.getPlayerList().canSendCommands(playerIn.getGameProfile()) && playerIn.isCreative() && !playerIn.isSneaking()) {
            Dispatcher.sendTo(new PacketEditTrigger(trigger), (EntityPlayerMP) playerIn);
        } else {
            trigger.rightClick.trigger(new DataContext(playerIn)
                    .set("x", pos.getX())
                    .set("y", pos.getY())
                    .set("z", pos.getZ()));
        }
        return true;
    }


    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        if (!worldIn.isRemote && placer instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) placer;
            MinecraftServer server = player.getServer();
            if(server == null || !server.getPlayerList().canSendCommands(player.getGameProfile())){
                worldIn.setBlockToAir(pos);
                if (!player.isCreative() && !player.inventory.addItemStackToInventory(new ItemStack(this))) {
                    player.dropItem(new ItemStack(this), false);
                }
                return;
            }
        }
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }

    /* Meta */

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(COLLIDABLE) ? 1 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(COLLIDABLE, meta == 1);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, COLLIDABLE);
    }

    /* Make the block walkable through and invisible */

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean canSpawnInBlock() {
        return true;
    }

    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return blockState.getValue(COLLIDABLE) ? getCustomBoundingBox(worldIn, pos) : Block.NULL_AABB;
    }

    public AxisAlignedBB getCustomBoundingBox(IBlockAccess worldIn, BlockPos pos) {
        TileEntity tile = worldIn.getTileEntity(pos);

        if (!(tile instanceof TileTrigger)) {
            return Block.FULL_BLOCK_AABB;
        }

        TileTrigger tileTrigger = (TileTrigger) tile;

        Vec3d pos1 = tileTrigger.boundingBoxPos1;
        Vec3d pos2 = tileTrigger.boundingBoxPos2;

        if (pos1.equals(pos2)) {
            return Block.FULL_BLOCK_AABB;
        }

        return new AxisAlignedBB(pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return getCustomBoundingBox(source, pos);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileTrigger();
    }
}
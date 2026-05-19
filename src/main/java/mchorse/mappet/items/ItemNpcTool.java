package mchorse.mappet.items;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.npcs.Npc;
import mchorse.mappet.api.npcs.NpcState;
import mchorse.mappet.config.MappetConfig;
import mchorse.mappet.entities.EntityNpc;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.packets.npc.PacketNpcList;
import mchorse.mappet.network.packets.npc.PacketNpcState;
import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ItemNpcTool extends Item {
    public ItemNpcTool() {
        setCreativeTab(ModItems.creativeTab);
        setMaxStackSize(1);
        setRegistryName(new ResourceLocation(Mappet.MOD_ID, "npc_tool"));
        setUnlocalizedName(Mappet.MOD_ID + ".npc_tool");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(I18n.format("item.mappet.npc_tool.tooltip"));
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase target, EnumHand hand) {
        if (player.world.isRemote || !(target instanceof EntityNpc) || !MappetConfig.npcToolRight.check(player))
            return super.itemInteractionForEntity(stack, player, target, hand);

        EntityNpc npc = (EntityNpc) target;
        if (player.isSneaking()) npc.setDead();
        else Dispatcher.sendTo(new PacketNpcState(target.getEntityId(), npc.getState().serializeNBT()), (EntityPlayerMP) player);

        return true;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (world.isRemote || !MappetConfig.npcToolRight.check(player)) return super.onItemRightClick(world, player, hand);
        if (openNpcTool(player, player.getHeldItem(hand))) return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
        return super.onItemRightClick(world, player, hand);
    }

    private boolean openNpcTool(EntityPlayer player, ItemStack stack) {
        Collection<String> npcs = Mappet.npcs.getIDs();

        if (!npcs.isEmpty() && player instanceof EntityPlayerMP) {
            List<String> states = new ArrayList<>();

            try {
                NBTTagCompound tag = stack.getTagCompound();
                if (tag != null) states.addAll(Mappet.npcs.load(tag.getString("Npc")).states.keySet());
            } catch (Exception ignored) {
            }

            Dispatcher.sendTo(new PacketNpcList(npcs, states), (EntityPlayerMP) player);

            return true;
        }

        return false;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);

        if (world.isRemote) return EnumActionResult.SUCCESS;
        if (!MappetConfig.npcToolRight.check(player)) return EnumActionResult.PASS;

        EntityNpc entity = new EntityNpc(world);
        BlockPos posOffset = pos.offset(facing);

        entity.setPosition(posOffset.getX() + hitX, posOffset.getY() + hitY, posOffset.getZ() + hitZ);

        setupState(entity, stack);

        entity.world.spawnEntity(entity);
        entity.initialize();

        if (!player.isSneaking())
            Dispatcher.sendTo(new PacketNpcState(entity.getEntityId(), entity.getState().serializeNBT()), (EntityPlayerMP) player);

        //        return stack.getItem() == NPC_TOOL ? EnumActionResult.SUCCESS : super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
        return EnumActionResult.SUCCESS;
    }

    private void setupState(EntityNpc entity, ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();

        if (tag != null) {
            String npcId = tag.getString("Npc");
            String stateId = tag.getString("State");

            Npc npc = Mappet.npcs.load(npcId);
            NpcState state = npc == null ? null : npc.states.get(stateId);

            if (npc != null && state == null && npc.states.containsKey("default")) {
                state = npc.states.get("default");
            }

            if (state != null) {
                entity.setNpc(npc, state);

                if (!npc.serializeNBT().getString("StateName").equals("default")) {
                    entity.setStringInData("StateName", stateId);
                }
            }
        }
        else {
            tag = new NBTTagCompound();
            tag.setString("Name", "blockbuster.fred");

            AbstractMorph morph = MorphManager.INSTANCE.morphFromNBT(tag);

            entity.getState().morph = morph;
            entity.setMorph(morph);
        }
    }
}
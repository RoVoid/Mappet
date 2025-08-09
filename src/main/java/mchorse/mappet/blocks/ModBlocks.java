package mchorse.mappet.blocks;

import mchorse.mappet.Mappet;
import mchorse.mappet.items.ModItems;
import mchorse.mappet.tile.TileConditionModel;
import mchorse.mappet.tile.TileEmitter;
import mchorse.mappet.tile.TileRegion;
import mchorse.mappet.tile.TileTrigger;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModBlocks {

    public static BlockEmitter EMITTER = new BlockEmitter();

    public static BlockTrigger TRIGGER = new BlockTrigger();

    public static BlockRegion REGION = new BlockRegion();

    public static BlockConditionModel CONDITION_MODEL = new BlockConditionModel();

    public static void register(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(ModItems.addItemBlock(EMITTER));
        event.getRegistry().register(ModItems.addItemBlock(TRIGGER));
        event.getRegistry().register(ModItems.addItemBlock(CONDITION_MODEL));
        event.getRegistry().register(ModItems.addItemBlock(REGION));
    }

    public static void bindEntities(RegistryEvent.Register<EntityEntry> event) {
        GameRegistry.registerTileEntity(TileEmitter.class, new ResourceLocation(Mappet.MOD_ID, "emitter"));
        GameRegistry.registerTileEntity(TileTrigger.class, new ResourceLocation(Mappet.MOD_ID, "trigger"));
        GameRegistry.registerTileEntity(TileRegion.class, new ResourceLocation(Mappet.MOD_ID, "region"));
        GameRegistry.registerTileEntity(TileConditionModel.class, new ResourceLocation(Mappet.MOD_ID, "condition_model"));
    }
}

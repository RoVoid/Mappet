package mchorse.mappet.events.handlers;

import mchorse.mappet.Mappet;
import mchorse.mappet.blocks.ModBlocks;
import mchorse.mappet.entities.EntityNpc;
import mchorse.mappet.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * One-time Forge registry events fired during mod loading (blocks, items,
 * entity types, models). Formerly half of {@code ForgeEventHandler}; the
 * other half (client connect/disconnect session lifecycle) moved to
 * {@link ClientSessionEventHandler} since it belongs to a completely
 * different lifecycle — "once at mod load" vs "once per server
 * connection".
 */
public class RegistrationEventHandler {
    @SubscribeEvent
    public void onBlocksRegister(RegistryEvent.Register<Block> event) {
        ModBlocks.register(event);
    }

    @SubscribeEvent
    public void onItemsRegister(RegistryEvent.Register<Item> event) {
        ModItems.register(event);
    }

    @SubscribeEvent
    public void onEntityRegister(RegistryEvent.Register<EntityEntry> event) {
        event
                .getRegistry()
                .register(EntityEntryBuilder
                        .create()
                        .entity(EntityNpc.class)
                        .name(Mappet.MOD_ID + ".npc")
                        .id(new ResourceLocation(Mappet.MOD_ID, "npc"), 0)
                        .tracker(EntityNpc.RENDER_DISTANCE, 3, false)
                        .build());
        ModBlocks.bindEntities(event);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onModelRegistry(ModelRegistryEvent event) {
        ModItems.bindModels(event);
    }
}

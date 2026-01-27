package mchorse.mappet;

import mchorse.mappet.api.utils.IContentType;
import mchorse.mappet.client.KeyboardHandler;
import mchorse.mappet.client.RenderingHandler;
import mchorse.mappet.client.ResourceReloadHandler;
import mchorse.mappet.client.SoundPack;
import mchorse.mappet.client.gui.scripts.themes.Themes;
import mchorse.mappet.client.renders.entity.RenderNpc;
import mchorse.mappet.client.renders.tile.TileConditionModelRenderer;
import mchorse.mappet.client.renders.tile.TileRegionRenderer;
import mchorse.mappet.client.renders.tile.TileTriggerRenderer;
import mchorse.mappet.entities.EntityNpc;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.packets.content.PacketContentRequestNames;
import mchorse.mappet.tile.TileConditionModel;
import mchorse.mappet.tile.TileRegion;
import mchorse.mappet.tile.TileTrigger;
import mchorse.mclib.McLib;
import mchorse.mclib.utils.ReflectionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    private static int requestId = 0;
    private static final Map<Integer, Consumer<List<String>>> consumers = new HashMap<>();

    public static File sounds;

    @Override
    public void runClient(Runnable task) {
        Minecraft.getMinecraft().addScheduledTask(task);
    }

    public static void requestNames(IContentType type, Consumer<List<String>> consumer) {
        consumers.put(requestId, consumer);
        Dispatcher.sendToServer(new PacketContentRequestNames(type, requestId));

        requestId += 1;
    }

    public static void process(List<String> names, int id) {
        Consumer<List<String>> consumer = consumers.remove(id);

        if (consumer != null) {
            consumer.accept(names);
        }
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        RenderingHandler handler = new RenderingHandler();

        MinecraftForge.EVENT_BUS.register(new KeyboardHandler());
        MinecraftForge.EVENT_BUS.register(handler);
        McLib.EVENT_BUS.register(handler);

        ClientRegistry.bindTileEntitySpecialRenderer(TileTrigger.class, new TileTriggerRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileRegion.class, new TileRegionRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileConditionModel.class, new TileConditionModelRenderer());

        RenderingRegistry.registerEntityRenderingHandler(EntityNpc.class, new RenderNpc.Factory());

        ReflectionUtils.registerResourcePack(new SoundPack(sounds = new File(CommonProxy.configFolder, "sounds")));

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.getResourceManager() instanceof IReloadableResourceManager) {
            ((IReloadableResourceManager) mc.getResourceManager()).registerReloadListener(new ResourceReloadHandler());
        }

        Mappet.loggerClient = event.getModLog();

        Themes.initiate();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }
}
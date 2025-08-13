package mchorse.mappet;

import mchorse.mappet.api.ServerSettings;
import mchorse.mappet.api.data.DataManager;
import mchorse.mappet.api.dialogues.DialogueManager;
import mchorse.mappet.api.events.EventManager;
import mchorse.mappet.api.expressions.ExpressionManager;
import mchorse.mappet.api.factions.FactionManager;
import mchorse.mappet.api.huds.HUDManager;
import mchorse.mappet.api.npcs.NpcManager;
import mchorse.mappet.api.quests.QuestManager;
import mchorse.mappet.api.quests.chains.QuestChainManager;
import mchorse.mappet.api.schematics.SchematicManager;
import mchorse.mappet.api.scripts.ScriptManager;
import mchorse.mappet.api.states.States;
import mchorse.mappet.api.utils.DataContext;
import mchorse.mappet.api.utils.logs.MappetLogger;
import mchorse.mappet.client.gui.GuiMappetDashboard;
import mchorse.mappet.commands.CommandMappet;
import mchorse.mappet.utils.MPIcons;
import mchorse.mappet.utils.ScriptUtils;
import mchorse.mclib.McLib;
import mchorse.mclib.commands.utils.L10n;
import mchorse.mclib.events.RegisterConfigEvent;
import mchorse.mclib.events.RemoveDashboardPanels;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.logging.Handler;

/**
 * Mappet mod
 * <p>
 * Adventure map toolset mod
 */
@Mod(
        modid = Mappet.MOD_ID,
        name = "Mappet",
        version = Mappet.VERSION,
        dependencies =
                "required-after:mclib@[@MCLIB@,);" +
                        "required-after:metamorph@[@METAMORPH@,);" +
                        "after:blockbuster@[@BLOCKBUSTER@,);" +
                        "after:aperture@[@APERTURE@,);" +
                        "after:chameleon@[@CHAMELEON@,);",
        updateJSON = "https://raw.githubusercontent.com/mchorse/mappet/master/version.json"
)

public final class Mappet {
    public static final String MOD_ID = "mappet";

    public static final String VERSION = "@MAPPET@";

    @Mod.Instance
    public static Mappet instance;

    @SidedProxy(serverSide = "mchorse.mappet.CommonProxy", clientSide = "mchorse.mappet.ClientProxy")
    public static CommonProxy proxy;

    public static L10n l10n = new L10n(MOD_ID);

    public static final EventBus EVENT_BUS = new EventBus();

    public static MappetLogger logger;

    public static Logger loggerClient;

    /* Server side data */
    public static ServerSettings settings;

    public static States states;

    public static QuestManager quests;

    public static SchematicManager schematics;

    public static EventManager events;

    public static DialogueManager dialogues;

    public static ExpressionManager expressions;

    public static NpcManager npcs;

    public static FactionManager factions;

    public static DataManager data;

    public static QuestChainManager chains;

    public static ScriptManager scripts;

    public static HUDManager huds;

    public Mappet() {
        MinecraftForge.EVENT_BUS.register(new ModEventHandler());
    }

    @SubscribeEvent
    public void onConfigRegister(RegisterConfigEvent event) {
        MappetConfig.register(event);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onDashboardPanelsRemove(RemoveDashboardPanels event) {
        GuiMappetDashboard.dashboard = null;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        McLib.EVENT_BUS.register(this);
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandMappet());

        File mappetWorldFolder = new File(DimensionManager.getCurrentSaveRootDirectory(), MOD_ID);

        mappetWorldFolder.mkdirs();

        if (logger != null) {
            Handler[] handlers = logger.getHandlers();

            for (Handler handler : handlers) {
                handler.close();
                logger.removeHandler(handler);
            }

            logger = null;
        }

        logger = new MappetLogger(MOD_ID, mappetWorldFolder);

        settings = new ServerSettings(new File(mappetWorldFolder, "settings.json"));
        settings.load();
        states = new States(new File(mappetWorldFolder, "states.json"));
        states.load();

        quests = new QuestManager(new File(mappetWorldFolder, "quests"));
        schematics = new SchematicManager(new File(mappetWorldFolder, "schematics"));
        events = new EventManager(new File(mappetWorldFolder, "events"));
        dialogues = new DialogueManager(new File(mappetWorldFolder, "dialogues"));
        expressions = new ExpressionManager();
        npcs = new NpcManager(new File(mappetWorldFolder, "npcs"));
        factions = new FactionManager(new File(mappetWorldFolder, "factions"));
        data = new DataManager(new File(mappetWorldFolder, "data"));
        chains = new QuestChainManager(new File(mappetWorldFolder, "chains"));
        scripts = new ScriptManager(new File(mappetWorldFolder, "scripts"));
        huds = new HUDManager(new File(mappetWorldFolder, "huds"));

        /* Initiate */
        if (!settings.serverLoad.isEmpty()) {
            settings.serverLoad.trigger(new DataContext(event.getServer()));
        }

        ScriptUtils.initiateScriptEngines();
        scripts.initiateAllScripts();

        TriggerEventHandler.getRegisteredEvents();

        if (event.getServer().isDedicatedServer()) MPIcons.initiate();
    }

    @Mod.EventHandler
    public void serverStopped(FMLServerStoppedEvent event) {
        if (settings != null) {
            settings.save();
            settings = null;
            states.save();
            states = null;

            quests = null;
            events = null;
            dialogues = null;
            expressions = null;
            npcs = null;
            factions = null;
            data = null;
            chains = null;
            scripts = null;
            huds = null;
        }

        CommonProxy.eventHandler.reset();
    }
}
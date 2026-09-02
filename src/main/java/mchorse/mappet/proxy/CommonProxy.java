package mchorse.mappet.proxy;

import mchorse.mappet.Mappet;
import mchorse.mappet.MappetFactories;
import mchorse.mappet.api.conditions.blocks.AbstractConditionBlock;
import mchorse.mappet.api.scripts.engine.ScriptEngineRegistry;
import mchorse.mappet.api.triggers.blocks.AbstractTriggerBlock;
import mchorse.mappet.api.utils.MapFactory;
import mchorse.mappet.capabilities.character.Character;
import mchorse.mappet.capabilities.character.CharacterStorage;
import mchorse.mappet.capabilities.character.ICharacter;
import mchorse.mappet.events.handlers.ClientSessionEventHandler;
import mchorse.mappet.events.handlers.PlayerSessionHandler;
import mchorse.mappet.events.handlers.ScriptedItemEventHandler;
import mchorse.mappet.events.handlers.WorldTriggerHandler;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.utils.MappetNpcSelector;
import mchorse.mappet.utils.MetamorphHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.io.File;

public class CommonProxy {
    public static File configFolder;

    public static WorldTriggerHandler worldTriggerHandler;
    public static PlayerSessionHandler playerSessionHandler;
    public static ScriptedItemEventHandler scriptedItemEventHandler;

    public void runClient(Runnable task) {}

    public void preInit(FMLPreInitializationEvent event) {
        String path = event.getModConfigurationDirectory().getAbsolutePath();
        configFolder = new File(path, Mappet.MOD_ID);
        configFolder.mkdir();

        Dispatcher.register();

        MinecraftForge.EVENT_BUS.register(worldTriggerHandler = new WorldTriggerHandler());
        MinecraftForge.EVENT_BUS.register(playerSessionHandler = new PlayerSessionHandler());
        MinecraftForge.EVENT_BUS.register(scriptedItemEventHandler = new ScriptedItemEventHandler());

        MinecraftForge.EVENT_BUS.register(new ClientSessionEventHandler());

        GameRegistry.registerEntitySelector(new MappetNpcSelector(), MappetNpcSelector.ARGUMENT_MAPPET_NPC_ID,
                                            MappetNpcSelector.ARGUMENT_MAPPET_STATES);

        CapabilityManager.INSTANCE.register(ICharacter.class, new CharacterStorage(), Character::new);
    }

    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new MetamorphHandler());
        Mappet.EVENT_BUS.register(worldTriggerHandler);
        Mappet.EVENT_BUS.register(playerSessionHandler);

        ScriptEngineRegistry.initiateScriptEngines();
    }

    public void postInit(FMLPostInitializationEvent event) {
        MappetFactories.register();
    }

    @Deprecated
    public static MapFactory<AbstractConditionBlock> getConditionBlocks() {
        return MappetFactories.getConditionBlocks();
    }

    @Deprecated
    public static MapFactory<AbstractTriggerBlock> getTriggerBlocks() {
        return MappetFactories.getTriggerBlocks();
    }
}
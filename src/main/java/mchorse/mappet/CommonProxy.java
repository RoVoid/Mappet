package mchorse.mappet;

import mchorse.mappet.api.conditions.blocks.*;
import mchorse.mappet.api.dialogues.nodes.*;
import mchorse.mappet.api.events.nodes.*;
import mchorse.mappet.api.quests.chains.QuestNode;
import mchorse.mappet.api.triggers.blocks.*;
import mchorse.mappet.api.ui.components.*;
import mchorse.mappet.api.utils.factory.IFactory;
import mchorse.mappet.api.utils.factory.MapFactory;
import mchorse.mappet.capabilities.character.Character;
import mchorse.mappet.capabilities.character.CharacterStorage;
import mchorse.mappet.capabilities.character.ICharacter;
import mchorse.mappet.client.gui.utils.Beautifier;
import mchorse.mappet.events.*;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.utils.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.io.File;

/**
 * Common proxy
 */
public class CommonProxy {
    private static IFactory<EventBaseNode> events;
    private static IFactory<EventBaseNode> dialogues;
    private static IFactory<QuestNode> chains;
    private static IFactory<AbstractConditionBlock> conditionBlocks;
    private static IFactory<AbstractTriggerBlock> triggerBlocks;
    private static IFactory<UIComponent> uiComponents;

    public static IFactory<EventBaseNode> getEvents() {
        return events;
    }

    public static IFactory<EventBaseNode> getDialogues() {
        return dialogues;
    }

    public static IFactory<QuestNode> getChains() {
        return chains;
    }

    public static IFactory<AbstractConditionBlock> getConditionBlocks() {
        return conditionBlocks;
    }

    public static IFactory<AbstractTriggerBlock> getTriggerBlocks() {
        return triggerBlocks;
    }

    public static IFactory<UIComponent> getUiComponents() {
        return uiComponents;
    }

    /**
     * Client folder where saved selectors and animations are getting
     * stored.
     */
    public static File configFolder;

    public static EventHandler eventHandler;
    public static ScriptedItemEventHandler scriptedItemEventHandler;

    public void preInit(FMLPreInitializationEvent event) {
        /* Setup config folder path */
        String path = event.getModConfigurationDirectory().getAbsolutePath();
        configFolder = new File(path, Mappet.MOD_ID);
        configFolder.mkdir();

        Dispatcher.register();

        MinecraftForge.EVENT_BUS.register(eventHandler = new EventHandler());
        MinecraftForge.EVENT_BUS.register(scriptedItemEventHandler = new ScriptedItemEventHandler());

        GameRegistry.registerEntitySelector(new MappetNpcSelector(), MappetNpcSelector.ARGUMENT_MAPPET_NPC_ID, MappetNpcSelector.ARGUMENT_MAPPET_STATES);

        CapabilityManager.INSTANCE.register(ICharacter.class, new CharacterStorage(), Character::new);
    }

    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new MetamorphHandler());
        Mappet.EVENT_BUS.register(eventHandler);

        ScriptUtils.initiateScriptEngines();
        Beautifier.init();
    }


    public void postInit(FMLPostInitializationEvent event) {
        MPIcons.initiate();

        /* Register event nodes */
        MapFactory<EventBaseNode> eventNodes = new MapFactory<EventBaseNode>()
                .register("command", CommandNode.class, Colors.COMMAND)
                .register("comment", CommentNode.class, Colors.COMMENT)
                .register("condition", ConditionNode.class, Colors.CONDITION)
                .register("switch", SwitchNode.class, Colors.FACTION)
                .register("timer", TimerNode.class, Colors.TIME)
                .register("trigger", TriggerNode.class, Colors.STATE)
                /* Backward compatibility with gamma build */
                .alias("trigger", "event")
                .alias("trigger", "dialogue")
                .alias("trigger", "script")
                .register("cancel", CancelNode.class, Colors.CANCEL);

        events = eventNodes;
        Mappet.EVENT_BUS.post(new RegisterEventNodeEvent(eventNodes));

        /* Register dialogue nodes */
        MapFactory<EventBaseNode> dialogueNodes = eventNodes.copy()
                .register("reply", ReplyNode.class, Colors.REPLY)
                .register("reaction", ReactionNode.class, Colors.STATE)
                .register("crafting", CraftingNode.class, Colors.CRAFTING)
                .register("quest_chain", QuestChainNode.class, Colors.QUEST)
                .register("quest", QuestDialogueNode.class, Colors.QUEST)
                .unregister("timer");

        dialogues = dialogueNodes;
        Mappet.EVENT_BUS.post(new RegisterDialogueNodeEvent(dialogueNodes));

        /* Register quest chain blocks */
        MapFactory<QuestNode> questChainNodes = new MapFactory<QuestNode>()
                .register("quest", QuestNode.class, Colors.QUEST);

        chains = questChainNodes;
        Mappet.EVENT_BUS.post(new RegisterQuestChainNodeEvent(questChainNodes));

        /* Register condition blocks */
        MapFactory<AbstractConditionBlock> conditions = new MapFactory<AbstractConditionBlock>()
                .register("quest", QuestConditionBlock.class, Colors.QUEST)
                .register("state", StateConditionBlock.class, Colors.STATE)
                .register("dialogue", DialogueConditionBlock.class, Colors.DIALOGUE)
                .register("faction", FactionConditionBlock.class, Colors.FACTION)
                .register("item", ItemConditionBlock.class, Colors.CRAFTING)
                .register("world_time", WorldTimeConditionBlock.class, Colors.TIME)
                .register("entity", EntityConditionBlock.class, Colors.ENTITY)
                .register("condition", ConditionConditionBlock.class, Colors.CONDITION)
                .register("morph", MorphConditionBlock.class, Colors.MORPH)
                .register("expression", ExpressionConditionBlock.class, Colors.CANCEL);

        conditionBlocks = conditions;
        Mappet.EVENT_BUS.post(new RegisterConditionBlockEvent(conditions));

        /* Register condition blocks */
        MapFactory<AbstractTriggerBlock> triggers = new MapFactory<AbstractTriggerBlock>()
                .register("command", CommandTriggerBlock.class, Colors.COMMAND)
                .register("sound", SoundTriggerBlock.class, Colors.REPLY)
                .register("event", EventTriggerBlock.class, Colors.STATE)
                .register("dialogue", DialogueTriggerBlock.class, Colors.DIALOGUE)
                .register("script", ScriptTriggerBlock.class, Colors.ENTITY)
                .register("item", ItemTriggerBlock.class, Colors.CRAFTING)
                .register("state", StateTriggerBlock.class, Colors.STATE)
                .register("morph", MorphTriggerBlock.class, Colors.MORPH);

        triggerBlocks = triggers;
        Mappet.EVENT_BUS.post(new RegisterTriggerBlockEvent(triggers));

        /* Register UI components */
        MapFactory<UIComponent> ui = new MapFactory<UIComponent>()
                .register("graphics", UIGraphicsComponent.class, 0xffffff)
                .register("button", UIButtonComponent.class, 0xffffff)
                .register("icon", UIIconButtonComponent.class, 0xffffff)
                .register("label", UILabelComponent.class, 0xffffff)
                .register("text", UITextComponent.class, 0xffffff)
                .register("textbox", UITextboxComponent.class, 0xffffff)
                .register("textarea", UITextareaComponent.class, 0xffffff)
                .register("toggle", UIToggleComponent.class, 0xffffff)
                .register("trackpad", UITrackpadComponent.class, 0xffffff)
                .register("strings", UIStringListComponent.class, 0xffffff)
                .register("item", UIStackComponent.class, 0xffffff)
                .register("layout", UILayoutComponent.class, 0xffffff)
                .register("morph", UIMorphComponent.class, 0xffffff)
                .register("clickarea", UIClickComponent.class, 0xffffff);

        uiComponents = ui;
        Mappet.EVENT_BUS.post(new RegisterUIComponentEvent(ui));
    }
}
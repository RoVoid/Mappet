package mchorse.mappet;

import mchorse.mappet.api.conditions.blocks.*;
import mchorse.mappet.api.dialogues.nodes.*;
import mchorse.mappet.api.events.nodes.*;
import mchorse.mappet.api.quests.chains.QuestNode;
import mchorse.mappet.api.scripts.code.ui.*;
import mchorse.mappet.api.triggers.blocks.*;
import mchorse.mappet.api.utils.MapFactory;
import mchorse.mappet.events.*;
import mchorse.mappet.utils.Colors;

public class MappetFactories {
    private static MapFactory<EventBaseNode> events;
    private static MapFactory<EventBaseNode> dialogues;
    private static MapFactory<QuestNode> chains;
    private static MapFactory<AbstractConditionBlock> conditionBlocks;
    private static MapFactory<AbstractTriggerBlock> triggerBlocks;
    private static MapFactory<UIComponent> uiComponents;

    public static MapFactory<EventBaseNode> getEvents() {
        return events;
    }

    public static MapFactory<EventBaseNode> getDialogues() {
        return dialogues;
    }

    public static MapFactory<QuestNode> getChains() {
        return chains;
    }

    public static MapFactory<AbstractConditionBlock> getConditionBlocks() {
        return conditionBlocks;
    }

    public static MapFactory<AbstractTriggerBlock> getTriggerBlocks() {
        return triggerBlocks;
    }

    public static MapFactory<UIComponent> getUiComponents() {
        return uiComponents;
    }

    public static void register() {
        /* Register event nodes */
        events = new MapFactory<EventBaseNode>().register("command", CommandNode.class, Colors.COMMAND)
                                                .register("comment", CommentNode.class, Colors.COMMENT)
                                                .register("condition", ConditionNode.class, Colors.CONDITION)
                                                .register("timer", TimerNode.class, Colors.TIME).register("trigger", TriggerNode.class, Colors.STATE)
                                                .register("cancel", CancelNode.class, Colors.CANCEL);
        Mappet.EVENT_BUS.post(new RegisterEventNodeEvent(events));

        /* Register dialogue nodes */
        dialogues = events.copy().register("reply", ReplyNode.class, Colors.REPLY).register("reaction", ReactionNode.class, Colors.STATE)
                          .register("quest_chain", QuestChainNode.class, Colors.QUEST).register("quest", QuestDialogueNode.class, Colors.QUEST)
                          .unregister("timer");
        Mappet.EVENT_BUS.post(new RegisterDialogueNodeEvent(dialogues));

        /* Register quest chain blocks */
        chains = new MapFactory<QuestNode>().register("quest", QuestNode.class, Colors.QUEST);
        Mappet.EVENT_BUS.post(new RegisterQuestChainNodeEvent(chains));

        /* Register condition blocks */
        conditionBlocks = new MapFactory<AbstractConditionBlock>().register("quest", QuestConditionBlock.class, Colors.QUEST)
                                                                  .register("state", StateConditionBlock.class, Colors.STATE)
                                                                  .register("dialogue", DialogueConditionBlock.class, Colors.DIALOGUE)
                                                                  .register("faction", FactionConditionBlock.class, Colors.FACTION)
                                                                  .register("item", ItemConditionBlock.class, Colors.CRAFTING)
                                                                  .register("world_time", WorldTimeConditionBlock.class, Colors.TIME)
                                                                  .register("entity", EntityConditionBlock.class, Colors.ENTITY)
                                                                  .register("condition", ConditionConditionBlock.class, Colors.CONDITION)
                                                                  .register("morph", MorphConditionBlock.class, Colors.MORPH);
        Mappet.EVENT_BUS.post(new RegisterConditionBlockEvent(conditionBlocks));

        /* Register trigger blocks */
        triggerBlocks = new MapFactory<AbstractTriggerBlock>().register("command", CommandTriggerBlock.class, Colors.COMMAND)
                                                              .register("sound", SoundTriggerBlock.class, Colors.REPLY)
                                                              .register("event", EventTriggerBlock.class, Colors.STATE)
                                                              .register("dialogue", DialogueTriggerBlock.class, Colors.DIALOGUE)
                                                              .register("script", ScriptTriggerBlock.class, Colors.ENTITY)
                                                              .register("item", ItemTriggerBlock.class, Colors.CRAFTING)
                                                              .register("state", StateTriggerBlock.class, Colors.STATE)
                                                              .register("morph", MorphTriggerBlock.class, Colors.MORPH);
        Mappet.EVENT_BUS.post(new RegisterTriggerBlockEvent(triggerBlocks));

        /* Register UI components */
        uiComponents = new MapFactory<UIComponent>().register("graphics", UIGraphicsComponent.class, 0xffffff)
                                                    .register("button", UIButtonComponent.class, 0xffffff)
                                                    .register("icon", UIIconButtonComponent.class, 0xffffff)
                                                    .register("keybind", UIKeybindComponent.class, 0xffffff)
                                                    .register("label", UILabelComponent.class, 0xffffff)
                                                    .register("text", UITextComponent.class, 0xffffff)
                                                    .register("textbox", UITextboxComponent.class, 0xffffff)
                                                    .register("textarea", UITextareaComponent.class, 0xffffff)
                                                    .register("toggle", UIToggleComponent.class, 0xffffff)
                                                    .register("trackpad", UITrackpadComponent.class, 0xffffff)
                                                    .register("strings", UIStringListComponent.class, 0xffffff)
                                                    .register("dropdown", UIDropdownComponent.class, 0xffffff)
                                                    .register("item", UIStackComponent.class, 0xffffff)
                                                    .register("layout", UILayoutComponent.class, 0xffffff)
                                                    .register("morph", UIMorphComponent.class, 0xffffff)
                                                    .register("clickarea", UIClickComponent.class, 0xffffff);
        Mappet.EVENT_BUS.post(new RegisterUIComponentEvent(uiComponents));
    }
}
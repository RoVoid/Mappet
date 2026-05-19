package mchorse.mappet.api.dialogues;

import mchorse.mappet.api.dialogues.nodes.*;
import mchorse.mappet.api.events.EventContext;
import mchorse.mappet.api.utils.DataContext;

import java.util.ArrayList;
import java.util.List;

public class DialogueContext extends EventContext {
    public ReactionNode reactionNode;
    public CommentNode commentNode;
    public List<ReplyNode> replyNodes = new ArrayList<ReplyNode>();
    public QuestChainNode questChain;
    public QuestDialogueNode quest;

    public CraftingNode crafting;



    public DialogueContext(DataContext data) {
        super(data);
    }

    public void resetAll() {
        this.reactionNode = null;
        this.commentNode = null;
        this.replyNodes.clear();
        this.questChain = null;
        this.quest = null;

        crafting = null;
    }

    public void addReply(ReplyNode node)
    {
        this.replyNodes.add(node);
        this.crafting = null;
        this.questChain = null;
        this.quest = null;
    }

    public void setCrafting(CraftingNode node)
    {
        this.replyNodes.clear();
        this.crafting = node;
        this.questChain = null;
        this.quest = null;
    }

    public void setQuestChain(QuestChainNode node)
    {
        this.replyNodes.clear();
        this.crafting = null;
        this.questChain = node;
        this.quest = null;
    }

    public void setQuest(QuestDialogueNode node)
    {
        this.replyNodes.clear();
        this.crafting = null;
        this.questChain = null;
        this.quest = node;
    }
}
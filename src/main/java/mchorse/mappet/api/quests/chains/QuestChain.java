package mchorse.mappet.api.quests.chains;

import mchorse.mappet.api.utils.MapFactory;
import mchorse.mappet.api.utils.nodes.NodeSystem;

public class QuestChain extends NodeSystem<QuestNode>
{
    public QuestChain(MapFactory<QuestNode> factory)
    {
        super(factory);
    }
}
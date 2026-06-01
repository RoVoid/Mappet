package mchorse.mappet.api.scripts.code.mappet;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.quests.Quest;
import mchorse.mappet.api.quests.Quests;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Set;

public class MappetQuests {
    public Quests quests;
    public EntityPlayer player;

    public MappetQuests(Quests quests, EntityPlayer player)
    {
        this.quests = quests;
        this.player = player;
    }

    public boolean has(String id)
    {
        return this.quests.has(id);
    }

    public boolean add(String id)
    {
        if (this.quests.has(id))
        {
            return false;
        }

        Quest quest = Mappet.quests.load(id);

        if (quest != null)
        {
            this.quests.add(quest, this.player);
        }

        return quest != null;
    }

    public boolean isComplete(String id)
    {
        Quest quest = this.quests.getByName(id);

        return quest != null && quest.isComplete(this.player);
    }

    public boolean complete(String id)
    {
        return this.quests.complete(id, this.player);
    }

    public boolean decline(String id)
    {
        return this.quests.decline(id, this.player);
    }

    public Set<String> getIds()
    {
        return this.quests.quests.keySet();
    }
}
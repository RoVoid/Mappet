package mchorse.mappet.api.factions;

import mchorse.mappet.api.conditions.Condition;
import mchorse.mappet.api.states.FactionStates;
import mchorse.mappet.api.utils.AbstractData;
import mchorse.mappet.api.utils.DataContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;

public class Faction extends AbstractData {
    /**
     * The display name of the faction
     */
    public String title = "";

    /**
     * Enabled condition
     */
    public Condition visible = new Condition(true);

    /**
     * Color of the faction
     */
    public int color = 0xffffff;

    /**
     * Default score upon player joining the faction
     */
    public int score = 500;

    /**
     * Default attitude towards players who has no factions
     */
    public FactionAttitude playerAttitude = FactionAttitude.PASSIVE;

    /**
     * Default attitude towards NPCs or any other entities that can
     * join factions who has no factions
     */
    public FactionAttitude othersAttitude = FactionAttitude.PASSIVE;

    /**
     * It's own relation toward its own
     */
    public FactionRelation ownRelation = new FactionRelation();

    /**
     * Relations to other factions
     */
    public Map<String, FactionAttitude> relations = new HashMap<>();

    public FactionAttitude get(FactionStates states) {
        if (states.has(getId())) return ownRelation.getAttitude(states.get(getId()));

        for (String key : relations.keySet())
            if (states.has(key)) return relations.get(key);

        return playerAttitude;
    }

    public FactionAttitude get(String faction) {
        if (faction.equals(getId())) return FactionAttitude.FRIENDLY;

        FactionAttitude attitude = relations.get(faction);

        return attitude == null ? othersAttitude : attitude;
    }

    public boolean isVisible(EntityPlayer player) {
        return visible.execute(new DataContext(player));
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setString("Title", title);
        tag.setTag("Visible", visible.serializeNBT());
        tag.setInteger("Color", color);
        tag.setInteger("DefaultScore", score);
        tag.setString("PlayerAttitude", playerAttitude.name());
        tag.setString("OthersAttitude", othersAttitude.name());
        tag.setTag("OwnRelation", ownRelation.serializeNBT());

        NBTTagCompound relations = new NBTTagCompound();

        for (Map.Entry<String, FactionAttitude> entry : this.relations.entrySet())
            relations.setString(entry.getKey(), entry.getValue().name());

        if (relations.getSize() > 0) tag.setTag("Relations", relations);

        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        if (tag.hasKey("Title")) title = tag.getString("Title");
        if (tag.hasKey("Visible")) visible.deserializeNBT(tag.getCompoundTag("Visible"));
        if (tag.hasKey("Color")) color = tag.getInteger("Color");
        if (tag.hasKey("DefaultScore")) score = tag.getInteger("DefaultScore");
        if (tag.hasKey("PlayerAttitude")) playerAttitude = FactionAttitude.get(tag.getString("PlayerAttitude"));
        if (tag.hasKey("OthersAttitude")) othersAttitude = FactionAttitude.get(tag.getString("OthersAttitude"));
        if (tag.hasKey("OwnRelation")) ownRelation.deserializeNBT(tag.getCompoundTag("OwnRelation"));
        if (tag.hasKey("Relations")) {
            NBTTagCompound relations = tag.getCompoundTag("Relations");

            for (String key : relations.getKeySet())
                this.relations.put(key, FactionAttitude.get(relations.getString(key)));
        }
    }
}
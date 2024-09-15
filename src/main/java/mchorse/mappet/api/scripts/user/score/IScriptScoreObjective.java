package mchorse.mappet.api.scripts.user.score;

import net.minecraft.scoreboard.ScoreObjective;

public interface IScriptScoreObjective {
    ScoreObjective getMinecraftScoreObjective();

    String getName();

    String getCriteria();

    String getDisplayName();

    void setDisplayName(String name);

    String getRenderType();

    void setRenderType(String type);
}

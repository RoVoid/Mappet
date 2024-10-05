package mchorse.mappet.api.scripts.user.score;

import mchorse.mappet.api.scripts.code.entities.ScriptPlayer;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;

import java.util.List;

public interface IScriptScoreObjective {
    ScoreObjective getMinecraftScoreObjective();

    String getName();

    String getCriteria();

    String getDisplayName();

    void setDisplayName(String name);

    String getRenderType();

    void setRenderType(String type);

    List<Integer> getSortedScores();

    void set(ScriptPlayer player, int value);

    int add(ScriptPlayer player, int value);

    int get(ScriptPlayer player);

    void reset(ScriptPlayer player);

    Score getScore(ScriptPlayer player);
}

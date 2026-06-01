package mchorse.mappet.api.scripts.code.score;

import mchorse.mappet.api.scripts.code.entities.player.ScriptPlayer;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;

import java.util.List;
import java.util.stream.Collectors;

public class ScriptScoreObjective {
    private final ScoreObjective objective;

    public ScriptScoreObjective(ScoreObjective objective) {
        this.objective = objective;
    }

    public ScoreObjective getMinecraftScoreObjective() {
        return objective;
    }

    public String getName() {
        return objective.getName();
    }

    public String getCriteria() {
        return objective.getCriteria().getName();
    }

    public String getDisplayName() {
        return objective.getDisplayName();
    }

    public void setDisplayName(String name) {
        objective.setDisplayName(name);
    }

    public String getRenderType() {
        return objective.getRenderType().getRenderType();
    }

    public void setRenderType(String type) {
        objective.setRenderType(IScoreCriteria.EnumRenderType.valueOf(type.toLowerCase()));
    }

    public List<Integer> getSortedScores() {
        return objective.getScoreboard().getSortedScores(objective).stream().map(Score::getScorePoints).collect(Collectors.toList());
    }

    public void set(ScriptPlayer player, int value) {
        getScore(player).setScorePoints(value);
    }

    public int add(ScriptPlayer player, int value) {
        Score score = getScore(player);
        score.increaseScore(value);
        return score.getScorePoints();
    }

    public int get(ScriptPlayer player) {
        return getScore(player).getScorePoints();
    }

    public void reset(ScriptPlayer player) {
        objective.getScoreboard().removeObjectiveFromEntity(player.getName(), objective);
    }

    public Score getScore(ScriptPlayer player) {
        return objective.getScoreboard().getOrCreateScore(player.getName(), objective);
    }
}

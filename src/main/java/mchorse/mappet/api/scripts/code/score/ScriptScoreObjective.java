package mchorse.mappet.api.scripts.code.score;

import mchorse.mappet.api.scripts.code.entities.ScriptPlayer;
import mchorse.mappet.api.scripts.user.score.IScriptScoreObjective;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;

import java.util.List;
import java.util.stream.Collectors;

public class ScriptScoreObjective implements IScriptScoreObjective {
    private final ScoreObjective objective;

    public ScriptScoreObjective(ScoreObjective objective) {
        this.objective = objective;
    }

    @Override
    public ScoreObjective getMinecraftScoreObjective() {
        return objective;
    }

    @Override
    public String getName() {
        return objective.getName();
    }

    @Override
    public String getCriteria() {
        return objective.getCriteria().getName();
    }

    @Override
    public String getDisplayName() {
        return objective.getDisplayName();
    }

    @Override
    public void setDisplayName(String name) {
        objective.setDisplayName(name);
    }

    @Override
    public String getRenderType() {
        return objective.getRenderType().getRenderType();
    }

    @Override
    public void setRenderType(String type) {
        objective.setRenderType(IScoreCriteria.EnumRenderType.valueOf(type.toLowerCase()));
    }

    @Override
    public List<Integer> getSortedScores() {
        return objective.getScoreboard().getSortedScores(objective).stream().map(Score::getScorePoints).collect(Collectors.toList());
    }

    @Override
    public void set(ScriptPlayer player, int value) {
        getScore(player).setScorePoints(value);
    }

    @Override
    public int add(ScriptPlayer player, int value) {
        Score score = getScore(player);
        score.increaseScore(value);
        return score.getScorePoints();
    }

    @Override
    public int get(ScriptPlayer player) {
        return getScore(player).getScorePoints();
    }

    @Override
    public void reset(ScriptPlayer player) {
        objective.getScoreboard().removeObjectiveFromEntity(player.getName(), objective);
    }

    @Override
    public Score getScore(ScriptPlayer player) {
        return objective.getScoreboard().getOrCreateScore(player.getName(), objective);
    }
}

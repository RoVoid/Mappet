package mchorse.mappet.api.scripts.code.score;

import mchorse.mappet.api.scripts.user.score.IScriptScoreObjective;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;

import java.util.List;

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

    //@Override
    public List<Score> getSortedScores() {
        return (List<Score>) objective.getScoreboard().getSortedScores(objective);
    }
}

package mchorse.mappet.api.scripts.code.score;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.scripts.user.score.IScriptScoreboard;
import net.minecraft.scoreboard.*;

import javax.annotation.Nullable;

public class ScriptScoreboard implements IScriptScoreboard {
    private final Scoreboard scoreboard;

    public ScriptScoreboard(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    @Nullable
    @Override
    public ScriptScoreObjective getObjective(String name) {
        return getObjective(name, false);
    }

    @Nullable
    @Override
    public ScriptScoreObjective getObjective(String name, boolean createNew) {
        ScoreObjective objective = scoreboard.getObjective(name);
        return objective == null ? createNew ? addObjective(name) : null : new ScriptScoreObjective(objective);
    }

    @Override
    public ScriptScoreObjective addObjective(String name) {
        if (name.length() > 16) {
            Mappet.logger.error("The objective name '" + name + "' is too long!");
            return null;
        }
        return new ScriptScoreObjective(scoreboard.addScoreObjective(name, IScoreCriteria.DUMMY));
    }

    @Override
    public void removeObjective(String name) {
        removeObjective(getObjective(name));
    }

    public void removeObjective(@Nullable ScriptScoreObjective objective) {
        if (objective == null || objective.getMinecraftScoreObjective() == null) return;
        scoreboard.removeObjective(objective.getMinecraftScoreObjective());
    }

    @Override
    public ScriptTeam getTeam(String name) {
        return new ScriptTeam(scoreboard, scoreboard.getTeam(name));
    }

    @Override
    public ScriptTeam createTeam(String name) {
        if (name.length() > 16) {
            Mappet.logger.error("The team name '" + name + "' is too long!");
            return null;
        }
        return new ScriptTeam(scoreboard, scoreboard.createTeam(name));
    }

    @Override
    public void removeTeam(String name) {
        scoreboard.removeTeam(getTeam(name).getMinecraftTeam());
    }
}
package mchorse.mappet.api.scripts.code.score;

import mchorse.mappet.Mappet;
import net.minecraft.scoreboard.*;

import javax.annotation.Nullable;

public class ScriptScoreboard {
    private final Scoreboard scoreboard;

    public ScriptScoreboard(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    @Nullable
    public ScriptScoreObjective getObjective(String name) {
        return getObjective(name, false);
    }

    @Nullable
    public ScriptScoreObjective getObjective(String name, boolean createNew) {
        ScoreObjective objective = scoreboard.getObjective(name);
        return objective == null ? createNew ? addObjective(name) : null : new ScriptScoreObjective(objective);
    }

    public ScriptScoreObjective addObjective(String name) {
        if (name.length() > 16) {
            Mappet.logger.error("The objective name '" + name + "' is too long!");
            return null;
        }
        return new ScriptScoreObjective(scoreboard.addScoreObjective(name, IScoreCriteria.DUMMY));
    }

    public void removeObjective(String name) {
        removeObjective(getObjective(name));
    }

    public void removeObjective(@Nullable ScriptScoreObjective objective) {
        if (objective == null || objective.getMinecraftScoreObjective() == null) return;
        scoreboard.removeObjective(objective.getMinecraftScoreObjective());
    }

    public ScriptTeam getTeam(String name) {
        return new ScriptTeam(scoreboard, scoreboard.getTeam(name));
    }

    public ScriptTeam createTeam(String name) {
        if (name.length() > 16) {
            Mappet.logger.error("The team name '" + name + "' is too long!");
            return null;
        }
        return new ScriptTeam(scoreboard, scoreboard.createTeam(name));
    }

    public void removeTeam(String name) {
        scoreboard.removeTeam(getTeam(name).getMinecraftTeam());
    }
}
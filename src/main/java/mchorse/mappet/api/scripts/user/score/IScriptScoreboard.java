package mchorse.mappet.api.scripts.user.score;

import mchorse.mappet.api.scripts.code.score.ScriptScoreObjective;
import mchorse.mappet.api.scripts.code.score.ScriptTeam;

import javax.annotation.Nullable;

public interface IScriptScoreboard {
    @Nullable
    ScriptScoreObjective getObjective(String name);

    @Nullable
    ScriptScoreObjective getObjective(String name, boolean createNew);

    ScriptScoreObjective addObjective(String name);

    void removeObjective(String name);
    
    ScriptTeam getTeam(String name);

    ScriptTeam createTeam(String name);

    void removeTeam(String name);
}

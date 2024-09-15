package mchorse.mappet.api.scripts.user.score;

import mchorse.mappet.api.scripts.code.entities.ScriptPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;

import java.util.List;

public interface IScriptTeam {
    /**
     * @return team name
     */
    String getName();

    /**
     * @return team color
     */
    String getColor();

    String getPrefix();

    String getSuffix();

    /**
     * always, never, hideForOtherTeams, hideForOwnTeam
     * @return visibility of player name tags in the team
     */
    String getNameTagVisibility();

    /**
     * always, never, hideForOtherTeams, hideForOwnTeam
     * @return visibility of death messages of players in the team
     */
    String getDeathMessageVisibility();

    /**
     * @return collision rule for players in a team
     */
    String getCollisionRule();

    /**
     * @return true if friendly fire is allowed, otherwise false
     */
    boolean isAllowFriendlyFire();

    /**
     * @return whether invisible players from the team are visible
     */
    boolean isSeeFriendlyInvisibles();

    /**
     * §0black§r, §1dark_blue§r, §2dark_green§r, §3dark_aqua§r, §4dark_red§r, §5dark_purple§r, §6gold§r, §7gray§r, §8dark_gray§r, §9blue§r, §agreen§r, §baqua§r, §ered§r, §dlight_purple§r, §eyellow§r, white
     * @param color new team color
     */
    void setColor(String color);

    void setPrefix(String prefix);

    void setSuffix(String suffix);

    /**
     * always, never, hideForOtherTeams, hideForOwnTeam
     * @param tagVisibility new visibility of player name tags in the team
     */
    void setNameTagVisibility(String tagVisibility);


    /**
     * always, never, hideForOtherTeams, hideForOwnTeam
     * @param deathMessageVisibility new visibility of player death messages in the team
     */
    void setDeathMessageVisibility(String deathMessageVisibility);

    /**
     * always, never, pushOtherTeams, pushOwnTeam
     * @param collisionRule new collision rule for players in the team
     */
    void setCollisionRule(String collisionRule);

    /**
     * @param allowFriendlyFire true to allow friendly fire, false to disallow it
     */
    void setAllowFriendlyFire(boolean allowFriendlyFire);

    /**
     * @param seeFriendlyInvisibles true to make invisible players from the team visible, false to make them invisible
     */
    void setSeeFriendlyInvisibles(boolean seeFriendlyInvisibles);

    ScorePlayerTeam getMinecraftTeam();

    void join(List<ScriptPlayer> players);

    void kick(List<ScriptPlayer> players);
}
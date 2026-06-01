package mchorse.mappet.api.scripts.code.score;

import mchorse.mappet.api.scripts.code.entities.player.ScriptPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class ScriptTeam {
    private final Scoreboard scoreboard;
    private final ScorePlayerTeam team;

    public ScriptTeam(Scoreboard scoreboard, ScorePlayerTeam team) {
        this.scoreboard = scoreboard;
        this.team = team;
    }

    public String getName() {
        return team.getName();
    }

    public String getColor() {
        return team.getColor().name().toLowerCase();
    }

    public String getPrefix() {
        return team.getPrefix();
    }

    public String getSuffix() {
        return team.getSuffix();
    }

    public String getNameTagVisibility() {
        return team.getNameTagVisibility().name();
    }

    public String getDeathMessageVisibility() {
        return team.getDeathMessageVisibility().name();
    }

    public String getCollisionRule() {
        return team.getCollisionRule().name();
    }

    public boolean isAllowFriendlyFire() {
        return team.getAllowFriendlyFire();
    }

    public boolean isSeeFriendlyInvisibles() {
        return team.getSeeFriendlyInvisiblesEnabled();
    }

    public void setColor(String color) {
        TextFormatting formatting = TextFormatting.valueOf(color.toUpperCase());
        team.setColor(formatting);
        team.setPrefix(formatting.toString());
        team.setSuffix(TextFormatting.RESET.toString());
    }

    public void setPrefix(String prefix) {
        team.setPrefix(prefix);
    }

    public void setSuffix(String suffix) {
        team.setSuffix(suffix);
    }

    public void setNameTagVisibility(String tagVisibility) {
        team.setNameTagVisibility(Team.EnumVisible.valueOf(tagVisibility.toUpperCase()));
    }

    public void setDeathMessageVisibility(String deathMessageVisibility) {
        team.setDeathMessageVisibility(Team.EnumVisible.valueOf(deathMessageVisibility.toLowerCase()));
    }

    public void setCollisionRule(String collisionRule) {
        team.setCollisionRule(Team.CollisionRule.valueOf(collisionRule.toLowerCase()));
    }

    public void setAllowFriendlyFire(boolean enabled) {
        team.setAllowFriendlyFire(enabled);
    }

    public void setSeeFriendlyInvisibles(boolean enabled) {
        team.setSeeFriendlyInvisiblesEnabled(enabled);
    }

    public ScorePlayerTeam getMinecraftTeam() {
        return team;
    }

    public void join(List<ScriptPlayer> players) {
        for (ScriptPlayer player : players) scoreboard.addPlayerToTeam(player.getName(), team.getName());
    }

    public void join(ScriptPlayer player) {
        scoreboard.addPlayerToTeam(player.getName(), team.getName());
    }

    public void kick(List<ScriptPlayer> players) {
        for (ScriptPlayer player : players) scoreboard.removePlayerFromTeam(player.getName(), team);
    }

    public void leave(ScriptPlayer player) {
        scoreboard.removePlayerFromTeam(player.getName(), team);
    }
}

package mchorse.mappet.api.scripts.code.score;

import mchorse.mappet.api.scripts.code.entities.player.ScriptPlayer;
import mchorse.mappet.api.scripts.user.score.IScriptTeam;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class ScriptTeam implements IScriptTeam {
    private final Scoreboard scoreboard;
    private final ScorePlayerTeam team;

    public ScriptTeam(Scoreboard scoreboard, ScorePlayerTeam team) {
        this.scoreboard = scoreboard;
        this.team = team;
    }

    @Override
    public String getName() {
        return team.getName();
    }

    @Override
    public String getColor() {
        return team.getColor().name().toLowerCase();
    }

    @Override
    public String getPrefix() {
        return team.getPrefix();
    }

    @Override
    public String getSuffix() {
        return team.getSuffix();
    }

    @Override
    public String getNameTagVisibility() {
        return team.getNameTagVisibility().name();
    }

    @Override
    public String getDeathMessageVisibility() {
        return team.getDeathMessageVisibility().name();
    }

    @Override
    public String getCollisionRule() {
        return team.getCollisionRule().name();
    }

    @Override
    public boolean isAllowFriendlyFire() {
        return team.getAllowFriendlyFire();
    }

    @Override
    public boolean isSeeFriendlyInvisibles() {
        return team.getSeeFriendlyInvisiblesEnabled();
    }

    @Override
    public void setColor(String color) {
        TextFormatting formatting = TextFormatting.valueOf(color.toUpperCase());
        team.setColor(formatting);
        team.setPrefix(formatting.toString());
        team.setSuffix(TextFormatting.RESET.toString());
    }

    @Override
    public void setPrefix(String prefix) {
        team.setPrefix(prefix);
    }

    @Override
    public void setSuffix(String suffix) {
        team.setSuffix(suffix);
    }

    @Override
    public void setNameTagVisibility(String tagVisibility) {
        team.setNameTagVisibility(Team.EnumVisible.valueOf(tagVisibility.toUpperCase()));
    }

    @Override
    public void setDeathMessageVisibility(String deathMessageVisibility) {
        team.setDeathMessageVisibility(Team.EnumVisible.valueOf(deathMessageVisibility.toLowerCase()));
    }

    @Override
    public void setCollisionRule(String collisionRule) {
        team.setCollisionRule(Team.CollisionRule.valueOf(collisionRule.toLowerCase()));
    }

    @Override
    public void setAllowFriendlyFire(boolean enabled) {
        team.setAllowFriendlyFire(enabled);
    }

    @Override
    public void setSeeFriendlyInvisibles(boolean enabled) {
        team.setSeeFriendlyInvisiblesEnabled(enabled);
    }

    @Override
    public ScorePlayerTeam getMinecraftTeam() {
        return team;
    }

    @Override
    public void join(List<ScriptPlayer> players) {
        for (ScriptPlayer player : players) scoreboard.addPlayerToTeam(player.getName(), team.getName());
    }

    public void join(ScriptPlayer player) {
        scoreboard.addPlayerToTeam(player.getName(), team.getName());
    }

    @Override
    public void kick(List<ScriptPlayer> players) {
        for (ScriptPlayer player : players) scoreboard.removePlayerFromTeam(player.getName(), team);
    }

    public void leave(ScriptPlayer player) {
        scoreboard.removePlayerFromTeam(player.getName(), team);
    }
}

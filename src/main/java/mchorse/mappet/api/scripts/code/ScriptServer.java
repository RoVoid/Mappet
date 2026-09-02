package mchorse.mappet.api.scripts.code;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.scripts.ScriptWrapper;
import mchorse.mappet.api.scripts.code.entities.IScriptEntity;
import mchorse.mappet.api.scripts.code.entities.ScriptEntity;
import mchorse.mappet.api.scripts.code.entities.player.ScriptPlayer;
import mchorse.mappet.api.scripts.code.score.ScriptScoreboard;
import mchorse.mappet.api.scripts.code.world.ScriptWorld;
import mchorse.mappet.api.states.ScriptStates;
import mchorse.mappet.api.utils.DataContext;
import net.minecraft.command.EntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ScriptServer extends ScriptWrapper<MinecraftServer> {
    private ScriptStates states;

    protected ScriptServer(MinecraftServer server) {
        super(server);
    }

    public ScriptWorld getWorld(int dimension) {
        return new ScriptWorld(asMinecraft().getWorld(dimension));
    }

    public List<IScriptEntity> getEntities(String targetSelector) {
        List<IScriptEntity> entities = new ArrayList<>();

        try {
            for (Entity entity : EntitySelector.matchEntities(asMinecraft(), targetSelector, Entity.class))
                entities.add(ScriptEntity.create(entity));
        } catch (Exception ignored) {
        }

        return entities;
    }

    public IScriptEntity getEntity(String uuid) {
        return ScriptEntity.create(asMinecraft().getEntityFromUuid(UUID.fromString(uuid)));
    }

    public List<ScriptPlayer> getAllPlayers() {
        List<ScriptPlayer> entities = new ArrayList<>();

        for (EntityPlayerMP player : asMinecraft().getPlayerList().getPlayers()) entities.add(new ScriptPlayer(player));

        return entities;
    }

    public ScriptPlayer getPlayer(String username) {
        EntityPlayerMP player = asMinecraft().getPlayerList().getPlayerByUsername(username);

        if (player != null) return new ScriptPlayer(player);

        return null;
    }

    public ScriptStates getStates() {
        if (states == null) states = Mappet.states.scripts;
        return states;
    }

    public boolean entityExists(String uuid) throws IllegalArgumentException {
        try {
            return asMinecraft().getEntityFromUuid(UUID.fromString(uuid)) != null;
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid UUID string: " + uuid, ex);
        }
    }

    public void executeScript(String scriptName) {
        executeScript(scriptName, "main");
    }

    public void executeScript(String scriptName, String function) {
        DataContext context = new DataContext(asMinecraft());
        try {
            Mappet.scripts.execute(scriptName, function, context);
        } catch (ScriptException e) {
            String fileName = e.getFileName() == null ? scriptName : e.getFileName();
            Mappet.logger.error(
                    "Script Error: " + fileName + " - Line: " + e.getLineNumber() + " - Column: " + e.getColumnNumber() + " - Message: " + e.getMessage());
            //throw new RuntimeException("Script Error: " + fileName + " - Line: " + e.getLineNumber() + " - Column: " + e.getColumnNumber() + " - Message: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Script Empty: " + scriptName + " - Error: " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    public void executeScript(String scriptName, String function, Object... args) {
        DataContext context = new DataContext(asMinecraft());

        try {
            Mappet.scripts.execute(scriptName, function, context, args);
        } catch (ScriptException e) {
            String fileName = e.getFileName() == null ? scriptName : e.getFileName();
            Mappet.logger.error(
                    "Script Error: " + fileName + " - Line: " + e.getLineNumber() + " - Column: " + e.getColumnNumber() + " - Message: " + e.getMessage());
            // throw new RuntimeException("Script Error: " + fileName + " - Line: " + e.getLineNumber() + " - Column: " + e.getColumnNumber() + " - Message: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Script Empty: " + scriptName + " - Error: " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    public List<String> getOppedPlayerNames() {
        return Arrays.asList(asMinecraft().getPlayerList().getOppedPlayerNames());
    }

    public ScriptScoreboard getScoreboard() {
        return new ScriptScoreboard(asMinecraft().getEntityWorld().getScoreboard());
    }
}
package mchorse.mappet.api.scripts.code;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.scripts.code.entities.ScriptEntity;
import mchorse.mappet.api.scripts.code.entities.player.ScriptPlayer;
import mchorse.mappet.api.scripts.code.score.ScriptScoreboard;
import mchorse.mappet.api.scripts.code.world.ScriptWorld;
import mchorse.mappet.api.scripts.user.IScriptServer;
import mchorse.mappet.api.scripts.user.entities.IScriptEntity;
import mchorse.mappet.api.scripts.user.entities.player.IScriptPlayer;
import mchorse.mappet.api.scripts.user.mappet.IMappetStates;
import mchorse.mappet.api.scripts.user.world.IScriptWorld;
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

public class ScriptServer implements IScriptServer {
    private final MinecraftServer server;

    private IMappetStates states;

    public ScriptServer(MinecraftServer server) {
        this.server = server;
    }

    @Override
    @Deprecated
    public MinecraftServer getMinecraftServer() {
        return server;
    }

    @Override
    public IScriptWorld getWorld(int dimension) {
        return new ScriptWorld(server.getWorld(dimension));
    }

    @Override
    public List<IScriptEntity> getEntities(String targetSelector) {
        List<IScriptEntity> entities = new ArrayList<>();

        try {
            for (Entity entity : EntitySelector.matchEntities(server, targetSelector, Entity.class))
                entities.add(ScriptEntity.create(entity));
        } catch (Exception ignored) {
        }

        return entities;
    }

    @Override
    public IScriptEntity getEntity(String uuid) {
        return ScriptEntity.create(server.getEntityFromUuid(UUID.fromString(uuid)));
    }

    @Override
    public List<IScriptPlayer> getAllPlayers() {
        List<IScriptPlayer> entities = new ArrayList<>();

        for (EntityPlayerMP player : server.getPlayerList().getPlayers()) entities.add(new ScriptPlayer(player));

        return entities;
    }

    @Override
    public IScriptPlayer getPlayer(String username) {
        EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(username);

        if (player != null) return new ScriptPlayer(player);

        return null;
    }

    @Override
    public IMappetStates getStates() {
        if (states == null) states = Mappet.states;
        return states;
    }

    @Override
    public boolean entityExists(String uuid) throws IllegalArgumentException {
        try {
            return server.getEntityFromUuid(UUID.fromString(uuid)) != null;
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid UUID string: " + uuid, ex);
        }
    }

    @Override
    public void executeScript(String scriptName) {
        executeScript(scriptName, "main");
    }

    @Override
    public void executeScript(String scriptName, String function) {
        DataContext context = new DataContext(server);
        try {
            Mappet.scripts.execute(scriptName, function, context);
        } catch (ScriptException e) {
            String fileName = e.getFileName() == null ? scriptName : e.getFileName();
            Mappet.logger.error("Script Error: " + fileName + " - Line: " + e.getLineNumber() + " - Column: " + e.getColumnNumber() + " - Message: " + e.getMessage());
            //throw new RuntimeException("Script Error: " + fileName + " - Line: " + e.getLineNumber() + " - Column: " + e.getColumnNumber() + " - Message: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Script Empty: " + scriptName + " - Error: " + e.getClass()
                                                                                       .getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void executeScript(String scriptName, String function, Object... args) {
        DataContext context = new DataContext(server);

        try {
            Mappet.scripts.execute(scriptName, function, context, args);
        } catch (ScriptException e) {
            String fileName = e.getFileName() == null ? scriptName : e.getFileName();
            Mappet.logger.error("Script Error: " + fileName + " - Line: " + e.getLineNumber() + " - Column: " + e.getColumnNumber() + " - Message: " + e.getMessage());
            // throw new RuntimeException("Script Error: " + fileName + " - Line: " + e.getLineNumber() + " - Column: " + e.getColumnNumber() + " - Message: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Script Empty: " + scriptName + " - Error: " + e.getClass()
                                                                                       .getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> getOppedPlayerNames() {
        return Arrays.asList(server.getPlayerList().getOppedPlayerNames());
    }

    @Override
    public ScriptScoreboard getScoreboard() {
        return new ScriptScoreboard(server.getEntityWorld().getScoreboard());
    }
}
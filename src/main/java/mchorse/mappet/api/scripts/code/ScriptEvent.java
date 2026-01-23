package mchorse.mappet.api.scripts.code;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import mchorse.mappet.CommonProxy;
import mchorse.mappet.Mappet;
import mchorse.mappet.api.scripts.ScriptExecutionFork;
import mchorse.mappet.api.scripts.code.entities.ScriptEntity;
import mchorse.mappet.api.scripts.code.world.ScriptWorld;
import mchorse.mappet.api.scripts.user.IScriptEvent;
import mchorse.mappet.api.scripts.user.IScriptServer;
import mchorse.mappet.api.scripts.user.entities.IScriptEntity;
import mchorse.mappet.api.scripts.user.entities.IScriptNpc;
import mchorse.mappet.api.scripts.user.entities.player.IScriptPlayer;
import mchorse.mappet.api.scripts.user.world.IScriptWorld;
import mchorse.mappet.api.utils.DataContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;

import javax.script.ScriptException;
import java.util.Map;
import java.util.function.Consumer;

public class ScriptEvent implements IScriptEvent {
    private final DataContext context;
    private final String script;
    private final String function;

    private IScriptEntity subject;
    private IScriptEntity object;
    private IScriptWorld world;
    private IScriptServer server;

    public ScriptEvent(DataContext context, String script, String function) {
        this.context = context;
        this.script = script;
        this.function = function;
    }

    @Override
    public String getScript() {
        return script == null ? "" : script;
    }

    @Override
    public String getFunction() {
        return function == null ? "" : function;
    }

    @Override
    public IScriptEntity getSubject() {
        if (subject == null && context.subject != null) subject = ScriptEntity.create(context.subject);
        return subject;
    }

    @Override
    public IScriptEntity getObject() {
        if (object == null && context.object != null) object = ScriptEntity.create(context.object);

        return object;
    }

    @Override
    public IScriptPlayer getPlayer() {
        IScriptEntity subject = getSubject();
        if (subject instanceof IScriptPlayer) return (IScriptPlayer) subject;

        IScriptEntity object = getObject();
        if (object instanceof IScriptPlayer) return (IScriptPlayer) object;

        return null;
    }

    public IScriptNpc getNPC() {
        IScriptEntity subject = getSubject();
        if (subject instanceof IScriptNpc) return (IScriptNpc) subject;

        IScriptEntity object = getObject();
        if (object instanceof IScriptNpc) return (IScriptNpc) object;

        return null;
    }

    @Override
    public IScriptWorld getWorld() {
        if (world == null && context.world != null) world = new ScriptWorld(context.world);

        return world;
    }

    @Override
    public IScriptServer getServer() {
        if (server == null && context.server != null) server = new ScriptServer(context.server);
        return server;
    }

    @Override
    public Map<String, Object> getValues() {
        return context.getValues();
    }

    @Override
    public Object getValue(String key) {
        return context.getValue(key);
    }

    @Override
    public void setValue(String key, Object value) {
        context.getValues().put(key, value);
    }

    /* Useful methods */

    @Override
    public void cancel() {
        context.cancel();
    }

    @Override
    public void scheduleScript(String script, String function, int delay) {
        CommonProxy.eventHandler.addExecutable(new ScriptExecutionFork(context.copy(), script, function, delay));
    }

    @Override
    public void scheduleScript(int delay, ScriptObjectMirror function) {
        if (function == null || !function.isFunction())
            throw new IllegalStateException("Given object is null in script " + script + " (" + function + " function)!");
        CommonProxy.eventHandler.addExecutable(new ScriptExecutionFork(context.copy(), function, delay));
    }

    @Override
    public void scheduleScript(int delay, Consumer<IScriptEvent> consumer) {
        if (consumer == null)
            throw new IllegalStateException("Given object is null in script " + script + " (" + function + " function)!");
        CommonProxy.eventHandler.addExecutable(new ScriptExecutionFork(context.copy(), consumer, delay));
    }

    @Override
    public int executeCommand(String command) {
        return context.execute(command);
    }

    @Override
    public void executeScript(String scriptName) {
        executeScript(scriptName, "main");
    }

    @Override
    public void executeScript(String scriptName, String function) {
        try {
            Mappet.scripts.execute(scriptName, function, context);
        } catch (ScriptException e) {
            Mappet.logger.error("Script Error: " + scriptName + " - Line: " + e.getLineNumber() + " - Column: " + e.getColumnNumber() + " - Message: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Script Empty: " + scriptName + " - Error: " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void executeScript(String scriptName, String function, Object... args) {
        try {
            Mappet.scripts.execute(scriptName, function, context, args);
        } catch (ScriptException e) {
            Mappet.logger.error("Script Error: " + scriptName + " - Line: " + e.getLineNumber() + " - Column: " + e.getColumnNumber() + " - Message: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Script Empty: " + scriptName + " - Error: " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void send(String... message) {
        TextComponentString text = new TextComponentString(message == null ? "null" : String.join(" ", message));
        for (EntityPlayer player : context.server.getPlayerList().getPlayers()) player.sendMessage(text);
    }
}
package mchorse.mappet.api.scripts.code;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import mchorse.mappet.Mappet;
import mchorse.mappet.api.scripts.ScriptExecutionFork;
import mchorse.mappet.api.scripts.code.entities.IScriptEntity;
import mchorse.mappet.api.scripts.code.entities.ScriptEntity;
import mchorse.mappet.api.scripts.code.entities.ScriptNpc;
import mchorse.mappet.api.scripts.code.entities.player.ScriptPlayer;
import mchorse.mappet.api.scripts.code.world.ScriptWorld;
import mchorse.mappet.api.utils.DataContext;
import mchorse.mappet.proxy.CommonProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;

import javax.script.ScriptException;
import java.util.Map;
import java.util.function.Consumer;

public class ScriptEvent {
    private final DataContext context;
    private final String script;
    private final String function;

    private IScriptEntity subject;
    private IScriptEntity object;
    private ScriptWorld world;
    private ScriptServer server;

    public ScriptEvent(DataContext context, String script, String function) {
        this.context = context;
        this.script = script;
        this.function = function;
    }

    public String getScript() {
        return script == null ? "" : script;
    }

    public String getFunction() {
        return function == null ? "" : function;
    }

    public IScriptEntity getSubject() {
        if (subject == null && context.subject != null) subject = ScriptEntity.create(context.subject);
        return subject;
    }

    public IScriptEntity getObject() {
        if (object == null && context.object != null) object = ScriptEntity.create(context.object);
        return object;
    }

    public ScriptPlayer getPlayer() {
        IScriptEntity subject = getSubject();
        if (subject instanceof ScriptPlayer) return (ScriptPlayer) subject;

        IScriptEntity object = getObject();
        if (object instanceof ScriptPlayer) return (ScriptPlayer) object;

        return null;
    }

    public ScriptNpc getNPC() {
        IScriptEntity subject = getSubject();
        if (subject instanceof ScriptNpc) return (ScriptNpc) subject;

        IScriptEntity object = getObject();
        if (object instanceof ScriptNpc) return (ScriptNpc) object;

        return null;
    }

    public ScriptWorld getWorld() {
        if (world == null && context.world != null) world = new ScriptWorld(context.world);
        return world;
    }

    public ScriptServer getServer() {
        if (server == null && context.server != null) server = new ScriptServer(context.server);
        return server;
    }

    public Map<String, Object> getValues() {
        return context.getValues();
    }

    public Object getValue(String key) {
        return context.getValue(key);
    }

    public void setValue(String key, Object value) {
        context.getValues().put(key, value);
    }

    /* Useful methods */

    public void cancel() {
        context.cancel();
    }

    public void scheduleScript(String script, String function, int delay) {
        CommonProxy.eventHandler.addExecutable(new ScriptExecutionFork(context.copy(), script, function, delay));
    }

    public void scheduleScript(int delay, ScriptObjectMirror function) {
        if (function == null || !function.isFunction())
            throw new IllegalStateException("Given object is null in script " + script + " (" + function + " function)!");
        CommonProxy.eventHandler.addExecutable(new ScriptExecutionFork(context.copy(), function, delay));
    }

    public void scheduleScript(int delay, Consumer<ScriptEvent> consumer) {
        if (consumer == null) throw new IllegalStateException("Given object is null in script " + script + " (" + function + " function)!");
        CommonProxy.eventHandler.addExecutable(new ScriptExecutionFork(context.copy(), consumer, delay));
    }

    public int executeCommand(String command) {
        return context.execute(command);
    }

    public void executeScript(String scriptName) {
        executeScript(scriptName, "main");
    }

    public void executeScript(String scriptName, String function) {
        try {
            Mappet.scripts.execute(scriptName, function, context);
        } catch (ScriptException e) {
            Mappet.logger.error(
                    "Script Error: " + scriptName + " - Line: " + e.getLineNumber() + " - Column: " + e.getColumnNumber() + " - Message: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Script Empty: " + scriptName + " - Error: " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    public void executeScript(String scriptName, String function, Object... args) {
        try {
            Mappet.scripts.execute(scriptName, function, context, args);
        } catch (ScriptException e) {
            Mappet.logger.error(
                    "Script Error: " + scriptName + " - Line: " + e.getLineNumber() + " - Column: " + e.getColumnNumber() + " - Message: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Script Empty: " + scriptName + " - Error: " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    public void send(String... message) {
        TextComponentString text = new TextComponentString(message == null ? "null" : String.join(" ", message));
        for (EntityPlayer player : context.server.getPlayerList().getPlayers()) player.sendMessage(text);
    }
}
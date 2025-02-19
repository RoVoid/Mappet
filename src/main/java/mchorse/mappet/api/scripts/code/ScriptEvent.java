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
import mchorse.mappet.api.scripts.user.entities.IScriptPlayer;
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
        return this.script == null ? "" : this.script;
    }

    @Override
    public String getFunction() {
        return this.function == null ? "" : this.function;
    }

    @Override
    public IScriptEntity getSubject() {
        if (this.subject == null && this.context.subject != null) {
            this.subject = ScriptEntity.create(this.context.subject);
        }

        return this.subject;
    }

    @Override
    public IScriptEntity getObject() {
        if (this.object == null && this.context.object != null) {
            this.object = ScriptEntity.create(this.context.object);
        }

        return this.object;
    }

    @Override
    public IScriptPlayer getPlayer() {
        IScriptEntity subject = this.getSubject();
        IScriptEntity object = this.getObject();

        if (subject instanceof IScriptPlayer) {
            return (IScriptPlayer) subject;
        } else if (object instanceof IScriptPlayer) {
            return (IScriptPlayer) object;
        }

        return null;
    }

    @Override
    public IScriptNpc getNPC() {
        IScriptEntity subject = this.getSubject();
        IScriptEntity object = this.getObject();

        if (subject instanceof IScriptNpc) {
            return (IScriptNpc) subject;
        } else if (object instanceof IScriptPlayer) {
            return (IScriptNpc) object;
        }

        return null;
    }

    @Override
    public IScriptWorld getWorld() {
        if (this.world == null && this.context.world != null) {
            this.world = new ScriptWorld(this.context.world);
        }

        return this.world;
    }

    @Override
    public IScriptServer getServer() {
        if (this.server == null && this.context.server != null) {
            this.server = new ScriptServer(this.context.server);
        }

        return this.server;
    }

    @Override
    public Map<String, Object> getValues() {
        return this.context.getValues();
    }

    @Override
    public Object getValue(String key) {
        return this.context.getValue(key);
    }

    @Override
    public void setValue(String key, Object value) {
        this.context.getValues().put(key, value);
    }

    /* Useful methods */

    @Override
    public void cancel() {
        this.context.cancel();
    }

    @Override
    public void scheduleScript(String script, String function, int delay) {
        CommonProxy.eventHandler.addExecutable(new ScriptExecutionFork(this.context.copy(), script, function, delay));
    }

    @Override
    public void scheduleScript(int delay, ScriptObjectMirror function) {
        if (function != null && function.isFunction()) {
            CommonProxy.eventHandler.addExecutable(new ScriptExecutionFork(this.context.copy(), function, delay));
        } else {
            throw new IllegalStateException("Given object is null in script " + this.script + " (" + this.function + " function)!");
        }
    }

    @Override
    public void scheduleScript(int delay, Consumer<IScriptEvent> consumer) {
        if (consumer != null) {
            CommonProxy.eventHandler.addExecutable(new ScriptExecutionFork(this.context.copy(), consumer, delay));
        } else {
            throw new IllegalStateException("Given object is null in script " + this.script + " (" + this.function + " function)!");
        }
    }

    @Override
    public int executeCommand(String command) {
        return this.context.execute(command);
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
            String fileName = e.getFileName() == null ? scriptName : e.getFileName();
            Mappet.logger.error("Script Error: " + fileName + " - Line: " + e.getLineNumber() + " - Column: " + e.getColumnNumber() + " - Message: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Script Empty: " + scriptName + " - Error: " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void executeScript(String scriptName, String function, Object... args) {
        try {
            Mappet.scripts.execute(scriptName, function, context, args);
        } catch (ScriptException e) {
            String fileName = e.getFileName() == null ? scriptName : e.getFileName();
            Mappet.logger.error("Script Error: " + fileName + " - Line: " + e.getLineNumber() + " - Column: " + e.getColumnNumber() + " - Message: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Script Empty: " + scriptName + " - Error: " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void send(String... message) {
        TextComponentString text = new TextComponentString(message == null ? "null" : String.join(" ", message));
        for (EntityPlayer player : this.context.server.getPlayerList().getPlayers()) {
            player.sendMessage(text);
        }
    }
}
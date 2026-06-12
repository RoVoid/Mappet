package mchorse.mappet.api.scripts.engine;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import mchorse.mappet.Mappet;
import mchorse.mappet.api.scripts.Script;
import mchorse.mappet.api.scripts.code.ScriptFactory;
import mchorse.mappet.api.scripts.code.math.ScriptMath;
import mchorse.mappet.events.RegisterScriptVariablesEvent;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.lang.reflect.Field;

public class ScriptEngineFactory {
    public static ScriptFactory FACTORY = new ScriptFactory();

    public static ScriptEngine create(Script script) throws ScriptException {
        String ext = script.getExtension();
        ScriptEngine engine =
                ext.equals("js") ? new NashornScriptEngineFactory().getScriptEngine("--language=es6", "-scripting") : createByExtension(ext);

        if (engine == null) throw new ScriptException("Can't find script engine for \"" + ext + "\" extension", script.getId(), -1);

        engine.put("mappet", FACTORY);
        engine.put("math", new ScriptMath());
        engine.getContext().setAttribute("javax.script.filename", script.getId(), ScriptContext.ENGINE_SCOPE);
        engine.getContext().setAttribute("polyglot.js.allowHostAccess", true, ScriptContext.ENGINE_SCOPE);

        sanitize(engine);
        Mappet.EVENT_BUS.post(new RegisterScriptVariablesEvent(engine));

        return engine;
    }

    private static ScriptEngine createByExtension(String ext) {
        ScriptEngine engine = ScriptEngineRegistry.getManager().getEngineByExtension(ext);

        if (ext.equals("py") && engine != null) {
            try {
                Field fieldInterpreter = Class.forName("org.python.jsr223.PyScriptEngine").getDeclaredField("interp");
                fieldInterpreter.setAccessible(true);
                Object interpreter = fieldInterpreter.get(engine);

                Field fieldcFlags = Class.forName("org.python.util.PythonInterpreter").getDeclaredField("cflags");
                fieldcFlags.setAccessible(true);
                Object cFlags = fieldcFlags.get(interpreter);

                Class.forName("org.python.core.CompilerFlags").getDeclaredField("source_is_utf8").setBoolean(cFlags, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return engine;
    }

    public static ScriptEngine sanitize(ScriptEngine engine) {
        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.remove("load");
        bindings.remove("loadWithNewGlobal");
        bindings.remove("exit");
        bindings.remove("quit");
        return engine;
    }
}
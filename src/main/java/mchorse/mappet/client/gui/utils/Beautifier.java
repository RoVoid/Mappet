package mchorse.mappet.client.gui.utils;

//from MappetExtra

import mchorse.mappet.Mappet;
import mchorse.mappet.api.scripts.engine.ScriptEngineRegistry;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.InputStreamReader;
import java.util.Objects;

public class Beautifier {
    private static ScriptEngine engine;

    private static ScriptEngine getEngine() {
        if (engine != null) return engine;

        try {
            engine = ScriptEngineRegistry.getManager().getEngineByExtension("js");
            engine.eval("var global = this;");
            engine.eval(new InputStreamReader(
                    Objects.requireNonNull(Beautifier.class.getResourceAsStream("/assets/" + Mappet.MOD_ID + "/js/beautify.js"))));
        } catch (Exception e) {
            Mappet.logger.error("Failed to init Beautifier: " + e.getMessage());
            engine = null;
        }

        return engine;
    }

    public static String beautify(String code) throws ScriptException, NoSuchMethodException {
        ScriptEngine e = getEngine();
        if (e == null) return code; // fallback — вернуть как есть
        return (String) ((Invocable) e).invokeFunction("js_beautify", code);
    }
}

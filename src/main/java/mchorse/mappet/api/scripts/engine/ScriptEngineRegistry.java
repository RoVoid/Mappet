package mchorse.mappet.api.scripts.engine;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ScriptEngineRegistry {
    private static ScriptEngineManager manager;

    public static ScriptEngineManager getManager() {
        if (manager == null) manager = new ScriptEngineManager();
        return manager;
    }

    public static List<ScriptEngine> getAllEngines() {
        return getManager().getEngineFactories().stream()
                .filter(factory -> !factory.getExtensions().contains("scala"))
                .map(factory -> {
                    try { return factory.getScriptEngine(); }
                    catch (Exception | NoClassDefFoundError ignored) { return null; }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Run something to avoid it loading first time
     */
    public static void initiateScriptEngines() {
        for (ScriptEngine engine : getAllEngines()) {
            try {
                boolean isPython = Objects.equals(engine.getFactory().getLanguageName(), "python");
                if (!engine.eval(isPython ? "True" : "true").equals(Boolean.TRUE)) {
                    throw new Exception("Something went wrong with " + engine.getFactory().getEngineName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
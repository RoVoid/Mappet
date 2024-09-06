package mchorse.mappet.client.gui.utils;

//from MappetExtra
import mchorse.mappet.Mappet;
import mchorse.mappet.utils.ScriptUtils;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.InputStreamReader;
import java.util.Objects;

public class Beautifier {

    public static ScriptEngine engine;

    public static void init(){
        try {
            engine = ScriptUtils.getEngineByExtension("js");
            engine.eval("var global = this;");
            engine.eval(new InputStreamReader(Objects.requireNonNull(Beautifier.class.getResourceAsStream("/assets/"+ Mappet.MOD_ID +"/js/beautify.js"))));
        } catch (Exception ignored) {
        }
    }

    public static String beautify(String javascriptCode) throws ScriptException, NoSuchMethodException {
        return (String) ((Invocable) engine).invokeFunction("js_beautify", javascriptCode);
    }
}

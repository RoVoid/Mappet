package mchorse.mappet.api.scripts;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import mchorse.mappet.Mappet;
import mchorse.mappet.api.scripts.code.ScriptEvent;
import mchorse.mappet.api.scripts.code.math.ScriptMath;
import mchorse.mappet.api.scripts.engine.ScriptEngineFactory;
import mchorse.mappet.api.scripts.engine.ScriptEngineRegistry;
import mchorse.mappet.api.utils.DataContext;
import mchorse.mappet.api.utils.manager.BaseManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.io.FileUtils;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ScriptManager extends BaseManager<Script> {
    public final Map<String, Object> objects = new HashMap<>();

    private final Map<String, ScriptEngine> uniqueEngines = new HashMap<>();
    private final Map<ScriptEngine, List<ScriptRange>> engineRanges = new IdentityHashMap<>();
    public final Map<String, Script> globalLibraries = new HashMap<>();

    private final Map<Object, ScriptEngine> replEngines = new HashMap<>();
    private String replOutput = "";

    public ScriptManager(File folder) {
        super(folder);
        ScriptEngineRegistry.getAllEngines();
    }

    /* Engine */

    private ScriptEngine getEngine(Script script) throws ScriptException {
        if (!script.unique) return initEngine(script);

        ScriptEngine engine = uniqueEngines.get(script.getId());
        if (engine != null) return engine;

        engine = initEngine(script);
        uniqueEngines.put(script.getId(), engine);
        return engine;
    }

    private ScriptEngine initEngine(Script script) throws ScriptException {
        ScriptEngine engine = ScriptEngineFactory.create(script);
        List<ScriptRange> ranges = new ArrayList<>();
        StringBuilder code = new StringBuilder();

        Set<String> allLibraries = new LinkedHashSet<>(globalLibraries.keySet());
        allLibraries.addAll(script.libraries);
        allLibraries.remove(script.getId());

        int total = 0;
        for (String library : allLibraries) total = loadLibrary(library, code, ranges, total);

        ranges.add(new ScriptRange(total, script.getId()));
        code.append(script.code);

        try {
            engine.eval(code.toString());
        } catch (ScriptException e) {
            throw processScriptException(e, ranges, script.getId());
        }

        engineRanges.put(engine, ranges);
        return engine;
    }

    private int loadLibrary(String id, StringBuilder finalCode, List<ScriptRange> ranges, int total) {
        File file = getScriptFile(id);

        if (file == null || !file.isFile()) {
            Mappet.logger.error("Didn't find library: " + id + ".js");
            return total;
        }

        try {
            String code = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            ranges.add(new ScriptRange(total, id));
            finalCode.append(code).append("\n");

            int lines = 1;
            for (int i = 0; i < code.length(); i++) if (code.charAt(i) == '\n') lines++;
            total += lines;
        } catch (Exception e) {
            Mappet.logger.error("Failed to load library '" + id + "': " + e.getMessage());
        }

        return total;
    }

    /* Execute */

    public Object execute(String id, String function, DataContext context) throws ScriptException, NoSuchMethodException {
        return execute(id, function, context, new Object[0]);
    }

    public Object execute(String id, String function, DataContext context, Object... args) throws ScriptException, NoSuchMethodException {
        Script script = getScript(id);

        if (script == null) {
            Mappet.logger.error("Script '" + id + "' not found");
            return null;
        }

        if (function.isEmpty()) function = "main";

        ScriptEngine engine = getEngine(script);
        ScriptEvent event = new ScriptEvent(context, id, function);

        try {
            return ((Invocable) engine).invokeFunction(function, args.length == 0 ? new Object[]{event} : args);
        } catch (ScriptException e) {
            ScriptException processed = processScriptException(e, engineRanges.get(engine), id);
            Mappet.logger.error(processed.getMessage());
            throw processed;
        }
    }

    private Script getScript(String id) {
        Script script = load(id);
        if (script != null && script.globalLibrary) globalLibraries.put(id, script);
//        if (script == null) {
//            ScriptEngine removed = uniqueEngines.remove(id);
//            if (removed != null) engineRanges.remove(removed);
//            globalLibraries.remove(id);
//        }
        return script;
    }

    private ScriptException processScriptException(ScriptException e, List<ScriptRange> ranges, String scriptId) {
        if (ranges == null) return e;

        ScriptRange range = null;
        for (int i = ranges.size() - 1; i >= 0; i--) {
            ScriptRange possibleRange = ranges.get(i);
            if (possibleRange.lineOffset <= e.getLineNumber() - 1) {
                range = possibleRange;
                break;
            }
        }
        if (range == null) return e;

        int lineNumber = e.getLineNumber() - range.lineOffset;
        String message = e.getMessage()
                .replaceFirst(scriptId, range.script + " (in " + scriptId + ")")
                .replaceFirst("at line number \\d+", "at line number " + lineNumber);

        return new ScriptException(message, range.script, lineNumber, e.getColumnNumber());
    }

    public void executeInline(Script script, DataContext context) throws ScriptException {
        ScriptEngine engine = ScriptEngineFactory.create(script);

        ScriptEvent event = new ScriptEvent(context, "__inline__", "");
        engine.put("c", event);
        engine.put("event", event);
        engine.put("s", event.getSubject());
        engine.put("subject", event.getSubject());

        for (String library : globalLibraries.keySet()) {
            File file = getScriptFile(library);
            if (file == null || !file.isFile()) continue;
            try {
                engine.eval(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
            } catch (Exception e) {
                Mappet.logger.error("Failed to load library '" + library + "': " + e.getMessage());
            }
        }

        engine.eval(script.code);
    }

    /* REPL */

    public String executeRepl(Object key, String code) throws ScriptException {
        ScriptEngine engine = replEngines.get(key);
        replOutput = "";

        if (engine == null) {
            engine = new NashornScriptEngineFactory().getScriptEngine("--language=es6", "-scripting");
            ScriptEngineFactory.sanitize(engine);

            engine.put("____manager____", this);
            engine.put("mappet", ScriptEngineFactory.FACTORY);
            engine.put("math", new ScriptMath());

            ScriptEvent event = new ScriptEvent(prepareContext(key), "", "");
            engine.put("c", event);
            engine.put("event", event);
            engine.put("s", event.getSubject());
            engine.put("subject", event.getSubject());

            engine.eval("var __p__ = print; print = function(message) { ____manager____.replPrint(message); __p__(message); };");
            replEngines.put(key, engine);
        }

        Object object = engine.eval(code);
        if (replOutput.isEmpty()) replPrint(object);

        return replOutput;
    }

    public void replPrint(Object object) {
        replOutput += (object == null ? TextFormatting.GRAY + "undefined" : object) + "\n";
    }

    // TODO: Can moveTo DataContext ?
    public DataContext prepareContext(Object key) {
        if (key instanceof EntityPlayerMP) return new DataContext((EntityPlayerMP) key);
        if (key instanceof MinecraftServer) return new DataContext((MinecraftServer) key);
        if (key instanceof EntityLiving) return new DataContext((EntityLiving) key);
        return new DataContext(FMLCommonHandler.instance().getMinecraftServerInstance());
    }

    /* IManager */

    @Override
    protected Script createData(String id, NBTTagCompound tag) {
        Script script = new Script();
        if (tag != null) script.deserializeNBT(tag);
        return script;
    }

    @Override
    public Script load(String id) {
        Script script = super.load(id);
        File scriptFile = getScriptFile(id);

        if (scriptFile == null || !scriptFile.isFile()) return script;

        try {
            String code = FileUtils.readFileToString(scriptFile, StandardCharsets.UTF_8);
            if (script == null) script = new Script();
            script.setId(id);
            script.code = code.replace("\t", "    ").replace("\r", "");
        } catch (Exception e) {
            Mappet.logger.error("Failed to load script file '" + id + "': " + e.getMessage());
        }

        return script;
    }

    @Override
    public boolean save(String id, NBTTagCompound tag) {
        String code = new String(tag.getByteArray("Code"), StandardCharsets.UTF_8);
        tag.removeTag("Code");

        if (!super.save(id, tag)) return false;

        try {
            FileUtils.writeStringToFile(getScriptFile(id), code, StandardCharsets.UTF_8);
        } catch (Exception e) {
            Mappet.logger.error("Failed to save script file '" + id + "': " + e.getMessage());
            return false;
        }

        uniqueEngines.remove(id);
        globalLibraries.remove(id);

        Script script = load(id);
        if (script != null && script.globalLibrary) globalLibraries.put(id, script);

        return true;
    }

    @Override
    public boolean exists(String id) {
        File scriptFile = getScriptFile(id);
        return super.exists(id) || scriptFile != null && scriptFile.exists();
    }

    @Override
    public boolean rename(String id, String newId) {
        if (!super.rename(id, newId)) return false;

        File scriptFile = getScriptFile(id);
        if (scriptFile != null) scriptFile.renameTo(getScriptFile(newId));

        ScriptEngine engine = uniqueEngines.remove(id);
        if (engine != null) uniqueEngines.put(newId, engine);

        Script lib = globalLibraries.remove(id);
        if (lib != null) {
            Script reloaded = load(newId);
            if (reloaded != null && reloaded.globalLibrary) globalLibraries.put(newId, reloaded);
        }

        return true; // don't check file rename
    }

    @Override
    public boolean delete(String id) {
        if (!super.delete(id)) return false;

        File scriptFile = getScriptFile(id);
        if (scriptFile != null) scriptFile.delete();

        ScriptEngine removed = uniqueEngines.remove(id);
        if (removed != null) engineRanges.remove(removed);
        globalLibraries.remove(id);
        return true; // don't check file delete
    }

    @Override
    public void renameFolder(String oldPath, String newPath) {
        String oldPrefix = oldPath.endsWith("/") ? oldPath : oldPath + "/";
        String newPrefix = newPath.endsWith("/") ? newPath : newPath + "/";
        evictByPrefix(oldPrefix);
        super.renameFolder(oldPath, newPath);
        reloadByPrefix(newPrefix);
    }

    @Override
    public void deleteFolder(String path) {
        String prefix = path.endsWith("/") ? path : path + "/";
        evictByPrefix(prefix);
        super.deleteFolder(path);
    }

    private void evictByPrefix(String prefix) {
        uniqueEngines.entrySet().removeIf(entry -> {
            if (!entry.getKey().startsWith(prefix)) return false;
            engineRanges.remove(entry.getValue());
            return true;
        });
        globalLibraries.keySet().removeIf(id -> id.startsWith(prefix));
    }

    private void reloadByPrefix(String prefix) {
        for (String id : getPaths()) {
            if (!id.startsWith(prefix)) continue;
            try {
                Script script = load(id);
                if (script == null) continue;
                if (script.globalLibrary) globalLibraries.put(id, script);
                if (script.unique) uniqueEngines.put(id, initEngine(script));
            } catch (Exception e) {
                Mappet.logger.error("Failed to reload script '" + id + "' after folder rename: " + e.getMessage());
            }
        }
    }

    public void initiateAllScripts() {
        for (String id : getPaths()) {
            if(BaseManager.isFolder(id)) continue;
            try {
                Script script = load(id);
                if (script == null) continue;

                if (script.globalLibrary) globalLibraries.put(id, script);

                if (script.unique) uniqueEngines.put(id, initEngine(script));
            } catch (Exception e) {
                Mappet.logger.error("Failed to initiate script '" + id + "': " + e.getMessage());
            }
        }
    }

    public File getScriptFile(String id) {
        if (root == null || id == null) return null;
        return new File(root, id.lastIndexOf('.') > id.lastIndexOf('/') ? id : id + ".js");
    }
}
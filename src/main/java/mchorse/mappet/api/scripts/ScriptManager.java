package mchorse.mappet.api.scripts;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.scripts.code.ScriptEvent;
import mchorse.mappet.api.scripts.code.ScriptFactory;
import mchorse.mappet.api.scripts.code.ScriptMath;
import mchorse.mappet.api.utils.DataContext;
import mchorse.mappet.api.utils.manager.BaseManager;
import mchorse.mappet.utils.ScriptUtils;
import mchorse.mappet.utils.Utils;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.io.FileUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ScriptManager extends BaseManager<Script> {
    public final Map<String, Object> objects = new HashMap<>();

    private final Map<String, Script> uniqueScripts = new HashMap<>();
    public Map<String, Script> globalLibraries = new HashMap<>();
    private final Map<Object, ScriptEngine> repls = new HashMap<>();
    private String replOutput = "";

    public ScriptManager(File folder) {
        super(folder);
        ScriptUtils.getAllEngines();
    }

    /**
     * Execute a REPL code that came from a player
     */
    public String executeRepl(Object key, String code) throws ScriptException {
        ScriptEngine engine = repls.get(key);

        replOutput = "";

        if (engine == null) {
            engine = ScriptUtils.sanitize(ScriptUtils.getEngineByExtension("js"));

            engine.put("____manager____", this);
            engine.put("mappet", new ScriptFactory());
            engine.put("math", new ScriptMath());

            ScriptEvent event = new ScriptEvent(prepareContext(key), "", "");
            engine.put("c", event);
            engine.put("s", event.getSubject());

            engine.eval("var __p__ = print; print = function(message) { ____manager____.replPrint(message); __p__(message); };");

            repls.put(key, engine);
        }

        Object object = engine.eval(code);

        if (replOutput.isEmpty()) {
            replPrint(object);
        }

        return replOutput;
    }

    public Object eval(ScriptEngine engine, String code, DataContext context) throws ScriptException {
        ScriptEvent event = new ScriptEvent(context, "", "");
        engine.put("mappet", new ScriptFactory());
        engine.put("math", new ScriptMath());
        engine.put("c", event);

        return engine.eval(code);
    }

    public DataContext prepareContext(Object key) {
        DataContext context;

        if (key instanceof EntityPlayerMP) {
            context = new DataContext((EntityPlayerMP) key);
        } else if (key instanceof MinecraftServer) {
            context = new DataContext((MinecraftServer) key);
        } else if (key instanceof EntityLiving) {
            context = new DataContext((EntityLiving) key);
        } else {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            context = new DataContext(server);
        }

        return context;
    }

    public void replPrint(Object object) {
        if (object == null) {
            object = TextFormatting.GRAY + "undefined";
        }

        replOutput += object + "\n";
    }

    /**
     * Execute given script
     */
    public Object execute(String id, String function, DataContext context) throws ScriptException, NoSuchMethodException {
        Script script = getScript(id);
        if (script == null) {
            Mappet.logger.error("Failed to execute script '" + id + "', maybe is not exists");
            return null;
        }
        return script.execute(function, context);
    }

    public Object execute(String id, String function, DataContext context, Object... args) throws ScriptException, NoSuchMethodException {
        Script script = getScript(id);

        return script == null ? null : script.execute(function, context, args);
    }

    private Script getScript(String id) throws ScriptException {
        Script script = uniqueScripts.get(id);

        if (script == null) {
            script = load(id);

            if (script != null && script.unique) {
                uniqueScripts.put(id, script);
            }

            if (script != null && script.globalLibrary) {
                globalLibraries.put(id, script);
            }
        }

        if (script == null) {
            return null;
        }

        script.start(this);

        return script;
    }

    @Override
    public Collection<String> getKeys() {
        if (folder == null) {
            return Collections.emptySet();
        }

        Set<String> set = new HashSet<>();

        recursiveFind(set, folder, "");

        if (folder.listFiles() == null) return set;

        for (File file : Objects.requireNonNull(folder.listFiles())) {
            String name = file.getName();

            if (!name.endsWith(".json")) {
                continue;
            }

            if (file.isFile() && isData(file)) {
                set.add(name.replace(".json", ""));
            }
        }

        return set;
    }

    @Override
    protected Script createData(String id, NBTTagCompound tag) {
        Script script = new Script();

        if (tag != null) {
            script.deserializeNBT(tag);
        }

        return script;
    }

    /* Custom implementation of base manager to support .js files */

    @Override
    public Script load(String id) {
        Script script = super.load(id);
        File scriptFile = getScriptFile(id);

        if (scriptFile != null && scriptFile.isFile()) {
            try {
                String code = FileUtils.readFileToString(scriptFile, Utils.getCharset());

                if (script == null) {
                    script = new Script();
                }

                script.code = code.replaceAll("\t", "    ").replaceAll("\r", "");
            } catch (Exception ignored) {
            }
        }

        return script;
    }

    @Override
    public boolean save(String id, NBTTagCompound tag) {
        String code = new String(tag.getByteArray("Code"), StandardCharsets.UTF_8);

        tag.removeTag("Code");

        boolean result = super.save(id, tag);

        if (!code.trim().isEmpty()) {
            try {
                FileUtils.writeStringToFile(getScriptFile(id), code, Utils.getCharset());

                result = true;
            } catch (Exception ignored) {
            }
        }

        if (result) {
            uniqueScripts.remove(id);

            Script script = load(id);

            if (script != null && script.unique) {
                uniqueScripts.put(id, script);
            }

            if (script != null && script.globalLibrary) {
                globalLibraries.put(id, script);
            }
        }

        return result;
    }

    /* Custom implementation of folder manager to support .js files */

    @Override
    public boolean exists(String name) {
        File scriptFile = getScriptFile(name);

        return super.exists(name) || (scriptFile != null && scriptFile.exists());
    }

    @Override
    public boolean rename(String id, String newId) {
        File scriptFile = getScriptFile(id);
        boolean result = super.rename(id, newId);

        if (scriptFile != null && scriptFile.exists()) {
            return scriptFile.renameTo(getScriptFile(newId)) || result;
        }

        return result;
    }

    @Override
    public boolean delete(String name) {
        boolean result = super.delete(name);
        File scriptFile = getScriptFile(name);

        return (scriptFile != null && scriptFile.delete()) || result;
    }

    @Override
    protected boolean isData(File file) {
        return super.isData(file) || !file.getName().endsWith(".json");
    }

    public File getScriptFile(String id) {
        if (folder == null) return null;
        return new File(folder, id.lastIndexOf(".") != -1 ? id : id + ".js");
    }

    public void initiateAllScripts() {
        for (String id : getKeys()) {
            try {
                Script script = load(id);

                if (script != null && script.unique) {
                    uniqueScripts.put(id, script);
                    script.start(this);
                }

                if (script != null && script.globalLibrary) {
                    globalLibraries.put(id, script);
                }
            } catch (Exception e) {
                Mappet.logger.error(e.getMessage());
            }
        }
    }
}
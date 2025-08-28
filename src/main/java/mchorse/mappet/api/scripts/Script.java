package mchorse.mappet.api.scripts;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import mchorse.mappet.Mappet;
import mchorse.mappet.api.scripts.code.ScriptEvent;
import mchorse.mappet.api.scripts.code.ScriptFactory;
import mchorse.mappet.api.scripts.code.math.ScriptMath;
import mchorse.mappet.api.utils.AbstractData;
import mchorse.mappet.api.utils.DataContext;
import mchorse.mappet.events.RegisterScriptVariablesEvent;
import mchorse.mappet.utils.ScriptUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Script extends AbstractData {
    public String code = "";

    public boolean unique = true;

    public boolean globalLibrary = false;

    public List<String> libraries = new ArrayList<>();

    private ScriptEngine engine;

    private List<ScriptRange> ranges;

    public Script() {
    }

    public void start(ScriptManager manager) throws ScriptException {
        if (engine != null) return;

        initializeEngine();
        configureEngineContext();
        registerScriptVariables();

        Set<String> uniqueImports = new HashSet<>();
        StringBuilder finalCode = new StringBuilder();
        Set<String> alreadyLoaded = new HashSet<>();
        int total = 0;

        List<String> allLibraries = new ArrayList<>();
        allLibraries.addAll(manager.globalLibraries.keySet());
        allLibraries.addAll(libraries);

        // import "module.js"
        String[] lines = code.split("\n");
        List<String> newLines = new ArrayList<>();
        boolean isComment = false;

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.startsWith("//")) {
                newLines.add(line);
                continue;
            }

            if (!isComment && trimmed.startsWith("/*")) {
                isComment = true;
                newLines.add(line);
                if (trimmed.endsWith("*/")) isComment = false;
                continue;
            }

            if (isComment) {
                newLines.add(line);
                if (trimmed.endsWith("*/")) isComment = false;
                continue;
            }

            if (!trimmed.startsWith("import ")) {
                newLines.add(line);
                continue;
            }

            String lib = trimmed.substring(7).trim();
            if (lib.length() < 3) {
                newLines.add(line);
                continue;
            }

            char quote = lib.charAt(0);
            if (quote != '"' && quote != '\'') {
                newLines.add(line);
                continue;
            }

            int index = 1;
            do index = lib.indexOf(quote, index); while (index != -1 && lib.charAt(index - 1) == '\\');

            if (index == -1) {
                newLines.add(line);
                continue;
            }

            String libName = lib.substring(1, index);
            allLibraries.add(libName);
        }
        code = String.join("\n", newLines);

        for (String library : allLibraries) {
            if (library.equals(getId()) || alreadyLoaded.contains(library)) continue;
            total = processLibrary(manager, library, uniqueImports, finalCode, total);
            alreadyLoaded.add(library);
        }

        finalCode.append(code);

        if (ranges != null) ranges.add(new ScriptRange(total, getId()));

        engine.put("mappet", new ScriptFactory());
        engine.put("math", new ScriptMath());

        engine.eval(finalCode.toString());
    }

    private void initializeEngine() throws ScriptException {
        String extension = getScriptExtension();

        engine = ScriptUtils.getEngineByExtension(extension);

        if (engine == null) {
            String message = "Looks like Mappet can't find script engine for a \"" + getScriptExtension() + "\" file extension.";
            throw new ScriptException(message, getId(), -1);
        }

        ScriptUtils.sanitize(engine);
    }

    private void configureEngineContext() {
        String extension = getScriptExtension();
        if (extension.equals("js")) {
            NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
            engine = factory.getScriptEngine("--language=es6", "-scripting");
        }
        engine.getContext().setAttribute("javax.script.filename", getId(), ScriptContext.ENGINE_SCOPE);
        engine.getContext().setAttribute("polyglot.js.allowHostAccess", true, ScriptContext.ENGINE_SCOPE);
    }

    private void registerScriptVariables() {
        Mappet.EVENT_BUS.post(new RegisterScriptVariablesEvent(engine));
    }

    private int processLibrary(ScriptManager manager, String library, Set<String> uniqueImports, StringBuilder finalCode, int total) {
        try {
            File scriptFile = manager.getScriptFile(library);
            if (scriptFile == null) {
                Mappet.logger.error("[Mappet] Didn't find " + library + ".js");
                return total;
            }
            String code = FileUtils.readFileToString(scriptFile, StandardCharsets.UTF_8);

            finalCode.append(code);
            finalCode.append("\n");

            if (ranges == null) ranges = new ArrayList<>();

            ranges.add(new ScriptRange(total, library));

            total += StringUtils.countMatches(code, "\n") + 1;
        } catch (Exception e) {
            System.err.println("[Mappet] Script library " + library + ".js failed to load...");
            Mappet.logger.error(e.getMessage());
        }

        return total;
    }

    public String getScriptExtension() {
        String id = getId();
        int index = id.lastIndexOf('.');
        return index >= 0 ? id.substring(index + 1) : "js";
    }

    public Object execute(String function, DataContext context, Object... args) throws ScriptException, NoSuchMethodException {
        if (function.isEmpty()) function = "main";

        engine.put("context", context);
        System.out.println("Script's Executing " + function);
        try {
            return ((Invocable) engine).invokeFunction(function, args);
        } catch (ScriptException e) {
            ScriptException exception = processScriptException(e);
            Mappet.logger.error(e.getMessage());
            throw exception == null ? e : exception;
        }
    }

    public Object execute(String function, DataContext context) throws ScriptException, NoSuchMethodException {
        return execute(function, context, new ScriptEvent(context, getId(), function));
    }

    private ScriptException processScriptException(ScriptException e) {
        if (ranges == null) return null;
        ScriptRange range = null;

        for (int i = ranges.size() - 1; i >= 0; i--) {
            ScriptRange possibleRange = ranges.get(i);
            if (possibleRange.lineOffset <= e.getLineNumber() - 1) {
                range = possibleRange;
                break;
            }
        }

        if (range == null) return null;

        String message = e.getMessage();
        int lineNumber = e.getLineNumber() - range.lineOffset;
        message = message.replaceFirst(getId(), range.script + " (in " + getId() + ")");
        message = message.replaceFirst("at line number \\d+", "at line number " + lineNumber);
        return new ScriptException(message, range.script, lineNumber, e.getColumnNumber());
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList librariesNBT = new NBTTagList();

        for (String library : libraries) {
            librariesNBT.appendTag(new NBTTagString(library));
        }

        tag.setBoolean("Unique", unique);
        tag.setBoolean("GlobalLibrary", globalLibrary);
        tag.setTag("Libraries", librariesNBT);
        tag.setByteArray("Code", code.getBytes(StandardCharsets.UTF_8));

        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        unique = tag.getBoolean("Unique");
        if (tag.hasKey("Libraries", Constants.NBT.TAG_LIST)) {
            NBTTagList librariesNBT = tag.getTagList("Libraries", Constants.NBT.TAG_STRING);
            libraries.clear();
            for (int i = 0, c = librariesNBT.tagCount(); i < c; i++) {
                libraries.add(librariesNBT.getStringTagAt(i));
            }
        }
        if (tag.hasKey("GlobalLibrary")) globalLibrary = tag.getBoolean("GlobalLibrary");
        code = new String(tag.getByteArray("Code"), StandardCharsets.UTF_8);
    }
}
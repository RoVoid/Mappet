package mchorse.mappet.api.scripts;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import mchorse.mappet.Mappet;
import mchorse.mappet.api.scripts.code.ScriptEvent;
import mchorse.mappet.api.scripts.code.ScriptFactory;
import mchorse.mappet.api.scripts.code.ScriptMath;
import mchorse.mappet.api.utils.AbstractData;
import mchorse.mappet.api.utils.DataContext;
import mchorse.mappet.events.RegisterScriptVariablesEvent;
import mchorse.mappet.utils.ScriptUtils;
import mchorse.mappet.utils.Utils;
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
        if (this.engine == null) {
            initializeEngine();
            configureEngineContext();
            registerScriptVariables();

            Set<String> uniqueImports = new HashSet<>();
            StringBuilder finalCode = new StringBuilder();
            Set<String> alreadyLoaded = new HashSet<>();
            int total = 0;

            boolean isKotlin = isKotlinEngine();

            List<String> allLibraries = new ArrayList<>();
            allLibraries.addAll(manager.globalLibraries.keySet());
            allLibraries.addAll(this.libraries);

            for (String library : allLibraries) {
                if (shouldSkipLibrary(library, alreadyLoaded)) continue;
                total = processLibrary(manager, library, isKotlin, uniqueImports, finalCode, total);
                alreadyLoaded.add(library);
            }
            processScriptCode(isKotlin, uniqueImports, finalCode);

            if (this.ranges != null) this.ranges.add(new ScriptRange(total, this.getId()));

            this.engine.put("mappet", new ScriptFactory());
            this.engine.put("math", new ScriptMath());
            evalEngineCode(isKotlin, uniqueImports, finalCode);
        }
    }

    private void initializeEngine() throws ScriptException {
        String extension = this.getScriptExtension();

        if (extension.equals("kts")) {
            System.setProperty("kotlin.jsr223.experimental.resolve.dependencies.from.context.classloader", "true");
        }

        this.engine = ScriptUtils.getEngineByExtension(extension);

        if (this.engine == null) {
            String message = "Looks like Mappet can't find script engine for a \"" + this.getScriptExtension() + "\" file extension.";
            throw new ScriptException(message, this.getId(), -1);
        }

        this.engine = ScriptUtils.sanitize(this.engine);
    }

    private void configureEngineContext() {
        String extension = this.getScriptExtension();
        if (extension.equals("js")) {
            NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
            this.engine = factory.getScriptEngine("--language=es6", "-scripting");
        }
        this.engine.getContext().setAttribute("javax.script.filename", this.getId(), ScriptContext.ENGINE_SCOPE);
        this.engine.getContext().setAttribute("polyglot.js.allowHostAccess", true, ScriptContext.ENGINE_SCOPE);
    }

    private void registerScriptVariables() {
        Mappet.EVENT_BUS.post(new RegisterScriptVariablesEvent(this.engine));
    }

    private boolean isKotlinEngine() {
        return this.engine.getFactory().getLanguageName().equals("kotlin");
    }

    private boolean shouldSkipLibrary(String library, Set<String> alreadyLoaded) {
        return library.equals(this.getId()) || alreadyLoaded.contains(library);
    }

    private int processLibrary(ScriptManager manager, String library, boolean isKotlin, Set<String> uniqueImports, StringBuilder finalCode, int total) {
        try {
            File scriptFile = manager.getScriptFile(library);
            String code = FileUtils.readFileToString(scriptFile, Utils.getCharset());

            if (isKotlin) code = processKotlinCode(code, uniqueImports);

            finalCode.append(code);
            finalCode.append("\n");

            if (this.ranges == null) this.ranges = new ArrayList<>();

            this.ranges.add(new ScriptRange(total, library));

            total += StringUtils.countMatches(code, "\n") + 1;
        } catch (Exception e) {
            System.err.println("[Mappet] Script library " + library + ".js failed to load...");
            Mappet.logger.error(e.getMessage());
        }

        return total;
    }

    private String processKotlinCode(String code, Set<String> uniqueImports) {
        String[] lines = code.split("\n");
        StringBuilder currentCode = new StringBuilder();

        for (String line : lines) {
            if (line.trim().startsWith("import")) {
                uniqueImports.add(line.trim());
            } else {
                currentCode.append(line);
                currentCode.append("\n");
            }
        }

        return currentCode.toString();
    }

    private void processScriptCode(boolean isKotlin, Set<String> uniqueImports, StringBuilder finalCode) {
        if (isKotlin) {
            String processedCode = processKotlinCode(this.code, uniqueImports);
            finalCode.insert(0, processedCode);
        } else {
            finalCode.append(this.code);
        }
    }


    private final String DEFAULT_KOTLIN_IMPORTS =
            "import mchorse.metamorph.api.morphs.AbstractMorph" + "\n" +
                    "import mchorse.mappet.entities.EntityNpc" + "\n" +
                    "import net.minecraft.potion.Potion" + "\n" +
                    "import net.minecraft.entity.Entity" + "\n" +
                    "import net.minecraft.inventory.IInventory" + "\n" +
                    "import net.minecraft.entity.player.EntityPlayerMP" + "\n" +
                    "import net.minecraft.item.Item" + "\n" +
                    "import net.minecraft.item.ItemStack" + "\n" +
                    "import net.minecraft.tileentity.TileEntity" + "\n" +
                    "import net.minecraft.block.state.IBlockState" + "\n" +
                    "import net.minecraft.util.EnumParticleTypes" + "\n" +

                    "import javax.vecmath.*" + "\n" +
                    "import java.lang.Math.*" + "\n" +

                    "import mchorse.mappet.api.scripts.user.*" + "\n" +
                    "import mchorse.mappet.api.scripts.user.blocks.*" + "\n" +
                    "import mchorse.mappet.api.scripts.user.data.*" + "\n" +
                    "import mchorse.mappet.api.scripts.user.entities.*" + "\n" +
                    "import mchorse.mappet.api.scripts.user.items.*" + "\n" +
                    "import mchorse.mappet.api.scripts.user.mappet.*" + "\n" +
                    "import mchorse.mappet.api.scripts.user.nbt.*" + "\n" +

                    "import mchorse.mappet.api.scripts.code.*" + "\n" +
                    "import mchorse.mappet.api.scripts.code.blocks.*" + "\n" +
                    "import mchorse.mappet.api.scripts.code.entities.*" + "\n" +
                    "import mchorse.mappet.api.scripts.code.items.*" + "\n" +
                    "import mchorse.mappet.api.scripts.code.mappet.*" + "\n" +
                    "import mchorse.mappet.api.scripts.code.nbt.*" + "\n";

    private void evalEngineCode(boolean isKotlin, Set<String> uniqueImports, StringBuilder finalCode) throws ScriptException {
        if (isKotlin) {
            String allCode = DEFAULT_KOTLIN_IMPORTS + "\n" + String.join("\n", uniqueImports) + "\n" + finalCode.toString();
            this.engine.eval(allCode);
        } else {
            this.engine.eval(finalCode.toString());
        }
    }

    public String getScriptExtension() {
        String id = this.getId();
        int index = id.lastIndexOf('.');

        return index >= 0 ? id.substring(index + 1) : "js";
    }

    public Object execute(String function, DataContext context, Object... args) throws ScriptException, NoSuchMethodException {
        if (function.isEmpty()) {
            function = "main";
        }

        this.engine.put("context", context);

        try {
            return ((Invocable) this.engine).invokeFunction(function, args);
        } catch (ScriptException e) {
            ScriptException exception = processScriptException(e);

            Mappet.logger.error(e.getMessage());

            throw exception == null ? e : exception;
        }
    }

    public Object execute(String function, DataContext context) throws ScriptException, NoSuchMethodException {
        return this.execute(function, context, new ScriptEvent(context, this.getId(), function));
    }

    private ScriptException processScriptException(ScriptException e) {
        if (this.ranges == null) return null;
        ScriptRange range = null;
        for (int i = this.ranges.size() - 1; i >= 0; i--) {
            ScriptRange possibleRange = this.ranges.get(i);
            if (possibleRange.lineOffset <= e.getLineNumber() - 1) {
                range = possibleRange;
                break;
            }
        }
        if (range != null) {
            String message = e.getMessage();
            int lineNumber = e.getLineNumber() - range.lineOffset;
            message = message.replaceFirst(this.getId(), range.script + " (in " + this.getId() + ")");
            message = message.replaceFirst("at line number \\d+", "at line number " + lineNumber);
            return new ScriptException(message, range.script, lineNumber, e.getColumnNumber());
        }
        return null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList libraries = new NBTTagList();

        for (String library : this.libraries) {
            libraries.appendTag(new NBTTagString(library));
        }

        tag.setBoolean("Unique", this.unique);
        tag.setBoolean("GlobalLibrary", this.globalLibrary);
        tag.setTag("Libraries", libraries);
        tag.setByteArray("Code", this.code.getBytes(StandardCharsets.UTF_8));

        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        this.unique = tag.getBoolean("Unique");

        if (tag.hasKey("Libraries", Constants.NBT.TAG_LIST)) {
            NBTTagList libraries = tag.getTagList("Libraries", Constants.NBT.TAG_STRING);

            this.libraries.clear();

            for (int i = 0, c = libraries.tagCount(); i < c; i++) {
                this.libraries.add(libraries.getStringTagAt(i));
            }
        }

        if (tag.hasKey("GlobalLibrary")) {
            this.globalLibrary = tag.getBoolean("GlobalLibrary");
        }

        this.code = new String(tag.getByteArray("Code"), StandardCharsets.UTF_8);
    }
}
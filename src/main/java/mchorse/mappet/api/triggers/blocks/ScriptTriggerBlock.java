package mchorse.mappet.api.triggers.blocks;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.scripts.Script;
import mchorse.mappet.api.utils.DataContext;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;

public class ScriptTriggerBlock extends DataTriggerBlock {
    public String function = "";

    public boolean inline = false;
    public String code = "";

    public ScriptTriggerBlock() {
        super();
    }

    public ScriptTriggerBlock(String string, String function) {
        super(string);
        this.function = function;
    }

    @Override
    public boolean isEmpty() {
        return inline ? code.isEmpty() : string.isEmpty();
    }

    @Override
    public String name() {
        return string.isEmpty() || function.isEmpty() ? super.name()
                : string + " (" + TextFormatting.GRAY + function + TextFormatting.RESET + ")";
    }

    @Override
    public void trigger(DataContext context) {
        if (inline && !code.isEmpty()) {
            try {
                Script script = new Script();
                script.setId("__inline__");
                script.code = code;
                script.unique = false;

                Mappet.scripts.executeInline(script, context);
            } catch (Exception e) {
                Mappet.logger.error(e.getMessage());
            }
        }

        if (!string.isEmpty()) {
            try {
                DataContext data = apply(context);
                Mappet.scripts.execute(string, function.trim(), data);
                if (!context.isCanceled()) context.cancel(data.isCanceled());
            } catch (Exception e) {
                Mappet.logger.error(string + " - " + e.getMessage());
            }
        }
    }

    @Override
    protected String getKey() {
        return "Script";
    }

    @Override
    protected void serializeNBT(NBTTagCompound tag) {
        super.serializeNBT(tag);
        tag.setString("Function", function);
        tag.setBoolean("Inline", inline);
        tag.setString("Code", code);
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        super.deserializeNBT(tag);
        function = tag.getString("Function");
        inline = tag.getBoolean("Inline");
        code = tag.getString("Code");
    }
}
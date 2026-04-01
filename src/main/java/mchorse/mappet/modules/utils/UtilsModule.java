package mchorse.mappet.modules.utils;

import mchorse.mclib.config.ConfigBuilder;
import mchorse.mclib.config.values.ValueBoolean;
import mchorse.mclib.config.values.ValueInt;

public class UtilsModule
{
    public ValueInt codeSearchColor;
    public ValueInt codeSearchBackgroundColor;
    public ValueBoolean codeSearchOnTop;

    public ValueBoolean beautifierIndentEmptyLines;
    public ValueBoolean beautifierUnindentChainedMethods;
    public ValueBoolean beautifierBreakChainedMethods;

    private static UtilsModule instance;

    public static UtilsModule getInstance()
    {
        if (instance == null)
        {
            instance = new UtilsModule();
        }

        return instance;
    }

    public void addConfigOptions(ConfigBuilder builder)
    {
        builder.category("utils_module");

        builder.category("utils_module.code_search");
        this.codeSearchColor = (ValueInt) builder.getInt("code_search_color", 0x22FFFFAA).colorAlpha().clientSide();
        this.codeSearchBackgroundColor = (ValueInt) builder.getInt("code_search_background_color", 0xCC000000).colorAlpha().clientSide();
        this.codeSearchOnTop = (ValueBoolean) builder.getBoolean("code_search_on_top", false).clientSide();
        builder.getCategory().markClientSide();

        builder.category("utils_module.beautifier");
        this.beautifierIndentEmptyLines = (ValueBoolean) builder.getBoolean("indent_empty_lines", false).clientSide();
        this.beautifierUnindentChainedMethods = (ValueBoolean) builder.getBoolean("unindent_chained_methods", false).clientSide();
        this.beautifierBreakChainedMethods = (ValueBoolean) builder.getBoolean("break_chained_methods", false).clientSide();
        builder.getCategory().markClientSide();
    }
}

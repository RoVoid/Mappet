package mchorse.mappet.config;

import mchorse.mclib.McLib;
import mchorse.mclib.config.ConfigBuilder;
import mchorse.mclib.config.values.ValueBoolean;
import mchorse.mclib.config.values.ValueInt;
import mchorse.mclib.config.values.ValueString;
import mchorse.mclib.events.RegisterConfigEvent;

import static mchorse.mappet.Mappet.MOD_ID;
import static mchorse.mappet.utils.Colors.shiftHue;

public final class MappetConfig {
    public static ValueBoolean generalDataCaching;
    public static ValueBoolean loadCustomSoundsOnLogin;
    public static ValueBoolean immediatelyOpenLink;
    public static ValueBoolean denyClientSettingChanges;
    public static ValueString trustedDomains;

    public static ValueBoolean npcsPeacefulDamage;
    public static ValueRights npcToolRight;

    public static ValueInt eventMaxExecutions;
    public static ValueBoolean eventUseServerForCommands;
    public static ValueBoolean enableForgeTriggers;

    public static ValueInt nodePulseBackgroundColor;
    public static ValueBoolean nodePulseBackgroundMcLibPrimary;
    public static ValueInt nodeThickness;
    public static ValueInt enumColor;

    public static ValueBoolean questsPreviewRewards;

    public static ValueSyntaxStyle scriptEditorSyntaxStyle;
    public static ValueBoolean scriptEditorSounds;
    public static ValueBoolean scriptUIDebug;
    public static ValueCodeEditor scriptCodeTemplate;
    public static ValueInt codeSearchColor;
    public static ValueInt codeSearchBackgroundColor;
    public static ValueBoolean codeSearchOnTop;

    public static void register(RegisterConfigEvent event) {
        ConfigBuilder builder = event.createBuilder(MOD_ID);

        builder.category("general");
        builder.register(new ValueButtons("buttons").clientSide());
        generalDataCaching = builder.getBoolean("data_caching", true);
        enableForgeTriggers = builder.getBoolean("enable_forge_triggers", false);
        loadCustomSoundsOnLogin = builder.getBoolean("load_custom_sounds_on_login", false);
        immediatelyOpenLink = builder.getBoolean("immediately_open_link", false);
        denyClientSettingChanges = builder.getBoolean("deny_client_setting_changes", true);
        trustedDomains = builder.getString("trusted_domains", "");

        builder.category("npc");
        npcsPeacefulDamage = builder.getBoolean("peaceful_damage", true);
        builder.register(npcToolRight = new ValueRights("npc_tool_for", ValueRights.Right.OPERATOR));

        eventMaxExecutions = builder.category("events").getInt("max_executions", 10000, 100, 1000000);
        eventUseServerForCommands = builder.getBoolean("use_server_for_commands", false);

        builder.category("gui").getCategory().markClientSide();
        nodePulseBackgroundColor = builder.getInt("pulse_background_color", 0x000000).color();
        nodePulseBackgroundMcLibPrimary = builder.getBoolean("pulse_background_mclib", false);
        nodeThickness = builder.getInt("node_thickness", 3, 0, 20);
        enumColor = builder.getInt("enum_color", shiftHue(McLib.primaryColor.get(), 0.21f)).color();
        questsPreviewRewards = builder.getBoolean("quest_preview_rewards", true);

        builder.category("script_editor").getCategory().markClientSide();
        builder.register(scriptEditorSyntaxStyle = new ValueSyntaxStyle("syntax_style"));
        scriptEditorSounds = builder.getBoolean("sounds", true);
        scriptUIDebug = builder.getBoolean("ui_debug", false);
        codeSearchColor = builder.getInt("code_search_color", 0x22FFFFAA).colorAlpha();
        codeSearchBackgroundColor = builder.getInt("code_search_background_color", 0xCC000000).colorAlpha();
        codeSearchOnTop = builder.getBoolean("code_search_on_top", false);
        builder.register(scriptCodeTemplate = new ValueCodeEditor("code_template"));
    }
}

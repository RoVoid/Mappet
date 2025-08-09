package mchorse.mappet;

import mchorse.mappet.utils.ValueButtons;
import mchorse.mappet.utils.ValueCodeEditor;
import mchorse.mappet.utils.ValueSyntaxStyle;
import mchorse.mclib.config.ConfigBuilder;
import mchorse.mclib.config.values.ValueBoolean;
import mchorse.mclib.config.values.ValueInt;
import mchorse.mclib.events.RegisterConfigEvent;

import static mchorse.mappet.Mappet.MOD_ID;

public final class MappetConfig {
    public static ValueBoolean generalDataCaching;
    public static ValueBoolean loadCustomSoundsOnLogin;
    public static ValueBoolean immediatelyOpenLink;

    public static ValueBoolean npcsPeacefulDamage;
    public static ValueBoolean npcsToolOnlyOP;
    public static ValueBoolean npcsToolOnlyCreative;

    public static ValueBoolean dashboardOnlyCreative;

    public static ValueInt eventMaxExecutions;
    public static ValueBoolean eventUseServerForCommands;
    public static ValueBoolean enableForgeTriggers;

    public static ValueInt nodePulseBackgroundColor;
    public static ValueBoolean nodePulseBackgroundMcLibPrimary;
    public static ValueInt nodeThickness;

    public static ValueBoolean questsPreviewRewards;

    public static ValueSyntaxStyle scriptEditorSyntaxStyle;
    public static ValueBoolean scriptEditorSounds;
    public static ValueBoolean scriptUIDebug;
    public static ValueCodeEditor scriptCodeTemplate;
    public static void register(RegisterConfigEvent event) {
        ConfigBuilder builder = event.createBuilder(MOD_ID);

        builder.category("general").register(new ValueButtons("buttons").clientSide());
        generalDataCaching = builder.getBoolean("data_caching", true);
        enableForgeTriggers = builder.getBoolean("enable_forge_triggers", false);
        loadCustomSoundsOnLogin = builder.getBoolean("load_custom_sounds_on_login", false);
        immediatelyOpenLink = builder.getBoolean("immediately_open_link", false);

        npcsPeacefulDamage = builder.category("npc").getBoolean("peaceful_damage", true);
        npcsToolOnlyOP = builder.getBoolean("tool_only_op", true);
        npcsToolOnlyCreative = builder.getBoolean("tool_only_creative", false);
        dashboardOnlyCreative = builder.getBoolean("dashboard_only_creative", false);

        eventMaxExecutions = builder.category("events").getInt("max_executions", 10000, 100, 1000000);
        eventUseServerForCommands = builder.getBoolean("use_server_for_commands", false);

        nodePulseBackgroundColor = builder.category("gui").getInt("pulse_background_color", 0x000000).color();
        nodePulseBackgroundMcLibPrimary = builder.getBoolean("pulse_background_mclib", false);
        nodeThickness = builder.getInt("node_thickness", 3, 0, 20);
        questsPreviewRewards = builder.getBoolean("quest_preview_rewards", true);
        builder.getCategory().markClientSide();

        builder.category("script_editor").register(scriptEditorSyntaxStyle = new ValueSyntaxStyle("syntax_style"));
        scriptEditorSounds = builder.getBoolean("sounds", true);
        scriptUIDebug = builder.getBoolean("ui_debug", false);
        builder.register(scriptCodeTemplate = new ValueCodeEditor("code_template"));
        builder.getCategory().markClientSide();
    }
}
package mchorse.mappet.client.gui.panels;

import mchorse.mappet.api.utils.logs.LoggerLevel;
import mchorse.mappet.client.gui.GuiMappetDashboard;
import mchorse.mappet.client.gui.utils.GuiLoggingLevelList;
import mchorse.mappet.client.gui.utils.GuiScrollLogsElement;
import mchorse.mappet.client.gui.utils.GuiTextLabeledElement;
import mchorse.mappet.client.gui.utils.text.GuiText;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.common.logs.PacketRequestLogs;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.context.GuiSimpleContextMenu;
import mchorse.mclib.client.gui.mclib.GuiDashboardPanel;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GuiLogPanel extends GuiDashboardPanel<GuiMappetDashboard> {
    String lastLogTime = "";
    List<String> logLines = new ArrayList<>();
    String search = "";

    GuiScrollLogsElement text;
    GuiLoggingLevelList levelFlags;
    GuiTextLabeledElement searchBar;

    boolean searchIgnoreCase = false;
    boolean searchRegex = false;
    boolean searchOnlyMessage = false;
    GuiToggleElement toggleIgnoreCase;
    GuiToggleElement toggleRegex;
    GuiToggleElement toggleOnlyMessage;
    GuiButtonElement clearHistory;

    private static final Pattern replacePattern = Pattern.compile("\\r(?=[^\\[\\n])");

    public GuiLogPanel(Minecraft mc, GuiMappetDashboard dashboard) {
        super(mc, dashboard);

        context(() ->
        {
            GuiSimpleContextMenu menu = new GuiSimpleContextMenu(mc);
            menu.action(Icons.REFRESH, IKey.lang("mappet.gui.logs.context.update"), this::sendRequestPacket);
            return menu.shadow();
        });

        /* Lines */

        text = new GuiScrollLogsElement(mc);
        text.background().flex().relative(this).xy(10, 10).w(0.75F, -10).h(1F, -50).column(5).vertical().stretch().scroll().padding(10);

        /* Search */

        searchBar = new GuiTextLabeledElement(mc, (s) ->
        {
            search = s;
            createTextElements();
        }).label(IKey.lang("mappet.gui.search"));

        searchBar.flex().relative(this).anchorY(1F).w(0.75F, -10).x(10).h(20).y(1F, -10);
        searchBar.field.setMaxStringLength(Integer.MAX_VALUE);

        toggleIgnoreCase = new GuiToggleElement(mc, IKey.lang("mappet.gui.logs.toggle.ignore_case"), (b) ->
        {
            searchIgnoreCase = b.isToggled();
            createTextElements();
        });
        toggleIgnoreCase.flex().relative(this).anchorY(1F).anchorX(1F).x(1F, -20).y(1F, -10).w(0.2F, -20).h(20);

        toggleRegex = new GuiToggleElement(mc, IKey.lang("mappet.gui.logs.toggle.regex"), (b) ->
        {
            searchRegex = b.isToggled();
            toggleIgnoreCase.setEnabled(!b.isToggled());
            createTextElements();
        });
        toggleRegex.flex().relative(toggleIgnoreCase).anchorY(1F).anchorX(1F).wh(1F, 1F).y(-1F, 10).x(1F);

        toggleOnlyMessage = new GuiToggleElement(mc, IKey.lang("mappet.gui.logs.toggle.onlyMessage"), (b) ->
        {
            searchOnlyMessage = b.isToggled();
            createTextElements();
        });
        toggleOnlyMessage.flex().relative(toggleRegex).anchorY(1F).anchorX(1F).wh(1F, 1F).y(-1F, 10).x(1F);

        clearHistory = new GuiButtonElement(mc, IKey.lang("mappet.gui.logs.button.clearHistory"), (b) ->
        {
            logLines.clear();
            lastLogTime = Instant.now().toString();
            createTextElements();
        });
        clearHistory.flex().relative(toggleOnlyMessage).anchorY(1F).anchorX(1F).wh(1F, 1F).y(-1F, 10).x(1F);

        /* Levels */

        levelFlags = new GuiLoggingLevelList(mc, (l) -> createTextElements());
        levelFlags.background().flex().relative(this).anchorX(1F)
                .x(1F, -10).y(10).w(0.2F).h(0.5F, -20)
                .column(10).vertical().stretch().scroll().padding(10);
        levelFlags.resize();

        add(text);
        add(levelFlags);
        add(searchBar);
        add(toggleIgnoreCase);
        add(toggleRegex);
        add(toggleOnlyMessage);
        add(clearHistory);

        resize();
    }

    @Override
    public void appear() {
        super.appear();
        sendRequestPacket();
    }

    public void update(String data) {
        fillList(data);
        lastLogTime = Instant.now().toString();
        createTextElements();
    }

    public void createTextElements() {
        text.removeAll();

        LoggerLevel prevLevel = LoggerLevel.INFO;
        for (String line : logLines) {
            if (!isMatchesSearch(line)) continue;

            LoggerLevel level = getLineLevel(line);
            if (level == null) level = prevLevel;
            else prevLevel = level;
            if (!levelFlags.flags.get(level)) continue;

            text.add(new GuiText(mc).text(line).color(level.color, false).context(() ->
            {
                GuiSimpleContextMenu menu = new GuiSimpleContextMenu(mc);

                menu.action(Icons.COPY, IKey.lang("mappet.gui.logs.context.copy"), () ->
                {
                    int secondCloseBracket = line.indexOf("]", line.indexOf("]") + 1);
                    GuiScreen.setClipboardString(line.substring(secondCloseBracket + 2));
                });

                return menu.shadow();
            }));
        }

        text.resize();
    }

    public void fillList(String data) {

        //data = replacePattern.matcher(data).replaceAll("\n");

        String[] lines = data.split("\n");

        for (String line : lines) {
            if (line.isEmpty()) continue;
            System.out.println(line);
            logLines.add(line);
        }
    }

    public boolean isMatchesSearch(String line) {
        if (searchOnlyMessage) {
            int secondIndex = line.indexOf(']', line.indexOf(']') + 1);
            line = line.substring(secondIndex + 1);
        }

        if (searchRegex) {
            try {
                Pattern searchPattern = Pattern.compile(search);
                return searchPattern.matcher(line).find();
            } catch (Exception e) {
                return false;
            }
        } else if (searchIgnoreCase) {
            return line.toLowerCase().contains(search.toLowerCase());
        }

        return line.contains(search);
    }

    public LoggerLevel getLineLevel(String line) {
        if (line.charAt(0) != '[') return null;

        int secondOpenBracket = line.indexOf('[', 1);
        int secondClosedBracket = line.indexOf(']', secondOpenBracket);

        if (secondClosedBracket == -1) return null;

        try {
            return LoggerLevel.valueOf(line.substring(secondOpenBracket + 1, secondClosedBracket));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void sendRequestPacket() {
        Dispatcher.sendToServer(new PacketRequestLogs(lastLogTime));
    }
}
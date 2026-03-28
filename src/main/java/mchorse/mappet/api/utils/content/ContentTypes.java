package mchorse.mappet.api.utils.content;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.dialogues.Dialogue;
import mchorse.mappet.api.events.nodes.EventBaseNode;
import mchorse.mappet.api.factions.Faction;
import mchorse.mappet.api.huds.HUDScene;
import mchorse.mappet.api.npcs.Npc;
import mchorse.mappet.api.quests.Quest;
import mchorse.mappet.api.quests.chains.QuestChain;
import mchorse.mappet.api.scripts.Script;
import mchorse.mappet.api.translations.Translation;
import mchorse.mappet.api.ui.UI;
import mchorse.mappet.api.utils.manager.IManager;
import mchorse.mappet.api.utils.nodes.NodeSystem;
import mchorse.mappet.client.gui.GuiMappetDashboard;
import mchorse.mappet.client.gui.panels.GuiMappetDashboardPanel;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ContentTypes // Registry
{
    private static final Map<String, IContentTypeBase> TYPES = new HashMap<>();

    public static final IContentType<Quest> QUEST = new IContentType<Quest>() {
        @Override
        public String name() {return "QUEST";}

        @Override
        public IManager<Quest> manager() {return Mappet.quests;}

        @Override
        @SideOnly(Side.CLIENT)
        public GuiMappetDashboardPanel<Quest> panel(GuiMappetDashboard dashboard) {return dashboard.quest;}

        @Override
        @SideOnly(Side.CLIENT)
        public IKey label() {return IKey.lang("mappet.gui.overlays.quest");}
    };

    public static final IContentType<NodeSystem<EventBaseNode>> EVENT = new IContentType<NodeSystem<EventBaseNode>>() {
        @Override
        public String name() {return "EVENT";}

        @Override
        public IManager<NodeSystem<EventBaseNode>> manager() {return Mappet.events;}

        @Override
        @SideOnly(Side.CLIENT)
        public GuiMappetDashboardPanel<NodeSystem<EventBaseNode>> panel(GuiMappetDashboard dashboard) {return dashboard.event;}

        @Override
        @SideOnly(Side.CLIENT)
        public IKey label() {return IKey.lang("mappet.gui.overlays.event");}
    };

    public static final IContentType<Dialogue> DIALOGUE = new IContentType<Dialogue>() {
        @Override
        public String name() {return "DIALOGUE";}

        @Override
        public IManager<Dialogue> manager() {return Mappet.dialogues;}

        @Override
        @SideOnly(Side.CLIENT)
        public GuiMappetDashboardPanel<Dialogue> panel(GuiMappetDashboard dashboard) {return dashboard.dialogue;}

        @Override
        @SideOnly(Side.CLIENT)
        public IKey label() {return IKey.lang("mappet.gui.overlays.dialogue");}
    };

    public static final IContentType<Npc> NPC = new IContentType<Npc>() {
        @Override
        public String name() {return "NPC";}

        @Override
        public IManager<Npc> manager() {return Mappet.npcs;}

        @Override
        @SideOnly(Side.CLIENT)
        public GuiMappetDashboardPanel<Npc> panel(GuiMappetDashboard dashboard) {return dashboard.npc;}

        @Override
        @SideOnly(Side.CLIENT)
        public IKey label() {return IKey.lang("mappet.gui.overlays.npc");}
    };

    public static final IContentType<Faction> FACTION = new IContentType<Faction>() {
        @Override
        public String name() {return "FACTION";}

        @Override
        public IManager<Faction> manager() {return Mappet.factions;}

        @Override
        @SideOnly(Side.CLIENT)
        public GuiMappetDashboardPanel<Faction> panel(GuiMappetDashboard dashboard) {return dashboard.faction;}

        @Override
        @SideOnly(Side.CLIENT)
        public IKey label() {return IKey.lang("mappet.gui.overlays.faction");}
    };

    public static final IContentType<QuestChain> CHAIN = new IContentType<QuestChain>() {
        @Override
        public String name() {return "CHAIN";}

        @Override
        public IManager<QuestChain> manager() {return Mappet.chains;}

        @Override
        @SideOnly(Side.CLIENT)
        public GuiMappetDashboardPanel<QuestChain> panel(GuiMappetDashboard dashboard) {return dashboard.chain;}

        @Override
        @SideOnly(Side.CLIENT)
        public IKey label() {return IKey.lang("mappet.gui.overlays.chain");}
    };

    public static final IContentType<Script> SCRIPT = new IContentType<Script>() {
        @Override
        public String name() {return "SCRIPT";}

        @Override
        public IManager<Script> manager() {return Mappet.scripts;}

        @Override
        @SideOnly(Side.CLIENT)
        public GuiMappetDashboardPanel<Script> panel(GuiMappetDashboard dashboard) {return dashboard.script;}

        @Override
        @SideOnly(Side.CLIENT)
        public IKey label() {return IKey.lang("mappet.gui.overlays.script");}
    };

    public static final IContentType<HUDScene> HUD = new IContentType<HUDScene>() {
        @Override
        public String name() {return "HUD";}

        @Override
        public IManager<HUDScene> manager() {return Mappet.huds;}

        @Override
        @SideOnly(Side.CLIENT)
        public GuiMappetDashboardPanel<HUDScene> panel(GuiMappetDashboard dashboard) {return dashboard.hud;}

        @Override
        @SideOnly(Side.CLIENT)
        public IKey label() {return IKey.lang("mappet.gui.overlays.hud");}
    };

    public static final IContentType<UI> UI = new IContentType<UI>() {
        @Override
        public String name() {return "UI";}

        @Override
        public IManager<UI> manager() {return Mappet.ui;}

        @Override
        @SideOnly(Side.CLIENT)
        public GuiMappetDashboardPanel<UI> panel(GuiMappetDashboard dashboard) {return dashboard.ui;}

        @Override
        @SideOnly(Side.CLIENT)
        public IKey label() {return IKey.lang("mappet.gui.overlays.ui");}
    };

    public static final IContentType<Translation> TRANSLATION = new IContentType<Translation>() {
        @Override
        public String name() {return "TRANSLATION";}

        @Override
        public IManager<Translation> manager() {return Mappet.translations;}

        @Override
        @SideOnly(Side.CLIENT)
        public GuiMappetDashboardPanel<Translation> panel(GuiMappetDashboard dashboard) {return dashboard.translation;}

        @Override
        @SideOnly(Side.CLIENT)
        public IKey label() {return IKey.lang("mappet.gui.overlays.translation");}
    };


    static {
        register(CHAIN);
        register(DIALOGUE);
        register(EVENT);
        register(FACTION);
        register(HUD);
        register(NPC);
        register(QUEST);
        register(SCRIPT);
        register(TRANSLATION);
    }

    public static void register(IContentTypeBase type) {
        if (!TYPES.containsKey(type.name())) TYPES.put(type.name(), type);
    }

    public static IContentTypeBase get(String name) {
        return TYPES.get(name);
    }

    public static Collection<IContentTypeBase> all() {
        return TYPES.values();
    }
}

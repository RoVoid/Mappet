package mchorse.mappet.api.utils.content;

import mchorse.mappet.api.utils.AbstractData;
import mchorse.mappet.api.utils.manager.IManager;
import mchorse.mappet.client.gui.GuiMappetDashboard;
import mchorse.mappet.client.gui.panels.GuiMappetDashboardPanel;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IContentType<T extends AbstractData> extends IContentTypeBase
{
    String name();

    // Every Karen be like :D @Toray-Life
    // No clue what he means
    IManager<T> manager();

    @SideOnly(Side.CLIENT)
    IKey label();

    @SideOnly(Side.CLIENT)
    GuiMappetDashboardPanel<T> panel(GuiMappetDashboard dashboard);
}

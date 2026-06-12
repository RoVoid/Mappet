package mchorse.mappet.client.gui.url;

import mchorse.mappet.client.gui.utils.text.GuiMultiTextElement;
import mchorse.mclib.McLib;
import mchorse.mclib.client.gui.framework.GuiBase;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDraw;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.client.gui.utils.keys.LangKey;
import mchorse.mclib.utils.ColorUtils;
import mchorse.mclib.utils.Direction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.util.function.BiConsumer;

public class GuiLinkOpenScreen extends GuiBase {
    private static final IKey titleKey = IKey.lang("mappet.gui.link.title");
    private static final IKey acceptKey = IKey.lang("mappet.gui.link.accept");
    private static final LangKey trustDomainKey = (LangKey) IKey.lang("mappet.gui.link.trust_domain");

    private final GuiElement panel;
    private final GuiMultiTextElement<?> link;
    private final GuiToggleElement trustDomain;

    BiConsumer<Boolean, Boolean> callback;

    public GuiLinkOpenScreen(String linkText, BiConsumer<Boolean, Boolean> callback) {
        Minecraft mc = Minecraft.getMinecraft();

        this.callback = callback;

        link = new GuiMultiTextElement<>(mc, null).background(true).wrap(true).readOnly(true);
        link.setText(linkText);

        GuiIconElement copy = new GuiIconElement(mc, Icons.COPY, b -> GuiScreen.setClipboardString(link.getText()));
        GuiIconElement close = new GuiIconElement(mc, Icons.CLOSE, b -> closeScreen());
        GuiButtonElement accept = new GuiButtonElement(mc, acceptKey, b -> closeScreen(true));

        trustDomain = new GuiToggleElement(mc, IKey.EMPTY, false, b -> {});
        String domainText = SafeWebLinkOpener.getLinkDomain(linkText);
        if (domainText == null) trustDomain.setEnabled(false);
        else trustDomain.tooltip(trustDomainKey.args(domainText), Direction.LEFT);


        panel = new GuiElement(mc);
        panel.flex().relative(viewport).xy(0.5F, 0.5F).anchor(0.5F, 0.5F).wh(400, 200);

        close.flex().anchorY(0.5f);

        GuiElement header = Elements.row(mc, 4, Elements.label(titleKey).anchor(0, 0.5f), close);
        header.flex().relative(panel).xy(0, 0).w(1F).h(20);

        link.flex().relative(panel).xy(0, 24).w(1F, -20).h(1F, -55);
        copy.flex().relative(panel).x(1F, -18).y(24).wh(18, 18);

        accept.flex().w(mc.fontRenderer.getStringWidth(acceptKey.get()) + 15);
        trustDomain.flex().w(20);

        GuiElement footer = Elements.row(mc, panel.flex().getW() - accept.flex().getW() - trustDomain.flex().getW(), accept, trustDomain);
        footer.flex().relative(panel).x(0).y(1F, -20).w(1F).h(20);

        panel.add(header, link, copy, footer);
        root.add(panel);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        panel.area.draw(ColorUtils.HALF_BLACK, -20);
        GuiDraw.drawBorder(panel.area, McLib.primaryColor.get());
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void closeScreen() {
        closeScreen(false);
    }

    protected void closeScreen(boolean result) {
        super.closeScreen();
        if (callback != null) callback.accept(result, trustDomain.isToggled());
    }
}
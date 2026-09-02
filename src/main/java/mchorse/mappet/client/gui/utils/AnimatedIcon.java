package mchorse.mappet.client.gui.utils;

import mchorse.mclib.client.gui.framework.elements.utils.GuiDraw;
import mchorse.mclib.client.gui.utils.Icon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class AnimatedIcon extends Icon {

    private long lastUpdate = -1;
    private int tick = 0;
    public int frame = 0;

    public int frames;
    public int frameTicks;

    public AnimatedIcon(ResourceLocation location, int x, int y) {
        super(location, x, y);
    }

    public AnimatedIcon(ResourceLocation location, int x, int y, int w, int h, int frames, int frameTicks) {
        super(location, x, y, w, h);
        this.frames = frames;
        this.frameTicks = frameTicks;
    }

    public AnimatedIcon(ResourceLocation location, int x, int y, int w, int h, int textureW, int textureH, int frames, int frameTicks) {
        super(location, x, y, w, h, textureW, textureH);
        this.frames = frames;
        this.frameTicks = frameTicks;
    }

    @Override
    public void render(int x, int y, float anchorX, float anchorY) {
        render(x, y, anchorX, anchorY, 0);
    }

    public void render(int x, int y, float anchorX, float anchorY, float rotation) {
        if (location == null) return;

        long time = System.currentTimeMillis() / 50L;
        if (time != lastUpdate) {
            lastUpdate = time;
            if (++tick >= frameTicks) {
                tick = 0;
                frame = (frame + 1) % frames;
            }
        }

        x -= (int) (anchorX * w);
        y -= (int) (anchorY * h);

        GlStateManager.disableDepth();
        GlStateManager.enableBlend();

        Minecraft.getMinecraft().getTextureManager().bindTexture(location);

        float angle = rotation % 360;
        if (angle != 0) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + w * anchorX, y + h * anchorY, 0);
            GlStateManager.rotate(angle, 0, 0, 1);
            GlStateManager.translate(-w * anchorX, -h * anchorY, 0);
            GuiDraw.drawBillboard(0, 0, this.x, this.y + frame * h, w, h, textureW, textureH);
            GlStateManager.popMatrix();
        }
        else GuiDraw.drawBillboard(x, y, this.x, this.y + frame * h, w, h, textureW, textureH);

        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
    }
}

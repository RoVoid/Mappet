package mchorse.mappet.utils;

import java.awt.*;

public class Colors
{
    /* General purpose colors */
    public static final int ACTIVE = 0x0088ff;
    public static final int INACTIVE = 0xffbb00;

    public static final int POSITIVE = 0x00ff44;
    public static final int NEGATIVE = 0xff0033;

    public static final int A = 0x00ff44;
    public static final int WHITE = 0xeeeeee;

    /* Data element colors */
    public static final int CANCEL = 0xeeeeee;
    public static final int COMMAND = 0x942aff;
    public static final int CONDITION = 0xff1493;
    public static final int CRAFTING = 0xff6600;
    public static final int DIALOGUE = 0x11ff33;
    public static final int ENTITY = 0x2d4163;
    public static final int FACTION = 0xb3ff00;
    public static final int QUEST = 0xffaa00;
    public static final int REPLY = 0x00a0ff;
    public static final int COMMENT = 0xf1fa8c;
    public static final int STATE = 0xff0033;
    public static final int TIME = 0x0088ff;
    public static final int MORPH = 0x4f00e0;

    public static int shiftHue(int color, float hueDelta) {
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;

        float[] hsb = Color.RGBtoHSB(r, g, b, null);
        hsb[0] = (hsb[0] + hueDelta) % 1.0F;
        if (hsb[0] < 0) hsb[0] += 1.0F;

        int shifted = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
        return color & 0xFF000000 | shifted & 0x00FFFFFF;
    }
}
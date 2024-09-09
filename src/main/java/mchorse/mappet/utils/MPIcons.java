package mchorse.mappet.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mchorse.mappet.Mappet;
import mchorse.mclib.client.gui.utils.Icon;
import mchorse.mclib.client.gui.utils.IconRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MPIcons {
    public static final ResourceLocation DEFAULT = new ResourceLocation(Mappet.MOD_ID, "textures/gui/icons/mappet.png");
    public static final ResourceLocation LV = new ResourceLocation(Mappet.MOD_ID, "textures/gui/icons/lv.png");
    public static final ResourceLocation KEYS = new ResourceLocation(Mappet.MOD_ID, "textures/gui/icons/keys.png");
    public static final ResourceLocation RIFLE_RANGE = new ResourceLocation(Mappet.MOD_ID, "textures/gui/icons/riflerange.png");


    public static final Icon REPL = registerIcon("repl", new Icon(DEFAULT, 0, 0));
    public static final Icon IN = registerIcon("in", new Icon(DEFAULT, 16, 0));
    public static final Icon OUT = registerIcon("out", new Icon(DEFAULT, 32, 0));

    public static final Icon ALLY = registerIcon("ally", new Icon(RIFLE_RANGE, 0, 0));
    public static final Icon ENEMY = registerIcon("enemy", new Icon(RIFLE_RANGE, 16, 0));
    public static final Icon ARROW = registerIcon("arrow", new Icon(RIFLE_RANGE, 32, 0));


    //HORSE
    public static final Icon BULLET = new Icon(LV, 0, 224);

    public static final Icon PARTICLE = new Icon(LV, 16, 224);

    public static final Icon SCENE = new Icon(LV, 32, 224);

    public static final Icon REWIND = new Icon(LV, 48, 224);

    public static final Icon FACES = new Icon(LV, 64, 224);

    public static final Icon ALEX = new Icon(LV, 80, 224);

    public static final Icon SELECTOR = new Icon(LV, 96, 224);

    public static final Icon SLASH = new Icon(LV, 112, 224);

    public static final Icon TEXT = new Icon(LV, 128, 224);

    public static final Icon LIKE = new Icon(LV, 144, 224);

    public static final Icon DISLIKE = new Icon(LV, 160, 224);

    public static final Icon YOUTUBE = new Icon(LV, 0, 240);

    public static final Icon DISCORD = new Icon(LV, 16, 240);

    public static final Icon TELEGRAM = new Icon(LV, 32, 240);

    public static final Icon VK = new Icon(LV, 48, 240);

    public static final Icon TWITTER = new Icon(LV, 64, 240);

    public static final Icon SOLID_LIKE = new Icon(LV, 80, 240);

    public static final Icon SOLID_DISLIKE = new Icon(LV, 96, 240);
    public static final Icon HUB = new Icon(LV, 112, 240);
    public static final Icon GITHUB = new Icon(LV, 128, 240);

    public static final Icon OTHER_BRIEFCASE = new Icon(LV, 0, 0);

    public static final Icon OTHER_MEDKIT = new Icon(LV, 16, 0);

    public static final Icon OTHER_WALLET = new Icon(LV, 32, 0);

    public static final Icon OTHER_HANGER = new Icon(LV, 48, 0);

    public static final Icon OTHER_STATS = new Icon(LV, 64, 0);

    public static final Icon OTHER_BANNER = new Icon(LV, 80, 0);

    public static final Icon OTHER_OPENED_LOCK = new Icon(LV, 96, 0);

    public static final Icon OTHER_CLOSED_LOCK = new Icon(LV, 112, 0);

    public static final Icon OTHER_FLAG = new Icon(LV, 128, 0);

    public static final Icon OTHER_FINISHFLAG = new Icon(LV, 144, 0);

    public static final Icon OTHER_UMBRELLA = new Icon(LV, 160, 0);

    public static final Icon OTHER_STAR = new Icon(LV, 176, 0);

    public static final Icon OTHER_STAR_2 = new Icon(LV, 192, 0);

    public static final Icon OTHER_DUNGEONKEY = new Icon(LV, 208, 0);

    public static final Icon OTHER_CARKEY = new Icon(LV, 224, 0);

    public static final Icon OTHER_KEY = new Icon(LV, 240, 0);

    public static final Icon OTHER_X = new Icon(LV, 0, 16);

    public static final Icon OTHER_BACKPACK = new Icon(LV, 16, 16);

    public static final Icon OTHER_GLOSARY = new Icon(LV, 32, 16);

    public static final Icon OTHER_BOOK = new Icon(LV, 48, 16);

    public static final Icon OTHER_DOCUMENT = new Icon(LV, 64, 16);

    public static final Icon OTHER_CANDLE = new Icon(LV, 80, 16);

    public static final Icon OTHER_HOOK = new Icon(LV, 96, 16);

    public static final Icon ADMINPANEL_USER = new Icon(LV, 0, 32);

    public static final Icon ADMINPANEL_USER_WHITE_CROSS = new Icon(LV, 16, 32);

    public static final Icon ADMINPANEL_USER_RED_CROSS = new Icon(LV, 32, 32);

    public static final Icon ADMINPANEL_MESSAGE = new Icon(LV, 48, 32);

    public static final Icon ADMINPANEL_MESSAGE_WHITE_CROSS = new Icon(LV, 64, 32);

    public static final Icon ADMINPANEL_MESSAGE_RED_CROSS = new Icon(LV, 80, 32);

    public static final Icon ADMINPANEL_VOICE = new Icon(LV, 96, 32);

    public static final Icon ADMINPANEL_VOICE_WHITE_CROSS = new Icon(LV, 112, 32);

    public static final Icon ADMINPANEL_VOICE_RED_CROSS = new Icon(LV, 128, 32);

    public static final Icon ADMINPANEL_SPEAKER = new Icon(LV, 144, 32);

    public static final Icon ADMINPANEL_SPEAKER_WHITE_CROSS = new Icon(LV, 160, 32);

    public static final Icon ADMINPANEL_SPEAKER_RED_CROSS = new Icon(LV, 176, 32);
    public static final Icon ADMINPANEL_SPEAKER_MUTED = new Icon(LV, 192, 32);
    public static final Icon ADMINPANEL_SPEAKER_LOW = new Icon(LV, 208, 32);
    public static final Icon ADMINPANEL_SPEAKER_HIGH = new Icon(LV, 224, 32);

    public static final Icon TOOLS_SWORD = new Icon(LV, 0, 48);

    public static final Icon TOOLS_KNIFE = new Icon(LV, 16, 48);

    public static final Icon TOOLS_DAGGER = new Icon(LV, 32, 48);

    public static final Icon TOOLS_SHORTSWORD = new Icon(LV, 48, 48);

    public static final Icon TOOLS_KATANA = new Icon(LV, 64, 48);

    public static final Icon TOOLS_HAMMER = new Icon(LV, 80, 48);

    public static final Icon TOOLS_SCREWDRIVER = new Icon(LV, 96, 48);

    public static final Icon TOOLS_SHOVEL = new Icon(LV, 112, 48);

    public static final Icon TOOLS_GLOVE = new Icon(LV, 128, 48);

    public static final Icon TOOLS_SHURIKEN = new Icon(LV, 144, 48);

    public static final Icon TOOLS_BOW = new Icon(LV, 160, 48);

    public static final Icon TOOLS_LONGBOW = new Icon(LV, 176, 48);

    public static final Icon TOOLS_ARROW = new Icon(LV, 192, 48);

    public static final Icon TOOLS_FLYINGARROWS = new Icon(LV, 208, 48);

    public static final Icon TOOLS_SHOT = new Icon(LV, 224, 48);

    public static final Icon TOOLS_BULLET = new Icon(LV, 240, 48);

    public static final Icon TOOLS_PISTOL = new Icon(LV, 0, 64);

    public static final Icon TOOLS_GLOCK = new Icon(LV, 16, 64);

    public static final Icon TOOLS_REVOLVER = new Icon(LV, 32, 64);

    public static final Icon TOOLS_UZI = new Icon(LV, 48, 64);

    public static final Icon TOOLS_DYNAMITE = new Icon(LV, 64, 64);

    public static final Icon TOOLS_GRENADE = new Icon(LV, 80, 64);

    public static final Icon TOOLS_TNT = new Icon(LV, 96, 64);

    public static final Icon CLOTHES_CROWN = new Icon(LV, 0, 80);

    public static final Icon CLOTHES_MINICROWN = new Icon(LV, 16, 80);

    public static final Icon CLOTHES_CYLINDER = new Icon(LV, 32, 80);

    public static final Icon CLOTHES_HAT = new Icon(LV, 48, 80);

    public static final Icon CLOTHES_CAP = new Icon(LV, 64, 80);

    public static final Icon CLOTHES_CHRISTMASHAT = new Icon(LV, 80, 80);

    public static final Icon CLOTHES_FAVOUR = new Icon(LV, 96, 80);

    public static final Icon CLOTHES_GLASSES = new Icon(LV, 112, 80);

    public static final Icon CLOTHES_TSHORT = new Icon(LV, 128, 80);

    public static final Icon CLOTHES_JACKET = new Icon(LV, 144, 80);

    public static final Icon CLOTHES_PANTS = new Icon(LV, 160, 80);

    public static final Icon CLOTHES_BOOTS = new Icon(LV, 176, 80);

    public static final Icon CLOTHES_SNEAKERS = new Icon(LV, 192, 80);

    public static final Icon CLOTHES_MOUSTACHE = new Icon(LV, 208, 80);

    public static final Icon DEVICE_LAPTOP = new Icon(LV, 0, 96);

    public static final Icon DEVICE_COMPUTER = new Icon(LV, 16, 96);

    public static final Icon DEVICE_COMPUTER_2 = new Icon(LV, 32, 96);

    public static final Icon DEVICE_DISKDRIVE = new Icon(LV, 48, 96);

    public static final Icon DEVICE_PHONE = new Icon(LV, 64, 96);

    public static final Icon DEVICE_RADIO = new Icon(LV, 80, 96);

    public static final Icon DEVICE_CAMERA = new Icon(LV, 96, 96);

    public static final Icon DEVICE_ARCADE = new Icon(LV, 112, 96);

    public static final Icon DEVICE_ANTENNA = new Icon(LV, 128, 96);

    public static final Icon DEVICE_CLOCK = new Icon(LV, 144, 96);

    public static final Icon DEVICE_MOUSE = new Icon(LV, 160, 96);

    public static final Icon DEVICE_KEYBOARD = new Icon(LV, 176, 96);

    public static final Icon DEVICE_GAMEPAD = new Icon(LV, 192, 96);

    public static final Icon DEVICE_MICROPHONE = new Icon(LV, 208, 96);

    public static final Icon DEVICE_HEADPHONES = new Icon(LV, 224, 96);

    public static final Icon DEVICE_SPEAKER = new Icon(LV, 240, 96);

    public static final Icon COMPUTER_CURSOR = new Icon(LV, 0, 112);

    public static final Icon COMPUTER_CLICK = new Icon(LV, 16, 112);

    public static final Icon COMPUTER_SHUTDOWN = new Icon(LV, 32, 112);

    public static final Icon COMPUTER_SETTINGS = new Icon(LV, 48, 112);

    public static final Icon COMPUTER_USB = new Icon(LV, 64, 112);

    public static final Icon COMPUTER_BATTERY_4 = new Icon(LV, 80, 112);

    public static final Icon COMPUTER_BATTERY_3 = new Icon(LV, 96, 112);

    public static final Icon COMPUTER_BATTERY_2 = new Icon(LV, 112, 112);

    public static final Icon COMPUTER_BATTERY_1 = new Icon(LV, 128, 112);

    public static final Icon COMPUTER_CHARGING = new Icon(LV, 144, 112);

    public static final Icon COMPUTER_NOTIFICATION = new Icon(LV, 160, 112);

    public static final Icon COMPUTER_DEFENDER = new Icon(LV, 176, 112);

    public static final Icon COMPUTER_HOME = new Icon(LV, 192, 112);

    public static final Icon COMPUTER_MAIL = new Icon(LV, 208, 112);

    public static final Icon COMPUTER_MARKET = new Icon(LV, 224, 112);

    public static final Icon COMPUTER_LIGHT = new Icon(LV, 240, 112);

    public static final Icon COMPUTER_MAP = new Icon(LV, 0, 128);

    public static final Icon COMPUTER_MAPPOINT = new Icon(LV, 16, 128);

    public static final Icon COMPUTER_SHARE = new Icon(LV, 32, 128);

    public static final Icon COMPUTER_SHARE_2 = new Icon(LV, 48, 128);

    public static final Icon COMPUTER_FOLDER = new Icon(LV, 64, 128);

    public static final Icon COMPUTER_FILE = new Icon(LV, 80, 128);

    public static final Icon COMPUTER_PASTE = new Icon(LV, 96, 128);

    public static final Icon COMPUTER_CREDITCARD = new Icon(LV, 112, 128);

    public static final Icon COMPUTER_PIANO = new Icon(LV, 128, 128);

    public static final Icon COMPUTER_CD = new Icon(LV, 144, 128);

    public static final Icon COMPUTER_CASSETE = new Icon(LV, 160, 128);

    public static final Icon COMPUTER_NOTE = new Icon(LV, 176, 128);

    public static final Icon COMPUTER_NOTE_2 = new Icon(LV, 192, 128);

    public static final Icon PAINT_PENCIL = new Icon(LV, 0, 144);

    public static final Icon PAINT_PENCILERASER = new Icon(LV, 16, 144);

    public static final Icon PAINT_HIGHLITGHTER = new Icon(LV, 32, 144);

    public static final Icon PAINT_BRUSH = new Icon(LV, 48, 144);

    public static final Icon PAINT_BRUSH_2 = new Icon(LV, 64, 144);

    public static final Icon PAINT_PICKER = new Icon(LV, 80, 144);

    public static final Icon PAINT_ERASER = new Icon(LV, 96, 144);

    public static final Icon PAINT_POURING = new Icon(LV, 112, 144);

    public static final Icon PAINT_CUT = new Icon(LV, 128, 144);

    public static final Icon FOOD_PAN = new Icon(LV, 0, 160);

    public static final Icon FOOD_SPOON = new Icon(LV, 16, 160);

    public static final Icon FOOD_MEAT = new Icon(LV, 32, 160);

    public static final Icon FOOD_PIZZA = new Icon(LV, 48, 160);

    public static final Icon FOOD_BREAD = new Icon(LV, 64, 160);

    public static final Icon FOOD_SOUP = new Icon(LV, 80, 160);

    public static final Icon FOOD_CAKE = new Icon(LV, 96, 160);

    public static final Icon FOOD_CHOCOLATE = new Icon(LV, 112, 160);

    public static final Icon FOOD_ICECREAM = new Icon(LV, 128, 160);

    public static final Icon FOOD_ICECREAM_2 = new Icon(LV, 144, 160);

    public static final Icon FOOD_CHUPACHUPS = new Icon(LV, 160, 160);

    public static final Icon FOOD_LOLLIPOP = new Icon(LV, 176, 160);

    public static final Icon FOOD_CHRISTMASLOLLIPOP = new Icon(LV, 192, 160);

    public static final Icon FOOD_TEA = new Icon(LV, 208, 160);

    public static final Icon FOOD_COFFEE = new Icon(LV, 224, 160);

    public static final Icon FOOD_BEAR = new Icon(LV, 240, 160);

    public static final Icon FOOD_JAM = new Icon(LV, 0, 176);

    public static final Icon FOOD_CHAMPAGNE = new Icon(LV, 16, 176);

    public static final Icon FOOD_COCACOLA = new Icon(LV, 32, 176);

    public static final Icon FOOD_CUP = new Icon(LV, 48, 176);

    public static final Icon FOOD_GLASS = new Icon(LV, 64, 176);

    public static final Icon FOOD_VIAL = new Icon(LV, 80, 176);

    public static final Icon FOOD_POTION = new Icon(LV, 96, 176);

    public static final Icon FOOD_CIGARETTE = new Icon(LV, 112, 176);

    public static final Icon FOOD_PIPE = new Icon(LV, 128, 176);

    public static final Icon FOOD_LIGHTER = new Icon(LV, 144, 176);

    public static final Icon FOOD_HOOKAH = new Icon(LV, 160, 176);
    public static final Icon FOOD_POOP = new Icon(LV, 176, 176);

    public static final Icon EMOJI_SMILEY = new Icon(LV, 0, 192);

    public static final Icon EMOJI_RELAXED = new Icon(LV, 16, 192);

    public static final Icon EMOJI_STUCKOUTTONGUE = new Icon(LV, 32, 192);

    public static final Icon EMOJI_SMIRKING = new Icon(LV, 48, 192);

    public static final Icon EMOJI_COOL = new Icon(LV, 64, 192);

    public static final Icon EMOJI_SOB = new Icon(LV, 80, 192);

    public static final Icon EMOJI_VERYSOB = new Icon(LV, 96, 192);

    public static final Icon EMOJI_CONFUSED = new Icon(LV, 112, 192);
    public static final Icon EMOJI_RAGE = new Icon(LV, 128, 192);

    public static final Icon EMOJI_SKULL_LARGE = new Icon(LV, 144, 192);

    public static final Icon EMOJI_SKULL_MEDIUM = new Icon(LV, 160, 192);

    public static final Icon EMOJI_SKULL_SMALL = new Icon(LV, 176, 192);

    public static final Icon MONEY_COIN = new Icon(LV, 0, 208);

    public static final Icon MONEY_COINDOLLAR = new Icon(LV, 16, 208);

    public static final Icon MONEY_BANKNOTES = new Icon(LV, 32, 208);

    public static final Icon MONEY_BAGMONEY = new Icon(LV, 48, 208);

    //KEYBOARD

    public static final Icon KEY_ESCAPE = new Icon(KEYS, 0, 0, 32, 16);

    public static final Icon KEY_F1 = new Icon(KEYS, 32, 0);

    public static final Icon KEY_F2 = new Icon(KEYS, 48, 0);

    public static final Icon KEY_F3 = new Icon(KEYS, 64, 0);

    public static final Icon KEY_F4 = new Icon(KEYS, 80, 0);

    public static final Icon KEY_F5 = new Icon(KEYS, 96, 0);

    public static final Icon KEY_F6 = new Icon(KEYS, 112, 0);

    public static final Icon KEY_F7 = new Icon(KEYS, 128, 0);

    public static final Icon KEY_F8 = new Icon(KEYS, 144, 0);

    public static final Icon KEY_F9 = new Icon(KEYS, 160, 0);

    public static final Icon KEY_F10 = new Icon(KEYS, 176, 0);

    public static final Icon KEY_F11 = new Icon(KEYS, 192, 0);

    public static final Icon KEY_F12 = new Icon(KEYS, 208, 0);

    public static final Icon KEY_PRINT = new Icon(KEYS, 224, 0, 32, 16);

    public static final Icon KEY_GRAVIAS = new Icon(KEYS, 0, 16);

    public static final Icon KEY_TILDA = new Icon(KEYS, 16, 16);

    public static final Icon KEY_EXCLAMATION = new Icon(KEYS, 32, 16);

    public static final Icon KEY_SYMBOL = new Icon(KEYS, 48, 16);

    public static final Icon KEY_GRID = new Icon(KEYS, 64, 16);

    public static final Icon KEY_DOLLAR = new Icon(KEYS, 80, 16);

    public static final Icon KEY_PROCENT = new Icon(KEYS, 96, 16);

    public static final Icon KEY_CARET = new Icon(KEYS, 112, 16);

    public static final Icon KEY_AMPERSAND = new Icon(KEYS, 128, 16);

    public static final Icon KEY_STAR = new Icon(KEYS, 144, 16);

    public static final Icon KEY_LEFT_PARENTTHESIS = new Icon(KEYS, 160, 16);

    public static final Icon KEY_RIGHT_PARENTTHESIS = new Icon(KEYS, 176, 16);

    public static final Icon KEY_INSERT = new Icon(KEYS, 192, 16, 32, 16);

    public static final Icon KEY_HOME = new Icon(KEYS, 224, 16, 32, 16);

    public static final Icon KEY_1 = new Icon(KEYS, 0, 32);

    public static final Icon KEY_2 = new Icon(KEYS, 16, 32);

    public static final Icon KEY_3 = new Icon(KEYS, 32, 32);

    public static final Icon KEY_4 = new Icon(KEYS, 48, 32);

    public static final Icon KEY_5 = new Icon(KEYS, 64, 32);

    public static final Icon KEY_6 = new Icon(KEYS, 80, 32);

    public static final Icon KEY_7 = new Icon(KEYS, 96, 32);

    public static final Icon KEY_8 = new Icon(KEYS, 112, 32);

    public static final Icon KEY_9 = new Icon(KEYS, 128, 32);

    public static final Icon KEY_0 = new Icon(KEYS, 144, 32);

    public static final Icon KEY_MINUS = new Icon(KEYS, 160, 32);

    public static final Icon KEY_UNDERLINE = new Icon(KEYS, 176, 32);

    public static final Icon KEY_EQUALS = new Icon(KEYS, 192, 32);

    public static final Icon KEY_PLUS = new Icon(KEYS, 208, 32);

    public static final Icon KEY_BACKSPACE = new Icon(KEYS, 224, 32, 32, 16);

    public static final Icon KEY_TAB = new Icon(KEYS, 0, 48, 32, 16);

    public static final Icon KEY_Q = new Icon(KEYS, 32, 48);

    public static final Icon KEY_W = new Icon(KEYS, 48, 48);

    public static final Icon KEY_E = new Icon(KEYS, 64, 48);

    public static final Icon KEY_R = new Icon(KEYS, 80, 48);

    public static final Icon KEY_T = new Icon(KEYS, 96, 48);

    public static final Icon KEY_Y = new Icon(KEYS, 112, 48);

    public static final Icon KEY_U = new Icon(KEYS, 128, 48);

    public static final Icon KEY_I = new Icon(KEYS, 144, 48);

    public static final Icon KEY_O = new Icon(KEYS, 160, 48);

    public static final Icon KEY_P = new Icon(KEYS, 176, 48);

    public static final Icon KEY_LEFT_BRACKET = new Icon(KEYS, 192, 48);

    public static final Icon KEY_LEFT_BRACE = new Icon(KEYS, 208, 48);

    public static final Icon KEY_RIGHT_BRACKET = new Icon(KEYS, 224, 48);

    public static final Icon KEY_RIGHT_BRACE = new Icon(KEYS, 240, 48);

    public static final Icon KEY_CAPSLOCK = new Icon(KEYS, 0, 64, 32, 16);

    public static final Icon KEY_A = new Icon(KEYS, 32, 64);

    public static final Icon KEY_S = new Icon(KEYS, 48, 64);

    public static final Icon KEY_D = new Icon(KEYS, 64, 64);

    public static final Icon KEY_F = new Icon(KEYS, 80, 64);

    public static final Icon KEY_G = new Icon(KEYS, 96, 64);

    public static final Icon KEY_H = new Icon(KEYS, 112, 64);

    public static final Icon KEY_J = new Icon(KEYS, 128, 64);

    public static final Icon KEY_K = new Icon(KEYS, 144, 64);

    public static final Icon KEY_L = new Icon(KEYS, 160, 64);

    public static final Icon KEY_SEMICOLON = new Icon(KEYS, 176, 64);

    public static final Icon KEY_COLON = new Icon(KEYS, 192, 64);

    public static final Icon KEY_SINGLE_QUOTE = new Icon(KEYS, 208, 64);

    public static final Icon KEY_QUOTATION_MARK = new Icon(KEYS, 224, 64);

    public static final Icon KEY_UP_ARROW = new Icon(KEYS, 240, 64);

    public static final Icon KEY_SHIFT = new Icon(KEYS, 0, 80, 32, 16);

    public static final Icon KEY_Z = new Icon(KEYS, 32, 80);

    public static final Icon KEY_X = new Icon(KEYS, 48, 80);

    public static final Icon KEY_C = new Icon(KEYS, 64, 80);

    public static final Icon KEY_V = new Icon(KEYS, 80, 80);

    public static final Icon KEY_B = new Icon(KEYS, 96, 80);

    public static final Icon KEY_N = new Icon(KEYS, 112, 80);

    public static final Icon KEY_M = new Icon(KEYS, 128, 80);

    public static final Icon KEY_COMMA = new Icon(KEYS, 144, 80);

    public static final Icon KEY_LEFT_ANGLE_BRACKET = new Icon(KEYS, 160, 80);

    public static final Icon KEY_DOT = new Icon(KEYS, 176, 80);

    public static final Icon KEY_RIGHT_ANGLE_BRACKET = new Icon(KEYS, 192, 80);

    public static final Icon KEY_SLASH = new Icon(KEYS, 208, 80);

    public static final Icon KEY_QUESTION = new Icon(KEYS, 224, 80);

    public static final Icon KEY_DOWN_ARROW = new Icon(KEYS, 240, 80);

    public static final Icon KEY_CTRL = new Icon(KEYS, 0, 96, 32, 16);

    public static final Icon KEY_WINDOW = new Icon(KEYS, 32, 96);

    public static final Icon KEY_ALT = new Icon(KEYS, 48, 96, 32, 16);

    public static final Icon KEY_SPACE_SMALL = new Icon(KEYS, 80, 96);

    public static final Icon KEY_SPACE_MEDIUM = new Icon(KEYS, 96, 96, 32, 16);

    public static final Icon KEY_SPACE_LARGE = new Icon(KEYS, 128, 96, 64, 16);

    public static final Icon KEY_LEFT_ARROW = new Icon(KEYS, 192, 96);

    public static final Icon KEY_RIGHT_ARROW = new Icon(KEYS, 192, 208);

    public static final Icon KEY_FN = new Icon(KEYS, 208, 96);

    public static final Icon KEY_RIGHT_SHIFT = new Icon(KEYS, 224, 96, 32, 16);

    public static final Icon KEY_SCROLL_LOCK = new Icon(KEYS, 192, 112, 32, 16);

    public static final Icon KEY_PAUSE = new Icon(KEYS, 224, 112, 32, 16);

    public static final Icon KEY_DELETE = new Icon(KEYS, 192, 128, 32, 16);

    public static final Icon KEY_END = new Icon(KEYS, 224, 128, 32, 16);

    public static final Icon KEY_PAGE_UP = new Icon(KEYS, 192, 144, 32, 16);

    public static final Icon KEY_PAGE_DOWN = new Icon(KEYS, 224, 144, 32, 16);

    public static final Icon KEY_BACKSLASH = new Icon(KEYS, 192, 160);

    public static final Icon KEY_VERTICAL_SLASH = new Icon(KEYS, 208, 160);

    public static final Icon KEY_NUMLCOK = new Icon(KEYS, 224, 160, 32, 16);

    public static final Icon KEY_ENTER = new Icon(KEYS, 192, 176, 32, 16);

    public static final Icon KEY_ENTER_TWO = new Icon(KEYS, 224, 176, 32, 16);

    public static final Icon KEY_CAPSLOCK_TWO = new Icon(KEYS, 192, 192, 32, 16);

    public static final Icon KEY_TAB_TWO = new Icon(KEYS, 224, 192, 32, 16);

    public static Icon registerIcon(String name, Icon instance) {
        IconRegistry.register(name, instance);
        return instance;
    }

    public static void register() {
        IconRegistry.register("bullet", BULLET);
        IconRegistry.register("particle", PARTICLE);
        IconRegistry.register("scene", SCENE);
        IconRegistry.register("rewind", REWIND);
        IconRegistry.register("faces", FACES);
        IconRegistry.register("alex", ALEX);
        IconRegistry.register("selector", SELECTOR);
        IconRegistry.register("slash", SLASH);
        IconRegistry.register("text", TEXT);
        IconRegistry.register("like", LIKE);
        IconRegistry.register("dislike", DISLIKE);
        IconRegistry.register("youtube", YOUTUBE);
        IconRegistry.register("discord", DISCORD);
        IconRegistry.register("telegram", TELEGRAM);
        IconRegistry.register("vk", VK);
        IconRegistry.register("twitter", TWITTER);
        IconRegistry.register("solid_like", SOLID_LIKE);
        IconRegistry.register("solid_dislike", SOLID_DISLIKE);
        IconRegistry.register("hub", HUB);
        IconRegistry.register("github", GITHUB);

        IconRegistry.register("other_briefcase", OTHER_BRIEFCASE);
        IconRegistry.register("other_medkit", OTHER_MEDKIT);
        IconRegistry.register("other_wallet", OTHER_WALLET);
        IconRegistry.register("other_hanger", OTHER_HANGER);
        IconRegistry.register("other_stats", OTHER_STATS);
        IconRegistry.register("other_banner", OTHER_BANNER);
        IconRegistry.register("other_openedLock", OTHER_OPENED_LOCK);
        IconRegistry.register("other_closedLock", OTHER_CLOSED_LOCK);
        IconRegistry.register("other_flag", OTHER_FLAG);
        IconRegistry.register("other_finishFlag", OTHER_FINISHFLAG);
        IconRegistry.register("other_umbrella", OTHER_UMBRELLA);
        IconRegistry.register("other_star", OTHER_STAR);
        IconRegistry.register("other_star_2", OTHER_STAR_2);
        IconRegistry.register("other_dungeonKey", OTHER_DUNGEONKEY);
        IconRegistry.register("other_carKey", OTHER_CARKEY);
        IconRegistry.register("other_key", OTHER_KEY);
        IconRegistry.register("other_x", OTHER_X);
        IconRegistry.register("other_backpack", OTHER_BACKPACK);
        IconRegistry.register("other_glosary", OTHER_GLOSARY);
        IconRegistry.register("other_book", OTHER_BOOK);
        IconRegistry.register("other_document", OTHER_DOCUMENT);
        IconRegistry.register("other_candle", OTHER_CANDLE);
        IconRegistry.register("other_hook", OTHER_HOOK);

        IconRegistry.register("adminpanel_user", ADMINPANEL_USER);
        IconRegistry.register("adminpanel_user_white_cross", ADMINPANEL_USER_WHITE_CROSS);
        IconRegistry.register("adminpanel_user_red_cross", ADMINPANEL_USER_RED_CROSS);
        IconRegistry.register("adminpanel_user_message", ADMINPANEL_MESSAGE);
        IconRegistry.register("adminpanel_user_message_white_cross", ADMINPANEL_MESSAGE_WHITE_CROSS);
        IconRegistry.register("adminpanel_user_message_red_cross", ADMINPANEL_MESSAGE_RED_CROSS);
        IconRegistry.register("adminpanel_voice", ADMINPANEL_VOICE);
        IconRegistry.register("adminpanel_white_voice", ADMINPANEL_VOICE_WHITE_CROSS);
        IconRegistry.register("adminpanel_red_voice", ADMINPANEL_VOICE_RED_CROSS);
        IconRegistry.register("adminpanel_speaker", ADMINPANEL_SPEAKER);
        IconRegistry.register("adminpanel_speaker_white_cross", ADMINPANEL_SPEAKER_WHITE_CROSS);
        IconRegistry.register("adminpanel_speaker_red_cross", ADMINPANEL_SPEAKER_RED_CROSS);
        IconRegistry.register("adminpanel_speaker_muted", ADMINPANEL_SPEAKER_MUTED);
        IconRegistry.register("adminpanel_speaker_low", ADMINPANEL_SPEAKER_LOW);
        IconRegistry.register("adminpanel_speaker_high", ADMINPANEL_SPEAKER_HIGH);

        IconRegistry.register("tools_sword", TOOLS_SWORD);
        IconRegistry.register("tools_knife", TOOLS_KNIFE);
        IconRegistry.register("tools_dagger", TOOLS_DAGGER);
        IconRegistry.register("tools_shortSword", TOOLS_SHORTSWORD);
        IconRegistry.register("tools_katana", TOOLS_KATANA);
        IconRegistry.register("tools_hammer", TOOLS_HAMMER);
        IconRegistry.register("tools_screwdriver", TOOLS_SCREWDRIVER);
        IconRegistry.register("tools_shovel", TOOLS_SHOVEL);
        IconRegistry.register("tools_glove", TOOLS_GLOVE);
        IconRegistry.register("tools_shuriken", TOOLS_SHURIKEN);
        IconRegistry.register("tools_bow", TOOLS_BOW);
        IconRegistry.register("tools_longBow", TOOLS_LONGBOW);
        IconRegistry.register("tools_arrow", TOOLS_ARROW);
        IconRegistry.register("tools_flyingArrows", TOOLS_FLYINGARROWS);
        IconRegistry.register("tools_shot", TOOLS_SHOT);
        IconRegistry.register("tools_bullet", TOOLS_BULLET);
        IconRegistry.register("tools_pistol", TOOLS_PISTOL);
        IconRegistry.register("tools_glock", TOOLS_GLOCK);
        IconRegistry.register("tools_revolver", TOOLS_REVOLVER);
        IconRegistry.register("tools_uzi", TOOLS_UZI);
        IconRegistry.register("tools_dynamite", TOOLS_DYNAMITE);
        IconRegistry.register("tools_grenade", TOOLS_GRENADE);
        IconRegistry.register("tools_tnt", TOOLS_TNT);

        IconRegistry.register("clothes_crown", CLOTHES_CROWN);
        IconRegistry.register("clothes_minicrown", CLOTHES_MINICROWN);
        IconRegistry.register("clothes_cylinder", CLOTHES_CYLINDER);
        IconRegistry.register("clothes_hat", CLOTHES_HAT);
        IconRegistry.register("clothes_cap", CLOTHES_CAP);
        IconRegistry.register("clothes_christmasHat", CLOTHES_CHRISTMASHAT);
        IconRegistry.register("clothes_favour", CLOTHES_FAVOUR);
        IconRegistry.register("clothes_glasses", CLOTHES_GLASSES);
        IconRegistry.register("clothes_tShort", CLOTHES_TSHORT);
        IconRegistry.register("clothes_jacket", CLOTHES_JACKET);
        IconRegistry.register("clothes_pants", CLOTHES_PANTS);
        IconRegistry.register("clothes_boots", CLOTHES_BOOTS);
        IconRegistry.register("clothes_sneakers", CLOTHES_SNEAKERS);
        IconRegistry.register("clothes_moustache", CLOTHES_MOUSTACHE);

        IconRegistry.register("device_laptop", DEVICE_LAPTOP);
        IconRegistry.register("device_computer", DEVICE_COMPUTER);
        IconRegistry.register("device_computer_2", DEVICE_COMPUTER_2);
        IconRegistry.register("device_diskdrive", DEVICE_DISKDRIVE);
        IconRegistry.register("device_phone", DEVICE_PHONE);
        IconRegistry.register("device_radio", DEVICE_RADIO);
        IconRegistry.register("device_camera", DEVICE_CAMERA);
        IconRegistry.register("device_arcade", DEVICE_ARCADE);
        IconRegistry.register("device_antenna", DEVICE_ANTENNA);
        IconRegistry.register("device_clock", DEVICE_CLOCK);
        IconRegistry.register("device_mouse", DEVICE_MOUSE);
        IconRegistry.register("device_keyboard", DEVICE_KEYBOARD);
        IconRegistry.register("device_gamepad", DEVICE_GAMEPAD);
        IconRegistry.register("device_microphone", DEVICE_MICROPHONE);
        IconRegistry.register("device_headphones", DEVICE_HEADPHONES);
        IconRegistry.register("device_speaker", DEVICE_SPEAKER);

        IconRegistry.register("computer_light", COMPUTER_LIGHT);
        IconRegistry.register("computer_cursor", COMPUTER_CURSOR);
        IconRegistry.register("computer_click", COMPUTER_CLICK);
        IconRegistry.register("computer_shutdown", COMPUTER_SHUTDOWN);
        IconRegistry.register("computer_settings", COMPUTER_SETTINGS);
        IconRegistry.register("computer_usb", COMPUTER_USB);
        IconRegistry.register("computer_battery_4", COMPUTER_BATTERY_4);
        IconRegistry.register("computer_battery_3", COMPUTER_BATTERY_3);
        IconRegistry.register("computer_battery_2", COMPUTER_BATTERY_2);
        IconRegistry.register("computer_battery_1", COMPUTER_BATTERY_1);
        IconRegistry.register("computer_battery_charging", COMPUTER_CHARGING);
        IconRegistry.register("computer_notification", COMPUTER_NOTIFICATION);
        IconRegistry.register("computer_defender", COMPUTER_DEFENDER);
        IconRegistry.register("computer_home", COMPUTER_HOME);
        IconRegistry.register("computer_mail", COMPUTER_MAIL);
        IconRegistry.register("computer_market", COMPUTER_MARKET);
        IconRegistry.register("computer_map", COMPUTER_MAP);
        IconRegistry.register("computer_mapPoint", COMPUTER_MAPPOINT);
        IconRegistry.register("computer_share", COMPUTER_SHARE);
        IconRegistry.register("computer_share_2", COMPUTER_SHARE_2);
        IconRegistry.register("computer_folder", COMPUTER_FOLDER);
        IconRegistry.register("computer_file", COMPUTER_FILE);
        IconRegistry.register("computer_paste", COMPUTER_PASTE);
        IconRegistry.register("computer_creditCard", COMPUTER_CREDITCARD);
        IconRegistry.register("computer_piano", COMPUTER_PIANO);
        IconRegistry.register("computer_cd", COMPUTER_CD);
        IconRegistry.register("computer_cassete", COMPUTER_CASSETE);
        IconRegistry.register("computer_note", COMPUTER_NOTE);
        IconRegistry.register("computer_note_2", COMPUTER_NOTE_2);

        IconRegistry.register("paint_pencil", PAINT_PENCIL);
        IconRegistry.register("paint_pencilEraser", PAINT_PENCILERASER);
        IconRegistry.register("paint_highlithter", PAINT_HIGHLITGHTER);
        IconRegistry.register("paint_brush", PAINT_BRUSH);
        IconRegistry.register("paint_brush_2", PAINT_BRUSH_2);
        IconRegistry.register("paint_picker", PAINT_PICKER);
        IconRegistry.register("paint_eraser", PAINT_ERASER);
        IconRegistry.register("paint_pouring", PAINT_POURING);
        IconRegistry.register("paint_cut", PAINT_CUT);

        IconRegistry.register("food_pan", FOOD_PAN);
        IconRegistry.register("food_spoon", FOOD_SPOON);
        IconRegistry.register("food_meat", FOOD_MEAT);
        IconRegistry.register("food_pizza", FOOD_PIZZA);
        IconRegistry.register("food_bread", FOOD_BREAD);
        IconRegistry.register("food_soup", FOOD_SOUP);
        IconRegistry.register("food_cake", FOOD_CAKE);
        IconRegistry.register("food_chocolate", FOOD_CHOCOLATE);
        IconRegistry.register("food_iceCream", FOOD_ICECREAM);
        IconRegistry.register("food_iceCream_2", FOOD_ICECREAM_2);
        IconRegistry.register("food_chupaChups", FOOD_CHUPACHUPS);
        IconRegistry.register("food_lollipop", FOOD_LOLLIPOP);
        IconRegistry.register("food_christmasLollipop", FOOD_CHRISTMASLOLLIPOP);
        IconRegistry.register("food_tea", FOOD_TEA);
        IconRegistry.register("food_coffee", FOOD_COFFEE);
        IconRegistry.register("food_bear", FOOD_BEAR);
        IconRegistry.register("food_jam", FOOD_JAM);
        IconRegistry.register("food_champagne", FOOD_CHAMPAGNE);
        IconRegistry.register("food_cocaCola", FOOD_COCACOLA);
        IconRegistry.register("food_cup", FOOD_CUP);
        IconRegistry.register("food_glass", FOOD_GLASS);
        IconRegistry.register("food_vial", FOOD_VIAL);
        IconRegistry.register("food_potion", FOOD_POTION);
        IconRegistry.register("food_cigarette", FOOD_CIGARETTE);
        IconRegistry.register("food_pipe", FOOD_PIPE);
        IconRegistry.register("food_lighter", FOOD_LIGHTER);
        IconRegistry.register("food_hookah", FOOD_HOOKAH);
        IconRegistry.register("food_poop", FOOD_POOP);

        IconRegistry.register("emoji_smiley", EMOJI_SMILEY);
        IconRegistry.register("emoji_relaxed", EMOJI_RELAXED);
        IconRegistry.register("emoji_stuckOutTongue", EMOJI_STUCKOUTTONGUE);
        IconRegistry.register("emoji_smirking", EMOJI_SMIRKING);
        IconRegistry.register("emoji_cool", EMOJI_COOL);
        IconRegistry.register("emoji_sob", EMOJI_SOB);
        IconRegistry.register("emoji_verySob", EMOJI_VERYSOB);
        IconRegistry.register("emoji_confused", EMOJI_CONFUSED);
        IconRegistry.register("emoji_rage", EMOJI_RAGE);
        IconRegistry.register("emoji_skull_large", EMOJI_SKULL_LARGE);
        IconRegistry.register("emoji_skull_medium", EMOJI_SKULL_MEDIUM);
        IconRegistry.register("emoji_skull_small", EMOJI_SKULL_SMALL);

        IconRegistry.register("money_coin", MONEY_COIN);
        IconRegistry.register("money_coinDollar", MONEY_COINDOLLAR);
        IconRegistry.register("money_banknotes", MONEY_BANKNOTES);
        IconRegistry.register("money_bagMoney", MONEY_BAGMONEY);

        IconRegistry.register("key_escape", KEY_ESCAPE);
        IconRegistry.register("key_f1", KEY_F1);
        IconRegistry.register("key_f2", KEY_F2);
        IconRegistry.register("key_f3", KEY_F3);
        IconRegistry.register("key_f4", KEY_F4);
        IconRegistry.register("key_f5", KEY_F5);
        IconRegistry.register("key_f6", KEY_F6);
        IconRegistry.register("key_f7", KEY_F7);
        IconRegistry.register("key_f8", KEY_F8);
        IconRegistry.register("key_f9", KEY_F9);
        IconRegistry.register("key_f10", KEY_F10);
        IconRegistry.register("key_f11", KEY_F11);
        IconRegistry.register("key_f12", KEY_F12);
        IconRegistry.register("key_print", KEY_PRINT);
        IconRegistry.register("key_gravias", KEY_GRAVIAS);
        IconRegistry.register("key_tilda", KEY_TILDA);
        IconRegistry.register("key_exclamation", KEY_EXCLAMATION);
        IconRegistry.register("key_symbol", KEY_SYMBOL);
        IconRegistry.register("key_grid", KEY_GRID);
        IconRegistry.register("key_dollar", KEY_DOLLAR);
        IconRegistry.register("key_procent", KEY_PROCENT);
        IconRegistry.register("key_caret", KEY_CARET);
        IconRegistry.register("key_ampersand", KEY_AMPERSAND);
        IconRegistry.register("key_star", KEY_STAR);
        IconRegistry.register("key_left_parentthesis", KEY_LEFT_PARENTTHESIS);
        IconRegistry.register("key_right_parentthesis", KEY_RIGHT_PARENTTHESIS);
        IconRegistry.register("key_insert", KEY_INSERT);
        IconRegistry.register("key_home", KEY_HOME);
        IconRegistry.register("key_1", KEY_1);
        IconRegistry.register("key_2", KEY_2);
        IconRegistry.register("key_3", KEY_3);
        IconRegistry.register("key_4", KEY_4);
        IconRegistry.register("key_5", KEY_5);
        IconRegistry.register("key_6", KEY_6);
        IconRegistry.register("key_7", KEY_7);
        IconRegistry.register("key_8", KEY_8);
        IconRegistry.register("key_9", KEY_9);
        IconRegistry.register("key_0", KEY_0);
        IconRegistry.register("key_minus", KEY_MINUS);
        IconRegistry.register("key_underline", KEY_UNDERLINE);
        IconRegistry.register("key_equals", KEY_EQUALS);
        IconRegistry.register("key_plus", KEY_PLUS);
        IconRegistry.register("key_backspace", KEY_BACKSPACE);
        IconRegistry.register("key_tab", KEY_TAB);
        IconRegistry.register("key_q", KEY_Q);
        IconRegistry.register("key_w", KEY_W);
        IconRegistry.register("key_e", KEY_E);
        IconRegistry.register("key_r", KEY_R);
        IconRegistry.register("key_t", KEY_T);
        IconRegistry.register("key_y", KEY_Y);
        IconRegistry.register("key_u", KEY_U);
        IconRegistry.register("key_i", KEY_I);
        IconRegistry.register("key_o", KEY_O);
        IconRegistry.register("key_p", KEY_P);
        IconRegistry.register("key_left_bracket", KEY_LEFT_BRACKET);
        IconRegistry.register("key_right_bracket", KEY_RIGHT_BRACKET);
        IconRegistry.register("key_left_brace", KEY_LEFT_BRACE);
        IconRegistry.register("key_right_brace", KEY_RIGHT_BRACE);
        IconRegistry.register("key_capslock", KEY_CAPSLOCK);
        IconRegistry.register("key_A", KEY_A);
        IconRegistry.register("key_s", KEY_S);
        IconRegistry.register("key_d", KEY_D);
        IconRegistry.register("key_f", KEY_F);
        IconRegistry.register("key_g", KEY_G);
        IconRegistry.register("key_h", KEY_H);
        IconRegistry.register("key_j", KEY_J);
        IconRegistry.register("key_k", KEY_K);
        IconRegistry.register("key_l", KEY_L);
        IconRegistry.register("key_semilicolon", KEY_SEMICOLON);
        IconRegistry.register("key_colon", KEY_COLON);
        IconRegistry.register("key_single_quote", KEY_SINGLE_QUOTE);
        IconRegistry.register("key_quotation_mark", KEY_QUOTATION_MARK);
        IconRegistry.register("key_up_arrow", KEY_UP_ARROW);
        IconRegistry.register("key_shift", KEY_SHIFT);
        IconRegistry.register("key_z", KEY_Z);
        IconRegistry.register("key_x", KEY_X);
        IconRegistry.register("key_c", KEY_C);
        IconRegistry.register("key_v", KEY_V);
        IconRegistry.register("key_b", KEY_B);
        IconRegistry.register("key_n", KEY_N);
        IconRegistry.register("key_m", KEY_M);
        IconRegistry.register("key_comma", KEY_COMMA);
        IconRegistry.register("key_left_angle_bracket", KEY_LEFT_ANGLE_BRACKET);
        IconRegistry.register("key_dot", KEY_DOT);
        IconRegistry.register("key_right_angle_bracket", KEY_RIGHT_ANGLE_BRACKET);
        IconRegistry.register("key_slash", KEY_SLASH);
        IconRegistry.register("key_question", KEY_QUESTION);
        IconRegistry.register("key_down_arrow", KEY_DOWN_ARROW);
        IconRegistry.register("key_ctrl", KEY_CTRL);
        IconRegistry.register("key_window", KEY_WINDOW);
        IconRegistry.register("key_alt", KEY_ALT);
        IconRegistry.register("key_space_small", KEY_SPACE_SMALL);
        IconRegistry.register("key_space_medium", KEY_SPACE_MEDIUM);
        IconRegistry.register("key_space_large", KEY_SPACE_LARGE);
        IconRegistry.register("key_left_arrow", KEY_LEFT_ARROW);
        IconRegistry.register("key_right_arrow", KEY_RIGHT_ARROW);
        IconRegistry.register("key_fn", KEY_FN);
        IconRegistry.register("key_right_shift", KEY_RIGHT_SHIFT);
        IconRegistry.register("key_scroll_lock", KEY_SCROLL_LOCK);
        IconRegistry.register("key_pause", KEY_PAUSE);
        IconRegistry.register("key_delete", KEY_DELETE);
        IconRegistry.register("key_end", KEY_END);
        IconRegistry.register("key_page_up", KEY_PAGE_UP);
        IconRegistry.register("key_page_down", KEY_PAGE_DOWN);
        IconRegistry.register("key_backslash", KEY_BACKSLASH);
        IconRegistry.register("key_vertical_slash", KEY_VERTICAL_SLASH);
        IconRegistry.register("key_numlock", KEY_NUMLCOK);
        IconRegistry.register("key_enter", KEY_ENTER);
        IconRegistry.register("key_enter_two", KEY_ENTER_TWO);
        IconRegistry.register("key_capslock_two", KEY_CAPSLOCK_TWO);
        IconRegistry.register("key_tab_two", KEY_TAB_TWO);

        // findCustomIcons();
    }

    @Deprecated
    public static void findCustomIcons() {
        String directoryPath = new File(Minecraft.getMinecraft().mcDataDir, "config/" + Mappet.MOD_ID + "/icons").getAbsolutePath();
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();
        if (files == null) return;
        JsonParser parser = new JsonParser();
        List<File> images = new ArrayList<>();
        for (File file : files) {
            String name = file.getName().toLowerCase();
            if (name.endsWith(".png")) images.add(file);
        }
        for (File image : images) {
            String baseName = image.getName().substring(0, image.getName().lastIndexOf('.'));
            try {
                DynamicTexture dynamicTexture = new DynamicTexture(ImageIO.read(image));
                ResourceLocation iconResource = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(Mappet.MOD_ID + ":" + baseName, dynamicTexture);
                File jsonFile = new File(directoryPath + "/" + baseName + ".json");
                if (jsonFile.exists()) {
                    try (FileReader reader = new FileReader(jsonFile)) {
                        JsonObject jsonObject = parser.parse(reader).getAsJsonObject();
                        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                            String key = entry.getKey();
                            JsonElement json = entry.getValue();
                            if (json.isJsonArray()) {
                                JsonArray array = json.getAsJsonArray();
                                IconRegistry.register(baseName + ":" + key, new Icon(iconResource, array.get(0).getAsInt(), array.get(1).getAsInt(), array.get(2).getAsInt(), array.get(3).getAsInt()));
                            }
                        }
                    } catch (IOException e) {
                        Mappet.logger.error(e.getMessage());
                    }
                } else {
                    System.out.println("No matching JSON file found for: " + image.getName());
                }
            } catch (IOException e) {
                Mappet.logger.error(e.getMessage());
            }
        }
    }
}
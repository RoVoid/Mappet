package mchorse.mappet.items;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static mchorse.mappet.Mappet.MOD_ID;

public class ModItems {
    public static CreativeTabs creativeTab = new CreativeTabs(MOD_ID) {
        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(NPC_TOOL);
        }
    };

    private static final Map<ResourceLocation, Item> blocks = new HashMap<>();

    public static Item NPC_TOOL = new ItemNpcTool();

    public static void register(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(NPC_TOOL);

        for (Item item : blocks.values()) event.getRegistry().register(item);
    }

    @SideOnly(Side.CLIENT)
    public static void bindModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(NPC_TOOL, 0, getNpcToolTexture());

        for (Item item : blocks.values()) {
            if (item.getRegistryName() != null)
                ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }

    @SideOnly(Side.CLIENT)
    public static Block addItemBlock(Block block) {
        ResourceLocation name = block.getRegistryName();
        if (name == null) return null;
        blocks.put(name, new ItemBlock(block).setRegistryName(name).setUnlocalizedName(block.getUnlocalizedName()));
        return block;
    }

    private static ModelResourceLocation getNpcToolTexture() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);

        String postfix = "";

        if (isChristmas(month, day)) postfix = "_christmas";
        else if (isEaster(year, month, day)) postfix = "_easter";
        else if (isAprilFoolsDay(month, day)) postfix = "_april";
        else if (isHalloween(month, day)) postfix = "_halloween";
        else if (isWinter(month)) postfix = "_winter";

        return new ModelResourceLocation(MOD_ID + ":npc_tool" + postfix, "inventory");
    }

    private static boolean isChristmas(int month, int day) {
        return month == Calendar.DECEMBER && day >= 24 && day <= 26;
    }

    private static boolean isAprilFoolsDay(int month, int day) {
        return month == Calendar.APRIL && day <= 2;
    }

    private static boolean isWinter(int month) {
        return month == Calendar.DECEMBER || month == Calendar.JANUARY || month == Calendar.FEBRUARY;
    }

    private static boolean isHalloween(int month, int day) {
        return month == Calendar.OCTOBER && day >= 24;
    }

    private static boolean isEaster(int year, int month, int day) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (h + l + 114) / 31;
        int p = (h + l + 114) % 31 + 1;

        return month == m - 1 && day == p;
    }
}

package mchorse.mappet.api.scripts.code;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import mchorse.mappet.Mappet;
import mchorse.mappet.api.scripts.code.blocks.ScriptBlockState;
import mchorse.mappet.api.scripts.code.entities.ScriptEntity;
import mchorse.mappet.api.scripts.code.items.ScriptItemStack;
import mchorse.mappet.api.scripts.code.nbt.ScriptNBTCompound;
import mchorse.mappet.api.scripts.code.nbt.ScriptNBTList;
import mchorse.mappet.api.scripts.code.ui.MappetUIBuilder;
import mchorse.mappet.api.scripts.user.IScriptFactory;
import mchorse.mappet.api.scripts.user.blocks.IScriptBlockState;
import mchorse.mappet.api.scripts.user.entities.IScriptEntity;
import mchorse.mappet.api.scripts.user.items.IScriptItemStack;
import mchorse.mappet.api.scripts.user.nbt.INBTCompound;
import mchorse.mappet.api.scripts.user.nbt.INBTList;
import mchorse.mappet.api.scripts.user.ui.IMappetUIBuilder;
import mchorse.mappet.api.ui.UI;
import mchorse.mappet.api.utils.SkinUtils;
import mchorse.mappet.api.utils.logs.MappetLogger;
import mchorse.mappet.utils.MPIcons;
import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScriptFactory implements IScriptFactory {
    private static final Map<String, String> formattingCodes = new HashMap<>();

    static {
        formattingCodes.put("black", "0");
        formattingCodes.put("dark_blue", "1");
        formattingCodes.put("dark_green", "2");
        formattingCodes.put("dark_aqua", "3");
        formattingCodes.put("dark_red", "4");
        formattingCodes.put("dark_purple", "5");
        formattingCodes.put("gold", "6");
        formattingCodes.put("gray", "7");
        formattingCodes.put("dark_gray", "8");
        formattingCodes.put("blue", "9");
        formattingCodes.put("green", "a");
        formattingCodes.put("aqua", "b");
        formattingCodes.put("red", "c");
        formattingCodes.put("light_purple", "d");
        formattingCodes.put("yellow", "e");
        formattingCodes.put("white", "f");
        formattingCodes.put("obfuscated", "k");
        formattingCodes.put("bold", "l");
        formattingCodes.put("strikethrough", "m");
        formattingCodes.put("underline", "n");
        formattingCodes.put("italic", "o");
        formattingCodes.put("reset", "r");
    }

    private NBTBase convertToNBT(Object object) {
        if (object instanceof String) return new NBTTagString((String) object);
        if (object instanceof Double) return new NBTTagDouble((Double) object);
        if (object instanceof Integer) return new NBTTagInt((Integer) object);
        if (object instanceof Boolean)
            return new NBTTagByte((Boolean) object ? Byte.valueOf("1") : Byte.valueOf("0"));
        if (object instanceof ScriptObjectMirror) {
            ScriptObjectMirror mirror = (ScriptObjectMirror) object;

            if (mirror.isArray()) {
                NBTTagList list = new NBTTagList();
                for (int i = 0, c = mirror.size(); i < c; i++) {
                    NBTBase base = this.convertToNBT(mirror.getSlot(i));
                    if (base != null) list.appendTag(base);
                }
                return list;
            }

            NBTTagCompound tag = new NBTTagCompound();
            for (String key : mirror.keySet()) {
                NBTBase base = this.convertToNBT(mirror.get(key));
                if (base != null) tag.setTag(key, base);
            }
            return tag;
        }

        return null;
    }

    @Deprecated
    @Override
    public IScriptBlockState createBlockState(String blockId, int meta) {
        return createBlock(blockId, meta);
    }

    @Deprecated
    @Override
    public IScriptBlockState createBlockState(String blockId) {
        return createBlock(blockId, 0);
    }

    @Override
    public IScriptBlockState createBlock(String blockId, int meta) {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
        return ScriptBlockState.create(block == null ? null : block.getStateFromMeta(meta));
    }

    @Override
    public IScriptBlockState createBlock(String blockId) {
        return createBlock(blockId, 0);
    }

    @Override
    public IScriptItemStack createBlockItem(String blockId, int count, int meta) {
        Block item = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
        return item == null ? null : ScriptItemStack.create(new ItemStack(item, count, meta));
    }

    @Override
    public INBTCompound createCompound(String nbt) {
        NBTTagCompound tag = new NBTTagCompound();

        if (nbt != null) {
            try {
                tag = JsonToNBT.getTagFromJson(nbt);
            } catch (Exception ignored) {
            }
        }

        return new ScriptNBTCompound(tag);
    }

    @Override
    public INBTCompound createCompoundFromJS(Object jsObject) {
        NBTBase base = convertToNBT(jsObject);
        return base instanceof NBTTagCompound ? new ScriptNBTCompound((NBTTagCompound) base) : null;
    }

    @Override
    public IScriptItemStack createItem(INBTCompound compound) {
        if (compound == null) return ScriptItemStack.EMPTY;
        return ScriptItemStack.create(new ItemStack(compound.asMinecraft()));
    }

    @Override
    public IScriptItemStack createItem(String itemId, int count, int meta) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
        return item == null ? null : ScriptItemStack.create(new ItemStack(item, count, meta));
    }

    @Override
    public INBTList createList(String nbt) {
        NBTTagList list = new NBTTagList();

        if (nbt != null) {
            try {
                list = (NBTTagList) JsonToNBT.getTagFromJson("{List:" + nbt + "}").getTag("List");
            } catch (Exception ignored) {
            }
        }

        return new ScriptNBTList(list);
    }

    @Override
    public INBTList createListFromJS(Object jsObject) {
        NBTBase base = convertToNBT(jsObject);
        return base instanceof NBTTagList ? new ScriptNBTList((NBTTagList) base) : null;
    }

    @Override
    public AbstractMorph createMorph(INBTCompound compound) {
        return compound == null ? null : MorphManager.INSTANCE.morphFromNBT(compound.asMinecraft());
    }

    @Override
    public IMappetUIBuilder createUI(String script, String function) {
        return new MappetUIBuilder(new UI(), script == null ? "" : script, function == null ? "" : function);
    }

    @Override
    public String encrypt(String text, String secretKey) {
        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[textBytes.length];

        for (int i = 0; i < textBytes.length; i++)
            result[i] = (byte) (textBytes[i] ^ keyBytes[i % keyBytes.length]);

        return Base64.getEncoder().encodeToString(result);
    }

    @Override
    public String decrypt(String encryptedText, String secretKey) {
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[encryptedBytes.length];

        for (int i = 0; i < encryptedBytes.length; i++)
            result[i] = (byte) (encryptedBytes[i] ^ keyBytes[i % keyBytes.length]);

        return new String(result, StandardCharsets.UTF_8);
    }

    @Override
    public String dump(Object object, boolean simple) {
        if (object instanceof ScriptObjectMirror) return object.toString();

        Class<?> clazz = object.getClass();
        StringBuilder output = new StringBuilder(simple ? clazz.getSimpleName() : clazz.getTypeName());
        output.append(" {\n");
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            output.append("    ");
            if (!simple) output.append(this.getModifier(field.getModifiers()));
            output.append(field.getName());
            if (!simple) output.append(" (").append(field.getType().getTypeName()).append(")");

            String value = "";
            try {
                field.setAccessible(true);
                Object o = field.get(object);

                value = o == null ? "null" : o.toString();
            } catch (Exception ignored) {
            }

            output.append(": ").append(value).append("\n");
        }

        output.append("\n");

        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers())) continue;

            output.append("    ");

            if (!simple) output.append(this.getModifier(method.getModifiers()));

            output.append(simple ? method.getReturnType().getSimpleName() : method.getReturnType().getTypeName());
            output.append(" ");
            output.append(method.getName()).append("(");

            int size = method.getParameterCount();

            for (int i = 0; i < size; i++) {
                Class<?> arg = method.getParameterTypes()[i];
                output.append(simple ? arg.getSimpleName() : arg.getTypeName());
                if (i < size - 1) output.append(", ");
            }

            output.append(")").append("\n");
        }

        output.append("}");

        return output.toString();
    }

    @Override
    public String format(String format, Object... args) {
        return String.format(format, args);
    }

    @Override
    public Object get(String key) {
        return Mappet.scripts.objects.get(key);
    }

    public MappetLogger getLogger() {
        return Mappet.logger;
    }

    public IScriptEntity getMappetEntity(Entity minecraftEntity) {
        return ScriptEntity.create(minecraftEntity);
    }

    private String getModifier(int m) {
        String modifier = Modifier.isFinal(m) ? "final " : "";

        if (Modifier.isPublic(m)) modifier += "public ";
        else if (Modifier.isProtected(m)) modifier += "protected ";
        else if (Modifier.isPrivate(m)) modifier += "private ";

        return modifier;
    }

    @Override
    public EnumParticleTypes getParticleType(String type) {
        return EnumParticleTypes.getByName(type);
    }

    @Override
    public Potion getPotion(String type) {
        return Potion.getPotionFromResourceLocation(type);
    }

    @Override
    public ScriptResourcePack pack(String name) {
        return new ScriptResourcePack(name);
    }

    @Override
    public void set(String key, Object object) {
        Mappet.scripts.objects.put(key, object);
    }

    @Override
    public String style(String... styles) {
        StringBuilder builder = new StringBuilder();
        for (String style : styles) {
            String code = formattingCodes.get(style);
            if (code != null) builder.append('§').append(code);
        }
        return builder.toString();
    }

    @Override
    public INBTCompound toNBT(Object object) {
        if (object instanceof INBTCompound) return (INBTCompound) object;
        if (object instanceof NBTTagCompound) return new ScriptNBTCompound((NBTTagCompound) object);
        if (object instanceof AbstractMorph) return new ScriptNBTCompound(((AbstractMorph) object).toNBT());
        return null;
    }

    @Override
    public String getSkin(String nickname) {
        return SkinUtils.getSkin(nickname);
    }

    @Override
    public String getSkin(String nickname, String source) {
        return SkinUtils.getSkin(nickname, source);
    }

    @Override
    public Object getSkinObject(String nickname) {
        return SkinUtils.getSkinObject(nickname);
    }

    @Override
    public Object getSkinObject(String nickname, String source) {
        return SkinUtils.getSkinObject(nickname, source);
    }

    @Override
    public List<String> getAllIcons() {
        return MPIcons.getAllNames();
    }
}
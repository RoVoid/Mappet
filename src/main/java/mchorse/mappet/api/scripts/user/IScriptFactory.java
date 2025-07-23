package mchorse.mappet.api.scripts.user;

import mchorse.mappet.api.scripts.code.ScriptResourcePack;
import mchorse.mappet.api.scripts.user.blocks.IScriptBlockState;
import mchorse.mappet.api.scripts.user.entities.IScriptEntity;
import mchorse.mappet.api.scripts.user.entities.IScriptPlayer;
import mchorse.mappet.api.scripts.user.items.IScriptItemStack;
import mchorse.mappet.api.scripts.user.logs.IMappetLogger;
import mchorse.mappet.api.scripts.user.nbt.INBTCompound;
import mchorse.mappet.api.scripts.user.nbt.INBTList;
import mchorse.mappet.api.scripts.user.ui.IMappetUIBuilder;
import mchorse.mappet.api.scripts.user.ui.IMappetUIContext;
import mchorse.mappet.api.scripts.user.world.IScriptWorld;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.entity.Entity;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumParticleTypes;

import java.util.List;

/**
 * Scripting API factory that allows to initialize/create different stuff.
 *
 * <p>You can access it in the script as <code>mappet</code> global variable. Here is a
 * code example:</p>
 *
 * <pre>{@code
 *    function main(c) {
 *        // Create a diamond hoe using Mappet's factory
 *        var item = mappet.createItem("minecraft:diamond_hoe");
 *
 *        c.getSubject().setMainItem(item);
 *    }
 * }</pre>
 */
public interface IScriptFactory {
    @Deprecated
    IScriptBlockState createBlockState(String blockId, int meta);

    @Deprecated
    IScriptBlockState createBlockState(String blockId);

    /**
     * Get a block state that can be used to place and compare blocks in
     * the {@link IScriptWorld}.
     *
     * <pre>{@code
     *    var fence = mappet.createBlock("minecraft:fence", 0);
     *
     *    // minecraft:fence 0
     *    c.send(fence.getBlockId() + " " + fence.getMeta());
     * }</pre>
     */
    IScriptBlockState createBlock(String blockId, int meta);


    /**
     * Create a block state that can with the default meta-value.
     *
     * <pre>{@code
     * var fence = mappet.createBlock("minecraft:fence");
     *
     * // minecraft:fence 0
     * c.send(fence.getBlockId() + " " + fence.getMeta());
     * }</pre>
     */
    IScriptBlockState createBlock(String blockId);

    /**
     * Create an empty NBT compound.
     *
     * <pre>{@code
     *    var tag = mappet.createCompound();
     *
     *    tag.setString("id", "minecraft:diamond_hoe");
     *    tag.setByte("Count", 1);
     *
     *    var item = mappet.createItemNBT(tag);
     *
     *    // {id:"minecraft:diamond_hoe",Count:1b,Damage:0s}
     *    c.send(item.serialize());
     * }</pre>
     */
    default INBTCompound createCompound() {
        return this.createCompound(null);
    }

    /**
     * Parse an NBT compound date out of given string, if string NBT was
     * invalid then an empty compound will be returned.
     *
     * <pre>{@code
     *    var tag = mappet.createCompound("{id:\"minecraft:diamond_hoe\",Count:1b}");
     *    var item = mappet.createItemNBT(tag);
     *
     *    // {id:"minecraft:diamond_hoe",Count:1b,Damage:0s}
     *    c.send(item.serialize());
     * }</pre>
     */
    INBTCompound createCompound(String nbt);

    /**
     * Turn a JS object into an NBT compound.
     *
     * <p><b>BEWARE</b>: when converting JS object to NBT keeps in mind some
     * limitations of the NBT format:</p>
     *
     * <ul>
     *     <li>NBT supports multiple number storage formats (byte, short, int, long, float,
     *     double), so the converter will only be able to convert numbers to either
     *     integer or double NBT tags, depending on how did you get the number, <code>42</code>
     *     being an integer, and <code>42.0</code> being a double.</li>
     *     <li>NBT lists support only storage of a <b>single type</b> at once, so if you
     *     provide an JS array like <code>[0, 1, 2, "test", {a:1,b:2}, 4, [0, 0, 0], 5.5]</code>
     *     then <b>only the first element's</b> type will be taken in the account, and the
     *     resulted NBT list will turn out like <code>[0.0d, 1.0d, 2.0d, 4.0d, 5.5d]</code>.
     *     <b>In case with numbers</b> if you had first integers, and somewhere in the
     *     middle of the list you got a double, then the integer type <b>will get converted
     *     to double</b>!</li>
     * </ul>
     *
     * <pre>{@code
     *    var tag = mappet.createCompoundFromJS({id:"minecraft:diamond_hoe",Count:1});
     *    var item = mappet.createItemNBT(tag);
     *
     *    // {id:"minecraft:diamond_hoe",Count:1b,Damage:0s}
     *    c.send(item.serialize());
     * }</pre>
     */
    INBTCompound createCompoundFromJS(Object jsObject);

    /**
     * Create an empty NBT list.
     *
     * <pre>{@code
     *    var list = mappet.createList();
     *
     *    list.addInt(1);
     *    list.addInt(2);
     *    list.addInt(3);
     *    list.addInt(4);
     *    list.addInt(5);
     *    list.addInt(6);
     *
     *    // [1,2,3,4,5,6]
     *    c.send(list.stringify());
     * }</pre>
     */
    default INBTList createList() {
        return this.createList(null);
    }

    /**
     * Parse an NBT list date out of given string, if string NBT was
     * invalid then an empty list will be returned.
     *
     * <pre>{@code
     *    var list = mappet.createList("[1, 2, 3, 4, 5, 6]");
     *
     *    // [1,2,3,4,5,6]
     *    c.send(list.stringify());
     * }</pre>
     */
    INBTList createList(String nbt);

    /**
     * Turn a JS object into an NBT compound.
     *
     * <p><b>Read carefully the description</b> of {@link #createCompoundFromJS(Object)}
     * for information about JS to NBT object conversion limitations!</p>
     *
     * <pre>{@code
     *    var list = mappet.createListFromJS([1, 2, 3, 4, 5, 6]);
     *
     *    // [1,2,3,4,5,6]
     *    c.send(list.stringify());
     * }</pre>
     */
    INBTList createListFromJS(Object jsObject);

    /**
     * Create an item stack out of string NBT.
     *
     * <pre>{@code
     *    var item = mappet.createItemNBT("{id:\"minecraft:enchanted_book\",Count:1b,tag:{StoredEnchantments:[{lvl:4s,id:4s}]},Damage:0s}");
     *
     *    // It will output "minecraft:enchanted_book"
     *    c.send(item.getItem().getId());
     * }</pre>
     *
     * @return an item stack from the string NBT data, or an empty item stack
     * if the data doesn't have a valid reference to an existing item
     */
    default IScriptItemStack createItemNBT(String nbt) {
        return this.createItem(this.createCompound(nbt));
    }

    /**
     * Create an item stack out of string NBT.
     *
     * <pre>{@code
     *    var tag = mappet.createCompound("{id:\"minecraft:diamond_hoe\",Count:1b}");
     *    var item = mappet.createItemNBT(tag);
     *
     *    // {id:"minecraft:diamond_hoe",Count:1b,Damage:0s}
     *    c.send(item.serialize());
     * }</pre>
     *
     * @return an item stack from the NBT data, or an empty item stack if the
     * data doesn't have a valid reference to an existing item
     */
    IScriptItemStack createItem(INBTCompound compound);

    /**
     * Create an item stack with item ID.
     *
     * <pre>{@code
     *    var item = mappet.createItem("minecraft:diamond");
     *
     *    // {id:"minecraft:diamond",Count:1b,Damage:0s}
     *    c.send(item.serialize());
     * }</pre>
     *
     * @return an item stack with an item specified by ID, or an empty item
     * stack if the block doesn't exist
     */
    default IScriptItemStack createItem(String itemId) {
        return this.createItem(itemId, 1);
    }

    /**
     * Create an item stack with item ID, count
     *
     * <pre>{@code
     *    var item = mappet.createItem("minecraft:diamond", 64);
     *
     *    // {id:"minecraft:diamond",Count:64b,Damage:0s}
     *    c.send(item.serialize());
     * }</pre>
     *
     * @return an item stack with an item specified by ID, or an empty item
     * stack if the block doesn't exist
     */
    default IScriptItemStack createItem(String itemId, int count) {
        return this.createItem(itemId, count, 0);
    }

    /**
     * Create an item stack with item ID, count and meta
     *
     * <pre>{@code
     *    var damaged_hoe = mappet.createItem("minecraft:diamond_hoe", 64, 5);
     *
     *    // {id:"minecraft:diamond_hoe",Count:64b,Damage:5s}
     *    c.send(damaged_hoe.serialize());
     * }</pre>
     *
     * @return an item stack with an item specified by ID, or an empty item
     * stack if the block doesn't exist
     */
    IScriptItemStack createItem(String itemId, int count, int meta);

    /**
     * Create an item stack with block ID.
     *
     * <pre>{@code
     *    var stone = mappet.createBlockItem("minecraft:stone");
     *
     *    // {id:"minecraft:stone",Count:1b,Damage:0s}
     *    c.send(stone.serialize());
     * }</pre>
     *
     * @return an item stack with an item specified by ID, or an empty item
     * stack if the block doesn't exist
     */
    default IScriptItemStack createBlockItem(String blockId) {
        return this.createItem(blockId, 1);
    }

    /**
     * Create an item stack with block ID and count.
     *
     * <pre>{@code
     *    var stone = mappet.createBlockItem("minecraft:stone", 64);
     *
     *    // {id:"minecraft:stone",Count:64b,Damage:0s}
     *    c.send(stone.serialize());
     * }</pre>
     *
     * @return an item stack with an item specified by ID, or an empty item
     * stack if the block doesn't exist
     */
    default IScriptItemStack createBlockItem(String blockId, int count) {
        return this.createItem(blockId, count, 0);
    }

    /**
     * Create an item stack with block ID, count and meta.
     *
     * <pre>{@code
     *    var andesite = mappet.createBlockItem("minecraft:stone", 64, 5);
     *
     *    // {id:"minecraft:stone",Count:64b,Damage:5s}
     *    c.send(andesite.serialize());
     * }</pre>
     *
     * @return an item stack with block specified by ID, or an empty item
     * stack if the block doesn't exist
     */
    IScriptItemStack createBlockItem(String blockId, int count, int meta);

    List<String> getAllIcons();

    /**
     * Get the Minecraft particle type by its name.
     *
     * <p>You can find out all the particle types by typing in <code>/particle</code>
     * command, and looking up the completion of the first argument (i.e., press tab after
     * typing in <code>/particle</code> and a space).</p>
     *
     * <pre>{@code
     *    var explode = mappet.getParticleType("explode");
     *    var pos = c.getSubject().getPosition();
     *
     *    c.getWorld().spawnParticles(explode, true, pos.x, pos.y + 1, pos.z, 50, 0.5, 0.5, 0.5, 0.1);
     * }</pre>
     */
    EnumParticleTypes getParticleType(String type);

    /**
     * Get skin texture URL for a player from Mojang
     *
     * <pre>{@code
     *    var skinUrl = mappet.getSkin("McHorseYT");
     * }</pre>
     *
     * @param nickname Player's Minecraft nickname
     */
    String getSkin(String nickname);

    /**
     * Get skin texture URL for a player from the specific source
     *
     * <p>Source defines where the skin should be fetched from</p>
     * <p>Valid sources: <code>Minecraft / Mojang</code>, <code>ElyBy / Ely.By</code>, <code>TL / TLauncher</code></p>
     *
     * <pre>{@code
     *    var skinUrl = mappet.getSkin("McHorseYT", "mojang");
     * }</pre>
     *
     * @param nickname Player's Minecraft nickname
     * @param source   Source of skin data
     */
    String getSkin(String nickname, String source);

    /**
     * Get an object containing both the skin texture URL and whether the model is slim (Alex)
     *
     * <pre>{@code
     *    var player = c.getPlayer();
     *    var skinObject = mappet.getSkinObject("McHorseYT");
     *
     *    var skin = skinObject.url;
     *    var type = skinObject.slim ? "slim" : "fred";
     *
     *    var morph = mappet.createMorph('{Name:"blockbuster.' + type + '",Skin:"' + skin  + '"}');
     *    player.setMorph(morph);
     * }</pre>
     *
     * @param nickname Player's Minecraft nickname
     * @return <code>url</code> – URL to the player's skin texture</li>, <code>slim</code> – boolean indicating if the model is Alex</li>
     */
    Object getSkinObject(String nickname);

    /**
     * Get an object containing both the skin texture URL and whether the model is slim (Alex)
     *
     * <p>Source defines where the skin should be fetched from</p>
     * <p>Valid sources: <code>Minecraft / Mojang</code>, <code>ElyBy / Ely.By</code>, <code>TL / TLauncher</code></p>
     *
     * <pre>{@code
     *    var player = c.getPlayer();
     *    var skinObject = mappet.getSkinObject("McHorseYT", "Mojang");
     *
     *    var skin = skinObject.url;
     *    var type = skinObject.slim ? "slim" : "fred";
     *
     *    var morph = mappet.createMorph('{Name:"blockbuster.' + type + '",Skin:"' + skin  + '"}');
     *    player.setMorph(morph);
     * }</pre>
     *
     * @param nickname Player's Minecraft nickname
     * @param source   Source of skin data
     * @return <code>url</code> – URL to the player's skin texture</li>, <code>slim</code> – boolean indicating if the model is Alex</li>
     */
    Object getSkinObject(String nickname, String source);

    /**
     * Get Minecraft potion effect by its name.
     *
     * <p>You can find out all the particle types by typing in <code>/effect</code>
     * command, and looking up the completion of the second argument (i.e., press tab after
     * typing in <code>/particle Player</code> and a space).</p>
     *
     * <pre>{@code
     *    var slowness = mappet.getPotion("slowness");
     *    c.getSubject().applyPotion(slowness, 200, 1, false);
     * }</pre>
     */
    Potion getPotion(String type);

    /**
     * Create a morph out of string NBT.
     *
     * <pre>{@code
     *    var morph = mappet.createMorph("{Name:\"blockbuster.alex\"}");
     *
     *    // Assuming c.getSubject() is a player
     *    c.getSubject().setMorph(morph);
     * }</pre>
     */
    default AbstractMorph createMorph(String nbt) {
        return this.createMorph(this.createCompound(nbt));
    }

    /**
     * Create a morph out of NBT.
     *
     * <pre>{@code
     *    var tag = mappet.createCompound();
     *
     *    tag.setString("Name", "blockbuster.alex");
     *
     *    var morph = mappet.createMorph(tag);
     *
     *    // Assuming c.getSubject() is a player
     *    c.getSubject().setMorph(morph);
     * }</pre>
     */
    AbstractMorph createMorph(INBTCompound compound);

    /**
     * Create a UI. You can send it to the player by using
     * {@link IScriptPlayer#openUI(IMappetUIBuilder)} method.
     *
     * <pre>{@code
     *    function main(c)
     *    {
     *        var ui = mappet.createUI().background();
     *        var label = ui.label("Hello, world!").background(0x88000000);
     *
     *        label.rxy(0.5, 0.5).wh(80, 20).anchor(0.5).labelAnchor(0.5);
     *
     *        c.getSubject().openUI(ui);
     *    }
     * }</pre>
     */
    default IMappetUIBuilder createUI() {
        return this.createUI("", "");
    }

    /**
     * Create a UI with a script handler. You can send it to the
     * player by using {@link IScriptPlayer#openUI(IMappetUIBuilder)} method.
     *
     *
     *
     * <pre>{@code
     *    function main(c)
     *    {
     *        var ui = mappet.createUI(c, "handler").background();
     *        var label = ui.label("Hello, world!").background(0x88000000);
     *        var button = ui.button("Push me!").id("button");
     *
     *        label.rxy(0.5, 0.5).wh(80, 20).anchor(0.5).labelAnchor(0.5);
     *        label.rx(0.5).ry(0.5, 25).wh(80, 20).anchor(0.5);
     *
     *        c.getSubject().openUI(ui);
     *    }
     *
     *    function handler(c)
     *    {
     *        var uiContext = c.getSubject().getUIContext();
     *
     *        if (uiContext.getLast() === "button")
     *        {
     *            // Button was pressed
     *        }
     *    }
     * }</pre>
     *
     * @param event    Script event (whose script ID will be used for UI's user input handler).
     * @param function Given script's function that will be used as UI's user input handler.
     */
    default IMappetUIBuilder createUI(IScriptEvent event, String function) {
        return this.createUI(event.getScript(), function);
    }

    /**
     * Create a UI with a script handler. You can send it to the
     * player by using {@link IScriptPlayer#openUI(IMappetUIBuilder)} method.
     *
     * <p>Script and function arguments allow pointing to the function in some
     * script, which it will be responsible for handling the user input from
     * scripted UI.</p>
     *
     * <p>In the UI handler, you can access subject's UI context ({@link IMappetUIContext})
     * which has all the necessary methods to handle user's input.</p>
     *
     * <pre>{@code
     *    // ui.js
     *    function main(c)
     *    {
     *        var ui = mappet.createUI("handler", "main").background();
     *        var label = ui.label("Hello, world!").background(0x88000000);
     *        var button = ui.button("Push me!").id("button");
     *
     *        label.rxy(0.5, 0.5).wh(80, 20).anchor(0.5).labelAnchor(0.5);
     *        label.rx(0.5).ry(0.5, 25).wh(80, 20).anchor(0.5);
     *
     *        c.getSubject().openUI(ui);
     *    }
     *
     *    // handler.js
     *    function main(c)
     *    {
     *        var uiContext = c.getSubject().getUIContext();
     *
     *        if (uiContext.getLast() === "button")
     *        {
     *            // Button was pressed
     *        }
     *    }
     * }</pre>
     *
     * @param script   The script which will be used as UI's user input handler.
     * @param function Given script's function that will be used as UI's user input handler.
     */
    IMappetUIBuilder createUI(String script, String function);

    /**
     * Get a global arbitrary object.
     *
     * <pre>{@code
     *    var number = mappet.get("number");
     *
     *    if (number === null || number === undefined)
     *    {
     *        number = 42;
     *        mappet.set("number", number);
     *    }
     * }</pre>
     */
    Object get(String key);

    /**
     * Set a global arbitrary object during server's existence (other scripts
     * can access this data too).
     *
     * <pre>{@code
     *    var number = mappet.get("number");
     *
     *    if (number === null || number === undefined)
     *    {
     *        number = 42;
     *        mappet.set("number", number);
     *    }
     * }</pre>
     */
    void set(String key, Object object);

    /**
     * Dump the simple representation of a given non-JS object into the string (to see
     * what fields and methods are available for use).
     *
     * <pre>{@code
     *    var morph = mappet.createMorph("{Name:\"blockbuster.alex\"}");
     *
     *    c.send(mappet.dump(morph));
     * }</pre>
     */
    default String dump(Object object) {
        return this.dump(object, true);
    }

    /**
     * Dump given a non-JS object into the string (to see what fields and methods are
     * available for use).
     *
     * <pre>{@code
     *    var morph = mappet.createMorph("{Name:\"blockbuster.alex\"}");
     *
     *    c.send(mappet.dump(morph, true));
     * }</pre>
     *
     * @param simple Whether you want to see simple or full information about
     *               the object.
     */
    String dump(Object object, boolean simple);

    /**
     * Return Minecraft's formatting code.
     *
     * <p>The Following colors are supported: black, dark_blue, dark_green, dark_aqua,
     * dark_red, dark_purple, gold, gray, dark_gray, blue, green, aqua, red,
     * light_purple, yellow, white</p>
     *
     * <p>The following styles are supported: obfuscated, bold, strikethrough, underline,
     * italic, reset.</p>
     *
     * <pre>{@code
     *    var style = mappet.style("dark_blue", "bold", "underline");
     *
     *    c.send(style + "This text is in blue!");
     * }</pre>
     *
     * @param codes An enumeration of formatting codes.
     */
    String style(String... codes);

    /**
     * Return a mappet logger instance.
     */
    IMappetLogger getLogger();

    /**
     * Return a mappet entity/player/npc by given minecraft entity.
     *
     * <pre>{@code
     * function main(c)
     * {
     *     var s = c.getSubject();
     *     var minecraftPlayer = s.minecraftPlayer;
     *     var mappetPlayer = mappet.getMappetEntity(minecraftPlayer);
     *     c.send(mappetPlayer.name);
     * }
     * }</pre>
     */
    IScriptEntity getMappetEntity(Entity minecraftEntity);

    /**
     * Converts an object to an INBTCompound representation.
     *
     * @param object The object to convert to an INBTCompound.
     * @return The INBTCompound representation of the object or null if the object is not of the expected types.
     */
    INBTCompound toNBT(Object object);

    /**
     * Formates strings (placeholders).
     *
     * <pre>{@code
     * // Example:
     * var name = "Steve";
     * var age = 18;
     * var message = mappet.format("Hello %s, you are %d years old!", name, age);
     * c.send(message);
     *
     * // You can also use the positional arguments:
     * var s = c.getSubject();
     * var pos = s.getPosition();
     * var message = mappet.format("Hello %1$s, you are in x:%2$.2f, y:%3$.2f, z:%4$.2f!", s.getName(), pos.x, pos.y, pos.z);
     * s.send(message);
     * }</pre>
     *
     * @param format string to format
     * @param args   arguments to replace
     * @return formatted string
     */
    String format(String format, Object... args);

    /**
     * Encrypt text
     *
     * @return Encrypted text by secretKey
     */
    String encrypt(String text, String secretKey);

    String decrypt(String text, String secretKey);

    ScriptResourcePack pack(String name);
}
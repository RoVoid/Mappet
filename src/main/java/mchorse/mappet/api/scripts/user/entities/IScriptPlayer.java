package mchorse.mappet.api.scripts.user.entities;

import mchorse.mappet.api.scripts.code.score.ScriptScoreObjective;
import mchorse.mappet.api.scripts.code.score.ScriptScoreboard;
import mchorse.mappet.api.scripts.code.score.ScriptTeam;
import mchorse.mappet.api.scripts.user.data.ScriptVector;
import mchorse.mappet.api.scripts.user.items.IScriptInventory;
import mchorse.mappet.api.scripts.user.items.IScriptItemStack;
import mchorse.mappet.api.scripts.user.mappet.IMappetQuests;
import mchorse.mappet.api.scripts.user.mappet.IMappetUIBuilder;
import mchorse.mappet.api.scripts.user.mappet.IMappetUIContext;
import mchorse.mappet.api.scripts.user.nbt.INBT;
import mchorse.mappet.api.scripts.user.nbt.INBTCompound;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.List;
import java.util.Set;

/**
 * Player entity interface.
 *
 * <p>This interface represents a player entity.</p>
 *
 * <pre>{@code
 *    function main(c)
 *    {
 *        if (c.getSubject().isPlayer())
 *        {
 *            // Do something with the player...
 *        }
 *    }
 * }</pre>
 */
public interface IScriptPlayer extends IScriptEntity {
    /**
     * Get Minecraft player entity instance. <b>BEWARE:</b> you need to know the
     * MCP mappings in order to directly call methods on this instance!
     */
    EntityPlayerMP getMinecraftPlayer();

    /**
     * @return Whether player is an operator
     */
    boolean isOperator();

    /**
     * Get player's game mode.
     *
     * <pre>{@code
     *    // Assuming that c.getSubject() is a player
     *    var player = c.getSubject();
     *    var gamemode = player.getGameMode();
     *
     *    if (gamemode === 0)
     *    {
     *        player.send("You're in survival mode!");
     *    }
     * }</pre>
     *
     * @return Player's game mode as an integer, <code>0</code> is survival, <code>1</code>
     * is creative, <code>2</code> is adventure , and <code>3</code> is spectator.
     */
    int getGameMode();

    /**
     * Set player's game mode.
     *
     * <pre>{@code
     *    // Assuming that c.getSubject() is a player
     *    var player = c.getSubject();
     *    var gamemode = c.getSubject().getGameMode();
     *
     *    // When player exits the mining region, set their game mode back to adventure
     *    if (gamemode === 0 && !player.getStates().has("region.mining_factory"))
     *    {
     *        player.setGameMode(2);
     *    }
     * }</pre>
     *
     * @param gameMode Player's game mode <code>0</code> is survival, <code>1</code>
     *                 is creative, <code>2</code> is adventure , and <code>3</code> is spectator.
     */
    void setGameMode(int gameMode);

    /**
     * Get player's inventory.
     *
     * <pre>{@code
     *    // Assuming that c.getSubject() is a player
     *    var inventory = c.getSubject().getInventory();
     *    var item = mappet.createItem("minecraft:diamond_sword");
     *
     *    // This will change the first slot in the hotbar to a diamond sword
     *    inventory.setStack(0, item);
     * }</pre>
     */
    IScriptInventory getInventory();

    /**
     * Get player's ender chest inventory.
     *
     * <pre>{@code
     *    // Assuming that c.getSubject() is a player
     *    var inventory = c.getSubject().getEnderChest();
     *    var item = mappet.createItem("minecraft:diamond_sword");
     *
     *    // This will change the first slot in player's ender chest to a diamond sword
     *    inventory.setStack(0, item);
     * }</pre>
     */
    IScriptInventory getEnderChest();

    /**
     * Executes a command as a player.
     *
     * <pre>{@code
     *    function main(c)
     *    {
     *        c.getSubject().executeCommand("/kill");
     *    }
     * }</pre>
     */
    void executeCommand(String command);

    /**
     * Sets the player's spawn point.
     *
     * <pre>{@code
     *   c.getSubject().setSpawnPoint(0, 0, 0);
     * }</pre>
     */
    void setSpawnPoint(double x, double y, double z);

    /**
     * Gets the player's spawn point.
     *
     * <pre>{@code
     *   var spawnPoint = c.getSubject().getSpawnPoint();
     *   c.send("Spawn point: " + spawnPoint.x + ", " + spawnPoint.y + ", " + spawnPoint.z);
     * }</pre>
     */
    ScriptVector getSpawnPoint();

    /**
     * Returns if the player is flying.
     *
     * <pre>{@code
     *    function main(c) {
     *        c.send("Is the player flying? " + c.getSubject().isFlying());
     *    }
     * }</pre>
     */
    boolean isFlying();

    /**
     * Returns if the walk speed of the player.
     *
     * <pre>{@code
     *    function main(c) {
     *        c.send("The walk speed of the player is: " + c.getSubject().getWalkSpeed());
     *    }
     * }</pre>
     */
    float getWalkSpeed();

    /**
     * Allows the player to fly in whatever game mode they're in.
     *
     * <pre>{@code
     * c.getSubject().setFlyingEnabled(true);
     * }</pre>
     *
     * @param enabled Whether the player can fly or not.
     */
    void setFlyingEnabled(boolean enabled);

    /**
     * Returns if the flight speed of the player.
     *
     * <pre>{@code
     *    function main(c)
     *    {
     *        c.send("The flight speed of the player is: " + c.getSubject().getFlySpeed());
     *    }
     * }</pre>
     */
    float getFlySpeed();

    /**
     * Set the walk speed of the player.
     *
     * <pre>{@code
     *    function main(c)
     *    {
     *        c.getSubject().setWalkSpeed(0.5);
     *    }
     * }</pre>
     */
    void setWalkSpeed(float speed);

    /**
     * Set the flight speed of the player.
     *
     * <pre>{@code
     *    function main(c)
     *    {
     *        c.getSubject().setFlySpeed(0.5);
     *    }
     * }</pre>
     */
    void setFlySpeed(float speed);

    /**
     * Reset the flight speed of the player.
     *
     * <pre>{@code
     *    function main(c)
     *    {
     *        c.getSubject().resetFlySpeed();
     *    }
     * }</pre>
     */
    void resetFlySpeed();

    /**
     * Reset the walking speed of the player.
     *
     * <pre>{@code
     *    function main(c)
     *    {
     *        c.getSubject().resetWalkSpeed();
     *    }
     * }</pre>
     */
    void resetWalkSpeed();

    /**
     * Get cooldown of a particular inventory index of the player.
     *
     * <pre>{@code
     * function main(c)
     * {
     *     var player = c.getSubject();
     *     var cooldown = player.getCooldown(40); // tip: 40 is the offhand slot
     *
     *     c.send(The held item cooldown " + ((1 - cooldown) * 100) + " percent expired.");
     * }
     * }</pre>
     */
    float getCooldown(int inventorySlot);

    /**
     * Get cooldown of a particular inventory index of the player.
     *
     * <pre>{@code
     *    function main(c)
     *    {
     *        var player = c.getSubject();
     *        var item = mappet.createItem("minecraft:diamond_sword");
     *        var cooldown = player.getCooldown(item);
     *
     *        c.send(The held item cooldown " + ((1 - cooldown) * 100) + " percent expired.");
     *    }
     * }</pre>
     */
    float getCooldown(IScriptItemStack item);

    /**
     * Set cooldown of a particular inventory index of the player.
     *
     * <pre>{@code
     *    function main(c)
     *    {
     *        var player = c.getSubject();
     *
     *        player.setCooldown(player.getHotbarIndex(), 100); // tip: 40 is the offhand slot
     *    }
     * }</pre>
     */
    void setCooldown(int inventorySlot, int ticks);

    /**
     * Set cooldown for given item.
     *
     * <pre>{@code
     *    function main(c)
     *    {
     *        var player = c.getSubject();
     *        var item = mappet.createItem("minecraft:diamond_sword");
     *
     *        player.setCooldown(item, 100);
     *    }
     * }</pre>
     */
    void setCooldown(IScriptItemStack item, int ticks);

    /**
     * Reset cooldown for given item.
     *
     * <pre>{@code
     *    function main(c)
     *    {
     *        var player = c.getSubject();
     *
     *        player.resetCooldown(player.getMainItemInventoryIndex()); // tip: 40 is the offhand slot
     *    }
     * }</pre>
     */
    void resetCooldown(int inventorySlot);

    /**
     * Reset cooldown of a particular inventory index of the player.
     *
     * <pre>{@code
     *    function main(c)
     *    {
     *        var player = c.getSubject();
     *        var item = mappet.createItem("minecraft:diamond_sword");
     *
     *        player.resetCooldown(item);
     *    }
     * }</pre>
     */
    void resetCooldown(IScriptItemStack item);

    /**
     * Get the inventory index of main item. Useful for e.g. main hand's cooldown methods.
     *
     * <pre>{@code
     *    function main(c)
     *    {
     *        var player = c.getSubject();
     *
     *        player.setCooldown(player.getHotbarIndex(), 100); //tip: 40 is the offhand slot
     *    }
     * }</pre>
     */
    int getHotbarIndex();

    /**
     * Set forcefully player's current hotbar inventory index. Acceptable values are <code>0</code> - <code>8</code>.
     */
    void setHotbarIndex(int slot);

    /**
     * Send a message to this player.
     *
     * <pre>{@code
     *    // Assuming that c.getSubject() is a player
     *    c.send("I love all my players equally.");
     *    c.getSubject().send("...but between you and me, you're my favorite player ;)");
     * }</pre>
     */
    void send(String message);

    /**
     * Send a message to this player using text component (like <code>/tellraw</code> command).
     *
     * <pre>{@code
     *    var message = mappet.createCompound();
     *
     *    message.setString("text", "This message displays an item...");
     *    message.setString("color", "gold");
     *    message.setNBT("hoverEvent",'{action:"show_item",value:"{id:\\"minecraft:diamond_hoe\\",Count:1b}"}');
     *
     *    c.getSubject().sendRaw(message);
     * }</pre>
     */
    void sendRaw(INBT message);

    /**
     * Get player's skin.
     *
     * <pre>{@code
     *    // Assuming that c.getSubject() is a player
     *    var player = c.getSubject();
     *    var morph = mappet.createMorph('{Name:"blockbuster.fred",Skin:"' + player.getSkin() + '",Pose:"dabbing"}');
     *
     *    player.setMorph(morph);
     * }</pre>
     *
     * @return Resource location in format of "minecraft:skins/..." (which can be used in morphs)
     */
    String getSkin();

    /**
     * Send title and subtitle durations (in ticks, <code>20</code> ticks = <code>1</code> second).
     * These must be sent before sending title or subtitle.
     *
     * <p><b>BEWARE</b>: these durations will stay the same until player logs out, so you may want
     * to change them before every time you send title and subtitle.</p>
     *
     * <p>Default values are: fadeIn = <code>10</code> ticks, idle = <code>70</code> ticks,
     * fadeOut = <code>20</code> ticks.</p>
     *
     * <pre>{@code
     *    var player = c.getSubject();
     *
     *    player.sendTitleDurations(5, 10, 5);
     *    player.sendTitle("Quick!");
     *    player.sendSubtitle("Get into cover!");
     * }</pre>
     *
     * @param fadeIn  How many ticks it will take for title and subtitle to appear.
     * @param idle    For how many ticks will title and subtitle stay after fading in.
     * @param fadeOut How many ticks it will take for title and subtitle to disappear after idling.
     */
    void sendTitleDurations(int fadeIn, int idle, int fadeOut);

    /**
     * Send the title to this player that will be displayed in the middle of the screen.
     *
     * <pre>{@code
     *    c.getSubject().sendTitle("Hello, world!");
     * }</pre>
     */
    void sendTitle(String title);

    /**
     * Send the subtitle to this player that will be displayed in the middle of the
     * screen. Title must be sent as well, using {@link #sendTitle(String)}, in order
     * for subtitle to appear.
     *
     * <pre>{@code
     *    c.getSubject().sendTitle("Hello,");
     *    c.getSubject().sendSubtitle("world!");
     * }</pre>
     */
    void sendSubtitle(String title);

    /**
     * Send a message to this player that will be displayed in action bar. The duration
     * of action bar line is <code>60</code> ticks (<code>3</code> seconds).
     */
    void sendActionBar(String title);

    /* XP methods */

    void setClipboard(String text);

    /**
     * Set experience level and amount of points for that level.
     *
     * <pre>{@code
     *    // For more information of how levels work (i.e. how many points per
     *    // level to level up) see this table:
     *    // https://minecraft.fandom.com/wiki/Experience#Leveling_up
     *
     *    // Set player's XP level to 17 and half of the bar (level 17 has
     *    // 42 points in total to level up)
     *    c.getSubject().setXp(17, 21);
     * }</pre>
     *
     * @param level  Experience level.
     * @param points Amount of experience points in that particular level.
     */
    void setXp(int level, int points);

    /**
     * Add experience points to this player. Inputting more points than player's
     * current level can contain will result into leveling up one or more times.
     *
     * <pre>{@code
     *    // For more information of how levels work (i.e. how many points per
     *    // level to level up) see this table:
     *    // https://minecraft.fandom.com/wiki/Experience#Leveling_up
     *
     *    // Add 1000 experience points
     *    c.getSubject().addXp(1000);
     * }</pre>
     *
     * @param points Amount of experience points to add to player.
     */
    void addXp(int points);

    /**
     * Get player's current experience level.
     *
     * <pre>{@code
     *    // For more information of how levels work (i.e. how many points per
     *    // level to level up) see this table:
     *    // https://minecraft.fandom.com/wiki/Experience#Leveling_up
     *
     *    var s = c.getSubject();
     *
     *    if (s.getXpLevel() < 50)
     *    {
     *        var section = "§";
     *
     *        // Teleport the player out of the area
     *        s.setPosition(10, 4, -15);
     *        s.send("Come back when you're level" + section + "7 50" + section + "r!");
     *    }
     * }</pre>
     */
    int getXpLevel();

    /**
     * Get player's experience points in their current level.
     */
    int getXpPoints();

    /* Hunger */

    void setHunger(int value);

    int getHunger();

    void setSaturation(float value);

    float getSaturation();

    /* Sounds */

    /**
     * Play a sound event only to this player.
     *
     * <p>For all possible sound event IDs, please refer to either <code>/playsound</code>
     * command, or script editor's sound picker.</p>
     *
     * <pre>{@code
     *    var player = c.getSubject();
     *    var pos = player.getPosition();
     *
     *    player.playSound("minecraft:entity.pig.ambient", pos.x, pos.y, pos.z);
     * }</pre>
     */
    default void playSound(String event, double x, double y, double z) {
        this.playSound(event, x, y, z, 1F, 1F);
    }

    /**
     * Play a sound event only to this player at specific sound channel.
     *
     * <pre>{@code
     *    var player = c.getSubject();
     *    var pos = player.getPosition();
     *
     *    player.playSound("minecraft:entity.pig.ambient", "voice", pos.x, pos.y, pos.z);
     * }</pre>
     */
    void playSound(String event, String soundCategory, double x, double y, double z);

    /**
     * Play a sound event only to this player with volume and pitch at specific channel.
     *
     * <pre>{@code
     *    var player = c.getSubject();
     *    var pos = player.getPosition();
     *
     *    player.playSound("minecraft:entity.pig.ambient", "voice", pos.x, pos.y, pos.z, 1.0, 0.8);
     * }</pre>
     */
    void playSound(String event, String soundCategory, double x, double y, double z, float volume, float pitch);

    ScriptScoreboard getScoreboard();

    void join(ScriptTeam team);
    void join(String name);

    void leave();

    void setScore(ScriptScoreObjective objective, int value);

    void setScore(String name, int value);

    int addScore(ScriptScoreObjective objective, int value);

    int addScore(String name, int value);

    int getScore(ScriptScoreObjective objective);

    int getScore(String name);

    void resetScore(ScriptScoreObjective objective);

    void resetScore(String name);

    /**
     * Play a sound event only to this player with volume and pitch.
     *
     * <pre>{@code
     *    var player = c.getSubject();
     *    var pos = player.getPosition();
     *
     *    player.playSound("minecraft:entity.pig.ambient", pos.x, pos.y, pos.z, 1.0, 0.8);
     * }</pre>
     */
    void playSound(String event, double x, double y, double z, float volume, float pitch);

    /**
     * Stop all playing sound events for this player.
     *
     * <pre>{@code
     *    c.getWorld().stopAllSounds();
     * }</pre>
     */
    default void stopAllSounds() {
        this.stopSound("", "");
    }

    /**
     * Stop specific sound event for this player.
     *
     * <pre>{@code
     *    c.getWorld().stopSound("minecraft:entity.pig.ambient");
     * }</pre>
     */
    default void stopSound(String event) {
        this.stopSound(event, "");
    }

    /**
     * <p>Stop specific sound event in given sound category for this player.</p>
     *
     * <p>For list of sound categories, type into chat
     * <code>/playsound minecraft:entity.pig.ambient</code>, press space, and press
     * Tab key. The list of sounds categories will be displayed.</p>
     *
     * <pre>{@code
     *    c.getWorld().stopSound("minecraft:entity.pig.ambient", "master");
     * }</pre>
     */
    void stopSound(String event, String category);

    /**
     * Play a sound event to this player stationary.
     *
     * <p>The difference between this method and {@link #playSound(String, double, double, double, float, float)}
     * is that if player will get teleported, the sound will continue playing.</p>
     *
     * <pre>{@code
     *    var player = c.getSubject();
     *
     *    player.playStaticSound("minecraft:block.portal.ambient", 1.0, 0.8);
     *
     *    c.scheduleScript(20, function (c)
     *    {
     *        c.getSubject().setPosition(-15, 4, 561);
     *    });
     * }</pre>
     */
    void playStaticSound(String event, float volume, float pitch);

    /**
     * Play a sound event to this player stationary at specific channel.
     *
     * <p>The difference between this method and {@link #playSound(String, double, double, double, float, float)}
     * is that if player will get teleported, the sound will continue playing.</p>
     *
     * <pre>{@code
     *    var player = c.getSubject();
     *
     *    player.playStaticSound("minecraft:block.portal.ambient", "voice", 1.0, 0.8);
     *
     *    c.scheduleScript(20, function (c)
     *    {
     *        c.getSubject().setPosition(-15, 4, 561);
     *    });
     * }</pre>
     */
    void playStaticSound(String event, String soundCategory, float volume, float pitch);

    /* Mappet stuff */

    /**
     * Get entity's quests (if it has some, only players have quests).
     *
     * <pre>{@code
     *    // Assuming that c.getSubject() is a player
     *    var quests = c.getSubject().getQuests();
     *
     *    if (!quests.has("important_quest"))
     *    {
     *        c.getSubject().send("I think you should complete the main quest chain before attempting side quests...");
     *    }
     * }</pre>
     */
    IMappetQuests getQuests();

    /**
     * Open UI for this player.
     *
     * <pre>{@code
     *    function main(c)
     *    {
     *        var ui = mappet.createUI().background();
     *        var button = ui.button("Push me").id("button");
     *
     *        // Place a button in the middle of the screen
     *        button.rxy(0.5, 0.5).wh(80, 20).anchor(0.5);
     *        c.getSubject().openUI(ui);
     *    }
     * }</pre>
     */
    default void openUI(IMappetUIBuilder builder) {
        this.openUI(builder, false);
    }

    /**
     * Open UI for this player with default data populated.
     *
     * <p>By default, default data population is disabled, meaning that
     * once the UI was opened, UI context's data will be empty. By enabling
     * default data population, UI context's data gets filled with all
     * component's default data.</p>
     *
     * <p>This is useful when you need to data to be present in the handler
     * at start, so you wouldn't need to do extra checks.</p>
     *
     * <pre>{@code
     *    function main(c)
     *    {
     *        var ui = mappet.createUI(c, "handler").background();
     *        var button = ui.button("Push me").id("button");
     *        var name = ui.textbox("John").id("name");
     *        var lastname = ui.textbox("Smith").id("lastname");
     *
     *        // Place a button in the middle of the screen
     *        button.rxy(0.5, 0.5).wh(80, 20).anchor(0.5);
     *        name.rx(0.5).ry(0.5, 25).wh(80, 20).anchor(0.5);
     *        lastname.rx(0.5).ry(0.5, 50).wh(80, 20).anchor(0.5);
     *
     *        // Open the UI with default data populated
     *        c.getSubject().openUI(ui, true);
     *    }
     *
     *    function handler(c)
     *    {
     *        var uiContext = c.getSubject().getUIContext();
     *        var data = uiContext.getData();
     *
     *        // If false was passed into openUI as second argument
     *        // Then name or last name wouldn't be immediately populated
     *        // as John Smith
     *        c.send("Your name is: " + data.getString("name") + " " + data.getString("lastname"));
     *    }
     * }</pre>
     */
    boolean openUI(IMappetUIBuilder builder, boolean defaultData);

    /**
     * Close the user interface.
     *
     * <p>You can use this method to close any GUI that player has opened, inventory,
     * chests, command block menu, Mappet dashboard, etc. However, Mappet won't close
     * the in-game pause menu (to avoid potential griefing).</p>
     */
    void closeUI();

    /**
     * Get the UI context of currently opened user UI. See {@link IMappetUIContext}
     * for code examples.
     */
    IMappetUIContext getUIContext();

    /**
     * Returns the faction of the npc as a string
     *
     * <pre>{@code
     * for each (var faction in c.getSubject().getFactions()){
     *    c.send(faction)
     * }
     * }</pre>
     */
    Set<String> getFactions();

    /* HUD scenes API */

    /**
     * Setup (initiate) an HUD scene for this player.
     *
     * @param id HUD scene's ID/filename.
     */
    boolean setupHUD(String id);

    /**
     * Change a morph in a HUD scene at given index with given morph.
     *
     * @param id    HUD scene's ID/filename.
     * @param index Index of the morph in the scene that should be changed (0 is the first, and so on).
     */
    void changeHUDMorph(String id, int index, AbstractMorph morph);

    /**
     * Change a morph in a HUD scene at given index with a morph described by given NBT data.
     *
     * @param id    HUD scene's ID/filename.
     * @param index Index of the morph in the scene that should be changed (0 is the first, and so on).
     * @param morph NBT data of the morph.
     */
    void changeHUDMorph(String id, int index, INBTCompound morph);

    /**
     * Close all HUD scenes.
     */
    default void closeAllHUDs() {
        this.closeHUD(null);
    }

    /**
     * Close specific HUD scene for this player.
     *
     * @param id HUD scene's ID/filename.
     */
    void closeHUD(String id);

    void closeAllHUDs(List<String> ignores);

    /**
     * Get all HUD scenes (including global HUDs) that are currently displayed for this player.
     *
     * <pre>{@code
     *   var player = c.getSubject();
     *   var huds = player.getDisplayedHUDs();
     *   print(huds);
     * }</pre>
     */
    INBTCompound getDisplayedHUDs();

    /**
     * Get all global HUD scenes that are currently saved on player and displayed for him and other players.
     *
     * <pre>{@code
     *   var player = c.getSubject();
     *   var huds = player.getGlobalDisplayedHUDs();
     *   print(huds);
     * }</pre>
     */
    INBTCompound getGlobalDisplayedHUDs();


    /**
     * Plays an Aperture scene for this player.
     *
     * <pre>{@code
     * c.getSubject().playScene("scene_name");
     * }</pre>
     *
     * @param sceneName Scene's ID.
     */
    void playScene(String sceneName);


    /**
     * Plays Aperture scenes for this player.
     *
     * <pre>{@code
     * c.getSubject().stopScene();
     * }</pre>
     */
    void stopScene();
}
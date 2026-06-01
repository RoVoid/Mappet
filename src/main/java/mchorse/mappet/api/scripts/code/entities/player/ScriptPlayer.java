package mchorse.mappet.api.scripts.code.entities.player;

import io.netty.buffer.Unpooled;
import mchorse.aperture.network.common.PacketCameraState;
import mchorse.mappet.api.scripts.code.ScriptResourcePack;
import mchorse.mappet.api.scripts.code.entities.ScriptEntity;
import mchorse.mappet.api.scripts.code.items.ScriptInventory;
import mchorse.mappet.api.scripts.code.items.ScriptItemStack;
import mchorse.mappet.api.scripts.code.mappet.MappetQuests;
import mchorse.mappet.api.scripts.code.math.ScriptVector;
import mchorse.mappet.api.scripts.code.nbt.ScriptNBTCompound;
import mchorse.mappet.api.scripts.code.score.ScriptScoreObjective;
import mchorse.mappet.api.scripts.code.score.ScriptScoreboard;
import mchorse.mappet.api.scripts.code.score.ScriptTeam;
import mchorse.mappet.api.scripts.code.ui.MappetUIBuilder;
import mchorse.mappet.api.scripts.code.ui.MappetUIContext;
import mchorse.mappet.api.ui.UI;
import mchorse.mappet.api.ui.UIContext;
import mchorse.mappet.api.utils.SkinUtils;
import mchorse.mappet.capabilities.character.Character;
import mchorse.mappet.capabilities.character.ICharacter;
import mchorse.mappet.client.gui.utils.SafeWebLinkOpener;
import mchorse.mappet.entities.utils.WalkSpeedManager;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.packets.PacketBlackAndWhiteShader;
import mchorse.mappet.network.packets.PacketPack;
import mchorse.mappet.network.packets.PacketPlayerPerspective;
import mchorse.mappet.network.packets.scripts.PacketClipboard;
import mchorse.mappet.network.packets.scripts.PacketEntityRotations;
import mchorse.mappet.network.packets.scripts.PacketSound;
import mchorse.mappet.network.packets.ui.PacketCloseUI;
import mchorse.mappet.network.packets.ui.PacketUI;
import mchorse.mappet.utils.PlayerUtils;
import mchorse.mappet.utils.WorldUtils;
import mchorse.metamorph.api.MorphAPI;
import mchorse.metamorph.api.MorphUtils;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import mchorse.metamorph.capabilities.morphing.Morphing;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.*;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScriptPlayer extends ScriptEntity<EntityPlayerMP> {
    private MappetQuests quests;

    private ScriptInventory inventory;

    private ScriptInventory enderChest;

    public ScriptPlayer(EntityPlayerMP entity) {
        super(entity);
    }

    @Deprecated
    public EntityPlayerMP getMinecraftPlayer() {
        return entity;
    }

    public EntityPlayerMP asMinecraft() {
        return entity;
    }

    public void setMotion(double x, double y, double z) {
        super.setMotion(x, y, z);

        entity.connection.sendPacket(new SPacketEntityVelocity(entity.getEntityId(), x, y, z));
    }

    public void setRotations(float pitch, float yaw, float yawHead) {
        super.setRotations(pitch, yaw, yawHead);

        Dispatcher.sendTo(new PacketEntityRotations(entity.getEntityId(), yaw, yawHead, pitch), entity);
    }

    public void swingArm(int arm) {
        super.swingArm(arm);
        entity.connection.sendPacket(new SPacketAnimation(entity, arm == 1 ? 3 : 0));
    }

    /* Player's methods */
    public boolean isOperator() {
        return PlayerUtils.isOperator(entity);
    }

    public int getGameMode() {
        return entity.interactionManager.getGameType().getID();
    }

    public void setGameMode(int gameMode) {
        GameType type = GameType.getByID(gameMode);
        if (type.getID() >= 0) entity.setGameType(type);
    }

    public ScriptInventory getInventory() {
        if (inventory == null) inventory = new ScriptInventory(entity.inventory);
        return inventory;
    }

    public ScriptInventory getEnderChest() {
        if (enderChest == null) enderChest = new ScriptInventory(entity.getInventoryEnderChest());
        return enderChest;
    }

    public void executeCommand(String command) {
        if (entity.world.getMinecraftServer() == null) return;
        entity.world.getMinecraftServer().getCommandManager().executeCommand(entity, command);
    }

    public void setSpawnPoint(double x, double y, double z) {
        entity.setSpawnPoint(new BlockPos(x, y, z), true);
    }

    public ScriptVector getSpawnPoint() {
        BlockPos pos = entity.getBedLocation(entity.dimension);

        return new ScriptVector(pos.getX(), pos.getY(), pos.getZ());
    }

    public boolean isFlying() {
        return entity.capabilities.isFlying;
    }

    public void setFlyingEnabled(boolean enabled) {
        entity.capabilities.allowFlying = enabled;
        entity.sendPlayerAbilities();
    }

    public float getFlySpeed() {
        return entity.capabilities.getFlySpeed();
    }

    public void setFlySpeed(float speed) {
        entity.capabilities.setFlySpeed(speed);
        entity.sendPlayerAbilities();
    }

    public void resetFlySpeed() {
        setFlySpeed(0.05F);
    }

    public float getWalkSpeed() {
        return WalkSpeedManager.getWalkSpeed(entity);
    }

    public void setWalkSpeed(float speed) {
        WalkSpeedManager.setWalkSpeed(entity, speed);
    }

    public void resetWalkSpeed() {
        WalkSpeedManager.resetWalkSpeed(entity);
    }

    public float getCooldown(ScriptItemStack item) {
        return entity.getCooldownTracker().getCooldown(item.asMinecraft().getItem(), 0);
    }

    public float getCooldown(int inventorySlot) {
        return getCooldown(getInventory().getStack(inventorySlot));
    }

    public void setCooldown(ScriptItemStack item, int ticks) {
        entity.getCooldownTracker().setCooldown(item.asMinecraft().getItem(), ticks);
    }

    public void setCooldown(int inventorySlot, int ticks) {
        setCooldown(getInventory().getStack(inventorySlot), ticks);
    }

    public void resetCooldown(ScriptItemStack item) {
        entity.getCooldownTracker().removeCooldown(item.asMinecraft().getItem());
    }

    public void resetCooldown(int inventorySlot) {
        resetCooldown(getInventory().getStack(inventorySlot));
    }

    public int getHotbarIndex() {
        return entity.inventory.currentItem;
    }

    public void setHotbarIndex(int slot) {
        if (slot < 0 || slot >= 9) return;

        entity.inventory.currentItem = slot;

        entity.connection.sendPacket(new SPacketHeldItemChange(slot));
    }

    public void send(String message) {
        entity.sendMessage(new TextComponentString(message == null ? "null" : message));
    }

    public void sendRaw(Object message) {
        ITextComponent component = ITextComponent.Serializer.fromJsonLenient(message.toString());

        if (component != null) entity.sendMessage(component);
    }

    public String getSkin() {
        return SkinUtils.getSkin(getName());
    }

    public String getSkin(String source) {
        return SkinUtils.getSkin(getName(), source);
    }

    public Object getSkinObject() {
        return SkinUtils.getSkinObject(getName());
    }

    public Object getSkinObject(String source) {
        return SkinUtils.getSkinObject(getName(), source);
    }

    public void sendTitleDurations(int fadeIn, int idle, int fadeOut) {
        SPacketTitle packet = new SPacketTitle(fadeIn, idle, fadeOut);

        asMinecraft().connection.sendPacket(packet);
    }

    public void sendTitle(String title) {
        SPacketTitle packet = new SPacketTitle(SPacketTitle.Type.TITLE, new TextComponentString(title));

        asMinecraft().connection.sendPacket(packet);
    }

    public void sendSubtitle(String title) {
        SPacketTitle packet = new SPacketTitle(SPacketTitle.Type.SUBTITLE, new TextComponentString(title));

        asMinecraft().connection.sendPacket(packet);
    }

    public void sendActionBar(String title) {
        SPacketTitle packet = new SPacketTitle(SPacketTitle.Type.ACTIONBAR, new TextComponentString(title));

        asMinecraft().connection.sendPacket(packet);
    }

    public void setClipboard(String text) {
        Dispatcher.sendTo(new PacketClipboard(text), asMinecraft());
    }

    public void openLink(String url) {
        SafeWebLinkOpener.requestToOpenWebLink(url, entity);
    }

    /* XP methods */

    public void setXp(int level, int points) {
        entity.addExperienceLevel(-getXpLevel() - 1);
        entity.addExperienceLevel(level);
        entity.addExperience(points);
    }

    public void addXp(int points) {
        entity.addExperience(points);
    }

    public int getXpLevel() {
        return entity.experienceLevel;
    }

    public int getXpPoints() {
        return (int) (entity.experience * entity.xpBarCap());
    }

    public void setHunger(int value) {
        entity.getFoodStats().setFoodLevel(value);
    }

    public int getHunger() {
        return entity.getFoodStats().getFoodLevel();
    }

    public void setSaturation(float value) {
        entity.getFoodStats().setFoodSaturationLevel(value);
    }

    public float getSaturation() {
        return entity.getFoodStats().getSaturationLevel();
    }

    public ScriptScoreboard getScoreboard() {
        return new ScriptScoreboard(entity.getWorldScoreboard());
    }

    public void join(ScriptTeam team) {
        if (team != null) team.join(this);
    }

    public void join(String name) {
        getScoreboard().getTeam(name).join(this);
    }

    public void leave() {
        ScorePlayerTeam team = (ScorePlayerTeam) entity.getTeam();
        entity.getWorldScoreboard().removePlayerFromTeam(asMinecraft().getName(), team);
    }

    public void setScore(ScriptScoreObjective objective, int value) {
        objective.set(this, value);
    }

    public void setScore(String name, int value) {
        ScriptScoreObjective objective = getScoreboard().getObjective(name);
        if (objective != null) objective.set(this, value);
    }

    public int addScore(ScriptScoreObjective objective, int value) {
        return objective.add(this, value);
    }

    public int addScore(String name, int value) {
        ScriptScoreObjective objective = getScoreboard().getObjective(name);
        return objective != null ? objective.add(this, value) : 0;
    }

    public int getScore(ScriptScoreObjective objective) {
        return objective.get(this);
    }

    public int getScore(String name) {
        ScriptScoreObjective objective = getScoreboard().getObjective(name);
        return objective != null ? objective.get(this) : 0;
    }

    public void resetScore(ScriptScoreObjective objective) {
        objective.reset(this);
    }

    public void resetScore(String name) {
        ScriptScoreObjective objective = getScoreboard().getObjective(name);
        if (objective != null) objective.reset(this);
    }

    public void updateServerPack(ScriptResourcePack resourcePack) {
        if (resourcePack.getPack() == null) return;
        Dispatcher.sendTo(new PacketPack(resourcePack.getPack()), entity);
    }

    public void clearServerPack() {
        Dispatcher.sendTo(new PacketPack(null), entity);
    }

    public void enableBlackAndWhiteShader(boolean enable) {
        Dispatcher.sendTo(new PacketBlackAndWhiteShader(enable), entity);
    }

    public void setPerspective(int perspective) {
        Dispatcher.sendTo(new PacketPlayerPerspective(perspective), entity);
    }

    public void lockPerspective(int perspective) {
        Dispatcher.sendTo(new PacketPlayerPerspective(perspective, true), entity);
    }

    public void unlockPerspective() {
        setPerspective(-1);
    }

    public ArrayList<String> getModsList() {
        NetworkDispatcher dispatcher = NetworkDispatcher.get(asMinecraft().connection.netManager);
        ArrayList<String> list = new ArrayList<>();
        if (dispatcher != null) dispatcher.getModList().forEach((modId, version) -> list.add(modId + ":" + version));
        return list;
    }

    public int getPing() {
        return entity.ping;
    }

    public ClientSettings getSettings() {
        return new ClientSettings(asMinecraft());
    }

    /* Sounds */

    public void playSound(String event, double x, double y, double z, float volume, float pitch) {
        WorldUtils.playSound(entity, event, x, y, z, volume, pitch);
    }

    public void playSound(String event, String soundCategory, double x, double y, double z, float volume, float pitch) {
        WorldUtils.playSound(entity, event, soundCategory, x, y, z, volume, pitch);
    }

    public void playSound(String event, String soundCategory, double x, double y, double z) {
        WorldUtils.playSound(entity, event, soundCategory, x, y, z, 1F, 1F);
    }

    public void stopSound(String event, String category) {
        PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());

        packetbuffer.writeString(category);
        packetbuffer.writeString(event);

        entity.connection.sendPacket(new SPacketCustomPayload("MC|StopSound", packetbuffer));
    }

    public void playStaticSound(String event, float volume, float pitch) {
        playStaticSound(event, "master", volume, pitch);
    }

    public void playStaticSound(String event, String soundCategory, float volume, float pitch) {
        Dispatcher.sendTo(new PacketSound(event, soundCategory, volume, pitch), entity);
    }

    /* Mappet stuff */

    public MappetQuests getQuests() {
        if (quests == null) {
            Character character = Character.get(entity);
            if (character != null) quests = new MappetQuests(character.getQuests(), entity);
        }

        return quests;
    }

    public AbstractMorph getMorph() {
        IMorphing cap = Morphing.get(entity);

        if (cap != null) return cap.getCurrentMorph();

        return super.getMorph();
    }

    public boolean setMorph(AbstractMorph morph) {
        if (morph == null) MorphAPI.demorph(entity);
        else MorphAPI.morph(entity, morph, true);

        return true;
    }

    public boolean openUI(MappetUIBuilder in, boolean defaultData) {
        if (in == null) return false;

        ICharacter character = Character.get(entity);
        if (character == null) return false;
        boolean noContext = character.getUIContext() == null;

        if (!noContext) character.getUIContext().close();

        UI ui = in.getUI();
        UIContext context = new UIContext(ui, entity, in.getScript(), in.getFunction());

        character.setUIContext(context);
        Dispatcher.sendTo(new PacketUI(ui), asMinecraft());

        if (defaultData) context.populateDefaultData();

        context.clearChanges();

        return !noContext;
    }

    public void closeUI() {
        Dispatcher.sendTo(new PacketCloseUI(), asMinecraft());
    }

    public MappetUIContext getUIContext() {
        ICharacter character = Character.get(entity);
        if (character == null) return null;
        UIContext context = character.getUIContext();

        return context == null ? null : new MappetUIContext(context);
    }

    public ScriptCamera getCamera() {
        return new ScriptCamera(asMinecraft());
    }

    public Set<String> getFactions() {
        Set<String> factions = new HashSet<>();

        ICharacter character = Character.get(entity);
        if (character != null) factions = character.getFactionStates().keys();

        return factions;
    }

    public String getLanguage() {
        return entity.language;
    }


    /* HUD scenes API */

    public boolean setupHUD(String id) {
        Character character = Character.get(entity);
        if (character == null) return false;
        return character.setupHUD(id, true);
    }

    public void changeHUDMorph(String id, int index, AbstractMorph morph) {
        if (morph == null) return;

        Character character = Character.get(entity);
        if (character == null) return;
        character.changeHUDMorph(id, index, MorphUtils.toNBT(morph));
    }

    public void changeHUDMorph(String id, int index, ScriptNBTCompound morph) {
        if (morph == null) return;

        Character character = Character.get(entity);
        if (character == null) return;
        character.changeHUDMorph(id, index, morph.asMinecraft());
    }

    public void closeHUD(String id) {
        Character character = Character.get(entity);
        if (character == null) return;
        character.closeHUD(id);
    }

    public void closeAllHUDs() {
        Character character = Character.get(entity);
        if (character == null) return;
        character.closeAllHUDs();
    }

    public void closeAllHUDs(List<String> ignores) {
        Character character = Character.get(entity);
        if (character == null) return;
        character.closeAllHUDs(ignores);
    }

    public ScriptNBTCompound getDisplayedHUDs() {
        Character character = Character.get(entity);
        NBTTagCompound tag = character != null ? character.getDisplayedHUDsTag() : null;
        return new ScriptNBTCompound(tag);
    }

    public ScriptNBTCompound getGlobalDisplayedHUDs() {
        Character character = Character.get(entity);
        NBTTagCompound tag = character != null ? character.getGlobalDisplayedHUDsTag() : null;
        return new ScriptNBTCompound(tag);
    }

    /* Aperture API */

    public void playScene(String sceneName) {
        if (Loader.isModLoaded("aperture")) playApertureScene(sceneName, true);
    }

    public void stopScene() {
        if (Loader.isModLoaded("aperture")) playApertureScene("", false);
    }

    @Optional.Method(modid = "aperture")
    private void playApertureScene(String sceneName, boolean toPlay) {
        mchorse.aperture.network.Dispatcher.sendTo(new PacketCameraState(sceneName, toPlay), entity);
    }
}
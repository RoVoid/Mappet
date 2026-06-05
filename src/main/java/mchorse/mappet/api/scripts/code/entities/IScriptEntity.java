package mchorse.mappet.api.scripts.code.entities;

import mchorse.mappet.api.scripts.code.ScriptRayTrace;
import mchorse.mappet.api.scripts.code.entities.player.ScriptPlayer;
import mchorse.mappet.api.scripts.code.items.ScriptItemStack;
import mchorse.mappet.api.scripts.code.math.ScriptBox;
import mchorse.mappet.api.scripts.code.math.ScriptVector;
import mchorse.mappet.api.scripts.code.nbt.ScriptNBTCompound;
import mchorse.mappet.api.scripts.code.world.ScriptWorld;
import mchorse.mappet.api.states.ScriptStates;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.entity.Entity;
import net.minecraft.potion.Potion;

import java.util.List;

public interface IScriptEntity {

    /* Minecraft */
    @Deprecated
    Entity getMinecraftEntity();

    Entity asMinecraft();

    ScriptWorld getWorld();

    /* Position & motion */
    ScriptVector getPosition();

    void setPosition(double x, double y, double z);

    int getDimension();

    void setDimension(int dimension);

    ScriptVector getMotion();

    void setMotion(double x, double y, double z);

    void addMotion(double x, double y, double z);

    /* Rotations */
    ScriptVector getRotations();

    void setRotations(float pitch, float yaw, float yawHead);

    float getPitch();

    float getYaw();

    float getYawHead();

    ScriptVector getLook();

    /* Size & health */
    float getEyeHeight();

    float getWidth();

    float getHeight();

    float getHp();

    void setHp(float hp);

    float getMaxHp();

    void setMaxHp(float hp);

    /* State checks */
    boolean isInWater();

    boolean isInLava();

    boolean isBurning();

    void setBurning(int seconds);

    boolean isSneaking();

    boolean isSprinting();

    boolean isOnGround();

    /* Ray tracing */
    ScriptRayTrace rayTrace(double maxDistance);

    ScriptRayTrace rayTraceBlock(double maxDistance);

    /* Items */
    ScriptItemStack getMainItem();

    void setMainItem(ScriptItemStack stack);

    ScriptItemStack getOffItem();

    void setOffItem(ScriptItemStack stack);

    void giveItem(ScriptItemStack stack);

    void giveItem(ScriptItemStack stack, boolean playSound, boolean dropIfInventoryFull);

    int removeItem(ScriptItemStack stack);

    int removeItem(ScriptItemStack stack, int count);

    int findItem(ScriptItemStack stack);

    int findItem(ScriptItemStack stack, int startIndex);

    ScriptItemStack getHelmet();

    ScriptItemStack getChestplate();

    ScriptItemStack getLeggings();

    ScriptItemStack getBoots();

    void setHelmet(ScriptItemStack itemStack);

    void setChestplate(ScriptItemStack itemStack);

    void setLeggings(ScriptItemStack itemStack);

    void setBoots(ScriptItemStack itemStack);

    /* Animation */
    void playAnimation(String animation);

    /* Taming */
    boolean isTamed();

    void setOwner(ScriptPlayer player);

    ScriptPlayer getOwner();

    /* Meta */
    void setSpeed(float speed);

    IScriptEntity getTarget();

    void setTarget(IScriptEntity entity);

    boolean isAIEnabled();

    void setAIEnabled(boolean enabled);

    String getUniqueId();

    @Deprecated
    String getEntityId();

    String getId();

    int getTicks();

    int getCombinedLight();

    String getName();

    void setName(String name);

    void setInvisible(boolean invisible);

    /* NBT */
    ScriptNBTCompound getFullData();

    void setFullData(ScriptNBTCompound data);

    ScriptNBTCompound getEntityData();

    /* Type checks */
    boolean isPlayer();

    boolean isNPC();

    boolean isItem();

    boolean isLivingBase();

    boolean isSame(IScriptEntity other);

    boolean isEntityInRadius(IScriptEntity target, double radius);

    boolean isInArea(double x1, double y1, double z1, double x2, double y2, double z2);

    /* Damage */
    void damage(float health);

    void damageAs(IScriptEntity attacker, float damage);

    void damageAs(IScriptEntity attacker, float damage, boolean ignore);

    void damageWithItemsAs(ScriptPlayer player);

    /* Riding */
    void mount(IScriptEntity entity);

    void dismount();

    IScriptEntity getMount();

    /* Bounding box */
    ScriptBox getBoundingBox();

    /* Dropping items */
    ScriptEntityItem dropItem(int amount);

    ScriptEntityItem dropItem();

    ScriptEntityItem dropItem(ScriptItemStack scriptItemStack);

    /* Fall */
    float getFallDistance();

    void setFallDistance(float distance);

    /* Lifecycle */
    void remove();

    void kill();

    void swingArm(int arm);

    /* Leash */
    List<IScriptEntity> getLeashedEntities();

    boolean setLeashHolder(IScriptEntity leashHolder);

    IScriptEntity getLeashHolder();

    boolean clearLeashHolder(boolean dropLead);

    /* Attribute modifiers */
    void setModifier(String modifierName, double value);

    double getModifier(String modifierName);

    void removeModifier(String modifierName);

    void removeAllModifiers();

    /* Potions */
    void applyPotion(Potion potion, int duration, int amplifier, boolean particles);

    boolean hasPotion(Potion potion);

    boolean removePotion(Potion potion);

    void clearPotions();

    /* Mappet states */
    ScriptStates getStates();

    /* Morphs */
    AbstractMorph getMorph();

    boolean setMorph(AbstractMorph morph);

    void displayMorph(AbstractMorph morph, int expiration, double x, double y, double z, float yaw, float pitch, boolean rotate, ScriptPlayer player);

    /* Blockbuster */
    IScriptEntity shootBBGunProjectile(String gunPropsNBT);

    /* Commands & scripts */
    void executeCommand(String command);

    void executeScript(String scriptName);

    void executeScript(String scriptName, String function);

    void executeScript(String scriptName, String function, Object... args);

    /* Position/rotation lock */
    void lockPosition(double x, double y, double z);

    void unlockPosition();

    boolean isPositionLocked();

    void lockRotation(float yaw, float pitch, float yawHead);

    void unlockRotation();

    boolean isRotationLocked();

    /* Movement */
    void moveTo(String interpolation, int durationTicks, double x, double y, double z, boolean disableAI);

    /* AI */
    void observe(IScriptEntity target);

    IScriptEntity getObservedEntity();

    void addEntityPatrol(double x, double y, double z, double speed, boolean shouldCirculate, String executeCommandOnArrival);

    void clearEntityPatrols();

    void setRotationsAI(float yaw, float pitch, float yawHead);

    void clearRotationsAI();

    void executeRepeatingCommand(String command, int frequency);

    void clearAllRepeatingCommands();

    void removeRepeatingCommand(String command);
}
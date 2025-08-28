package mchorse.mappet.api.scripts.code.entities;

import mchorse.blockbuster.common.GunProps;
import mchorse.blockbuster.common.entity.EntityActor;
import mchorse.blockbuster.common.entity.EntityGunProjectile;
import mchorse.blockbuster.network.common.PacketModifyActor;
import mchorse.mappet.CommonProxy;
import mchorse.mappet.Mappet;
import mchorse.mappet.api.scripts.code.ScriptRayTrace;
import mchorse.mappet.api.scripts.code.math.ScriptBox;
import mchorse.mappet.api.scripts.code.math.ScriptVector;
import mchorse.mappet.api.scripts.code.entities.ai.EntitiesAIPatrol;
import mchorse.mappet.api.scripts.code.entities.ai.EntityAILookAtTarget;
import mchorse.mappet.api.scripts.code.entities.ai.repeatingCommand.EntityAIRepeatingCommand;
import mchorse.mappet.api.scripts.code.entities.ai.repeatingCommand.RepeatingCommandDataStorage;
import mchorse.mappet.api.scripts.code.entities.ai.rotations.EntityAIRotations;
import mchorse.mappet.api.scripts.code.entities.ai.rotations.RotationDataStorage;
import mchorse.mappet.api.scripts.code.items.ScriptItemStack;
import mchorse.mappet.api.scripts.code.nbt.ScriptNBTCompound;
import mchorse.mappet.api.scripts.code.world.ScriptWorld;
import mchorse.mappet.api.scripts.user.IScriptRayTrace;
import mchorse.mappet.api.scripts.user.entities.IScriptEntity;
import mchorse.mappet.api.scripts.user.entities.IScriptPlayer;
import mchorse.mappet.api.scripts.user.items.IScriptItemStack;
import mchorse.mappet.api.scripts.user.mappet.IMappetStates;
import mchorse.mappet.api.scripts.user.nbt.INBTCompound;
import mchorse.mappet.api.scripts.user.world.IScriptWorld;
import mchorse.mappet.api.states.States;
import mchorse.mappet.api.utils.DataContext;
import mchorse.mappet.client.morphs.WorldMorph;
import mchorse.mappet.entities.EntityNpc;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.PacketPlayAnimation;
import mchorse.mappet.network.common.scripts.PacketEntityRotations;
import mchorse.mappet.network.common.scripts.PacketWorldMorph;
import mchorse.mappet.utils.EntityUtils;
import mchorse.mappet.utils.RunnableExecutionFork;
import mchorse.mclib.utils.Interpolation;
import mchorse.mclib.utils.RayTracing;
import mchorse.metamorph.api.models.IMorphProvider;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nonnull;
import javax.script.ScriptException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScriptEntity<T extends Entity> implements IScriptEntity {
    protected T entity;

    protected IMappetStates states;

//    protected ScriptVector moveTarget = ScriptVector.EMPTY;
//    protected int movingTick = 0;

    public static IScriptEntity create(Entity entity) {
        if (entity instanceof EntityPlayerMP) return new ScriptPlayer((EntityPlayerMP) entity);
        if (entity instanceof EntityNpc) return new ScriptNpc((EntityNpc) entity);
        if (entity instanceof EntityItem) return new ScriptEntityItem((EntityItem) entity);
        if (entity != null) return new ScriptEntity<>(entity);
        return null;
    }

    protected ScriptEntity(T entity) {
        this.entity = entity;
    }

    @Override
    @Deprecated
    public Entity getMinecraftEntity() {
        return entity;
    }

    @Override
    public Entity asMinecraft() {
        return entity;
    }

    @Override
    public IScriptWorld getWorld() {
        return new ScriptWorld(entity.world);
    }

    /* Entity properties */

    @Override
    public ScriptVector getPosition() {
        return new ScriptVector(entity.posX, entity.posY, entity.posZ);
    }

    @Override
    public void setPosition(double x, double y, double z) {
        if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z)) throw new IllegalArgumentException();

        entity.setPositionAndUpdate(x, y, z);
        //fix if multiple players were teleported to the same position, they appear bugged to each other:
        if (entity instanceof EntityPlayerMP)
            ((EntityPlayerMP) entity).connection.setPlayerLocation(x, y, z, entity.rotationYaw, entity.rotationPitch);
    }

    @Override
    public int getDimension() {
        return entity.dimension;
    }

    @Override
    public void setDimension(int dimension) {
        // Check if the entity is already in the target dimension.
        if (entity.dimension == dimension) return;

        if (dimension < -1 || dimension > 1)
            throw new IllegalArgumentException("Dimension must be -1 (Nether), 0 (Overworld), or 1 (End).");

        MinecraftServer minecraftServer = entity.getServer();
        if (minecraftServer == null) return;
        WorldServer worldServer = minecraftServer.getWorld(dimension);

        // anonymous class to override the Teleporter within the method itself
        Teleporter teleporter = new Teleporter(worldServer) {
            @Override
            public void placeInPortal(@Nonnull Entity entityIn, float rotationYaw) {
                // This method is intentionally left blank to prevent portal creation.
            }

            @Override
            public boolean placeInExistingPortal(@Nonnull Entity entityIn, float rotationYaw) {
                // We always return false to prevent the game from placing the entity in an existing portal.
                return false;
            }
        };

        if (entity instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) entity;
            minecraftServer.getPlayerList().transferPlayerToDimension(player, dimension, teleporter);
        }
        else entity.changeDimension(dimension, teleporter);
    }

    @Override
    public ScriptVector getMotion() {
        return new ScriptVector(entity.motionX, entity.motionY, entity.motionZ);
    }

    @Override
    public void setMotion(double x, double y, double z) {
        entity.motionX = x;
        entity.motionY = y;
        entity.motionZ = z;
    }

    @Override
    public void addMotion(double x, double y, double z) {
        entity.velocityChanged = true;
        entity.addVelocity(x, y, z);
    }

    @Override
    public ScriptVector getRotations() {
        return new ScriptVector(getPitch(), getYaw(), getYawHead());
    }

    @Override
    public void setRotations(float pitch, float yaw, float yawHead) {
        if (Float.isNaN(pitch) || Float.isNaN(yaw) || Float.isNaN(yawHead)) throw new IllegalArgumentException();

        entity.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, yaw, pitch);
        entity.setRotationYawHead(yawHead);
        entity.setRenderYawOffset(yawHead);

        if (!isPlayer()) {
            EntityTracker tracker = ((WorldServer) entity.world).getEntityTracker();

            for (EntityPlayer player : tracker.getTrackingPlayers(entity))
                Dispatcher.sendTo(new PacketEntityRotations(entity.getEntityId(), yaw, yawHead, pitch), (EntityPlayerMP) player);
        }
    }

    @Override
    public float getPitch() {
        return entity.rotationPitch;
    }

    @Override
    public float getYaw() {
        return entity.rotationYaw;
    }

    @Override
    public float getYawHead() {
        return entity.getRotationYawHead();
    }

    @Override
    public ScriptVector getLook() {
        //this.entity.getLookVec() is not used because it does not work on entities that are not moving (but for players)
        //after many tests, this is the best way to get the most accurate look vector for all entities, whether they are moving or not
        float f1 = -(entity.rotationPitch * ((float) Math.PI / 180F));
        float f2 = entity.getRotationYawHead() * ((float) Math.PI / 180F);
        float f3 = -MathHelper.sin(f2);
        float f4 = MathHelper.cos(f2);
        float f6 = MathHelper.cos(f1);
        return new ScriptVector(f3 * f6, entity.getLookVec().y, f4 * f6);
    }

    @Override
    public float getEyeHeight() {
        return EntityUtils.getEyeHeight(entity);
    }

    @Override
    public float getWidth() {
        return entity.width;
    }

    @Override
    public float getHeight() {
        return entity.height;
    }

    @Override
    public float getHp() {
        if (isLivingBase()) return ((EntityLivingBase) entity).getHealth();

        return 0;
    }

    @Override
    public void setHp(float hp) {
        if (isLivingBase()) ((EntityLivingBase) entity).setHealth(hp);
    }

    @Override
    public float getMaxHp() {
        if (isLivingBase()) return ((EntityLivingBase) entity).getMaxHealth();

        return 0;
    }

    @Override
    public void setMaxHp(float hp) {
        if (isLivingBase() && hp > 0.0F)
            ((EntityLivingBase) entity).getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(hp);
    }

    @Override
    public boolean isInWater() {
        return entity.isInWater();
    }

    @Override
    public boolean isInLava() {
        return entity.isInLava();
    }

    @Override
    public boolean isBurning() {
        return entity.isBurning();
    }

    @Override
    public void setBurning(int seconds) {
        if (seconds <= 0) entity.extinguish();
        else entity.setFire(seconds);
    }

    @Override
    public boolean isSneaking() {
        return entity.isSneaking();
    }

    @Override
    public boolean isSprinting() {
        return entity.isSprinting();
    }

    @Override
    public boolean isOnGround() {
        return entity.onGround;
    }

    /* Ray tracing */

    @Override
    public IScriptRayTrace rayTrace(double maxDistance) {
        return new ScriptRayTrace(RayTracing.rayTraceWithEntity(entity, maxDistance));
    }

    @Override
    public IScriptRayTrace rayTraceBlock(double maxDistance) {
        return new ScriptRayTrace(RayTracing.rayTrace(entity, maxDistance, 0));
    }

    /* Items */

    @Override
    public IScriptItemStack getMainItem() {
        if (isLivingBase()) return ScriptItemStack.create(((EntityLivingBase) entity).getHeldItemMainhand());

        return ScriptItemStack.EMPTY;
    }

    @Override
    public void setMainItem(IScriptItemStack stack) {
        setItem(EnumHand.MAIN_HAND, stack);
    }

    @Override
    public IScriptItemStack getOffItem() {
        if (isLivingBase()) return ScriptItemStack.create(((EntityLivingBase) entity).getHeldItemOffhand());

        return ScriptItemStack.EMPTY;
    }

    @Override
    public void setOffItem(IScriptItemStack stack) {
        setItem(EnumHand.OFF_HAND, stack);
    }

    private void setItem(EnumHand hand, IScriptItemStack stack) {
        if (stack == null) stack = ScriptItemStack.EMPTY;

        if (isLivingBase()) ((EntityLivingBase) entity).setHeldItem(hand, stack.asMinecraft().copy());
    }

    @Override
    public void giveItem(IScriptItemStack stack) {
        giveItem(stack, true, true);
    }

    @Override
    public void giveItem(IScriptItemStack stack, boolean playSound, boolean dropIfInventoryFull) {
        if (stack == null || stack.isEmpty()) return;

        if (isPlayer()) {
            EntityPlayer player = (EntityPlayer) entity;
            ItemStack itemStack = stack.asMinecraft().copy();
            boolean flag = player.inventory.addItemStackToInventory(itemStack);

            if (flag) {
                if (playSound)
                    player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((player.getRNG()
                                                                                                                                                             .nextFloat() - player.getRNG()
                                                                                                                                                                                  .nextFloat()) * 0.7F + 1.0F) * 2.0F);

                player.inventoryContainer.detectAndSendChanges();
            }
            else if (dropIfInventoryFull) if (!player.world.isRemote) {
                EntityItem entityItem = new EntityItem(player.world, player.posX, player.posY, player.posZ, itemStack);

                entityItem.setNoPickupDelay();

                player.getEntityWorld().spawnEntity(entityItem);
            }
        }
        else if (isLivingBase()) {
            EntityLivingBase living = (EntityLivingBase) entity;

            if (living.getHeldItemMainhand().isEmpty())
                living.setHeldItem(EnumHand.MAIN_HAND, stack.asMinecraft().copy());
            else if (living.getHeldItemOffhand().isEmpty())
                living.setHeldItem(EnumHand.OFF_HAND, stack.asMinecraft().copy());
            else living.entityDropItem(stack.asMinecraft().copy(), getEyeHeight());
        }
    }

    @Override
    public int removeItem(IScriptItemStack stack) {
        return removeItem(stack, 1);
    }

    @Override
    public int removeItem(IScriptItemStack stack, int count) {
        if (stack == null || stack.isEmpty() || count == 0) return 0;
        ItemStack itemStack = stack.asMinecraft().copy();
        int deleteCount = 0;
        if (isPlayer()) {
            EntityPlayer player = (EntityPlayer) entity;
            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                if (count > 0 && deleteCount >= count) return deleteCount;
                ItemStack _stack = player.inventory.getStackInSlot(i);
                if (_stack.isItemEqualIgnoreDurability(itemStack))
                    if (count > 0 && deleteCount + _stack.getCount() > count) {
                        _stack.setCount(_stack.getCount() - (count - deleteCount));
                        deleteCount = count;
                    }
                    else {
                        deleteCount += _stack.getCount();
                        player.inventory.removeStackFromSlot(i);
                    }
            }
        }
        else if (isLivingBase()) {
            EntityLivingBase living = (EntityLivingBase) entity;

            if (living.getHeldItemMainhand().isItemEqualIgnoreDurability(itemStack))
                if (count > 0 && deleteCount + living.getHeldItemMainhand().getCount() > count) {
                    living.getHeldItemMainhand()
                          .setCount(living.getHeldItemMainhand().getCount() - (count - deleteCount));
                    deleteCount = count;
                }
                else {
                    deleteCount += living.getHeldItemMainhand().getCount();
                    living.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
                }

            if (living.getHeldItemOffhand().isItemEqualIgnoreDurability(itemStack))
                if (count > 0 && deleteCount + living.getHeldItemOffhand().getCount() > count) {
                    living.getHeldItemOffhand()
                          .setCount(living.getHeldItemOffhand().getCount() - (count - deleteCount));
                    deleteCount = count;
                }
                else {
                    deleteCount += living.getHeldItemOffhand().getCount();
                    living.setHeldItem(EnumHand.OFF_HAND, ItemStack.EMPTY);
                }
        }
        return deleteCount;
    }

    @Override
    public int findItem(IScriptItemStack stack) {
        return findItem(stack, 0);
    }

    @Override
    public int findItem(IScriptItemStack stack, int startIndex) {
        if (stack == null || stack.isEmpty() || startIndex < 0) return -1;
        ItemStack itemStack = stack.asMinecraft().copy();
        if (isPlayer()) {
            EntityPlayer player = (EntityPlayer) entity;
            for (int i = startIndex; i < player.inventory.getSizeInventory(); i++)
                if (player.inventory.getStackInSlot(i).isItemEqualIgnoreDurability(itemStack)) return i;
        }
        else if (isLivingBase()) {
            EntityLivingBase living = (EntityLivingBase) entity;
            if (startIndex == 0 && living.getHeldItemMainhand().isItemEqualIgnoreDurability(itemStack)) return 0;
            if (startIndex <= 1 && living.getHeldItemOffhand().isItemEqualIgnoreDurability(itemStack)) return 1;
        }
        return -1;
    }

    @Override
    public IScriptItemStack getHelmet() {
        if (isLivingBase())
            return ScriptItemStack.create(((EntityLivingBase) entity).getItemStackFromSlot(EntityEquipmentSlot.HEAD)
                                                                     .copy());

        return null;
    }

    @Override
    public IScriptItemStack getChestplate() {
        if (isLivingBase())
            return ScriptItemStack.create(((EntityLivingBase) entity).getItemStackFromSlot(EntityEquipmentSlot.CHEST)
                                                                     .copy());

        return null;
    }

    @Override
    public IScriptItemStack getLeggings() {
        if (isLivingBase())
            return ScriptItemStack.create(((EntityLivingBase) entity).getItemStackFromSlot(EntityEquipmentSlot.LEGS)
                                                                     .copy());
        return null;
    }

    @Override
    public IScriptItemStack getBoots() {
        if (isLivingBase())
            return ScriptItemStack.create(((EntityLivingBase) entity).getItemStackFromSlot(EntityEquipmentSlot.FEET)
                                                                     .copy());
        return null;
    }

    @Override
    public void setHelmet(IScriptItemStack itemStack) {
        entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, itemStack.asMinecraft());
    }

    @Override
    public void setChestplate(IScriptItemStack itemStack) {
        entity.setItemStackToSlot(EntityEquipmentSlot.CHEST, itemStack.asMinecraft());
    }

    @Override
    public void setLeggings(IScriptItemStack itemStack) {
        entity.setItemStackToSlot(EntityEquipmentSlot.LEGS, itemStack.asMinecraft());
    }

    @Override
    public void setBoots(IScriptItemStack itemStack) {
        entity.setItemStackToSlot(EntityEquipmentSlot.FEET, itemStack.asMinecraft());
    }

    @Override
    public void playAnimation(String animation) {
        PacketPlayAnimation packet = new PacketPlayAnimation(animation, entity.getUniqueID().toString());
        Dispatcher.sendToTracked(entity, packet);
        if (entity instanceof EntityPlayerMP) Dispatcher.sendTo(packet, (EntityPlayerMP) entity);
    }

    @Override
    public boolean isTamed(IScriptPlayer player) {
        if (!(entity instanceof EntityTameable)) return false;
        return ((EntityTameable) entity).isTamed();
    }

    @Override
    public void setOwner(IScriptPlayer player) {
        if (!(entity instanceof EntityTameable)) return;
        if (player == null) ((EntityTameable) entity).setTamed(false);
        else ((EntityTameable) entity).setTamedBy(player.asMinecraft());
    }

    @Override
    public IScriptPlayer getOwner() {
        if (!(entity instanceof EntityTameable)) return null;
        EntityTameable _entity = (EntityTameable) entity;
        if (_entity.isTamed() && _entity.getOwner() != null)
            return new ScriptPlayer((EntityPlayerMP) _entity.getOwner());
        return null;
    }

    /* Entity meta */

    @Override
    public void setSpeed(float speed) {
        if (isLivingBase())
            ((EntityLivingBase) entity).getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(speed);
    }

    @Override
    public IScriptEntity getTarget() {
        if (entity instanceof EntityLiving) return ScriptEntity.create(((EntityLiving) entity).getAttackTarget());

        return null;
    }

    @Override
    public void setTarget(IScriptEntity entity) {

        if (this.entity instanceof EntityLiving && entity == null) {
            EntityLiving livingBase = (EntityLiving) this.entity;

            /* This should be enough, but it does not work most of the time for some reason. */
            livingBase.setAttackTarget(null);
            livingBase.setRevengeTarget(null);

            /* So I solved it by spawning an armor stand and making the entity focus on it and removing it after 1 tick. */
            String id = "minecraft:armor_stand";
            double x = this.entity.getPosition().getX();
            double y = this.entity.getPosition().getY() - 1;
            double z = this.entity.getPosition().getZ();
            String nbt = "{Marker:1b,NoGravity:1,Invisible:1b,CustomName:\"target_canceler\"}";
            NBTTagCompound tag = new NBTTagCompound();

            try {
                tag = JsonToNBT.getTagFromJson(nbt);
            } catch (Exception ignored) {
            }

            INBTCompound compound = new ScriptNBTCompound(tag);
            ScriptWorld world = new ScriptWorld(this.entity.world);

            IScriptEntity targetCanceller = world.spawnEntity(id, x, y, z, compound);
            livingBase.setAttackTarget((EntityLivingBase) targetCanceller.asMinecraft());
            livingBase.setRevengeTarget((EntityLivingBase) targetCanceller.asMinecraft());
            targetCanceller.remove();
        }
        else if (this.entity instanceof EntityLiving && entity.isLivingBase()) {
            EntityLiving livingBase = (EntityLiving) this.entity;

            livingBase.setAttackTarget((EntityLivingBase) entity.asMinecraft());
        }
    }

    @Override
    public boolean isAIEnabled() {
        if (isLivingBase()) return !((EntityLiving) entity).isAIDisabled();

        return false;
    }

    @Override
    public void setAIEnabled(boolean enabled) {
        if (isLivingBase()) ((EntityLiving) entity).setNoAI(!enabled);
    }

    @Override
    public String getUniqueId() {
        return entity.getCachedUniqueIdString();
    }

    @Override
    @Deprecated
    public String getEntityId() {
        ResourceLocation rl = EntityList.getKey(entity);
        return rl == null ? "" : rl.toString();
    }

    @Override
    public String getId() {
        ResourceLocation rl = EntityList.getKey(entity);
        return rl == null ? "" : rl.toString();
    }

    @Override
    public int getTicks() {
        return entity.ticksExisted;
    }

    @Override
    public int getCombinedLight() {
        return EntityUtils.getCombinedLight(entity);
    }

    @Override
    public String getName() {
        return entity.getName();
    }

    @Override
    public void setName(String name) {
        entity.setCustomNameTag(name);
        entity.setAlwaysRenderNameTag(!name.isEmpty());
    }

    @Override
    public void setInvisible(boolean invisible) {
        entity.setInvisible(invisible);
    }

    @Override
    public INBTCompound getFullData() {
        return new ScriptNBTCompound(entity.writeToNBT(new NBTTagCompound()));
    }

    @Override
    public void setFullData(INBTCompound data) {
        entity.readFromNBT(data.asMinecraft());
    }

    @Override
    public INBTCompound getEntityData() {
        return new ScriptNBTCompound(entity.getEntityData());
    }

    @Override
    public boolean isPlayer() {
        return entity instanceof EntityPlayer;
    }

    @Override
    @Deprecated
    public boolean isNpc() {
        return isNPC();
    }

    @Override
    public boolean isNPC() {
        return entity instanceof EntityNpc;
    }

    @Override
    public boolean isItem() {
        return entity instanceof EntityItem;
    }

    @Override
    public boolean isLivingBase() {
        return entity instanceof EntityLivingBase;
    }

    @Override
    public boolean isSame(IScriptEntity other) {
        return entity == other.asMinecraft();
    }

    @Override
    public boolean isEntityInRadius(IScriptEntity target, double radius) {
        return entity.getDistanceSq(target.asMinecraft()) <= radius * radius;
    }

    @Override
    public boolean isInArea(double x1, double y1, double z1, double x2, double y2, double z2) {
        return new AxisAlignedBB(x1, y1, z1, x2, y2, z2).intersects(entity.getEntityBoundingBox());
    }

    @Override
    public void damage(float health) {
        if (isLivingBase()) entity.attackEntityFrom(DamageSource.OUT_OF_WORLD, health);
    }

    @Override
    public void damageAs(IScriptEntity _attacker, float damage) {
        damageAs(_attacker, damage, false);
    }

    @Override
    public void damageAs(IScriptEntity _attacker, float damage, boolean ignore) {
        Entity attacker = _attacker.asMinecraft();
        if (!(attacker instanceof EntityLivingBase)) return;
        EntityLivingBase attackerLiving = (EntityLivingBase) attacker;
        attackerLiving.setLastAttackedEntity(entity);
        DamageSource damageSource = DamageSource.causeIndirectDamage(attacker, attackerLiving);
        entity.attackEntityFrom(ignore ? damageSource.setDamageBypassesArmor() : damageSource, damage);
    }

    @Override
    public void damageWithItemsAs(IScriptPlayer player) {
        player.asMinecraft().attackTargetEntityWithCurrentItem(entity);
    }

    @Override
    public void mount(IScriptEntity entity) {
        this.entity.startRiding(entity.asMinecraft(), true);
    }

    @Override
    public void dismount() {
        entity.dismountRidingEntity();
    }

    @Override
    public IScriptEntity getMount() {
        return ScriptEntity.create(entity.getRidingEntity());
    }

    @Override
    public ScriptBox getBoundingBox() {
        AxisAlignedBB aabb = entity.getEntityBoundingBox();

        return new ScriptBox(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
    }

    private ScriptEntityItem dropItemInternal(ItemStack itemStack) {
        if (itemStack.isEmpty()) return null;

        EntityItem entityItem = new EntityItem(entity.world, getPosition().x, getPosition().y + getEyeHeight(), getPosition().z, itemStack);

        entityItem.setPickupDelay(40);
        if (isNpc()) entityItem.setThrower(((EntityNpc) entity).getId());
        else entityItem.setThrower(entity.getName());


        entityItem.velocityChanged = true;
        entityItem.addVelocity(getLook().x / 3, getLook().y / 3, getLook().z / 3);

        if (entity.world.spawnEntity(entityItem)) return new ScriptEntityItem(entityItem);
        return null;
    }

    @Override
    public ScriptEntityItem dropItem(int amount) {
        ItemStack heldItemStack = getMainItem().asMinecraft();

        if (heldItemStack.isEmpty()) return null;

        int count = heldItemStack.getCount();

        if (amount > count) amount = count;

        ItemStack droppedStack = heldItemStack.copy();
        droppedStack.setCount(amount);

        heldItemStack.shrink(amount);

        return dropItemInternal(droppedStack);
    }

    @Override
    public ScriptEntityItem dropItem() {
        return dropItem(1);
    }

    @Override
    public ScriptEntityItem dropItem(IScriptItemStack scriptItemStack) {
        return dropItemInternal(scriptItemStack.asMinecraft());
    }

    @Override
    public float getFallDistance() {
        return entity.fallDistance;
    }

    @Override
    public void setFallDistance(float distance) {
        entity.fallDistance = distance;
    }

    @Override
    public void remove() {
        if (!isPlayer()) entity.setDead();
    }

    @Override
    public void kill() {
        entity.onKillCommand();
    }

    @Override
    public void swingArm(int arm) {
        if (isLivingBase()) ((EntityLivingBase) entity).swingArm(arm == 1 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
    }

    @Override
    public List<IScriptEntity> getLeashedEntities() {
        List<IScriptEntity> entities = new ArrayList<>();
        World world = entity.world;

        for (Entity entity : world.loadedEntityList)
            if (entity instanceof EntityLiving) {
                EntityLiving entityLiving = (EntityLiving) entity;
                if (entityLiving.getLeashed() && entityLiving.getLeashHolder() == this.entity)
                    entities.add(ScriptEntity.create(entityLiving));
            }

        return entities;
    }

    @Override
    public boolean setLeashHolder(IScriptEntity leashHolder) {
        if (!(entity instanceof EntityLiving)) return false;

        EntityLiving leashedEntity = (EntityLiving) entity;
        boolean wasLeashed = leashedEntity.getLeashed();
        leashedEntity.setLeashHolder(leashHolder.asMinecraft(), true);

        // If the entity was not leashed before and is leashed now, the operation was successful.
        return !wasLeashed && leashedEntity.getLeashed();
    }

    @Override
    public IScriptEntity getLeashHolder() {
        if (!(entity instanceof EntityLiving)) return null;

        EntityLiving leashedEntity = (EntityLiving) entity;
        Entity leashHolder = leashedEntity.getLeashHolder();

        return ScriptEntity.create(leashHolder);
    }

    @Override
    public boolean clearLeashHolder(boolean dropLead) {
        if (!(entity instanceof EntityLiving)) return false;

        EntityLiving leashedEntity = (EntityLiving) entity;
        boolean wasLeashed = leashedEntity.getLeashed();

        leashedEntity.clearLeashed(true, dropLead);

        // If the entity was leashed before and is not leashed now, the operation was successful.
        return wasLeashed && !leashedEntity.getLeashed();
    }

    /* Modifiers */

    @Override
    public void setModifier(String modifierName, double value) {
        if (isLivingBase()) {
            EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
            UUID uuid = entityLivingBase.getUniqueID();
            IAttributeInstance attribute = entityLivingBase.getAttributeMap().getAttributeInstanceByName(modifierName);

            if (attribute == null) return;

            AttributeModifier modifier = new AttributeModifier(uuid, "script." + modifierName, value, 0);

            if (attribute.hasModifier(modifier)) attribute.removeModifier(modifier);

            attribute.applyModifier(modifier);
        }
    }

    @Override
    public double getModifier(String modifierName) {
        if (isLivingBase()) {
            EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
            IAttributeInstance attribute = entityLivingBase.getAttributeMap().getAttributeInstanceByName(modifierName);

            if (attribute != null) {
                AttributeModifier modifier = attribute.getModifier(entityLivingBase.getUniqueID());

                return modifier == null ? 0 : modifier.getAmount();
            }
        }

        return 0;
    }

    @Override
    public void removeModifier(String modifierName) {
        if (isLivingBase()) {
            EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
            IAttributeInstance attribute = entityLivingBase.getAttributeMap().getAttributeInstanceByName(modifierName);

            if (attribute != null) {
                AttributeModifier modifier = attribute.getModifier(entityLivingBase.getUniqueID());
                if (modifier != null) attribute.removeModifier(modifier);
            }
        }
    }

    @Override
    public void removeAllModifiers() {
        if (isLivingBase()) {
            EntityLivingBase entityLivingBase = (EntityLivingBase) entity;

            for (IAttributeInstance attribute : entityLivingBase.getAttributeMap().getAllAttributes())
                attribute.removeModifier(entityLivingBase.getUniqueID());
        }
    }

    /* Potion effects */

    @Override
    public void applyPotion(Potion potion, int duration, int amplifier, boolean particles) {
        if (isLivingBase()) {
            PotionEffect effect = new PotionEffect(potion, duration, amplifier, false, particles);

            ((EntityLivingBase) entity).addPotionEffect(effect);
        }
    }

    @Override
    public boolean hasPotion(Potion potion) {
        if (isLivingBase()) return ((EntityLivingBase) entity).isPotionActive(potion);

        return false;
    }

    @Override
    public boolean removePotion(Potion potion) {
        if (isLivingBase()) {
            EntityLivingBase entity = (EntityLivingBase) this.entity;
            int size = entity.getActivePotionMap().size();

            entity.removePotionEffect(potion);

            return size != entity.getActivePotionMap().size();
        }

        return false;
    }

    @Override
    public void clearPotions() {
        if (isLivingBase()) ((EntityLivingBase) entity).clearActivePotions();
    }

    /* Mappet stuff */

    @Override
    public IMappetStates getStates() {
        if (states == null) {
            States entityStates = EntityUtils.getStates(entity);
            if (entityStates != null) states = entityStates;
        }

        return states;
    }

    @Override
    public AbstractMorph getMorph() {
        return entity instanceof IMorphProvider ? ((IMorphProvider) entity).getMorph() : null;
    }

    @Override
    public boolean setMorph(AbstractMorph morph) {
        if (Loader.isModLoaded("blockbuster")) return setActorsMorph(morph);

        return false;
    }

    @Optional.Method(modid = "blockbuster")
    private boolean setActorsMorph(AbstractMorph morph) {
        if (entity instanceof EntityActor) {
            EntityActor actor = (EntityActor) entity;
            actor.morph.setDirect(morph);

            PacketModifyActor message = new PacketModifyActor(actor);
            mchorse.blockbuster.network.Dispatcher.sendToTracked(actor, message);

            return true;
        }

        return false;
    }

    @Override
    public void displayMorph(AbstractMorph morph, int expiration, double x, double y, double z, float yaw, float pitch, boolean rotate, IScriptPlayer player) {
        if (morph == null) return;

        WorldMorph worldMorph = new WorldMorph();

        worldMorph.morph = morph;
        worldMorph.expiration = expiration;
        worldMorph.rotate = rotate;
        worldMorph.x = x;
        worldMorph.y = y;
        worldMorph.z = z;
        worldMorph.yaw = yaw;
        worldMorph.pitch = pitch;
        worldMorph.entity = entity;

        PacketWorldMorph message = new PacketWorldMorph(worldMorph);

        if (player == null) {
            Dispatcher.sendToTracked(entity, message);

            if (isPlayer()) Dispatcher.sendTo(message, (EntityPlayerMP) entity);
        }
        else Dispatcher.sendTo(message, player.asMinecraft());
    }

    @Override
    public IScriptEntity shootBBGunProjectile(String gunPropsNBT) {
        if (isLivingBase() && Loader.isModLoaded("blockbuster")) try {
            return shootBBGunProjectileMethod(gunPropsNBT);
        } catch (Exception e) {
            Mappet.logger.error(e.getMessage());
        }

        return null;
    }

    @Optional.Method(modid = "blockbuster")
    private IScriptEntity shootBBGunProjectileMethod(String gunPropsNBT) throws NBTException {
        if (isLivingBase()) {
            EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
            NBTTagCompound gunPropsNBTCompound = JsonToNBT.getTagFromJson(gunPropsNBT).getCompoundTag("Gun");
            GunProps gunProps = new GunProps(gunPropsNBTCompound.getCompoundTag("Projectile"));
            gunProps.fromNBT(gunPropsNBTCompound);
            EntityGunProjectile projectile = new EntityGunProjectile(entityLivingBase.world, gunProps, gunProps.projectileMorph);

            projectile.setPosition(entityLivingBase.posX, entityLivingBase.posY + 1.8, entityLivingBase.posZ);
            projectile.shoot(entityLivingBase, entityLivingBase.rotationPitch, entityLivingBase.getRotationYawHead(), 0, gunProps.speed, 0);
            projectile.setInitialMotion();
            entityLivingBase.world.spawnEntity(projectile);

            return ScriptEntity.create(projectile);
        }

        return null;
    }

    @Override
    public void executeCommand(String command) {
        if (entity.world.getMinecraftServer() != null)
            entity.world.getMinecraftServer().getCommandManager().executeCommand(entity, command);
    }

    @Override
    public void executeScript(String scriptName) {
        executeScript(scriptName, "main");
    }

    @Override
    public void executeScript(String scriptName, String function) {
        DataContext context = new DataContext(entity);
        try {
            Mappet.scripts.execute(scriptName, function, context);
        } catch (ScriptException e) {
            String fileName = e.getFileName() == null ? scriptName : e.getFileName();
            Mappet.logger.error("Script Error: " + fileName + " - Line: " + e.getLineNumber() + " - Column: " + e.getColumnNumber() + " - Message: " + e.getMessage());
            //throw new RuntimeException("Script Error: " + fileName + " - Line: " + e.getLineNumber() + " - Column: " + e.getColumnNumber() + " - Message: " + e.getMessage(), e);
        } catch (Exception e) {
            Mappet.logger.error("Script Empty: " + scriptName + " - Error: " + e.getClass()
                                                                                .getSimpleName() + ": " + e.getMessage());
            //throw new RuntimeException("Script Empty: " + scriptName + " - Error: " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void executeScript(String scriptName, String function, Object... args) {
        DataContext context = new DataContext(entity);

        try {
            Mappet.scripts.execute(scriptName, function, context, args);
        } catch (ScriptException e) {
            String fileName = e.getFileName() == null ? scriptName : e.getFileName();
            Mappet.logger.error("Script Error: " + fileName + " - Line: " + e.getLineNumber() + " - Column: " + e.getColumnNumber() + " - Message: " + e.getMessage());
            //throw new RuntimeException("Script Error: " + fileName + " - Line: " + e.getLineNumber() + " - Column: " + e.getColumnNumber() + " - Message: " + e.getMessage(), e);
        } catch (Exception e) {
            Mappet.logger.error("Script Empty: " + scriptName + " - Error: " + e.getClass()
                                                                                .getSimpleName() + ": " + e.getMessage());
            //throw new RuntimeException("Script Empty: " + scriptName + " - Error: " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void lockPosition(double x, double y, double z) {
        entity.getEntityData().setBoolean("positionLocked", true);
        entity.getEntityData().setDouble("lockX", x);
        entity.getEntityData().setDouble("lockY", y);
        entity.getEntityData().setDouble("lockZ", z);
    }

    @Override
    public void unlockPosition() {
        entity.getEntityData().setBoolean("positionLocked", false);
    }

    @Override
    public boolean isPositionLocked() {
        return entity.getEntityData().getBoolean("positionLocked");
    }

    @Override
    public void lockRotation(float yaw, float pitch, float yawHead) {
        entity.getEntityData().setBoolean("rotationLocked", true);
        entity.getEntityData().setFloat("lockYaw", yaw);
        entity.getEntityData().setFloat("lockPitch", pitch);
        entity.getEntityData().setFloat("lockYawHead", yawHead);
    }

    @Override
    public void unlockRotation() {
        entity.getEntityData().setBoolean("rotationLocked", false);
    }

    @Override
    public boolean isRotationLocked() {
        return entity.getEntityData().getBoolean("rotationLocked");
    }

    @Override
    public void moveTo(String interpolation, int durationTicks, double x, double y, double z, boolean disableAI) {
        if (disableAI) {
            setAIEnabled(false);
            moveTo(interpolation, durationTicks, x, y, z);
            CommonProxy.eventHandler.addExecutable(new RunnableExecutionFork(durationTicks, () -> setAIEnabled(true)));
        }
        else moveTo(interpolation, durationTicks, x, y, z);
    }

    private void moveTo(String interpolation, int durationTicks, double x, double y, double z) {
        Interpolation interp = Interpolation.valueOf(interpolation.toUpperCase());
        double startX = entity.posX;
        double startY = entity.posY;
        double startZ = entity.posZ;

        for (int i = 0; i < durationTicks; i++) {
            double progress = (double) i / (double) durationTicks;
            double interpX = interp.interpolate(startX, x, progress);
            double interpY = interp.interpolate(startY, y, progress);
            double interpZ = interp.interpolate(startZ, z, progress);

            CommonProxy.eventHandler.addExecutable(new RunnableExecutionFork(i, () -> setPosition(interpX, interpY, interpZ)));
        }
    }

//    public void breakMove(String interpolation, int durationTicks, double x, double y, double z, boolean disableAI) {
//        if (disableAI) {
//            setAIEnabled(false);
//            moveTo(interpolation, durationTicks, x, y, z);
//            CommonProxy.eventHandler.addExecutable(new RunnableExecutionFork(durationTicks, () -> setAIEnabled(true)));
//        }
//        else moveTo(interpolation, durationTicks, x, y, z);
//    }

    /* Entity AI */

    @Override
    public void observe(IScriptEntity target) {
        if (entity instanceof EntityLiving) {
            EntityLiving entityLiving = (EntityLiving) entity;

            if (target == null) {
                EntityAITasks.EntityAITaskEntry taskToRemove = null;

                for (EntityAITasks.EntityAITaskEntry task : entityLiving.tasks.taskEntries)
                    if (task.action instanceof EntityAILookAtTarget) {
                        taskToRemove = task;
                        break;
                    }

                if (taskToRemove != null) entityLiving.tasks.removeTask(taskToRemove.action);
            }
            else entityLiving.tasks.addTask(8, new EntityAILookAtTarget(entityLiving, target.asMinecraft(), 1.0F));
        }
    }

    public IScriptEntity getObservedEntity() {
        if (entity instanceof EntityLiving) {
            EntityLiving entityLiving = (EntityLiving) entity;

            for (EntityAITasks.EntityAITaskEntry task : entityLiving.tasks.taskEntries) {
                Entity target = null;

                if (task.action instanceof EntityAILookAtTarget) {
                    EntityAILookAtTarget lookAtTask = (EntityAILookAtTarget) task.action;
                    target = lookAtTask.getTarget();
                }
                else if (task.action instanceof EntityAIWatchClosest) {
                    EntityAIWatchClosest watchClosestTask = (EntityAIWatchClosest) task.action;
                    target = getEntityFromWatchClosest(watchClosestTask);
                }

                if (target != null) return ScriptEntity.create(target);
            }
        }

        return null;
    }

    private Entity getEntityFromWatchClosest(EntityAIWatchClosest watchClosestTask) {
        try {
            Field field = EntityAIWatchClosest.class.getDeclaredField("closestEntity");
            field.setAccessible(true);
            return (Entity) field.get(watchClosestTask);
        } catch (Exception e) {
            Mappet.logger.error("Failed to get closest entity: " + e.getMessage());
            return null;
        }
    }


    @Override
    public void addEntityPatrol(double x, double y, double z, double speed, boolean shouldCirculate, String executeCommandOnArrival) {
        if (entity instanceof EntityLiving) {
            EntityLiving entityLiving = (EntityLiving) entity;
            EntityAITasks.EntityAITaskEntry taskToRemove = findEntitiesAIPatrolTask(entityLiving);
            EntitiesAIPatrol patrolTask;

            if (taskToRemove != null) {
                patrolTask = (EntitiesAIPatrol) taskToRemove.action;
                entityLiving.tasks.removeTask(patrolTask);

                patrolTask.addPatrolPoint(new BlockPos(x, y, z), shouldCirculate, executeCommandOnArrival);
            }
            else
                patrolTask = new EntitiesAIPatrol((EntityLiving) entity, speed, new BlockPos[]{new BlockPos(x, y, z)}, new boolean[]{shouldCirculate}, new String[]{executeCommandOnArrival});

            entityLiving.tasks.addTask(1, patrolTask);
        }
    }

    @Override
    public void clearEntityPatrols() {
        if (entity instanceof EntityLiving) {
            EntityLiving entityLiving = (EntityLiving) entity;
            EntityAITasks.EntityAITaskEntry taskToRemove = findEntitiesAIPatrolTask(entityLiving);

            if (taskToRemove != null) entityLiving.tasks.removeTask(taskToRemove.action);
        }
    }

    private EntityAITasks.EntityAITaskEntry findEntitiesAIPatrolTask(EntityLiving entityLiving) {
        for (EntityAITasks.EntityAITaskEntry task : entityLiving.tasks.taskEntries)
            if (task.action instanceof EntitiesAIPatrol) return task;
        return null;
    }

    @Override
    public void setRotationsAI(float yaw, float pitch, float yawHead) {
        if (entity instanceof EntityLiving) {
            EntityLiving entityLiving = (EntityLiving) entity;
            EntityAITasks.EntityAITaskEntry taskToRemove = removeTaskIfExists(entityLiving, EntityAIRotations.class);
            entityLiving.tasks.addTask(9, new EntityAIRotations(entityLiving, yaw, pitch, yawHead, 1.0F));

            RotationDataStorage.getRotationDataStorage(entity.world)
                               .addRotationData(entityLiving.getUniqueID(), yaw, pitch, yawHead);
        }
    }

    @Override
    public void clearRotationsAI() {
        if (entity instanceof EntityLiving) {
            EntityLiving entityLiving = (EntityLiving) entity;

            removeTaskIfExists(entityLiving, EntityAIRotations.class);
            RotationDataStorage.getRotationDataStorage(entity.world).removeRotationData(entityLiving.getUniqueID());
        }
    }

    private EntityAITasks.EntityAITaskEntry removeTaskIfExists(EntityLiving entityLiving, Class<?> taskClass) {
        EntityAITasks.EntityAITaskEntry taskToRemove = null;

        for (EntityAITasks.EntityAITaskEntry task : entityLiving.tasks.taskEntries)
            if (task.action.getClass().equals(taskClass)) {
                taskToRemove = task;
                break;
            }

        if (taskToRemove != null) entityLiving.tasks.removeTask(taskToRemove.action);

        return taskToRemove;
    }


    @Override
    public void executeRepeatingCommand(String command, int frequency) {
        if (entity instanceof EntityLiving) {
            EntityLiving entityLiving = (EntityLiving) entity;
            entityLiving.tasks.addTask(10, new EntityAIRepeatingCommand(entityLiving, command, frequency));
            RepeatingCommandDataStorage.getRepeatingCommandDataStorage(entity.world)
                                       .addRepeatingCommandData(entityLiving.getUniqueID(), command, frequency);
        }
    }

    @Override
    public void clearAllRepeatingCommands() {
        if (entity instanceof EntityLiving) {
            EntityLiving entityLiving = (EntityLiving) entity;

            removeTaskIfExists(entityLiving, EntityAIRepeatingCommand.class);
            RepeatingCommandDataStorage.getRepeatingCommandDataStorage(entity.world)
                                       .removeRepeatingCommandData(entityLiving.getUniqueID());
        }
    }

    @Override
    public void removeRepeatingCommand(String command) {
        if (entity instanceof EntityLiving) {
            EntityLiving entityLiving = (EntityLiving) entity;

            removeSpecificRepeatingCommandTaskIfExists(entityLiving, command);
            RepeatingCommandDataStorage.getRepeatingCommandDataStorage(entity.world)
                                       .removeSpecificRepeatingCommandData(entityLiving.getUniqueID(), command);
        }
    }

    private void removeSpecificRepeatingCommandTaskIfExists(EntityLiving entityLiving, String command) {
        List<EntityAITasks.EntityAITaskEntry> tasksToRemove = new ArrayList<>();

        for (EntityAITasks.EntityAITaskEntry task : entityLiving.tasks.taskEntries)
            if (task.action instanceof EntityAIRepeatingCommand && ((EntityAIRepeatingCommand) task.action).getCommand()
                                                                                                           .equals(command))
                tasksToRemove.add(task);
        for (EntityAITasks.EntityAITaskEntry taskToRemove : tasksToRemove)
            entityLiving.tasks.removeTask(taskToRemove.action);
    }
}

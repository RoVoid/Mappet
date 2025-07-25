package mchorse.mappet.entities;

import io.netty.buffer.ByteBuf;
import mchorse.mappet.Mappet;
import mchorse.mappet.api.factions.Faction;
import mchorse.mappet.api.factions.FactionAttitude;
import mchorse.mappet.api.npcs.Npc;
import mchorse.mappet.api.npcs.NpcDrop;
import mchorse.mappet.api.npcs.NpcState;
import mchorse.mappet.api.scripts.code.nbt.ScriptNBTCompound;
import mchorse.mappet.api.scripts.user.nbt.INBTCompound;
import mchorse.mappet.api.states.States;
import mchorse.mappet.api.utils.DataContext;
import mchorse.mappet.capabilities.character.Character;
import mchorse.mappet.capabilities.character.ICharacter;
import mchorse.mappet.entities.ai.*;
import mchorse.mappet.entities.ai.fly.EntityAINpcFly;
import mchorse.mappet.entities.ai.fly.FlyingMoveHelper;
import mchorse.mappet.entities.utils.MappetNpcRespawnManager;
import mchorse.mappet.entities.utils.NpcDamageSource;
import mchorse.mappet.items.ItemNpcTool;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.common.npc.PacketNpcStateChange;
import mchorse.mappet.utils.EntityUtils;
import mchorse.mclib.utils.Interpolations;
import mchorse.metamorph.api.Morph;
import mchorse.metamorph.api.MorphUtils;
import mchorse.metamorph.api.models.IMorphProvider;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateFlying;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class EntityNpc extends EntityCreature implements IEntityAdditionalSpawnData, IMorphProvider {
    public static final int RENDER_DISTANCE = 160;

    private final Morph morph = new Morph();
    private NpcState state = new NpcState();

    private int lastDamageTime;
    private boolean unkillableFailsafe = true;
    private Faction faction;
    private EntityAIHurtByTargetNpc targetAI;

    public float smoothYawHead;
    public float prevSmoothYawHead;
    public float smoothBodyYawHead;
    public float prevSmoothBodyYawHead;

    private Entity lastTarget;

    /**
     * Needs to fix a clone issue, when npc dies and you quick reload world
     */
    public boolean dieOnLoad = false;

    public EntityNpc(World worldIn) {
        super(worldIn);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();

        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3125D);
    }

    @Override
    public void applyEntityCollision(Entity entityIn) {
        if (!this.state.immovable.get()) {
            super.applyEntityCollision(entityIn);
        }
    }

    @Override
    protected void collideWithEntity(Entity entityIn) {
        if (!this.state.immovable.get()) {
            super.collideWithEntity(entityIn);
        }

        if (world.isRemote) {
            return;
        }

        // Call the trigger for entity collision
        this.state.triggerEntityCollision.trigger(new DataContext(this, entityIn));
    }

    @Override
    public boolean hasNoGravity() {
        return this.state.hasNoGravity.get();
    }

    @Override
    public boolean canPickUpLoot() {
        return this.state.canPickUpLoot.get();
    }

    @Override
    public boolean canBeSteered() {
        return this.state.canBeSteered.get();
    }

    @Override
    public void updatePassenger(Entity passenger) {
        if (this.isPassenger(passenger)) {
            int index = this.getPassengers().indexOf(passenger);

            BlockPos offsetPos;
            if (this.state.steeringOffset.isEmpty() || index >= this.state.steeringOffset.size()) {
                offsetPos = new BlockPos(0, 0, 0); // default offset
            } else {
                offsetPos = this.state.steeringOffset.get(index);
            }

            double offsetX = offsetPos.getX();
            double offsetY = this.posY - 0.5 + EntityUtils.getHeight(this) + offsetPos.getY();
            double offsetZ = offsetPos.getZ();

            // Convert bodyYaw to radians as Java Math functions expect arguments in radians
            double bodyYaw = Math.toRadians(this.renderYawOffset);

            // Rotate the offset vector by the entity's bodyYaw
            double rotatedOffsetX = offsetX * Math.cos(bodyYaw) - offsetZ * Math.sin(bodyYaw);
            double rotatedOffsetZ = offsetX * Math.sin(bodyYaw) + offsetZ * Math.cos(bodyYaw);

            // Add the rotated offset to the entity's position
            double finalPosX = this.posX + rotatedOffsetX;
            double finalPosZ = this.posZ + rotatedOffsetZ;

            // Update the passenger's position on both the server and the client
            passenger.setPosition(finalPosX, offsetY, finalPosZ);

            // Check side and execute appropriate code
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
                // This is a client
                passenger.setPositionAndRotationDirect(finalPosX, offsetY, finalPosZ, passenger.rotationYaw, passenger.rotationPitch, 3, true);
            } else {
                // This is a server
                passenger.setPosition(finalPosX, offsetY, finalPosZ);
            }

            // Always align this entity's rotation with the passenger
            this.rotationYaw = passenger.rotationYaw;
            this.rotationPitch = passenger.rotationPitch;
        }

        // Check if the passenger is a player and the entity can be steered and only allow the first passanger to steer it.
        if (passenger instanceof EntityPlayer && canBeSteered() && this.getPassengers().indexOf(passenger) == (this.getPassengers().size() - 1)) {
            handleSteering((EntityPlayer) passenger);
        }
    }

    private void handleSteering(EntityPlayer player) {
        if (!this.world.isRemote) {
            float forward = player.moveForward;
            float strafe = player.moveStrafing;
            this.rotationYaw = player.rotationYaw;
            this.rotationYawHead = player.rotationYawHead;

            if (forward != 0 || strafe != 0) {
                float speed = state.speed.get() / 15;

                // Calculate motion based on player input
                double motionX = -Math.sin(Math.toRadians(this.rotationYaw)) * forward + Math.cos(Math.toRadians(this.rotationYaw)) * strafe;
                double motionZ = Math.cos(Math.toRadians(this.rotationYaw)) * forward + Math.sin(Math.toRadians(this.rotationYaw)) * strafe;

                // Normalize motion vector
                double motionMagnitude = Math.sqrt(motionX * motionX + motionZ * motionZ);
                motionX /= motionMagnitude;
                motionZ /= motionMagnitude;

                // Apply speed
                this.motionX = motionX * speed;
                this.motionZ = motionZ * speed;

                // Use the move method to handle collisions and movement more accurately
                this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);

                // Set position and rotation on the client side
                this.setPositionAndRotation(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
            }
        }
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
        if (!this.isBeingRidden()) {
            super.fall(distance, damageMultiplier);
        }
    }

    @Override
    protected PathNavigate createNavigator(World world) {
        if (this.state != null && this.state.canFly.get()) {
            return new PathNavigateFlying(this, world);
        }
        return new PathNavigateGround(this, world);
    }

    @Override
    protected void initEntityAI() {
        super.initEntityAI();

        this.tasks.taskEntries.clear();
        this.targetTasks.taskEntries.clear();

        double speed = 1D;

        if (this.state != null) {
            speed = this.state.speed.get();

            if (this.state.canSwim.get()) {
                this.tasks.addTask(0, new EntityAISwimming(this));
            }

            if (!this.state.follow.get().isEmpty()) {
                this.tasks.addTask(6, new EntityAIFollowTarget(this, speed, 2, 10));
            } else if (this.state.hasPost.get() && this.state.postPosition != null) {
                this.tasks.addTask(6, new EntityAIReturnToPost(this, this.state.postPosition, speed, this.state.postRadius.get()));
            } else if (!this.state.patrol.isEmpty()) {
                this.tasks.addTask(6, new EntityAIPatrol(this));
            }

            if (this.state.lookAround.get()) {
                this.tasks.addTask(8, new EntityAILookIdle(this));
            }

            if (this.state.lookAtPlayer.get()) {
                this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityPlayer.class, this.state.pathDistance.get(), 1.0F));
            }

            if (this.state.wander.get()) {
                this.tasks.addTask(9, new EntityAIWanderAvoidWater(this, speed / 2D));
            }

            if (this.state.canFly.get()) {
                this.moveHelper = new FlyingMoveHelper(this);
                this.tasks.addTask(10, new EntityAINpcFly(this));
            } else if (this.state.alwaysWander.get()) {
                this.tasks.addTask(10, new EntityAIAlwaysWander(this, speed / 2D));
            }
        }

        this.targetTasks.addTask(1, targetAI = new EntityAIHurtByTargetNpc(this, false));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityLivingBase.class, 10, true, false, this::targetCheck));

        if (this.state != null) {
            this.tasks.addTask(4, new EntityAIAttackNpcMelee(this, speed, false, this.state.damageDelay.get()));
        }
    }

    private boolean targetCheck(EntityLivingBase entity) {
        if (this.isEntityOutOfPostDistance(entity)) {
            return false;
        }

        Faction faction = this.getFaction();

        if (entity instanceof EntityNpc) {
            EntityNpc npc = (EntityNpc) entity;

            if (faction != null) {
                return faction.get(npc.getState().faction.get()) == FactionAttitude.AGGRESSIVE;
            }
        }

        if (entity instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) entity;

            if (player.isSpectator() || player.isCreative()) {
                return false;
            }

            FactionAttitude attitude = this.getPlayerAttitude(faction, entity);

            if (attitude != null) {
                return attitude == FactionAttitude.AGGRESSIVE;
            }
        }

        return faction != null && faction.othersAttitude == FactionAttitude.AGGRESSIVE;
    }

    private FactionAttitude getPlayerAttitude(Faction faction, Entity entity) {
        if (entity instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) entity;
            ICharacter character = Character.get(player);

            if (faction != null && character != null) {
                return faction.get(character.getStates());
            }
        }

        return null;
    }

    public void initialize() {
        this.state.triggerInitialize.trigger(this);
    }

    /* Getter and setters */

    public States getStates() {
        return this.state.states;
    }

    public Faction getFaction() {
        if (this.faction == null) {
            String faction = this.state.faction.get();

            this.faction = faction.isEmpty() ? null : Mappet.factions.load(faction);
        }

        return this.faction;
    }

    public void setNpc(Npc npc, NpcState state) {
        this.setState(state, false);

        if (this.state.id.get().isEmpty()) {
            this.state.id.set(npc.getId());
        }
    }

    public String getId() {
        return this.state.id.get();
    }

    public NpcState getState() {
        return this.state;
    }

    public void setState(NpcState state) {
        setState(state, true);
    }

    public void setState(NpcState state, boolean notify) {

        this.state = new NpcState();
        this.state.deserializeNBT(state.serializeNBT());

        /* Set */
        this.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(this.state.pathDistance.get());
        this.navigator = this.createNavigator(this.world);

        /* Set health */
        double max = this.getMaxHealth();
        double health = this.getHealth();

        this.setMaxHealth(state.maxHealth.get());
        this.setHealth((float) MathHelper.clamp(state.maxHealth.get() * (health / max), 1, state.maxHealth.get()));

        this.isImmuneToFire = !this.state.canGetBurned.get();
        this.experienceValue = this.state.xp.get();

        /* Morphing */
        this.morph.set(state.morph);

        if (notify) this.sendNpcStateChangePacket();

        this.faction = null;
        this.initEntityAI();
    }

    public void sendNpcStateChangePacket() {
        if (this.world instanceof WorldServer) {
            Dispatcher.sendToTracked(this, new PacketNpcStateChange(this));
        }
    }

    @Override
    public AbstractMorph getMorph() {
        return this.morph.get();
    }


    public EntityLivingBase getFollowTarget() {
        if (this.state.follow.get().isEmpty()) {
            return null;
        }

        if (this.state.follow.get().equals("@r")) {
            List<EntityPlayer> players = this.world.playerEntities;
            int index = MathHelper.clamp((int) (Math.random() * players.size() - 1), 0, players.size() - 1);

            return players.isEmpty() ? null : players.get(index);
        }

        if (this.state.follow.get().startsWith("@")) {
            try {
                ICommandSender sender = CommandNpc.getCommandSender(this);
                List<Entity> entities = EntitySelector.matchEntities(sender, this.state.follow.get(), Entity.class);
                for (Entity entity : entities) {
                    if (entity instanceof EntityLivingBase) {
                        return (EntityLivingBase) entity;
                    }
                }
            } catch (Exception e) {
                Mappet.logger.error(e.getMessage());
            }
        } else {
            try {
                EntityPlayer player = this.world.getPlayerEntityByName(this.state.follow.get());
                return player == null ? this.world.getPlayerEntityByUUID(UUID.fromString(this.state.follow.get())) : player;
            } catch (Exception e) {
                Mappet.logger.error(e.getMessage());
            }
        }

        return null;
    }

    public void setMorph(AbstractMorph morph) {
        this.morph.set(morph);
        this.state.morph = morph;
    }

    public void setMaxHealth(double value) {
        this.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(value);
    }

    /* Other stuff */

    @Override
    public void onUpdate() {
        if (this.lastTarget != this.getAttackTarget()) {
            this.lastTarget = this.getAttackTarget();

            this.state.triggerTarget.trigger(new DataContext(this, this.lastTarget));
        }

        this.healthFailsafe();
        this.updateAttackTarget();

        super.onUpdate();

        this.updateArmSwingProgress();

        if (!this.morph.isEmpty()) {
            this.morph.get().update(this);
        }

        if (this.state.regenDelay.get() > 0 && !this.world.isRemote) {
            int regen = this.state.regenFrequency.get() == 0 ? 1 : this.state.regenFrequency.get();

            if (this.lastDamageTime >= this.state.regenDelay.get() && this.ticksExisted % regen == 0) {
                if (this.getHealth() > 0 && this.getHealth() < this.getMaxHealth()) {
                    this.heal(1F);
                }
            }

            this.lastDamageTime += 1;
        }

        if (this.world.isRemote) {
            this.prevSmoothYawHead = this.smoothYawHead;
            this.smoothYawHead = Interpolations.lerpYaw(this.smoothYawHead, this.rotationYawHead, 0.5F);
            this.prevSmoothBodyYawHead = this.smoothBodyYawHead;
            this.smoothBodyYawHead = Interpolations.lerpYaw(this.smoothBodyYawHead, this.renderYawOffset, 0.5F);
        } else {
            this.state.triggerTick.trigger(this);
        }
    }

    /**
     * If player's attitude has switched, then NPC should stop chasing
     */
    private boolean isEntityOutOfPostDistance(Entity entity) {
        if (this.state == null || this.state.postPosition == null || !this.state.hasPost.get()) {
            return false;
        }

        BlockPos post = this.state.postPosition;
        BlockPos position = entity.getPosition();
        double distance = post.distanceSq(position);

        return distance > this.state.fallback.get() * this.state.fallback.get();
    }

    private void updateAttackTarget() {
        if (this.state != null && this.getAttackTarget() != null && this.state.postPosition != null && this.state.hasPost.get() && this.isEntityOutOfPostDistance(this.getAttackTarget())) {
            this.targetAI.reset = true;
            this.setAttackTarget(null);
        }

        if (this.faction == null || this.ticksExisted % 10 != 0) {
            return;
        }

        Entity entity = this.getAttackTarget();

        if (entity instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) entity;
            Faction faction = this.getFaction();
            FactionAttitude attitude = this.getPlayerAttitude(faction, player);

            if (attitude == FactionAttitude.FRIENDLY || player.isCreative()) {
                this.setAttackTarget(null);
            }
        }
    }

    @Override
    protected void onDeathUpdate() {
        if (this.state.killable.get() || !this.unkillableFailsafe) {
            super.onDeathUpdate();
        }
    }

    @Override
    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        for (NpcDrop drop : this.state.drops) {
            if (this.rand.nextFloat() < drop.chance) {
                this.entityDropItem(drop.stack.copy(), 0F);
            }
        }
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        DamageSource source = Mappet.npcsPeacefulDamage.get() ? new NpcDamageSource(this) : DamageSource.causeMobDamage(this);

        entityIn.attackEntityFrom(source, this.state.damage.get());

        return super.attackEntityAsMob(entityIn);
    }

    @Override
    protected void damageEntity(DamageSource damage, float damageAmount) {
        super.damageEntity(damage, damageAmount);

        if (!this.isEntityInvulnerable(damage)) {
            this.lastDamageTime = 0;
        }

        this.healthFailsafe();
        DataContext context = new DataContext(this, damage.getTrueSource());
        context.set("damage", damageAmount);
        context.set("damageType", damage.getDamageType());
        this.state.triggerDamaged.trigger(context);
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        if (this.world.isRemote) {
            return true;
        }

        Entity entity = source.getTrueSource();

        if (entity instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) entity;
            Faction faction = this.getFaction();
            FactionAttitude attitude = this.getPlayerAttitude(faction, player);

            if (attitude == FactionAttitude.FRIENDLY && !player.isCreative()) {
                return true;
            }
        }

        if (this.state.invincible.get()) {
            return !(source.isCreativePlayer() || source == DamageSource.OUT_OF_WORLD);
        }

        if (!this.state.canFallDamage.get() && source == DamageSource.FALL) {
            return true;
        }

        return super.isEntityInvulnerable(source);
    }

    @Override
    public void onKillCommand() {
        this.unkillableFailsafe = false;

        super.onKillCommand();
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        if (this.state.respawn.get() && !this.dieOnLoad) {
            MappetNpcRespawnManager respawnManager = MappetNpcRespawnManager.get(this.world);

            respawnManager.addDiedNpc(this);
            this.dieOnLoad = true;
        }
        this.state.triggerDied.trigger(this, cause.getTrueSource());
    }

    @Override
    protected boolean canDespawn() {
        return this.state.unique.get();
    }

    public void healthFailsafe() {
        if (!this.state.killable.get() && this.getHealth() <= 0 && this.unkillableFailsafe) {
            this.setHealth(0.001F);
        }
    }

    @Override
    protected boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (!this.world.isRemote) {
            if (!player.getHeldItem(hand).interactWithEntity(player, this, hand)) {
                this.state.triggerInteract.trigger(new DataContext(this, player));
            }

            // Start riding the NPC when interacted with
            if (
                    ((this.getPassengers().size() < this.state.steeringOffset.size()) || this.state.steeringOffset.isEmpty()) &&
                            this.canBeSteered() &&
                            !(player.getHeldItem(hand).getItem() instanceof ItemNpcTool)
            ) {
                player.startRiding(this, true);
            }
        }

        return true;
    }

    /* NBT (de)serialization */

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        tag.setBoolean("DieOnLoad", this.dieOnLoad);

        /*
         * Do not load data to NBT, if NPC has to die
         * Prevents repeated quest trigger and drop.
         */

        if (!this.dieOnLoad) {
            super.writeEntityToNBT(tag);

            tag.setTag("State", this.state.serializeNBT());
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);

        NpcState state = new NpcState();

        state.deserializeNBT(tag.getCompoundTag("State"));

        /* gamma -> public alpha */
        if (tag.hasKey("States")) {
            state.states.deserializeNBT(tag.getCompoundTag("States"));
        }

        this.setState(state, false);

        if (tag.hasKey("NpcId")) {
            state.id.set(tag.getString("NpcId"));
        }

        if (tag.hasKey("DieOnLoad") && tag.getBoolean("DieOnLoad")) {
            this.setDead();
        }
    }

    /* Network (de)serialization */

    @Override
    public void writeSpawnData(ByteBuf buf) {
        MorphUtils.morphToBuf(buf, this.morph.get());
        this.state.writeToBuf(buf);
    }

    @Override
    public void readSpawnData(ByteBuf buf) {
        this.morph.setDirect(MorphUtils.morphFromBuf(buf));
        this.state.readFromBuf(buf);

        this.prevRotationYawHead = this.rotationYawHead;
        this.smoothYawHead = this.rotationYawHead;
    }

    /* Client stuff */

    /**
     * Is actor in range in render distance
     * <p>
     * This method is responsible for checking if this entity is
     * available for rendering. Rendering range is configurable.
     */
    @SideOnly(Side.CLIENT)
    @Override
    public boolean isInRangeToRenderDist(double distance) {
        double d0 = this.getEntityBoundingBox().getAverageEdgeLength();

        if (Double.isNaN(d0)) {
            d0 = 1.0D;
        }

        d0 = d0 * RENDER_DISTANCE;

        return distance < d0 * d0;
    }

    public void setStringInData(String key, String value) {
        INBTCompound fullData = new ScriptNBTCompound(this.writeToNBT(new NBTTagCompound()));
        fullData.getCompound("State").setString(key, value);
        this.readFromNBT(fullData.asMinecraft());
    }

    @Nullable
    public AxisAlignedBB getCollisionBoundingBox()
    {
        return isEntityAlive() && state.collision.get() ? getEntityBoundingBox() : null;
    }
}
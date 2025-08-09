package mchorse.mappet.entities;

import io.netty.buffer.ByteBuf;
import mchorse.mappet.Mappet;
import mchorse.mappet.MappetConfig;
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
     * Needs to fix a clone issue, when npc dies and you quickly reload world
     */
    public boolean dieOnLoad = false;

    public EntityNpc(World worldIn) {
        super(worldIn);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();

        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3125D);
    }

    @Override
    public void applyEntityCollision(Entity entityIn) {
        if (!state.immovable.get()) {
            super.applyEntityCollision(entityIn);
        }
    }

    @Override
    protected void collideWithEntity(Entity entityIn) {
        if (!state.immovable.get()) {
            super.collideWithEntity(entityIn);
        }

        if (world.isRemote) {
            return;
        }

        // Call the trigger for entity collision
        state.triggerEntityCollision.trigger(new DataContext(this, entityIn));
    }

    @Override
    public boolean hasNoGravity() {
        return state.hasNoGravity.get();
    }

    @Override
    public boolean canPickUpLoot() {
        return state.canPickUpLoot.get();
    }

    @Override
    public boolean canBeSteered() {
        return state.canBeSteered.get();
    }

    @Override
    public void updatePassenger(Entity passenger) {
        if (isPassenger(passenger)) {
            int index = getPassengers().indexOf(passenger);

            BlockPos offsetPos;
            if (state.steeringOffset.isEmpty() || index >= state.steeringOffset.size()) {
                offsetPos = new BlockPos(0, 0, 0); // default offset
            }
            else {
                offsetPos = state.steeringOffset.get(index);
            }

            double offsetX = offsetPos.getX();
            double offsetY = posY - 0.5 + EntityUtils.getHeight(this) + offsetPos.getY();
            double offsetZ = offsetPos.getZ();

            // Convert bodyYaw to radians as Java Math functions expect arguments in radians
            double bodyYaw = Math.toRadians(renderYawOffset);

            // Rotate the offset vector by the entity's bodyYaw
            double rotatedOffsetX = offsetX * Math.cos(bodyYaw) - offsetZ * Math.sin(bodyYaw);
            double rotatedOffsetZ = offsetX * Math.sin(bodyYaw) + offsetZ * Math.cos(bodyYaw);

            // Add the rotated offset to the entity's position
            double finalPosX = posX + rotatedOffsetX;
            double finalPosZ = posZ + rotatedOffsetZ;

            // Update the passenger's position on both the server and the client
            passenger.setPosition(finalPosX, offsetY, finalPosZ);

            // Check side and execute appropriate code
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
                // This is a client
                passenger.setPositionAndRotationDirect(finalPosX, offsetY, finalPosZ, passenger.rotationYaw, passenger.rotationPitch, 3, true);
            }
            else {
                // This is a server
                passenger.setPosition(finalPosX, offsetY, finalPosZ);
            }

            // Always align this entity's rotation with the passenger
            rotationYaw = passenger.rotationYaw;
            rotationPitch = passenger.rotationPitch;
        }

        // Check if the passenger is a player and the entity can be steered and only allow the first passenger to steer it.
        if (passenger instanceof EntityPlayer && canBeSteered() && getPassengers().indexOf(passenger) == getPassengers().size() - 1) {
            handleSteering((EntityPlayer) passenger);
        }
    }

    private void handleSteering(EntityPlayer player) {
        if (!world.isRemote) {
            float forward = player.moveForward;
            float strafe = player.moveStrafing;
            rotationYaw = player.rotationYaw;
            rotationYawHead = player.rotationYawHead;

            if (forward != 0 || strafe != 0) {
                float speed = state.speed.get() / 15;

                // Calculate motion based on player input
                double motionX = -Math.sin(Math.toRadians(rotationYaw)) * forward + Math.cos(Math.toRadians(rotationYaw)) * strafe;
                double motionZ = Math.cos(Math.toRadians(rotationYaw)) * forward + Math.sin(Math.toRadians(rotationYaw)) * strafe;

                // Normalize motion vector
                double motionMagnitude = Math.sqrt(motionX * motionX + motionZ * motionZ);
                motionX /= motionMagnitude;
                motionZ /= motionMagnitude;

                // Apply speed
                this.motionX = motionX * speed;
                this.motionZ = motionZ * speed;

                // Use the move method to handle collisions and movement more accurately
                move(MoverType.SELF, this.motionX, motionY, this.motionZ);

                // Set position and rotation on the client side
                setPositionAndRotation(posX, posY, posZ, rotationYaw, rotationPitch);
            }
        }
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
        if (!isBeingRidden()) {
            super.fall(distance, damageMultiplier);
        }
    }

    @Override
    protected PathNavigate createNavigator(World world) {
        if (state != null && state.canFly.get()) {
            return new PathNavigateFlying(this, world);
        }
        return new PathNavigateGround(this, world);
    }

    @Override
    protected void initEntityAI() {
        super.initEntityAI();

        tasks.taskEntries.clear();
        targetTasks.taskEntries.clear();

        double speed = 1D;

        if (state != null) {
            speed = state.speed.get();

            if (state.canSwim.get()) {
                tasks.addTask(0, new EntityAISwimming(this));
            }

            if (!state.follow.get().isEmpty()) {
                tasks.addTask(6, new EntityAIFollowTarget(this, speed, 2, 10));
            }
            else if (state.hasPost.get() && state.postPosition != null) {
                tasks.addTask(6, new EntityAIReturnToPost(this, state.postPosition, speed, state.postRadius.get()));
            }
            else if (!state.patrol.isEmpty()) {
                tasks.addTask(6, new EntityAIPatrol(this));
            }

            if (state.lookAround.get()) {
                tasks.addTask(8, new EntityAILookIdle(this));
            }

            if (state.lookAtPlayer.get()) {
                tasks.addTask(9, new EntityAIWatchClosest2(this, EntityPlayer.class, state.pathDistance.get(), 1.0F));
            }

            if (state.wander.get()) {
                tasks.addTask(9, new EntityAIWanderAvoidWater(this, speed / 2D));
            }

            if (state.canFly.get()) {
                moveHelper = new FlyingMoveHelper(this);
                tasks.addTask(10, new EntityAINpcFly(this));
            }
            else if (state.alwaysWander.get()) {
                tasks.addTask(10, new EntityAIAlwaysWander(this, speed / 2D));
            }
        }

        targetTasks.addTask(1, targetAI = new EntityAIHurtByTargetNpc(this, false));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityLivingBase.class, 10, true, false, this::targetCheck));

        if (state != null) {
            tasks.addTask(4, new EntityAIAttackNpcMelee(this, speed, false, state.damageDelay.get()));
        }
    }

    private boolean targetCheck(EntityLivingBase entity) {
        if (isEntityOutOfPostDistance(entity)) {
            return false;
        }

        Faction faction = getFaction();

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

            FactionAttitude attitude = getPlayerAttitude(faction, entity);

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
        state.triggerInitialize.trigger(this);
    }

    /* Getter and setters */

    public States getStates() {
        return state.states;
    }

    public Faction getFaction() {
        if (faction == null) {
            String faction = state.faction.get();

            this.faction = faction.isEmpty() ? null : Mappet.factions.load(faction);
        }

        return faction;
    }

    public void setNpc(Npc npc, NpcState state) {
        setState(state, false);

        if (this.state.id.get().isEmpty()) {
            this.state.id.set(npc.getId());
        }
    }

    public String getId() {
        return state.id.get();
    }

    public NpcState getState() {
        return state;
    }

    public void setState(NpcState state) {
        setState(state, true);
    }

    public void setState(NpcState state, boolean notify) {

        this.state = new NpcState();
        this.state.deserializeNBT(state.serializeNBT());

        /* Set */
        getAttributeMap().getAttributeInstance(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(this.state.pathDistance.get());
        navigator = createNavigator(world);

        /* Set health */
        double max = getMaxHealth();
        double health = getHealth();

        setMaxHealth(state.maxHealth.get());
        setHealth((float) MathHelper.clamp(state.maxHealth.get() * (health / max), 1, state.maxHealth.get()));

        isImmuneToFire = !this.state.canGetBurned.get();
        experienceValue = this.state.xp.get();

        /* Morphing */
        morph.set(state.morph);

        if (notify) sendNpcStateChangePacket();

        faction = null;
        initEntityAI();
    }

    public void sendNpcStateChangePacket() {
        if (world instanceof WorldServer) {
            Dispatcher.sendToTracked(this, new PacketNpcStateChange(this));
        }
    }

    @Override
    public AbstractMorph getMorph() {
        return morph.get();
    }


    public EntityLivingBase getFollowTarget() {
        if (state.follow.get().isEmpty()) {
            return null;
        }

        if (state.follow.get().equals("@r")) {
            List<EntityPlayer> players = world.playerEntities;
            int index = MathHelper.clamp((int) (Math.random() * players.size() - 1), 0, players.size() - 1);

            return players.isEmpty() ? null : players.get(index);
        }

        if (state.follow.get().startsWith("@")) {
            try {
                ICommandSender sender = CommandNpc.getCommandSender(this);
                List<Entity> entities = EntitySelector.matchEntities(sender, state.follow.get(), Entity.class);
                for (Entity entity : entities) {
                    if (entity instanceof EntityLivingBase) {
                        return (EntityLivingBase) entity;
                    }
                }
            } catch (Exception e) {
                Mappet.logger.error(e.getMessage());
            }
        }
        else {
            try {
                EntityPlayer player = world.getPlayerEntityByName(state.follow.get());
                return player == null ? world.getPlayerEntityByUUID(UUID.fromString(state.follow.get())) : player;
            } catch (Exception e) {
                Mappet.logger.error(e.getMessage());
            }
        }

        return null;
    }

    public void setMorph(AbstractMorph morph) {
        this.morph.set(morph);
        state.morph = morph;
    }

    public void setMaxHealth(double value) {
        getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(value);
    }

    /* Other stuff */

    @Override
    public void onUpdate() {
        if (lastTarget != getAttackTarget()) {
            lastTarget = getAttackTarget();

            state.triggerTarget.trigger(new DataContext(this, lastTarget));
        }

        healthFailsafe();
        updateAttackTarget();

        super.onUpdate();

        updateArmSwingProgress();

        if (!morph.isEmpty()) {
            morph.get().update(this);
        }

        if (state.regenDelay.get() > 0 && !world.isRemote) {
            int regen = state.regenFrequency.get() == 0 ? 1 : state.regenFrequency.get();

            if (lastDamageTime >= state.regenDelay.get() && ticksExisted % regen == 0) {
                if (getHealth() > 0 && getHealth() < getMaxHealth()) {
                    heal(1F);
                }
            }

            lastDamageTime += 1;
        }

        if (world.isRemote) {
            prevSmoothYawHead = smoothYawHead;
            smoothYawHead = Interpolations.lerpYaw(smoothYawHead, rotationYawHead, 0.5F);
            prevSmoothBodyYawHead = smoothBodyYawHead;
            smoothBodyYawHead = Interpolations.lerpYaw(smoothBodyYawHead, renderYawOffset, 0.5F);
        }
        else {
            state.triggerTick.trigger(this);
        }
    }

    /**
     * If player's attitude has switched, then NPC should stop chasing
     */
    private boolean isEntityOutOfPostDistance(Entity entity) {
        if (state == null || state.postPosition == null || !state.hasPost.get()) {
            return false;
        }

        BlockPos post = state.postPosition;
        BlockPos position = entity.getPosition();
        double distance = post.distanceSq(position);

        return distance > state.fallback.get() * state.fallback.get();
    }

    private void updateAttackTarget() {
        if (state != null && getAttackTarget() != null && state.postPosition != null && state.hasPost.get() && isEntityOutOfPostDistance(getAttackTarget())) {
            targetAI.reset = true;
            setAttackTarget(null);
        }

        if (faction == null || ticksExisted % 10 != 0) {
            return;
        }

        Entity entity = getAttackTarget();

        if (entity instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) entity;
            Faction faction = getFaction();
            FactionAttitude attitude = getPlayerAttitude(faction, player);

            if (attitude == FactionAttitude.FRIENDLY || player.isCreative()) {
                setAttackTarget(null);
            }
        }
    }

    @Override
    protected void onDeathUpdate() {
        if (state.killable.get() || !unkillableFailsafe) {
            super.onDeathUpdate();
        }
    }

    @Override
    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        for (NpcDrop drop : state.drops) {
            if (rand.nextFloat() < drop.chance) {
                entityDropItem(drop.stack.copy(), 0F);
            }
        }
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        DamageSource source = MappetConfig.npcsPeacefulDamage.get() ? new NpcDamageSource(this) : DamageSource.causeMobDamage(this);

        entityIn.attackEntityFrom(source, state.damage.get());

        return super.attackEntityAsMob(entityIn);
    }

    @Override
    protected void damageEntity(DamageSource damage, float damageAmount) {
        super.damageEntity(damage, damageAmount);

        if (!isEntityInvulnerable(damage)) {
            lastDamageTime = 0;
        }

        healthFailsafe();
        DataContext context = new DataContext(this, damage.getTrueSource());
        context.set("damage", damageAmount);
        context.set("damageType", damage.getDamageType());
        state.triggerDamaged.trigger(context);
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        if (world.isRemote) {
            return true;
        }

        Entity entity = source.getTrueSource();

        if (entity instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) entity;
            Faction faction = getFaction();
            FactionAttitude attitude = getPlayerAttitude(faction, player);

            if (attitude == FactionAttitude.FRIENDLY && !player.isCreative()) {
                return true;
            }
        }

        if (state.invincible.get()) {
            return !(source.isCreativePlayer() || source == DamageSource.OUT_OF_WORLD);
        }

        if (!state.canFallDamage.get() && source == DamageSource.FALL) {
            return true;
        }

        return super.isEntityInvulnerable(source);
    }

    @Override
    public void onKillCommand() {
        unkillableFailsafe = false;

        super.onKillCommand();
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        if (state.respawn.get() && !dieOnLoad) {
            MappetNpcRespawnManager respawnManager = MappetNpcRespawnManager.get(world);

            respawnManager.addDiedNpc(this);
            dieOnLoad = true;
        }
        state.triggerDied.trigger(this, cause.getTrueSource());
    }

    @Override
    protected boolean canDespawn() {
        return state.unique.get();
    }

    public void healthFailsafe() {
        if (!state.killable.get() && getHealth() <= 0 && unkillableFailsafe) {
            setHealth(0.001F);
        }
    }

    @Override
    protected boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (!world.isRemote) {
            if (!player.getHeldItem(hand).interactWithEntity(player, this, hand)) {
                state.triggerInteract.trigger(new DataContext(this, player));
            }

            // Start riding the NPC when interacted with
            if ((getPassengers().size() < state.steeringOffset.size() || state.steeringOffset.isEmpty()) && canBeSteered() && !(player
                    .getHeldItem(hand)
                    .getItem() instanceof ItemNpcTool)) {
                player.startRiding(this, true);
            }
        }

        return true;
    }

    /* NBT (de)serialization */

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        tag.setBoolean("DieOnLoad", dieOnLoad);

        /*
         * Do not load data to NBT, if NPC has to die
         * Prevents repeated quest trigger and drop.
         */

        if (!dieOnLoad) {
            super.writeEntityToNBT(tag);

            tag.setTag("State", state.serializeNBT());
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

        setState(state, false);

        if (tag.hasKey("NpcId")) {
            state.id.set(tag.getString("NpcId"));
        }

        if (tag.hasKey("DieOnLoad") && tag.getBoolean("DieOnLoad")) {
            setDead();
        }
    }

    /* Network (de)serialization */

    @Override
    public void writeSpawnData(ByteBuf buf) {
        MorphUtils.morphToBuf(buf, morph.get());
        state.writeToBuf(buf);
    }

    @Override
    public void readSpawnData(ByteBuf buf) {
        morph.setDirect(MorphUtils.morphFromBuf(buf));
        state.readFromBuf(buf);

        prevRotationYawHead = rotationYawHead;
        smoothYawHead = rotationYawHead;
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
        double d0 = getEntityBoundingBox().getAverageEdgeLength();

        if (Double.isNaN(d0)) {
            d0 = 1.0D;
        }

        d0 = d0 * RENDER_DISTANCE;

        return distance < d0 * d0;
    }

    public void setStringInData(String key, String value) {
        INBTCompound fullData = new ScriptNBTCompound(writeToNBT(new NBTTagCompound()));
        fullData.getCompound("State").setString(key, value);
        readFromNBT(fullData.asMinecraft());
    }

    @Nullable
    public AxisAlignedBB getCollisionBoundingBox() {
        return isEntityAlive() && state.collision.get() ? getEntityBoundingBox() : null;
    }
}
package mchorse.mappet.api.npcs;

import com.google.common.base.CaseFormat;
import io.netty.buffer.ByteBuf;
import mchorse.mappet.api.states.FactionStates;
import mchorse.mappet.api.states.ScriptStates;
import mchorse.mappet.api.states.StatesProvider;
import mchorse.mappet.api.triggers.Trigger;
import mchorse.mappet.entities.EntityNpc;
import mchorse.mappet.utils.NBTUtils;
import mchorse.mappet.utils.NpcStateUtils;
import mchorse.mclib.config.values.GenericValue;
import mchorse.mclib.config.values.Value;
import mchorse.mclib.config.values.ValueBoolean;
import mchorse.mclib.config.values.ValueDouble;
import mchorse.mclib.config.values.ValueFloat;
import mchorse.mclib.config.values.ValueInt;
import mchorse.mclib.config.values.ValueString;
import mchorse.mclib.utils.ValueSerializer;
import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NpcState implements INBTSerializable<NBTTagCompound>
{

    /* Meta */
    public ValueString stateName = new ValueString("StateName", "");

    public ValueString id = new ValueString("Id", "");

//    public ScriptStates states = new ScriptStates();
//    public FactionStates factions = new FactionStates();

    public StatesProvider<EntityNpc> states = new StatesProvider<>(new ScriptStates(), null, null, new FactionStates());

    /**
     * Unique
     */
    public ValueBoolean unique = new ValueBoolean("Unique");

    /**
     * Path distance
     */
    public ValueFloat pathDistance = new ValueFloat("PathDistance", 32);

    /* Health */

    /**
     * Max health an NPC can have
     */
    public ValueFloat maxHealth = new ValueFloat("MaxHealth", 20);

    /**
     * The initial health
     */
    public ValueFloat health = new ValueFloat("Health", 20);

    /**
     * Regeneration delay (in ticks)
     */
    public ValueInt regenDelay = new ValueInt("RegenDelay", 80);

    /**
     * How frequently will an NPC regenerate one heart (in ticks)
     */
    public ValueInt regenFrequency = new ValueInt("RegenFrequency", 20);

    /* Damage */

    /**
     * What is this NPC's melee damage
     */
    public ValueFloat damage = new ValueFloat("Damage", 2F);

    /**
     * NPC attack delay
     */
    public ValueInt damageDelay = new ValueInt("DamageDelay", 10);

    /**
     * Whether this NPC can use ranged weapons
     */
    public ValueBoolean canRanged = new ValueBoolean("CanRanged");

    /**
     * Whether this NPC is resistible to fall damage
     */
    public ValueBoolean canFallDamage = new ValueBoolean("CanFallDamage", true);

    /**
     * Whether this NPC is resistible to fire damage
     */
    public ValueBoolean canGetBurned = new ValueBoolean("CanGetBurned", true);

    /**
     * Whether this NPC can be damaged
     */
    public ValueBoolean invincible = new ValueBoolean("Invincible");

    /**
     * If false, then NPCs can be killed only by a command.
     * Regardless of the state, killable allows to make this NPC damaged
     * until 0 health.
     */
    public ValueBoolean killable = new ValueBoolean("Killable", true);

    /* Movement */

    /**
     * What is NPC's movement speed
     */
    public ValueFloat speed = new ValueFloat("Speed", 3F);

    /**
     * NPC's jumping power when it's steered
     */
    public ValueFloat jumpPower = new ValueFloat("JumpPower", 0.6F);

    /**
     * Can NPC move around in the water
     */
    public ValueBoolean canSwim = new ValueBoolean("CanSwim",true);

    /**
     * Can NPC be pushed by players moving through an NPC
     */
    public ValueBoolean immovable = new ValueBoolean("Immovable");

    public ValueBoolean collision = new ValueBoolean("Collision");

    /**
     * Whether post options
     */
    public ValueBoolean hasPost = new ValueBoolean("HasPost");

    /**
     * Block position where the NPC must stay on the post
     */
    public BlockPos postPosition; //TODO make it work with ValueAPI

    /**
     * What distance away from the post will NPC will return into the
     * radius. This could be used if wandering option wandered NPC out of
     * the radius
     */
    public ValueFloat postRadius = new ValueFloat("PostRadius", 1F);

    /**
     * After what distance from the post or patrol point the NPC will
     * stop chasing and come back
     */
    public ValueFloat fallback = new ValueFloat("Fallback", 15F);

    /**
     * Whether patrol points should circulate rather than ping-pong
     */
    public ValueBoolean patrolCirculate = new ValueBoolean("PatrolCirculate");

    /**
     * List of position through which an NPC must patrol through
     */
    public List<BlockPos> patrol = new ArrayList<>(); //TODO make it work with ValueAPI

    /**
     * List of triggers on each patrol point
     */
    public List<Trigger> patrolTriggers = new ArrayList<>(); //TODO make it work with ValueAPI

    /**
     * List of NPC's x-offset when steered
     */
    public List<BlockPos> steeringOffset = new ArrayList<>(); //TODO make it work with ValueAPI

    /**
     * The UUID of the player that must be followed
     */
    public ValueString follow = new ValueString("Follow", "");

    /* General */

    /**
     * To which faction does this NPC belongs to
     */
    public ValueString faction = new ValueString("Faction", "");

    /**
     * How does the NPC looks like
     */
    public AbstractMorph morph; //TODO make it work with ValueAPI

    /**
     * Sight distance, is how far away can an NPC find a target to attack
     */
    public ValueFloat sightDistance = new ValueFloat("SightDistance", 25F);

    /**
     * Sight radius determines the angle of how narrow does the NPC has to look
     * in the direction of potential targets
     */
    public ValueFloat sightRadius = new ValueFloat("SightRadius", 120F);

    /**
     * What kind of items will the NPC is going to drop
     */
    public List<NpcDrop> drops = new ArrayList<>(); //TODO make it work with ValueAPI

    /**
     * How much XP drops an NPC after getting killed
     */
    public ValueInt xp = new ValueInt("Xp", 0);

    /**
     * NPC shadow size
     */
    public ValueFloat shadowSize = new ValueFloat("ShadowSize", 0.6F);

    /* Behavior */

    /**
     * Whether NPC looks at a player when it
     */
    public ValueBoolean lookAtPlayer = new ValueBoolean("LookAtPlayer");

    /**
     * Whether NPC pointlessly looks around in its idle state
     */
    public ValueBoolean lookAround = new ValueBoolean("LookAround");

    /**
     * NPCs can go around in different direction, this property
     * sets its maximum distance it can wander off at a time
     */
    public ValueBoolean wander = new ValueBoolean("Wander");

    /**
     * Whether NPC should always wander
     */
    public ValueBoolean alwaysWander = new ValueBoolean("AlwaysWander");

    /**
     * Whether the NPC should be able to fly. (experimental)
     */
    public ValueBoolean canFly = new ValueBoolean("CanFly");

    /**
     * Flight max height
     */
    public ValueDouble flightMaxHeight = new ValueDouble("FlightMaxHeight", 6.0D);

    /**
     * Flight min height
     */
    public ValueDouble flightMinHeight = new ValueDouble("FlightMinHeight", 4.0D);

    /**
     * The health threshold until NPC starts to run away
     */
    public ValueFloat flee = new ValueFloat("Flee", 4F);

    /**
     * NPC will pick up items if this option is enabled and Gamerule canPickupItems is true
     */
    public ValueBoolean canPickUpLoot = new ValueBoolean("CanPickUpLoot");

    /**
     * Whether NPC has gravity or not
     */
    public ValueBoolean hasNoGravity = new ValueBoolean("HasNoGravity", false);

    /**
     * Whether NPC can be steered by a player
     */
    public ValueBoolean canBeSteered = new ValueBoolean("CanBeSteered", false);

    /* Triggers */

    public Trigger triggerDied = new Trigger();
    public Trigger triggerDamaged = new Trigger();
    public Trigger triggerInteract = new Trigger();
    public Trigger triggerTick = new Trigger();
    public Trigger triggerTarget = new Trigger();
    public Trigger triggerInitialize = new Trigger();
    public Trigger triggerRespawn = new Trigger();
    public Trigger triggerEntityCollision = new Trigger();

    /* Respawn */

    /**
     * Is NPC respawns with provided delay
     */
    public ValueBoolean respawn = new ValueBoolean("Respawn");

    /**
     * Delay to respawn
     */
    public ValueInt respawnDelay = new ValueInt("RespawnDelay");

    /**
     * Whether NPC respawns at the specified coordinates
     */
    public ValueBoolean respawnOnCoordinates = new ValueBoolean("RespawnOnCoordinates");

    /**
     * X coordinate to respawn
     */
    public ValueDouble respawnPosX = new ValueDouble("RespawnPosX");

    /**
     * Y coordinate to respawn
     */
    public ValueDouble respawnPosY = new ValueDouble("RespawnPosY");

    /**
     * Z coordinate to respawn
     */
    public ValueDouble respawnPosZ = new ValueDouble("RespawnPosZ");

    /**
     * Whether NPC respawns with same UUID
     */
    public ValueBoolean respawnSaveUUID = new ValueBoolean("RespawnSaveUUID");

    public ValueSerializer serializer;
    public Map<String, GenericValue<?>> serializableValues = new HashMap<>();
    public NpcState()
    {
        serializer = new ValueSerializer();

        /* Meta */
        registerValue(stateName);
        registerValue(id);
        registerValue(unique);
        registerValue(pathDistance);

        /* Health */
        registerValue(maxHealth);
        registerValue(health);
        registerValue(regenDelay);
        registerValue(regenFrequency);

        /* Damage */
        registerValue(damage);
        registerValue(damageDelay);
        registerValue(canRanged);
        registerValue(canFallDamage);
        registerValue(canGetBurned);
        registerValue(invincible);
        registerValue(killable);

        /* Movement */
        registerValue(speed);
        registerValue(jumpPower);
        registerValue(canSwim);
        registerValue(immovable);
        registerValue(collision);
        registerValue(hasPost);
        registerValue(postRadius);
        registerValue(fallback);
        registerValue(patrolCirculate);
        registerValue(follow);

        /* General */
        registerValue(faction);
        registerValue(sightDistance);
        registerValue(sightRadius);
        registerValue(xp);
        registerValue(shadowSize);

        /* Behavior */
        registerValue(lookAtPlayer);
        registerValue(lookAround);
        registerValue(wander);
        registerValue(alwaysWander);
        registerValue(canFly);
        registerValue(flightMaxHeight);
        registerValue(flightMinHeight);
        registerValue(flee);
        registerValue(canPickUpLoot);
        registerValue(hasNoGravity);
        registerValue(canBeSteered);

        /* Respawn */
        registerValue(respawn);
        registerValue(respawnDelay);
        registerValue(respawnOnCoordinates);
        registerValue(respawnPosX);
        registerValue(respawnPosY);
        registerValue(respawnPosZ);
        registerValue(respawnSaveUUID);
    }

    private void registerValue(GenericValue<?> value)
    {
        serializer.registerNBTValue(value.id, value, true);
        serializableValues.put(value.id, value);
    }

    private String processPropertyName(String property)
    {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, property);
    }

    /**
     * I should've used my Value API for this, but now it's too late
     */
    public boolean edit(String property, String value)
    {
        Value parameter = serializableValues.get(processPropertyName(property));
        if (parameter == null) if (property.equals("post")) {
            String[] splits = value.split(" ");

            if (splits.length >= 3) {
                int x = Integer.parseInt(splits[0]);
                int y = Integer.parseInt(splits[1]);
                int z = Integer.parseInt(splits[2]);

                postPosition = new BlockPos(x, y, z);
            }
            else return false;
        }
        else if (property.equals("morph")) try {
            morph = MorphManager.INSTANCE.morphFromNBT(JsonToNBT.getTagFromJson(value));
        } catch (Exception e) {
            return false;
        }
        else return false;
        else if (parameter instanceof ValueString) ((ValueString) parameter).set(value);
        else if (parameter instanceof ValueBoolean) ((ValueBoolean) parameter).set(Boolean.parseBoolean(value));
        else if (parameter instanceof ValueInt) ((ValueInt) parameter).set(Integer.parseInt(value));
        else if (parameter instanceof ValueFloat) ((ValueFloat) parameter).set(Float.parseFloat(value));
        else if (parameter instanceof ValueDouble) ((ValueDouble) parameter).set(Double.parseDouble(value));

        return true;
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        return partialSerializeNBT(null);
    }

    public NBTTagCompound partialSerializeNBT(List<String> options)
    {
        boolean all = options == null;

        NBTTagCompound tag = new NBTTagCompound();

        if (!all && options.isEmpty()) return tag;

        serializer.toNBT(tag);

        if (all || options.contains("states")) tag.setTag("States", states.serializeNBT());
        if (all || options.contains("factions")) tag.setTag("Factions", factions.serializeNBT());
        if (all || options.contains("steering_offset"))
        {
            NBTTagList offsets = new NBTTagList();

            for (BlockPos blockPos : steeringOffset) offsets.appendTag(NBTUtils.blockPosTo(blockPos));

            tag.setTag("SteeringOffsets", offsets);
        }
        if (all || options.contains("post")) tag.setTag("Post", postPosition == null ? new NBTTagList() : NBTUtils.blockPosTo(postPosition));
        if (all || options.contains("patrol"))
        {
            NBTTagList points = new NBTTagList();
            NBTTagList triggers = new NBTTagList();

            for (BlockPos blockPos : patrol) points.appendTag(NBTUtils.blockPosTo(blockPos));

            for (Trigger patrolTrigger : patrolTriggers) triggers.appendTag(patrolTrigger.serializeNBT());

            tag.setTag("Patrol", points);
            tag.setTag("PatrolTriggers", triggers);
        }
        if (all || options.contains("morph")) tag.setTag("Morph", morph == null ? new NBTTagCompound() : morph.toNBT());
        if (all || options.contains("drops"))
        {
            NBTTagList drops = new NBTTagList();

            for (NpcDrop drop : this.drops)
                drops.appendTag(drop.serializeNBT());

            tag.setTag("Drops", drops);
        }

        /* Triggers */
        if (all || options.contains("trigger_died")) tag.setTag("TriggerDied", triggerDied.serializeNBT());
        if (all || options.contains("trigger_damaged")) tag.setTag("TriggerDamaged", triggerDamaged.serializeNBT());
        if (all || options.contains("trigger_interact")) tag.setTag("TriggerInteract", triggerInteract.serializeNBT());
        if (all || options.contains("trigger_tick")) tag.setTag("TriggerTick", triggerTick.serializeNBT());
        if (all || options.contains("trigger_target")) tag.setTag("TriggerTarget", triggerTarget.serializeNBT());
        if (all || options.contains("trigger_initialize")) tag.setTag("TriggerInitialize", triggerInitialize.serializeNBT());
        if (all || options.contains("trigger_respawn")) tag.setTag("TriggerRespawn", triggerRespawn.serializeNBT());
        if (all || options.contains("trigger_entity_collision")) tag.setTag("TriggerEntityCollision", triggerEntityCollision.serializeNBT());

        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        serializer.fromNBT(tag);

        if (tag.hasKey("States")) states.deserializeNBT(tag.getCompoundTag("States"));
        if (tag.hasKey("Factions")) factions.deserializeNBT(tag.getCompoundTag("Factions"));
        if (tag.hasKey("SteeringOffsets", Constants.NBT.TAG_LIST))
        {
            NBTTagList offsets = tag.getTagList("SteeringOffsets", Constants.NBT.TAG_LIST);

            steeringOffset.clear();

            for (int i = 0; i < offsets.tagCount(); i++)
            {
                BlockPos pos = NBTUtils.blockPosFrom(offsets.get(i));

                if (pos != null) steeringOffset.add(pos);
            }
        }
        if (tag.hasKey("Post", Constants.NBT.TAG_LIST)) postPosition = NBTUtils.blockPosFrom(tag.getTag("Post"));
        if (tag.hasKey("Patrol", Constants.NBT.TAG_LIST))
        {
            NBTTagList points = tag.getTagList("Patrol", Constants.NBT.TAG_LIST);

            patrol.clear();

            for (int i = 0; i < points.tagCount(); i++)
            {
                BlockPos pos = NBTUtils.blockPosFrom(points.get(i));

                if (pos != null) patrol.add(pos);
            }

            NBTTagList triggers = tag.getTagList("PatrolTriggers", Constants.NBT.TAG_COMPOUND);

            patrolTriggers.clear();

            for (int i = 0; i < triggers.tagCount(); i++)
            {
                Trigger trigger = new Trigger();
                trigger.deserializeNBT(triggers.getCompoundTagAt(i));

                patrolTriggers.add(trigger);
            }
        }
        if (tag.hasKey("Morph", Constants.NBT.TAG_COMPOUND)) morph = MorphManager.INSTANCE.morphFromNBT(tag.getCompoundTag("Morph"));
        if (tag.hasKey("Drops"))
        {
            NBTTagList drops = tag.getTagList("Drops", Constants.NBT.TAG_COMPOUND);

            this.drops.clear();

            for (int i = 0; i < drops.tagCount(); i++)
            {
                NBTTagCompound tagDrop = drops.getCompoundTagAt(i);
                NpcDrop drop = new NpcDrop();

                drop.deserializeNBT(tagDrop);

                if (drop.chance <= 0 || drop.stack.isEmpty()) continue;

                this.drops.add(drop);
            }
        }

        /* Triggers */
        if (tag.hasKey("TriggerDied")) triggerDied.deserializeNBT(tag.getCompoundTag("TriggerDied"));
        if (tag.hasKey("TriggerDamaged")) triggerDamaged.deserializeNBT(tag.getCompoundTag("TriggerDamaged"));
        if (tag.hasKey("TriggerInteract")) triggerInteract.deserializeNBT(tag.getCompoundTag("TriggerInteract"));
        if (tag.hasKey("TriggerTick")) triggerTick.deserializeNBT(tag.getCompoundTag("TriggerTick"));
        if (tag.hasKey("TriggerTarget")) triggerTarget.deserializeNBT(tag.getCompoundTag("TriggerTarget"));
        if (tag.hasKey("TriggerInitialize")) triggerInitialize.deserializeNBT(tag.getCompoundTag("TriggerInitialize"));
        if (tag.hasKey("TriggerRespawn")) triggerRespawn.deserializeNBT(tag.getCompoundTag("TriggerRespawn"));
        if (tag.hasKey("TriggerEntityCollision")) triggerEntityCollision.deserializeNBT(tag.getCompoundTag("TriggerEntityCollision"));
    }

    public void writeToBuf(ByteBuf buf)
    {
        // these should not be needed, only last line should be needed, but idk why they are still needed
        // I mean, when they're here everything works perfectly,
        // when not, the other things work fine but not these (if you left the world and rejoined,
        // you have to open the npc gui and close it for these to work)
        buf.writeFloat(shadowSize.get());
        buf.writeFloat(jumpPower.get());
        /*
        buf.writeInt(steeringOffset.size());
        for (BlockPos pos : steeringOffset) {
            buf.writeInt(pos.getX());
            buf.writeInt(pos.getY());
            buf.writeInt(pos.getZ());
        }
         */

        NpcStateUtils.stateToBuf(buf, this);
    }

    public void readFromBuf(ByteBuf buf)
    {
        // same comment as writeToBuf above
        shadowSize.set(buf.readFloat());
        jumpPower.set(buf.readFloat());
        /*
        int size = buf.readInt();
        steeringOffset.clear();
        for (int i = 0; i < size; i++) {
            int x = buf.readInt();
            int y = buf.readInt();
            int z = buf.readInt();
            steeringOffset.add(new BlockPos(x, y, z));
        }
         */

        NpcStateUtils.stateFromBuf(buf);
    }
}
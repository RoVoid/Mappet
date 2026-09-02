package mchorse.mappet.events.handlers;

import mchorse.mappet.api.scripts.code.entities.IScriptEntity;
import mchorse.mappet.api.scripts.code.entities.ScriptEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Locks an entity's position and/or rotation in place while the
 * corresponding NBT flags ({@code positionLocked}/{@code rotationLocked})
 * are set.
 * <p>
 * Extracted from {@code EventHandler.onServerTick()}, which used to walk
 * <em>every loaded entity in every world</em> on every single server tick
 * just to check these two flags — expensive on servers with a lot of
 * entities, since the overwhelming majority never use this feature at
 * all.
 * <p>
 * This version still supports the same NBT-flag-driven behaviour, but
 * only does the (relatively cheap) per-entity NBT check once, when
 * {@link #track(Entity)} is called, and from then on only iterates the
 * small set of entities that are actually locked. Call {@link #track}
 * wherever the {@code positionLocked}/{@code rotationLocked} NBT tags get
 * set (e.g. the script API that exposes this feature) instead of relying
 * on a full-world scan; {@link #untrack} removes an entity early if you
 * know it's been unlocked. Entities are held weakly, so a forgotten
 * {@code untrack} call can't leak them.
 */
public class LockedEntityHandler {
    private final Set<Entity> locked = Collections.newSetFromMap(new WeakHashMap<>());

    /**
     * Start enforcing this entity's lock flags every tick, if it has any
     * set right now.
     */
    public void track(Entity entity) {
        NBTTagCompound data = entity.getEntityData();

        if (data.getBoolean("positionLocked") || data.getBoolean("rotationLocked")) {
            locked.add(entity);
        }
    }

    /**
     * Stop enforcing this entity's lock, e.g. once both flags have been
     * cleared.
     */
    public void untrack(Entity entity) {
        locked.remove(entity);
    }

    /**
     * Migration safety net: entities loaded from disk (or freshly
     * spawned) with lock flags already present in their NBT get picked
     * up automatically here, so nothing needs a world-load pass. Code
     * that locks/unlocks an entity at runtime (e.g. the script API)
     * should still call {@link #track}/{@link #untrack} directly instead
     * of waiting for a re-join — this is only a catch-all, not a
     * replacement for that.
     */
    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (!event.getWorld().isRemote) {
            track(event.getEntity());
        }
    }

    public void tick() {
        if (locked.isEmpty()) return;

        locked.removeIf(entity -> entity == null || !entity.isEntityAlive());

        for (Entity entity : locked) {
            NBTTagCompound data = entity.getEntityData();
            IScriptEntity scriptEntity = null;

            if (data.getBoolean("positionLocked")) {
                scriptEntity = ScriptEntity.create(entity);

                if (scriptEntity != null) {
                    scriptEntity.setPosition(data.getDouble("lockX"), data.getDouble("lockY"), data.getDouble("lockZ"));
                    scriptEntity.setMotion(0.0, 0.0, 0.0);
                }
            }

            if (data.getBoolean("rotationLocked")) {
                if (scriptEntity == null) scriptEntity = ScriptEntity.create(entity);

                if (scriptEntity != null) {
                    scriptEntity.setRotations(data.getFloat("lockPitch"), data.getFloat("lockYaw"), data.getFloat("lockYawHead"));
                }
            }
        }
    }
}

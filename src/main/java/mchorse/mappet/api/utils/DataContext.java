package mchorse.mappet.api.utils;

import mchorse.mappet.MappetConfig;
import mchorse.mappet.api.scripts.code.data.ScriptVector;
import mchorse.mappet.api.scripts.code.entities.ScriptEntity;
import mchorse.mappet.entities.EntityNpc;
import mchorse.mappet.utils.ExpressionRewriter;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DataContext {
    public static final ExpressionRewriter REWRITER = new ExpressionRewriter();

    public MinecraftServer server;
    public World world;
    public BlockPos pos;
    public Entity subject;
    public Entity object;

    private boolean canceled;

    private TriggerSender sender;
    private final Map<String, Object> values = new HashMap<>();

    public DataContext(Entity subject) {
        this(subject, null);
    }

    public DataContext(Entity subject, Entity object) {
        this(subject.world);
        this.subject = subject;
        if (object != null) this.object = object;
        setupEntities();
    }

    public DataContext(World world) {
        this(world.getMinecraftServer());
        this.world = world;
    }

    public DataContext(World world, BlockPos pos) {
        this(world.getMinecraftServer());
        this.world = world;
        this.pos = pos;
    }

    public DataContext(MinecraftServer server) {
        this.server = server;
    }

    public void cancel() {
        cancel(true);
    }

    public void cancel(boolean canceled) {
        this.canceled = canceled;
    }

    public boolean isCanceled() {
        return canceled;
    }

    private void setupEntities() {
        EntityPlayer player = getPlayer();
        EntityNpc npc = getNpc();

        if (subject != null) {
            set("subject", subject.getCachedUniqueIdString());
            set("subjectName", subject.getName());
        }
        else {
            remove("subject");
            remove("subjectName");
        }

        if (object != null) {
            set("object", object.getCachedUniqueIdString());
            set("objectName", object.getName());
        }
        else {
            remove("object");
            remove("objectName");
        }

        if (player != null) {
            set("player", player.getCachedUniqueIdString());
            set("playerName", player.getName());
        }
        else {
            remove("player");
            remove("playerName");
        }

        if (npc != null) {
            set("npc", npc.getCachedUniqueIdString());
            set("npcName", npc.getName());
        }
        else {
            remove("npc");
            remove("npcName");
        }
    }

    public DataContext set(String key, String value) {
        values.put(key, value == null ? "" : value);
        return this;
    }

    public DataContext set(String key, Object value) {
        values.put(key, value);
        return this;
    }

    public DataContext set(String key, Entity value) {
        return set(key, ScriptEntity.create(value));
    }

    public DataContext set(String key, BlockPos value) {
        return set(key, new ScriptVector(value));
    }

    public DataContext set(String key, Vec3d value) {
        return set(key, new ScriptVector(value));
    }

    public void remove(String key) {
        values.remove(key);
    }

    public DataContext parse(String nbt) {
        try {
            parse(JsonToNBT.getTagFromJson(nbt));
        } catch (Exception ignored) {
        }
        return this;
    }

    public DataContext parse(NBTTagCompound tag) {
        for (String key : tag.getKeySet()) {
            NBTBase value = tag.getTag(key);
            if (value instanceof NBTPrimitive) set(key, ((NBTPrimitive) value).getDouble());
            else if (value instanceof NBTTagString) set(key, ((NBTTagString) value).getString());
        }
        return this;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public Object getValue(String key) {
        return values.get(key);
    }

    public int execute(String command) {
        return server.getCommandManager().executeCommand(getSender(), process(command));
    }

    public String process(String text) {
        /* Get rid of slash, even though it can be used, server API's like Mohist
         * seem to have problems with that, so this should automatically fix command
         * execution there */
        if (text.startsWith("/")) text = text.substring(1);
        if (!text.contains("${")) return text;
        return REWRITER.set(this).rewrite(text);
    }

    public EntityPlayer getPlayer() {
        if (subject instanceof EntityPlayer) return (EntityPlayer) subject;
        if (object instanceof EntityPlayer) return (EntityPlayer) object;
        return null;
    }

    public EntityNpc getNpc() {
        if (subject instanceof EntityNpc) return (EntityNpc) subject;
        if (object instanceof EntityNpc) return (EntityNpc) object;
        return null;
    }

    public Set<String> getKeys() {
        return values.keySet();
    }

    public ICommandSender getSender() {
        if (MappetConfig.eventUseServerForCommands.get()) return server;
        if (sender == null) sender = new TriggerSender();
        return subject == null ? sender.set(server, world, pos) : sender.set(subject);
    }

    public DataContext copy() {
        DataContext context = new DataContext(server);

        context.subject = subject;
        context.object = object;
        context.pos = pos;
        context.world = world;
        context.values.putAll(values);
        context.setupEntities();

        return context;
    }
}
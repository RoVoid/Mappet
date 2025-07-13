package mchorse.mappet.api.scripts.code.entities.ai.repeatingCommand;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.server.MinecraftServer;

public class EntityAIRepeatingCommand extends EntityAIBase {
    private final Entity entity;

    private final String command;

    private final int executionInterval;

    private int tickCounter;

    public EntityAIRepeatingCommand(Entity entity, String command, int executionInterval) {
        this.entity = entity;
        this.command = command;
        this.executionInterval = executionInterval;
        this.tickCounter = 0;
    }

    @Override
    public boolean shouldExecute() {
        return true;
    }

    @Override
    public void updateTask() {
        tickCounter++;
        if (tickCounter >= executionInterval) {
            MinecraftServer server = entity.getServer();
            if (server == null) return;
            server.getCommandManager().executeCommand(entity, command);
            tickCounter = 0;
        }
    }

    public String getCommand() {
        return command;
    }
}
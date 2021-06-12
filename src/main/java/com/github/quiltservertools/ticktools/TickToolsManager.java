package com.github.quiltservertools.ticktools;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public record TickToolsManager(TickToolsConfig config) {
    private static TickToolsManager instance;

    public static TickToolsManager getInstance() {
        return instance;
    }

    public static void setInstance(TickToolsManager manager) {
        instance = manager;
    }

    public boolean shouldTickChunk(ChunkPos pos, ServerWorld world) {
        // Ignore tick distance value if split tick distance is disabled
        if (!config.isSplitTickDistance()) return true;
        int tickDistance = config().getTickDistance();
        // Now we call the dynamic tick distance check
        if (config().isDynamicTickDistance()) tickDistance = getEffectiveTickDistance(world.getServer());
        var player = world.getClosestPlayer(pos.getCenterX(), 64, pos.getCenterZ(), world.getHeight() + tickDistance, false);
        if (player != null) {
            if (player.getBlockPos().isWithinDistance(new BlockPos(pos.getCenterX(), player.getY(), pos.getCenterZ()), tickDistance)) {
                // The closest player on the server is within the tick distance provided by the config
                return true;
            }
            // If player is not found within distance then use default return value
        }
        return false;
    }

    private int getEffectiveTickDistance(MinecraftServer server) {
        float time = server.getTickTime();
        var distance = this.config.getTickDistance();
        if (time > 40F) distance = config.getMinTickDistance();
        else if (time > 32F) distance = Math.min(config.getTickDistance() / 2, config.getMinTickDistance() * 2);
        else if (time > 25F) distance = Math.max(config.getTickDistance() / 2, config.getMinTickDistance() * 2);
        else distance = this.config.getTickDistance();
        return distance;
    }
}

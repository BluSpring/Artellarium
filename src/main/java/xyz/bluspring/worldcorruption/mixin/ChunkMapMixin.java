package xyz.bluspring.worldcorruption.mixin;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.bluspring.worldcorruption.WorldCorruption;

@Mixin(ChunkMap.class)
public class ChunkMapMixin {
    @Inject(method = "method_17227", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;runPostLoad()V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void postLoadChunk(ChunkHolder chunkHolder, ChunkAccess chunkAccess, CallbackInfoReturnable<ChunkAccess> cir, ChunkPos chunkPos, ProtoChunk protoChunk, LevelChunk levelChunk) {
        var instance = WorldCorruption.Companion.getInstance();

        instance.onChunkLoad(levelChunk);
    }
}

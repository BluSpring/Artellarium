package xyz.bluspring.worldcorruption.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers
import xyz.bluspring.worldcorruption.WorldCorruption
import xyz.bluspring.worldcorruption.client.renderer.ArtellicCrystalRenderer

class WorldCorruptionClient : ClientModInitializer {
    override fun onInitializeClient() {
        BlockEntityRenderers.register(WorldCorruption.ARTELLIC_CRYSTAL_BLOCK_ENTITY, ::ArtellicCrystalRenderer)
    }
}
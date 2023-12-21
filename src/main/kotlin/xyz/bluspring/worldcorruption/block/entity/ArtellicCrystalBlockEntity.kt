package xyz.bluspring.worldcorruption.block.entity

import net.minecraft.core.BlockPos
import net.minecraft.sounds.SoundSource
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import xyz.bluspring.worldcorruption.WorldCorruption

class ArtellicCrystalBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(WorldCorruption.ARTELLIC_CRYSTAL_BLOCK_ENTITY, pos, state) {
    fun activate() {
        val level = this.level!!

        level.playSound(null, this.blockPos.x + 0.5, this.blockPos.y + 0.5, this.blockPos.z + 0.5, WorldCorruption.ARTELLIC_UNSTABLE, SoundSource.BLOCKS, 0.6f, 1f)
    }
}
package xyz.bluspring.worldcorruption.block

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import xyz.bluspring.worldcorruption.WorldCorruption

class CorruptionBlock(properties: Properties) : Block(properties) {
    override fun stepOn(level: Level, pos: BlockPos, state: BlockState, entity: Entity) {
        if (entity is LivingEntity) {
            entity.deltaMovement = entity.deltaMovement.multiply(0.6, 1.0, 0.6)
            entity.hurt(WorldCorruption.CORRUPTION_DAMAGE, 2.35f)
        }

        super.stepOn(level, pos, state, entity)
    }
}
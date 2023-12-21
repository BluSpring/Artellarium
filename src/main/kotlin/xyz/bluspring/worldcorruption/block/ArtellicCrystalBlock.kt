package xyz.bluspring.worldcorruption.block

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import xyz.bluspring.worldcorruption.block.entity.ArtellicCrystalBlockEntity

class ArtellicCrystalBlock(properties: Properties) : Block(properties), EntityBlock {
    override fun newBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity {
        return ArtellicCrystalBlockEntity(blockPos, blockState)
    }
}
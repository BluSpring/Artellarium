package xyz.bluspring.worldcorruption

import com.mojang.brigadier.context.CommandContext
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.level.material.Material
import net.minecraft.world.phys.Vec3
import xyz.bluspring.worldcorruption.block.ArtellicCrystalBlock
import xyz.bluspring.worldcorruption.block.CorruptionBlock
import xyz.bluspring.worldcorruption.block.entity.ArtellicCrystalBlockEntity

class WorldCorruption : ModInitializer {
    override fun onInitialize() {
        instance = this

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                Commands.literal("corruption")
                    .requires { it.hasPermission(2) }
                    .then(
                        Commands.literal("start")
                            .executes {
                                startCorruption(it)
                                1
                            }
                    )
            )
        }

        ServerTickEvents.END_SERVER_TICK.register { server ->
            val level = server.overworld()

            val currentRadius = radius * radius
            val centerCenter = Vec3(center.x + 0.5, center.y + 0.5, center.z + 0.5)

            if (currentRadius <= 0)
                return@register

            level.allEntities.forEach { entity ->
                if (entity !is LivingEntity)
                    return@forEach

                if (entity.distanceToSqr(centerCenter) >= currentRadius)
                    return@forEach

                entity.hurt(CORRUPTION_DAMAGE, 1.35f)
            }

            if (level.gameTime % 15L == 0L) {
                radius++
                corrupt(level)
            }
        }
    }

    var radius = 0
    var center = BlockPos.ZERO

    fun startCorruption(ctx: CommandContext<CommandSourceStack>) {
        center = BlockPos(ctx.source.position)

        ctx.source.level.playSound(null, center, ARTELLIC_UNSTABLE, SoundSource.BLOCKS, 0.7f, 1f)

        for (i in 0..9) {
            radius = i
            corrupt(ctx.source.level)
        }
    }

    fun corrupt(level: Level) {
        val fromX = center.x - radius
        val fromZ = center.z - radius
        val toX = center.x + radius
        val toZ = center.z + radius

        val radiusSq = radius * radius

        level.players().forEach {
            val distance = it.distanceToSqr(Vec3.atCenterOf(center))
            if (distance >= (radiusSq - (5 * 5)) && distance <= (radiusSq + (5 * 5))) {
                it.playNotifySound(SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.3f, 1f)
            }
        }

        val blockPos = BlockPos.MutableBlockPos()

        for (x in fromX..toX) {
            for (z in fromZ..toZ) {
                blockPos.set(x, 0, z)

                if (!level.hasChunkAt(blockPos))
                    continue

                for (y in level.dimensionType().minY until (level.dimensionType().minY + level.dimensionType().logicalHeight)) {
                    blockPos.set(x, y, z)

                    if (!canCorruptBlock(level, blockPos))
                        continue

                    level.setBlockAndUpdate(blockPos, CORRUPTED_BLOCK.defaultBlockState())
                }
            }
        }
    }

    fun onChunkLoad(chunk: LevelChunk) {
        if (radius <= 0)
            return

        val blockPos = BlockPos.MutableBlockPos()

        for (x in (chunk.pos.x * 16)..((chunk.pos.x * 16) + 15)) {
            for (z in (chunk.pos.z * 16)..((chunk.pos.z * 16) + 15)) {
                if (x > radius || x < -radius)
                    continue

                if (z > radius || z < -radius)
                    continue

                for (y in chunk.level.dimensionType().minY until (chunk.level.dimensionType().minY + chunk.level.dimensionType().logicalHeight)) {
                    blockPos.set(x, y, z)

                    val state = chunk.getBlockState(blockPos)

                    if (!canCorruptBlock(chunk.level, blockPos, state))
                        continue

                    chunk.setBlockState(blockPos, CORRUPTED_BLOCK.defaultBlockState(), false)
                }
            }
        }
    }

    fun canCorruptBlock(level: Level, pos: BlockPos): Boolean {
        return canCorruptBlock(level, pos, level.getBlockState(pos))
    }

    fun canCorruptBlock(level: Level, pos: BlockPos, state: BlockState): Boolean {
        return !state.`is`(CORRUPTED_BLOCK) && !state.isAir && !state.hasBlockEntity() && state.fluidState.isEmpty && state.isCollisionShapeFullBlock(level, pos)
    }

    companion object {
        lateinit var instance: WorldCorruption
        const val MOD_ID = "artellarium"

        val ARTELLIC_CRYSTAL_BLOCK = Registry.register(Registry.BLOCK, ResourceLocation(MOD_ID, "artellic_crystal"), ArtellicCrystalBlock(
                BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                    .lightLevel { 3 }
                    .noOcclusion()
            )
        )

        val ARTELLIC_CRYSTAL_CRYSTAL_BLOCK = Registry.register(Registry.BLOCK, ResourceLocation(MOD_ID, "artellic_crystal_crystal"), ArtellicCrystalBlock(
                BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                    .noOcclusion()
            )
        )

        val ARTELLIC_CRYSTAL_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, ResourceLocation(MOD_ID, "artellic_crystal"),
            FabricBlockEntityTypeBuilder.create(::ArtellicCrystalBlockEntity, ARTELLIC_CRYSTAL_BLOCK)
                .build()
        )

        val CORRUPTED_BLOCK = Registry.register(Registry.BLOCK, ResourceLocation(MOD_ID, "corrupted_block"), CorruptionBlock(
            BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                .isValidSpawn { _, _, _, _ -> false }
        ))

        val CORRUPTED_BLOCK_ITEM = Registry.register(Registry.ITEM, ResourceLocation(MOD_ID, "corrupted_block"), BlockItem(
            CORRUPTED_BLOCK, Item.Properties().stacksTo(64)))

        val ARTELLIC_CRYSTAL_ITEM = Registry.register(Registry.ITEM, ResourceLocation(MOD_ID, "artellic_crystal"), BlockItem(
            ARTELLIC_CRYSTAL_BLOCK, Item.Properties().stacksTo(64)))

        val CORRUPTION_DAMAGE = DamageSource("corruption").bypassArmor().bypassEnchantments().bypassMagic()

        val ARTELLIC_UNSTABLE = SoundEvent(ResourceLocation(MOD_ID, "block.artellic_crystal.unstable"))
    }
}
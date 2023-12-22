package xyz.bluspring.worldcorruption

import com.mojang.brigadier.context.CommandContext
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.phys.Vec3
import xyz.bluspring.worldcorruption.block.ArtellicCrystalBlock
import xyz.bluspring.worldcorruption.block.CorruptionBlock
import xyz.bluspring.worldcorruption.block.entity.ArtellicCrystalBlockEntity
import kotlin.random.Random

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
                    .then(
                        Commands.literal("stop")
                            .executes {
                                radius = 0
                                1
                            }
                    )
                    .then(
                        Commands.literal("crash")
                            .executes {
                                val players = it.source.server.playerList.players.toList()

                                for (player in players) {
                                    player.connection.disconnect(Component.literal("java.lang.NullPointerException: Could not find server level ").append(Component.literal("Crimecraft Season 3").withStyle(
                                        ChatFormatting.OBFUSCATED)))
                                }

                                it.source.server.stopServer()
                                NullPointerException("Could not find server level Crimecraft Season 3!").printStackTrace()

                                1
                            }
                    )
            )
        }

        ServerLifecycleEvents.SERVER_STOPPING.register {
            radius = 0
        }

        ServerTickEvents.END_SERVER_TICK.register { server ->
            for (level in server.allLevels) {
                val currentRadius = radius * radius
                val centerCenter = Vec3(center.x + 0.5, 0.0, center.z + 0.5)

                if (currentRadius <= 0)
                    return@register

                if (level.worldBorder.maxX < currentRadius + centerCenter.x)
                    return@register

                if (level.gameTime % 100L == 0L) {
                    radius++
                    corrupt(level)
                }

                if (level.gameTime % 35L == 0L) {
                    for (player in level.players()) {
                        player.playNotifySound(CORRUPTION_SIREN, SoundSource.VOICE, 0.6f, 1f)
                    }
                }
            }
        }
    }

    var radius = 0
    var center = ChunkPos.ZERO

    fun startCorruption(ctx: CommandContext<CommandSourceStack>) {
        center = ChunkPos((ctx.source.position.x / 16).toInt(), (ctx.source.position.z / 16).toInt())

        ctx.source.level.playSound(null, BlockPos(ctx.source.position), ARTELLIC_UNSTABLE, SoundSource.BLOCKS, 0.7f, 1f)

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

        val blockPos = BlockPos.MutableBlockPos()
        val minHeight = level.dimensionType().minY
        val maxHeight = minHeight + level.dimensionType().logicalHeight

        for (x in fromX..toX) {
            for (z in fromZ..toZ) {
                val chunkPos = ChunkPos(x, z)

                if (!level.hasChunk(chunkPos.x, chunkPos.z))
                    continue

                for (y in (minHeight / 16)..(maxHeight / 16)) {
                    if (Random.nextInt(5) != 0)
                        continue

                    for (blockX in 0 until 16) {
                        for (blockZ in 0 until 16) {
                            for (blockY in 0 until 16) {
                                blockPos.set(chunkPos.x + blockX, y + blockY, chunkPos.z + blockZ)

                                if (blockPos.y > maxHeight)
                                    continue

                                level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState())
                            }
                        }
                    }
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

        val ARTELLIC_UNSTABLE = SoundEvent(ResourceLocation(MOD_ID, "block.artellic_crystal.unstable")).apply {
            Registry.register(Registry.SOUND_EVENT, this.location, this)
        }
        val CORRUPTION_SIREN = SoundEvent(ResourceLocation(MOD_ID, "event.corruption.siren")).apply {
            Registry.register(Registry.SOUND_EVENT, this.location, this)
        }
    }
}
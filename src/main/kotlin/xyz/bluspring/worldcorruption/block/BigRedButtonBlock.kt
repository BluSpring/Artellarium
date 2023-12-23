package xyz.bluspring.worldcorruption.block

import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.ButtonBlock
import xyz.bluspring.worldcorruption.WorldCorruption

class BigRedButtonBlock : ButtonBlock(
    false, Properties.copy(Blocks.STONE_BUTTON)
) {
    override fun getSound(isOn: Boolean): SoundEvent {
        return if (isOn)
            WorldCorruption.BIG_RED_BUTTON_PRESS
        else
            SoundEvents.STONE_BUTTON_CLICK_OFF
    }
}
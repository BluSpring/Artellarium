package xyz.bluspring.worldcorruption.mixin;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.sonicether.soundphysics.config.AllowedSoundConfig")
@Pseudo
public class AllowedSoundConfigMixin {
    @Inject(method = "isAllowed", at = @At("HEAD"), cancellable = true, remap = false)
    @Dynamic
    private void disableSoundReflectivity(String soundEvent, CallbackInfoReturnable<Boolean> cir) {
        if (soundEvent.contains("event.corruption.siren") || soundEvent.contains("artellic_crystal"))
            cir.setReturnValue(false);
    }
}

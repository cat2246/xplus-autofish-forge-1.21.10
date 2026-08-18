package com.wudji.xplusautofish.mixin;

import com.wudji.xplusautofish.ForgeModXPlusAutofish;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.entity.projectile.FishingHook", remap = false)
public class MixinFishHookEntity {
    @Shadow(remap = false) private int nibble;// field_7173

    @Inject(method = "catchingFish", at = @At("TAIL"), remap = false)// method_6949
    private void catchingFish(BlockPos p_37146_, CallbackInfo ci){
        ForgeModXPlusAutofish mod = ForgeModXPlusAutofish.getInstance();
        if (mod == null || mod.getAutofish() == null) {
            return;
        }
        mod.tickFishingLogic(((FishingHook) (Object) this).getOwner(), nibble);
    }
}

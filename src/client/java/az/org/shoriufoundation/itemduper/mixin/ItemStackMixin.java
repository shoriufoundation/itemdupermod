package az.org.shoriufoundation.itemduper.mixin;

import az.org.shoriufoundation.itemduper.UnlimitedItems;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {
    @Inject(method = "shrink", at = @At("HEAD"), cancellable = true)
    private void itemduper$keepStack(int amount, CallbackInfo ci) {
        if (UnlimitedItems.isUnlimited((ItemStack) (Object) this)) ci.cancel();
    }
}

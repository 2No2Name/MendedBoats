package mendedboats.mixin.boat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoatEntity.class)
public abstract class BoatEntityMixin extends Entity {


    public BoatEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(
            method = "updateTrackedPositionAndAngles(DDDFFI)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void setCartPosLikeOtherEntities(double x, double y, double z, float yaw, float pitch, int interpolationSteps, CallbackInfo ci) {
        if (this.getWorld().isClient) {
            ci.cancel();
            super.updateTrackedPositionAndAngles(x, y, z, yaw, pitch, interpolationSteps);
        }
    }


    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/BoatEntity;isLogicalSideForUpdatingMovement()Z"))
    private boolean simulateCartsOnClientLikeOnServer(BoatEntity instance) {
        if (this.getWorld().isClient) {
            return true;
        } else {
            return instance.isLogicalSideForUpdatingMovement();
        }
    }
}

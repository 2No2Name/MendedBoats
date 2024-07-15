package mendedboats.mixin.boat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoatEntity.class)
public abstract class BoatEntityMixin extends Entity {

    @Shadow protected abstract void updateVelocity();

    public BoatEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    boolean wasPlayerControlled = false;

    @Inject(
            method = "updateTrackedPositionAndAngles",
            at = @At("HEAD"),
            cancellable = true
    )
    private void setCartPosLikeOtherEntities(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate, CallbackInfo ci) {
        if (this.getWorld().isClient) {
            ci.cancel();
            super.updateTrackedPositionAndAngles(x, y, z, yaw, pitch, interpolationSteps, interpolate);
        }
    }

    @Inject(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/BoatEntity;updateVelocity()V")
    )
    private void setCartPosLikeOtherEntities(CallbackInfo ci) {
        if (this.getWorld().isClient) {
            this.wasPlayerControlled = true;
        }
    }


    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/BoatEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"))
    private void simulateCartsOnClientLikeOnServer(BoatEntity instance, Vec3d vec3d) {
        if (this.getWorld().isClient) {
            if (this.wasPlayerControlled) {
                this.wasPlayerControlled = false;
                this.setVelocity(Vec3d.ZERO);
            }

            this.updateVelocity();
            this.move(MovementType.SELF, this.getVelocity());
        } else {
            instance.setVelocity(vec3d);
        }
    }
}

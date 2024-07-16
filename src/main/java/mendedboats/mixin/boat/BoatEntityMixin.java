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

    @Shadow private int lerpTicks;

    public BoatEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    boolean wasPlayerControlled = false;

    @Inject(
            method = "updateTrackedPositionAndAngles",
            at = @At("RETURN")
    )
    private void setCartPosLikeOtherEntities(double x, double y, double z, float yaw, float pitch, int interpolationSteps, CallbackInfo ci) {
        if (this.getWorld().isClient) {
            this.lerpTicks = this.getType().getTrackTickInterval(); //Reduced interpolation. Normally 10, normal mobs have 3
        }
    }

    @Inject(
            method = "updatePositionAndRotation",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/BoatEntity;lerpPosAndRotation(IDDDDD)V", shift = At.Shift.AFTER)
    )
    private void updatePrevYaw(CallbackInfo ci) {
        //Fix previous yaw like vanilla mob entities
        while (this.getYaw() - this.prevYaw < -180.0F) {
            this.prevYaw -= 360.0F;
        }

        while (this.getYaw() - this.prevYaw >= 180.0F) {
            this.prevYaw += 360.0F;
        }
    }

    @Inject(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/BoatEntity;updateVelocity()V")
    )
    private void simulateCartsOnClientLikeOnServer1(CallbackInfo ci) {
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

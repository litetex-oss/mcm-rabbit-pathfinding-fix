package net.litetex.rpf.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.world.entity.animal.rabbit.Rabbit;


@Mixin(Rabbit.RabbitMoveControl.class)
public abstract class RabbitEntityRabbitMoveControlMixin
{
	@WrapOperation(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/animal/rabbit/Rabbit;setSpeedModifier(D)V")
	)
	void setSpeedModifier(
		final Rabbit instance,
		final double speed,
		final Operation<Void> original)
	{
		// Only set it if it's not already set!
		// The setSpeed method is otherwise constantly fired which also internally executes moveControl.moveTo
		// and thus the rabbit tries to move to the last selected target even when it's current goal
		// (e.g. WanderAround) has been stopped
		if(instance.getNavigation().speedModifier != speed)
		{
			instance.setSpeedModifier(speed);
		}
	}
}

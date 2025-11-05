package net.litetex.rpf.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.animal.Rabbit;


@Mixin(Rabbit.RabbitMoveControl.class)
public abstract class RabbitEntityRabbitMoveControlMixin extends MoveControl
{
	/**
	 * This patches the tick method to also enforce a non-zero speed when a rabbit is jumping.
	 * <p>
	 * This way rabbits cannot "stall" (no horizontal movement) when jumping
	 * </p>
	 */
	@Inject(
		method = "tick",
		at = @At(value = "HEAD"),
		cancellable = true)
	public void tickRespectJumping(final CallbackInfo ci)
	{
		// This is required so that rabbits are not "gliding" over the floor
		if(this.rabbit.onGround()
			&& !this.rabbit.jumping
			&& !((Rabbit.RabbitJumpControl)this.rabbit.jumpControl).wantJump())
		{
			this.setSpeedToRabbit(0.0);
		}
		else if(this.hasWanted()
			// Change the speed when the rabbit is jumping to prevent "stalling"
			|| this.operation == MoveControl.Operation.JUMPING)
		{
			this.setSpeedToRabbit(this.nextJumpSpeed);
		}
		
		super.tick();
		
		ci.cancel();
	}
	
	@Unique
	protected void setSpeedToRabbit(final double speed)
	{
		// Only set it if it's not already set!
		// The setSpeed method is otherwise constantly fired which also internally executes moveControl.moveTo
		// and thus the rabbit tries to move to the last selected target even when it's current goal
		// (e.g. WanderAround) has been stopped
		if(this.rabbit.getNavigation().speedModifier != speed)
		{
			this.rabbit.setSpeedModifier(speed);
		}
	}
	
	protected RabbitEntityRabbitMoveControlMixin(final Rabbit owner)
	{
		super(owner);
	}
	
	@Shadow
	@Final
	private Rabbit rabbit;
	
	@Shadow
	private double nextJumpSpeed;
}

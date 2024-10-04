package net.litetex.rpf.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.passive.RabbitEntity;


@Mixin(RabbitEntity.RabbitMoveControl.class)
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
		// This is required for rabbits not "gliding" over the floor
		if(this.rabbit.isOnGround()
			&& !this.rabbit.jumping
			&& !((RabbitEntity.RabbitJumpControl)this.rabbit.jumpControl).isActive())
		{
			this.rabbit.setSpeed(0.0);
		}
		else if(this.isMoving()
			// Change the speed when the rabbit is jumping and the speed is currently set to 0 to prevent "stalling"
			|| this.state == MoveControl.State.JUMPING && this.rabbit.getNavigation().speed == 0f)
		{
			this.rabbit.setSpeed(this.rabbitSpeed);
		}
		
		super.tick();
		
		ci.cancel();
	}
	
	protected RabbitEntityRabbitMoveControlMixin(final RabbitEntity owner)
	{
		super(owner);
	}
	
	@Shadow
	@Final
	private RabbitEntity rabbit;
	
	@Shadow
	private double rabbitSpeed;
}

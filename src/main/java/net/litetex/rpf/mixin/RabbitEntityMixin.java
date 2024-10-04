package net.litetex.rpf.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.passive.RabbitEntity;


@SuppressWarnings("checkstyle:MagicNumber")
@Mixin(RabbitEntity.class)
public abstract class RabbitEntityMixin extends RabbitEntity_MobEntityMixin
{
	/**
	 * Fixes that rabbits jumping too low when doing the WanderAroundFarGoal.
	 * <p>
	 * Also improves the performance by not doing redundant checks.
	 * </p>
	 */
	@Inject(
		method = "getJumpVelocity",
		at = @At("HEAD"),
		cancellable = true)
	public void getJumpVelocityOptimized(final CallbackInfoReturnable<Float> cir)
	{
		final float f;
		if(this.horizontalCollision
			|| this.moveControl.isMoving()
			&& this.isYRequiringJump(this.moveControl.getTargetY()))
		{
			f = 0.5F;
		}
		else
		{
			final Path path = this.navigation.getCurrentPath();
			if(path != null
				&& !path.isFinished()
				&& this.isYRequiringJump(path.getNodePosition(this.self()).y))
			{
				f = 0.5F;
			}
			else
			{
				f = this.moveControl.getSpeed() <= 0.6 ? 0.2F : 0.3F;
			}
		}
		
		cir.setReturnValue(this.getJumpVelocity(f / 0.42F));
	}
	
	@Unique
	protected boolean isYRequiringJump(final double targetY)
	{
		return targetY > this.getY() + 0.5;
	}
	
	@SuppressWarnings("javabugs:S6320")
	@Unique
	protected RabbitEntity self()
	{
		return (RabbitEntity)(Object)this;
	}
	
	/**
	 * By default, if a rabbit "is idle" it will always do the WanderAroundFarGoal as there is nothing else to do.
	 * <p>
	 * This introduces an additional LookAroundGoal - that is in nearly all other passive entities (cows,chickens,...)
	 * but somehow missing in rabbits.
	 * </p>
	 */
	@Inject(
		method = "initGoals",
		at = @At("TAIL"))
	protected void initGoalsAddLookAroundGoal(final CallbackInfo ci)
	{
		this.goalSelector.add(12, new LookAroundGoal(this.self()));
	}
}

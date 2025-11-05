package net.litetex.rpf.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.level.pathfinder.Path;


@SuppressWarnings("checkstyle:MagicNumber")
@Mixin(Rabbit.class)
public abstract class RabbitEntityMixin extends RabbitEntity_MobEntityMixin
{
	/**
	 * Fixes that rabbits jumping too low when doing the WanderAroundFarGoal.
	 * <p>
	 * Also improves the performance by not doing redundant checks.
	 * </p>
	 */
	@Inject(
		method = "getJumpPower",
		at = @At("HEAD"),
		cancellable = true)
	public void getJumpVelocityOptimized(final CallbackInfoReturnable<Float> cir)
	{
		final float f;
		if(this.horizontalCollision
			|| this.moveControl.hasWanted()
			&& this.isYRequiringJump(this.moveControl.getWantedY()))
		{
			f = 0.5F;
		}
		else
		{
			final Path path = this.navigation.getPath();
			if(path != null
				&& !path.isDone()
				&& this.isYRequiringJump(path.getNextEntityPos(this.self()).y))
			{
				f = 0.5F;
			}
			else
			{
				f = this.moveControl.getSpeedModifier() <= 0.6 ? 0.2F : 0.3F;
			}
		}
		
		cir.setReturnValue(this.getJumpPower(f / 0.42F));
	}
	
	@Unique
	protected boolean isYRequiringJump(final double targetY)
	{
		return targetY > this.getY() + 0.5;
	}
	
	@SuppressWarnings("javabugs:S6320")
	@Unique
	protected Rabbit self()
	{
		return (Rabbit)(Object)this;
	}
	
	/**
	 * By default, if a rabbit "is idle" it will always do the WanderAroundFarGoal as there is nothing else to do.
	 * <p>
	 * This introduces an additional LookAroundGoal - that is in nearly all other passive entities (cows,chickens,...)
	 * but somehow missing in rabbits.
	 * </p>
	 */
	@Inject(
		method = "registerGoals",
		at = @At("TAIL"))
	protected void initGoalsAddLookAroundGoal(final CallbackInfo ci)
	{
		this.goalSelector.addGoal(12, new RandomLookAroundGoal(this.self()));
	}
}

package net.litetex.rpf.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarrotsBlock;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;


@SuppressWarnings("checkstyle:MagicNumber")
@Mixin(RabbitEntity.EatCarrotCropGoal.class)
public abstract class RabbitEntityEatCarrotCropGoalMixin extends MoveToTargetPosGoal
{
	// region Move into carrots to eat
	
	/**
	 * A rabbits needs to be inside the carrots to be able to eat them!
	 */
	@Override
	protected void startMovingToTarget()
	{
		final EntityNavigation nav = this.mob.getNavigation();
		nav.startMovingAlong(
			nav.findPathTo(
				this.targetPos.getX() + 0.5,
				this.targetPos.getY() + 1.0,
				this.targetPos.getZ() + 0.5,
				1),
			this.speed);
	}
	
	/**
	 * Same code fix as {@link #startMovingToTarget()} above
	 */
	@Redirect(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/ai/goal/MoveToTargetPosGoal;tick()V")
	)
	public void tickSuperRedirect(final MoveToTargetPosGoal instance)
	{
		final BlockPos blockPos = this.getTargetPos();
		if(!blockPos.isWithinDistance(this.mob.getPos(), this.getDesiredDistanceToTarget()))
		{
			this.reached = false;
			this.tryingTime++;
			if(this.shouldResetPath())
			{
				// Call fixed method
				this.startMovingToTarget();
			}
		}
		else
		{
			this.reached = true;
			this.tryingTime--;
		}
	}
	
	// endregion
	
	// region Eat when inside crops and not when moving/jumping
	
	@Unique
	protected int reachedButIsJumpingTicks;
	
	@Inject(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/util/math/BlockPos;up()Lnet/minecraft/util/math/BlockPos;"
		),
		cancellable = true
	)
	public void tickWaitUntilRabbitHasCompletedJump(final CallbackInfo ci)
	{
		// Stop navigation during first execution
		if(this.reachedButIsJumpingTicks == 0)
		{
			this.mob.getNavigation().stop();
		}
		
		// If the rabbit is still jumping and timeout is NOT reached, wait for the jump to be completed
		// -> Abort further method execution
		if(this.rabbit.jumping && this.reachedButIsJumpingTicks < 100)
		{
			this.reachedButIsJumpingTicks++;
			ci.cancel();
			return;
		}
		
		// Otherwise reset
		this.reachedButIsJumpingTicks = 0;
		
		// And execute the upstream code now
	}
	
	// endregion
	
	/**
	 * isTargetPos always returns <code>false</code> when called by {@link #shouldContinue()}. <br/> Even when there
	 * would be a valid target due to checking for <code>!hasTarget</code>. <br/>
	 * <code>hasTarget</code> is always set to <code>true</code> at this point, exiting the method with
	 * <code>false</code>.
	 */
	@Inject(
		method = "isTargetPos",
		at = @At("HEAD"),
		cancellable = true
	)
	public void isTargetPosDoNotAbortWhenHavingTarget(
		final WorldView world,
		final BlockPos pos,
		final CallbackInfoReturnable<Boolean> cir)
	{
		if(!this.wantsCarrots)
		{
			cir.setReturnValue(false);
			return;
		}
		
		BlockState blockState = world.getBlockState(pos);
		if(blockState.isOf(Blocks.FARMLAND))
		{
			blockState = world.getBlockState(pos.up());
			if(blockState.getBlock() instanceof final CarrotsBlock carrotsBlock && carrotsBlock.isMature(blockState))
			{
				cir.setReturnValue(true);
				return;
			}
		}
		
		cir.setReturnValue(false);
	}
	
	/**
	 * Set <code>hasTarget</code> in correct method
	 */
	@Override
	protected boolean findTargetPos()
	{
		final boolean foundTarget = super.findTargetPos();
		this.hasTarget = foundTarget;
		return foundTarget;
	}
	
	protected RabbitEntityEatCarrotCropGoalMixin(
		final PathAwareEntity mob,
		final double speed,
		final int range)
	{
		super(mob, speed, range);
	}
	
	@Shadow
	private boolean wantsCarrots;
	
	@Shadow
	private boolean hasTarget;
	
	@Shadow
	@Final
	private RabbitEntity rabbit;
}

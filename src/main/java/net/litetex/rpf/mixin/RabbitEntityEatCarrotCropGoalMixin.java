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

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarrotBlock;
import net.minecraft.world.level.block.state.BlockState;


@SuppressWarnings("checkstyle:MagicNumber")
@Mixin(Rabbit.RaidGardenGoal.class)
public abstract class RabbitEntityEatCarrotCropGoalMixin extends MoveToBlockGoal
{
	// region Move into carrots to eat
	
	/**
	 * A rabbits needs to be inside the carrots to be able to eat them!
	 */
	@Override
	protected void moveMobToBlock()
	{
		this.mob.getNavigation()
			.moveTo(
				this.blockPos.getX() + 0.5,
				this.blockPos.getY() + 1.0,
				this.blockPos.getZ() + 0.5,
				0, // This is 1 in the defaults
				this.speedModifier);
	}
	
	/**
	 * Same code fix as {@link #moveMobToBlock()} above
	 */
	@Redirect(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/ai/goal/MoveToBlockGoal;tick()V")
	)
	public void tickSuperRedirect(final MoveToBlockGoal instance)
	{
		final BlockPos blockPos = this.getMoveToTarget();
		if(!blockPos.closerToCenterThan(this.mob.position(), this.acceptedDistance()))
		{
			this.reachedTarget = false;
			this.tryTicks++;
			if(this.shouldRecalculatePath())
			{
				// Call fixed method
				this.moveMobToBlock();
			}
		}
		else
		{
			this.reachedTarget = true;
			this.tryTicks--;
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
			target = "Lnet/minecraft/world/entity/animal/Rabbit;level()Lnet/minecraft/world/level/Level;"
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
	 * isTargetPos always returns <code>false</code> when called by {@link #canContinueToUse()}. <br/> Even when there
	 * would be a valid target due to checking for <code>!hasTarget</code>. <br/>
	 * <code>hasTarget</code> is always set to <code>true</code> at this point, exiting the method with
	 * <code>false</code>.
	 */
	@Inject(
		method = "isValidTarget",
		at = @At("HEAD"),
		cancellable = true
	)
	public void isTargetPosDoNotAbortWhenHavingTarget(
		final LevelReader world,
		final BlockPos pos,
		final CallbackInfoReturnable<Boolean> cir)
	{
		if(!this.wantsToRaid)
		{
			cir.setReturnValue(false);
			return;
		}
		
		BlockState blockState = world.getBlockState(pos);
		if(blockState.is(Blocks.FARMLAND))
		{
			blockState = world.getBlockState(pos.above());
			if(blockState.getBlock() instanceof final CarrotBlock carrotsBlock && carrotsBlock.isMaxAge(blockState))
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
	protected boolean findNearestBlock()
	{
		final boolean foundTarget = super.findNearestBlock();
		this.canRaid = foundTarget;
		return foundTarget;
	}
	
	protected RabbitEntityEatCarrotCropGoalMixin(
		final PathfinderMob mob,
		final double speed,
		final int range)
	{
		super(mob, speed, range);
	}
	
	@Shadow
	private boolean wantsToRaid;
	
	@Shadow
	private boolean canRaid;
	
	@Shadow
	@Final
	private Rabbit rabbit;
}

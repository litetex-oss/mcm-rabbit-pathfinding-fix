package net.litetex.rpf.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;


@SuppressWarnings("checkstyle:MagicNumber")
@Mixin(PathNavigation.class)
public abstract class EntityNavigationMixin
{
	@Unique
	private static final double DEFAULT_NODE_TIMEOUT = 200.0;
	@Unique
	private static final double MAX_NODE_TIMEOUT = 4000.0;
	
	/**
	 * Ensures that navigation can never be executed for an infinite time.
	 * <p>
	 * Ensured by setting currentNodeTimeout to a higher value then 0 when there is no movement.
	 * </p>
	 *
	 * @see #resetNodeBreakInfinite(CallbackInfo)
	 */
	@Inject(
		method = "doStuckDetection",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/pathfinder/Path;getNextNodePos()Lnet/minecraft/core/BlockPos;"),
		cancellable = true
	)
	public void checkTimeoutsBreakInfinite(final Vec3 currentPos, final CallbackInfo ci)
	{
		final Vec3i vec3i = this.path.getNextNodePos();
		final long currentWorldTime = this.level.getGameTime();
		if(vec3i.equals(this.timeoutCachedNode))
		{
			this.timeoutTimer = this.timeoutTimer + (currentWorldTime - this.lastTimeoutCheck);
		}
		else
		{
			this.timeoutCachedNode = vec3i;
			final float movementSpeed = this.mob.getSpeed();
			this.timeoutLimit = movementSpeed > 0.0F
				? Math.min(
				currentPos.distanceTo(Vec3.atBottomCenterOf(this.timeoutCachedNode)) / movementSpeed * 20.0,
				MAX_NODE_TIMEOUT) // Set a max timeout to handle situations where movement speed is near zero
				: DEFAULT_NODE_TIMEOUT; // Always set a timeout > 0 when speed is 0
		}
		
		// Ignore currentNodeTimeout != 0
		if(this.timeoutTimer > this.timeoutLimit * 3.0)
		{
			this.timeoutPath();
		}
		
		this.lastTimeoutCheck = currentWorldTime;
		
		ci.cancel();
	}
	
	@Inject(
		method = "resetStuckTimeout",
		at = @At("TAIL")
	)
	public void resetNodeBreakInfinite(final CallbackInfo ci)
	{
		this.timeoutLimit = DEFAULT_NODE_TIMEOUT;
	}
	
	@Shadow
	@Final
	protected Mob mob;
	
	@Shadow
	@Nullable
	protected Path path;
	
	@Shadow
	@Final
	protected Level level;
	
	@Shadow
	protected Vec3i timeoutCachedNode;
	
	@Shadow
	protected long timeoutTimer;
	
	@Shadow
	protected long lastTimeoutCheck;
	
	@Shadow
	protected double timeoutLimit;
	
	@Shadow
	protected abstract void timeoutPath();
}

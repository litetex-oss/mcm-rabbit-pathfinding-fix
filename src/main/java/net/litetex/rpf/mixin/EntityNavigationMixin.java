package net.litetex.rpf.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;


@SuppressWarnings("checkstyle:MagicNumber")
@Mixin(EntityNavigation.class)
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
		method = "checkTimeouts",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/ai/pathing/Path;getCurrentNodePos()Lnet/minecraft/util/math/BlockPos;"),
		cancellable = true
	)
	public void checkTimeoutsBreakInfinite(final Vec3d currentPos, final CallbackInfo ci)
	{
		final Vec3i vec3i = this.currentPath.getCurrentNodePos();
		final long currentWorldTime = this.world.getTime();
		if(vec3i.equals(this.lastNodePosition))
		{
			this.currentNodeMs = this.currentNodeMs + (currentWorldTime - this.lastActiveTickMs);
		}
		else
		{
			this.lastNodePosition = vec3i;
			this.currentNodeTimeout = this.entity.getMovementSpeed() > 0.0F
				? Math.min(
				currentPos.distanceTo(Vec3d.ofBottomCenter(this.lastNodePosition)) / this.entity.getMovementSpeed()
					* 20.0,
				MAX_NODE_TIMEOUT) // Set a max timeout to handle situations where movement speed is near zero
				: DEFAULT_NODE_TIMEOUT; // Always set a timeout > 0 when speed is 0
		}
		
		// Ignore currentNodeTimeout != 0
		if(this.currentNodeMs > this.currentNodeTimeout * 3.0)
		{
			this.resetNodeAndStop();
		}
		
		this.lastActiveTickMs = currentWorldTime;
		
		ci.cancel();
	}
	
	@Inject(
		method = "resetNode",
		at = @At("TAIL")
	)
	public void resetNodeBreakInfinite(final CallbackInfo ci)
	{
		this.currentNodeTimeout = DEFAULT_NODE_TIMEOUT;
	}
	
	@Shadow
	@Final
	protected MobEntity entity;
	
	@Shadow
	@Nullable
	protected Path currentPath;
	
	@Shadow
	@Final
	protected World world;
	
	@Shadow
	protected Vec3i lastNodePosition;
	
	@Shadow
	protected long currentNodeMs;
	
	@Shadow
	protected long lastActiveTickMs;
	
	@Shadow
	protected double currentNodeTimeout;
	
	@Shadow
	protected abstract void resetNodeAndStop();
}

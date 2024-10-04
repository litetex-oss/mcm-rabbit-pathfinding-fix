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
	private static final double DEFAULT_NODE_TIMEOUT = 100.0; // TODO Select good value
	
	/**
	 * Exactly the same as upstream but ensures that this can never be executed for an infinite time.
	 * <p>
	 * Ensured by setting currentNodeTimeout to a higher value then 0 when there is no movement.
	 * </p>
	 *
	 * @see #resetNodeBreakInfinite(CallbackInfo)
	 */
	@Inject(
		method = "checkTimeouts",
		at = @At("HEAD"),
		cancellable = true
	)
	public void checkTimeoutsBreakInfinite(final Vec3d currentPos, final CallbackInfo ci)
	{
		if(this.tickCount - this.pathStartTime > 100)
		{
			final float f = this.entity.getMovementSpeed() >= 1.0F
				? this.entity.getMovementSpeed()
				: this.entity.getMovementSpeed() * this.entity.getMovementSpeed();
			final float g = f * 100.0F * 0.25F;
			if(currentPos.squaredDistanceTo(this.pathStartPos) < (g * g))
			{
				this.nearPathStartPos = true;
				this.stop();
			}
			else
			{
				this.nearPathStartPos = false;
			}
			
			this.pathStartTime = this.tickCount;
			this.pathStartPos = currentPos;
		}
		
		if(this.currentPath != null && !this.currentPath.isFinished())
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
				// Modification here
				this.currentNodeTimeout = this.entity.getMovementSpeed() > 0.0F
					? currentPos.distanceTo(Vec3d.ofBottomCenter(this.lastNodePosition))
					/ this.entity.getMovementSpeed() * 20.0
					: DEFAULT_NODE_TIMEOUT;
			}
			
			// Modification here
			if(this.currentNodeMs > this.currentNodeTimeout * 3.0)
			{
				this.resetNodeAndStop();
			}
			
			this.lastActiveTickMs = currentWorldTime;
		}
		
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
	protected int tickCount;
	
	@Shadow
	protected int pathStartTime;
	
	@Shadow
	@Final
	protected MobEntity entity;
	
	@Shadow
	protected Vec3d pathStartPos;
	
	@Shadow
	private boolean nearPathStartPos;
	
	@Shadow
	public abstract void stop();
	
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

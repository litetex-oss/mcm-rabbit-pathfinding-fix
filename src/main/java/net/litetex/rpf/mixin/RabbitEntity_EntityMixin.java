package net.litetex.rpf.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;


@SuppressWarnings({"checkstyle:VisibilityModifier", "checkstyle:TypeName", "java:S101"})
@Mixin(net.minecraft.world.entity.Entity.class)
public abstract class RabbitEntity_EntityMixin
{
	@Shadow
	public abstract double getY();
	
	@Shadow
	public boolean horizontalCollision;
}

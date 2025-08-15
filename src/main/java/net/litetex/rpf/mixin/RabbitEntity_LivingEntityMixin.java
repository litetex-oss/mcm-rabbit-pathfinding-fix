package net.litetex.rpf.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;


@SuppressWarnings({"checkstyle:VisibilityModifier", "checkstyle:TypeName", "java:S101"})
@Mixin(net.minecraft.entity.LivingEntity.class)
public abstract class RabbitEntity_LivingEntityMixin extends RabbitEntity_EntityMixin
{
	@Shadow
	public abstract float getJumpBoostVelocityModifier();
}

package net.litetex.rpf.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.pathing.EntityNavigation;


@SuppressWarnings({"checkstyle:VisibilityModifier", "checkstyle:TypeName", "java:S101"})
@Mixin(net.minecraft.entity.mob.MobEntity.class)
public abstract class RabbitEntity_MobEntityMixin extends RabbitEntity_EntityMixin
{
	@Shadow
	@Final
	protected GoalSelector goalSelector;
	@Shadow
	protected EntityNavigation navigation;
	@Shadow
	protected MoveControl moveControl;
}

package net.litetex.rpf.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.navigation.PathNavigation;


@SuppressWarnings({"checkstyle:VisibilityModifier", "checkstyle:TypeName", "java:S101"})
@Mixin(net.minecraft.world.entity.Mob.class)
public abstract class RabbitEntity_MobEntityMixin extends RabbitEntity_LivingEntityMixin
{
	@Shadow
	@Final
	protected GoalSelector goalSelector;
	@Shadow
	protected PathNavigation navigation;
	@Shadow
	protected MoveControl moveControl;
}

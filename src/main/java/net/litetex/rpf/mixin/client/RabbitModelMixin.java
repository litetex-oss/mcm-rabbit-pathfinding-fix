package net.litetex.rpf.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.animal.rabbit.RabbitModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.RabbitRenderState;


@Mixin(RabbitModel.class)
public abstract class RabbitModelMixin
{
	@SuppressWarnings("checkstyle:MagicNumber")
	@Inject(
		method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/RabbitRenderState;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Ljava/lang/Object;)V",
			shift = At.Shift.AFTER
		),
		cancellable = true
	)
	void setupAnim(final RabbitRenderState state, final CallbackInfo ci)
	{
		// Always execute this to correctly align the head
		this.head.yRot = state.yRot * (float)(Math.PI / 180.0);
		// Correct the offset caused by the rotated body and "counter"-rotated head of AdultRabbitModel
		this.head.xRot = state.xRot * (float)(Math.PI / 180.0) + this.head.getInitialPose().xRot();
		
		this.idleHeadTiltAnimation.apply(state.idleHeadTiltAnimationState, state.ageInTicks);
		this.hopAnimation.apply(state.hopAnimationState, state.ageInTicks);
		
		ci.cancel();
	}
	
	@Shadow
	@Final
	private ModelPart head;
	
	@Shadow
	@Final
	private KeyframeAnimation hopAnimation;
	
	@Shadow
	@Final
	private KeyframeAnimation idleHeadTiltAnimation;
}

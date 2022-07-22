package com.simibubi.create.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.jozufozu.flywheel.fabric.event.FlywheelEvents;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.KineticDebugger;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.components.fan.AirCurrent;
import com.simibubi.create.content.contraptions.components.steam.SteamEngineBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionHandler;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionHandlerClient;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.ChassisRangeDisplay;
import com.simibubi.create.content.contraptions.components.structureMovement.interaction.controls.ControlsHandler;
import com.simibubi.create.content.contraptions.components.structureMovement.interaction.controls.TrainHUD;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingHandlerClient;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingPhysics;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.CapabilityMinecartController;
import com.simibubi.create.content.contraptions.components.turntable.TurntableHandler;
import com.simibubi.create.content.contraptions.itemAssembly.SequencedAssemblyRecipe;
import com.simibubi.create.content.contraptions.relays.belt.item.BeltConnectorHandler;
import com.simibubi.create.content.curiosities.armor.CopperBacktankArmorLayer;
import com.simibubi.create.content.curiosities.girder.GirderWrenchBehavior;
import com.simibubi.create.content.curiosities.symmetry.SymmetryHandler;
import com.simibubi.create.content.curiosities.toolbox.ToolboxHandlerClient;
import com.simibubi.create.content.curiosities.tools.BlueprintOverlayRenderer;
import com.simibubi.create.content.curiosities.tools.ExtendoGripRenderHandler;
import com.simibubi.create.content.curiosities.zapper.ZapperItem;
import com.simibubi.create.content.curiosities.zapper.terrainzapper.WorldshaperRenderHandler;
import com.simibubi.create.content.logistics.block.depot.EjectorTargetHandler;
import com.simibubi.create.content.logistics.block.display.DisplayLinkBlockItem;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmInteractionPointHandler;
import com.simibubi.create.content.logistics.item.LinkedControllerClientHandler;
import com.simibubi.create.content.logistics.trains.CameraDistanceModifier;
import com.simibubi.create.content.logistics.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.logistics.trains.entity.CarriageCouplingRenderer;
import com.simibubi.create.content.logistics.trains.entity.TrainRelocator;
import com.simibubi.create.content.logistics.trains.management.edgePoint.TrackTargetingClient;
import com.simibubi.create.content.logistics.trains.management.schedule.TrainHatArmorLayer;
import com.simibubi.create.content.logistics.trains.track.CurvedTrackInteraction;
import com.simibubi.create.content.logistics.trains.track.TrackBlockItem;
import com.simibubi.create.content.logistics.trains.track.TrackBlockOutline;
import com.simibubi.create.content.logistics.trains.track.TrackPlacement;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.ui.OpenCreateMenuButton;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.networking.LeftClickPacket;
import com.simibubi.create.foundation.ponder.PonderTooltipHandler;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.sound.SoundScapes;
import com.simibubi.create.foundation.tileEntity.behaviour.edgeInteraction.EdgeInteractionRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.linked.LinkRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueHandler;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.CameraAngleAnimationService;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.foundation.utility.placement.PlacementHelpers;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedClientWorld;

import io.github.fabricators_of_create.porting_lib.event.client.CameraSetupCallback;
import io.github.fabricators_of_create.porting_lib.event.client.CameraSetupCallback.CameraInfo;
import io.github.fabricators_of_create.porting_lib.event.client.DrawSelectionEvents;
import io.github.fabricators_of_create.porting_lib.event.client.EntityAddedLayerCallback;
import io.github.fabricators_of_create.porting_lib.event.common.AttackAirCallback;
import io.github.fabricators_of_create.porting_lib.event.client.ClientWorldEvents;
import io.github.fabricators_of_create.porting_lib.event.client.FogEvents;
import io.github.fabricators_of_create.porting_lib.event.client.FogEvents.ColorData;
import io.github.fabricators_of_create.porting_lib.event.client.OnStartUseItemCallback;
import io.github.fabricators_of_create.porting_lib.event.client.OverlayRenderCallback;
import io.github.fabricators_of_create.porting_lib.event.client.ParticleManagerRegistrationCallback;
import io.github.fabricators_of_create.porting_lib.event.common.MountEntityCallback;
import io.github.fabricators_of_create.porting_lib.event.common.PlayerTickEvents;
import io.github.fabricators_of_create.porting_lib.event.client.RenderHandCallback;
import io.github.fabricators_of_create.porting_lib.event.client.RenderTickStartCallback;
import io.github.fabricators_of_create.porting_lib.event.client.RenderTooltipBorderColorCallback;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback.RegistrationHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ClientEvents {

	private static final String ITEM_PREFIX = "item." + Create.ID;
	private static final String BLOCK_PREFIX = "block." + Create.ID;

	public static void onTickStart(Minecraft client) {
		LinkedControllerClientHandler.tick();
		ControlsHandler.tick();
		AirCurrent.tickClientPlayerSounds();
	}

	public static void onTick(Minecraft client) {
		if (!isGameActive())
			return;

		Level world = Minecraft.getInstance().level;

		SoundScapes.tick();
		AnimationTickHolder.tick();
		ScrollValueHandler.tick();

		CreateClient.SCHEMATIC_SENDER.tick();
		CreateClient.SCHEMATIC_AND_QUILL_HANDLER.tick();
		CreateClient.GLUE_HANDLER.tick();
		CreateClient.SCHEMATIC_HANDLER.tick();
		CreateClient.ZAPPER_RENDER_HANDLER.tick();
		CreateClient.POTATO_CANNON_RENDER_HANDLER.tick();
		CreateClient.SOUL_PULSE_EFFECT_HANDLER.tick(world);
		CreateClient.RAILWAYS.clientTick();

		ContraptionHandler.tick(world);
		CapabilityMinecartController.tick(world);
		CouplingPhysics.tick(world);

		PonderTooltipHandler.tick();
		// ScreenOpener.tick();
		ServerSpeedProvider.clientTick();
		BeltConnectorHandler.tick();
//		BeltSlicer.tickHoveringInformation();
		FilteringRenderer.tick();
		LinkRenderer.tick();
		ScrollValueRenderer.tick();
		ChassisRangeDisplay.tick();
		EdgeInteractionRenderer.tick();
		GirderWrenchBehavior.tick();
		WorldshaperRenderHandler.tick();
		CouplingHandlerClient.tick();
		CouplingRenderer.tickDebugModeRenders();
		KineticDebugger.tick();
		ExtendoGripRenderHandler.tick();
		// CollisionDebugger.tick();
		ArmInteractionPointHandler.tick();
		EjectorTargetHandler.tick();
		PlacementHelpers.tick();
		CreateClient.OUTLINER.tickOutlines();
		CreateClient.GHOST_BLOCKS.tickGhosts();
		ContraptionRenderDispatcher.tick(world);
		BlueprintOverlayRenderer.tick();
		ToolboxHandlerClient.clientTick();
		TrackTargetingClient.clientTick();
		TrackPlacement.clientTick();
		TrainRelocator.clientTick();
		DisplayLinkBlockItem.clientTick();
		CurvedTrackInteraction.clientTick();
		CameraDistanceModifier.tick();
		CameraAngleAnimationService.tick();
		TrainHUD.tick();
	}

	public static boolean onRenderSelection(LevelRenderer context, Camera info, HitResult target, float partialTicks, PoseStack matrix, MultiBufferSource buffers) { return false; }

	public static void onJoin(ClientPacketListener handler, PacketSender sender, Minecraft client) {
		CreateClient.checkGraphicsFanciness();
	}

	public static void onLeave(ClientPacketListener handler, Minecraft client) {
		CreateClient.RAILWAYS.cleanUp();
	}

	public static void onLoadWorld(Minecraft client, ClientLevel world) {
		if (world.isClientSide() && world instanceof ClientLevel && !(world instanceof WrappedClientWorld)) {
			CreateClient.invalidateRenderers();
			AnimationTickHolder.reset();
		}
	}

	public static void onUnloadWorld(Minecraft client, ClientLevel world) {
		if (world
			.isClientSide()) {
			CreateClient.invalidateRenderers();
			CreateClient.SOUL_PULSE_EFFECT_HANDLER.refresh();
			AnimationTickHolder.reset();
			ControlsHandler.levelUnloaded(world);
		}
	}

	public static void onRenderWorld(WorldRenderContext event) {
		Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera()
			.getPosition();
		float pt = AnimationTickHolder.getPartialTicks();

		PoseStack ms = event.matrixStack();
		ms.pushPose();
		ms.translate(-cameraPos.x(), -cameraPos.y(), -cameraPos.z());
		SuperRenderTypeBuffer buffer = SuperRenderTypeBuffer.getInstance();

		TrackBlockOutline.drawCurveSelection(ms, buffer);
		TrackTargetingClient.render(ms, buffer);
		CouplingRenderer.renderAll(ms, buffer);
		CarriageCouplingRenderer.renderAll(ms, buffer);
		CreateClient.SCHEMATIC_HANDLER.render(ms, buffer);
		CreateClient.GHOST_BLOCKS.renderAll(ms, buffer);

		CreateClient.OUTLINER.renderOutlines(ms, buffer, pt);
		buffer.draw();
		RenderSystem.enableCull();

		ms.popPose();
	}

	public static boolean onCameraSetup(CameraInfo info) {
		float partialTicks = AnimationTickHolder.getPartialTicks();

		if (CameraAngleAnimationService.isYawAnimating())
			info.yaw = CameraAngleAnimationService.getYaw(partialTicks);

		if (CameraAngleAnimationService.isPitchAnimating())
			info.pitch = CameraAngleAnimationService.getPitch(partialTicks);
		return false;
	}

	public static RenderTooltipBorderColorCallback.BorderColorEntry getItemTooltipColor(ItemStack stack, int originalBorderColorStart, int originalBorderColorEnd) {
		return PonderTooltipHandler.handleTooltipColor(stack, originalBorderColorStart, originalBorderColorEnd);
	}

	public static void addToItemTooltip(ItemStack stack, TooltipFlag iTooltipFlag, List<Component> itemTooltip) {
		if (!AllConfigs.CLIENT.tooltips.get())
			return;
		if (Minecraft.getInstance().player == null)
			return;

		String translationKey = stack.getItem()
			.getDescriptionId(stack);

		if (translationKey.startsWith(ITEM_PREFIX) || translationKey.startsWith(BLOCK_PREFIX))
			if (TooltipHelper.hasTooltip(stack, Minecraft.getInstance().player)) {
				List<Component> toolTip = new ArrayList<>();
				toolTip.add(itemTooltip.remove(0));
				TooltipHelper.getTooltip(stack)
					.addInformation(toolTip);
				itemTooltip.addAll(0, toolTip);
			}

		if (stack.getItem() instanceof BlockItem) {
			BlockItem item = (BlockItem) stack.getItem();
			if (item.getBlock() instanceof IRotate || item.getBlock() instanceof SteamEngineBlock) {
				List<Component> kineticStats = ItemDescription.getKineticStats(item.getBlock());
				if (!kineticStats.isEmpty()) {
					itemTooltip
						.add(new TextComponent(""));
					itemTooltip
						.addAll(kineticStats);
				}
			}
		}

		PonderTooltipHandler.addToTooltip(itemTooltip, stack);
		SequencedAssemblyRecipe.addToTooltip(itemTooltip, stack);
	}

	public static void onRenderTick() {
		if (!isGameActive())
			return;
		TurntableHandler.gameRenderTick();
	}

	public static InteractionResult onMount(Entity mounted, Entity mounting, boolean isMounting) {
		if (mounting != Minecraft.getInstance().player)
			return InteractionResult.PASS;

		if (!isMounting) {
			CameraDistanceModifier.reset();
			return InteractionResult.PASS;
		}

		if (!isMounting || !(mounted instanceof CarriageContraptionEntity carriage)) {
			return InteractionResult.PASS;
		}

		CameraDistanceModifier.zoomOut();
		return InteractionResult.PASS;
	}

	protected static boolean isGameActive() {
		return !(Minecraft.getInstance().level == null || Minecraft.getInstance().player == null);
	}

	public static float getFogDensity(Camera info, float currentDensity) {
		Level level = Minecraft.getInstance().level;
		BlockPos blockPos = info.getBlockPosition();
		FluidState fluidState = level.getFluidState(blockPos);
		if (info.getPosition().y > blockPos.getY() + fluidState.getHeight(level, blockPos))
			return currentDensity;
		Fluid fluid = fluidState.getType();

		if (AllFluids.CHOCOLATE.get()
			.isSame(fluid)) {
//			event.scaleFarPlaneDistance(1f/32f);
//			event.setCanceled(true);
			return 5f;
		}

		if (AllFluids.HONEY.get()
			.isSame(fluid)) {
//			event.scaleFarPlaneDistance(1f/8f);
//			event.setCanceled(true);
			return 1.5f;
		}

		if (FluidHelper.isWater(fluid) && AllItems.DIVING_HELMET.get()
			.isWornBy(Minecraft.getInstance().cameraEntity)) {
//			event.scaleFarPlaneDistance(6.25f);
//			event.setCanceled(true);
			return 300f;
		}
		return currentDensity;
	}

	public static void getFogColor(ColorData event, float partialTicks) {
		Camera info = event.getCamera();
		Level level = Minecraft.getInstance().level;
		BlockPos blockPos = info.getBlockPosition();
		FluidState fluidState = level.getFluidState(blockPos);
		if (info.getPosition().y > blockPos.getY() + fluidState.getHeight(level, blockPos))
			return;

		Fluid fluid = fluidState.getType();

		if (AllFluids.CHOCOLATE.get()
			.isSame(fluid)) {
			event.setRed(98 / 255f);
			event.setGreen(32 / 255f);
			event.setBlue(32 / 255f);
			return;
		}

		if (AllFluids.HONEY.get()
			.isSame(fluid)) {
			event.setRed(234 / 255f);
			event.setGreen(174 / 255f);
			event.setBlue(47 / 255f);
			return;
		}
	}

	public static void leftClickEmpty(LocalPlayer player) {
		ItemStack stack = player.getMainHandItem();
		if (stack.getItem() instanceof ZapperItem) {
			AllPackets.channel.sendToServer(new LeftClickPacket());
		}
	}

	public static class ModBusEvents {

		public static void registerClientReloadListeners() {
			ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
			if (resourceManager instanceof ReloadableResourceManager reloadable)
				reloadable.registerReloadListener(CreateClient.RESOURCE_RELOAD_LISTENER);
		}

//		@SubscribeEvent
//		public static void addEntityRendererLayers(EntityRenderersEvent.AddLayers event) {
//			EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
//			CopperBacktankArmorLayer.registerOnAll(dispatcher);
//		}

//		@SubscribeEvent
//		public static void loadCompleted(FMLLoadCompleteEvent event) {
//			ModContainer createContainer = ModList.get()
//				.getModContainerById(Create.ID)
//				.orElseThrow(() -> new IllegalStateException("Create Mod Container missing after loadCompleted"));
//			createContainer.registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
//				() -> new ConfigGuiHandler.ConfigGuiFactory((mc, previousScreen) -> BaseConfigScreen.forCreate(previousScreen)));
//		}

	}

	public static void addEntityRendererLayers(EntityType<? extends LivingEntity> entityType, LivingEntityRenderer<?, ?> entityRenderer,
											   RegistrationHelper registrationHelper, EntityRendererProvider.Context context) {
		CopperBacktankArmorLayer.registerOn(entityRenderer, registrationHelper);
		TrainHatArmorLayer.registerOn(entityRenderer, registrationHelper);
	}

	public static void register() {
		ModBusEvents.registerClientReloadListeners();

		ClientTickEvents.END_CLIENT_TICK.register(ClientEvents::onTick);
		ClientTickEvents.START_CLIENT_TICK.register(ClientEvents::onTickStart);
		ClientTickEvents.END_WORLD_TICK.register(CommonEvents::onWorldTick);
		ClientWorldEvents.LOAD.register(ClientEvents::onLoadWorld);
		ClientWorldEvents.UNLOAD.register(ClientEvents::onUnloadWorld);
		ClientChunkEvents.CHUNK_UNLOAD.register(CommonEvents::onChunkUnloaded);
		ClientPlayConnectionEvents.JOIN.register(ClientEvents::onJoin);
		ClientEntityEvents.ENTITY_LOAD.register(CommonEvents::onEntityAdded);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(ClientEvents::onRenderWorld);
		ItemTooltipCallback.EVENT.register(ClientEvents::addToItemTooltip);
		FogEvents.SET_DENSITY.register(ClientEvents::getFogDensity);
		FogEvents.SET_COLOR.register(ClientEvents::getFogColor);
		RenderTickStartCallback.EVENT.register(ClientEvents::onRenderTick);
		RenderTooltipBorderColorCallback.EVENT.register(ClientEvents::getItemTooltipColor);
		AttackAirCallback.EVENT.register(ClientEvents::leftClickEmpty);
		UseBlockCallback.EVENT.register(TrackBlockItem::sendExtenderPacket);
		MountEntityCallback.EVENT.register(ClientEvents::onMount);
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register(ClientEvents::addEntityRendererLayers);
		CameraSetupCallback.EVENT.register(ClientEvents::onCameraSetup);

		// External Events

		ClientTickEvents.END_CLIENT_TICK.register(SymmetryHandler::onClientTick);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(SymmetryHandler::render);
		UseBlockCallback.EVENT.register(ArmInteractionPointHandler::rightClickingBlocksSelectsThem);
		UseBlockCallback.EVENT.register(EjectorTargetHandler::rightClickingBlocksSelectsThem);
		AttackBlockCallback.EVENT.register(ArmInteractionPointHandler::leftClickingBlocksDeselectsThem);
		AttackBlockCallback.EVENT.register(EjectorTargetHandler::leftClickingBlocksDeselectsThem);
		ParticleManagerRegistrationCallback.EVENT.register(AllParticleTypes::registerFactories);
		RenderHandCallback.EVENT.register(ExtendoGripRenderHandler::onRenderPlayerHand);
		OnStartUseItemCallback.EVENT.register(ContraptionHandlerClient::rightClickingOnContraptionsGetsHandledLocally);
		PlayerTickEvents.END.register(ContraptionHandlerClient::preventRemotePlayersWalkingAnimations);
		OverlayRenderCallback.EVENT.register(PlacementHelpers::afterRenderOverlayLayer);
		ClientPlayConnectionEvents.DISCONNECT.register(ClientEvents::onLeave);
		DrawSelectionEvents.BLOCK.register(ClientEvents::onRenderSelection);
		DrawSelectionEvents.BLOCK.register(TrackBlockOutline::drawCustomBlockSelection);
		DrawSelectionEvents.ENTITY.register(ClientEvents::onRenderSelection);
		// we need to add our config button after mod menu, so we register our event with a phase that comes later
		ResourceLocation latePhase = Create.asResource("late");
		ScreenEvents.AFTER_INIT.addPhaseOrdering(Event.DEFAULT_PHASE, latePhase);
		ScreenEvents.AFTER_INIT.register(latePhase, OpenCreateMenuButton.OpenConfigButtonHandler::onGuiInit);

		// Flywheel Events

		FlywheelEvents.BEGIN_FRAME.register(ContraptionRenderDispatcher::beginFrame);
		FlywheelEvents.RENDER_LAYER.register(ContraptionRenderDispatcher::renderLayer);
		FlywheelEvents.RELOAD_RENDERERS.register(ContraptionRenderDispatcher::onRendererReload);
		FlywheelEvents.GATHER_CONTEXT.register(ContraptionRenderDispatcher::gatherContext);
	}

}

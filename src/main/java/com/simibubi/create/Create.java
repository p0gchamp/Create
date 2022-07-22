package com.simibubi.create;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import com.simibubi.create.compat.Mods;
import com.simibubi.create.compat.trinkets.Trinkets;
import com.tterrag.registrate.fabric.GatherDataEvent;

import io.github.tropheusj.milk.Milk;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.simibubi.create.api.behaviour.BlockSpoutingBehaviour;
import com.simibubi.create.content.CreateItemGroup;
import com.simibubi.create.content.contraptions.TorquePropagator;
import com.simibubi.create.content.contraptions.fluids.tank.BoilerHeaters;
import com.simibubi.create.content.curiosities.deco.SlidingDoorBlock;
import com.simibubi.create.content.curiosities.weapons.BuiltinPotatoProjectileTypes;
import com.simibubi.create.content.logistics.RedstoneLinkNetworkHandler;
import com.simibubi.create.content.logistics.block.display.AllDisplayBehaviours;
import com.simibubi.create.content.logistics.block.mechanicalArm.AllArmInteractionPointTypes;
import com.simibubi.create.content.logistics.trains.GlobalRailwayManager;
import com.simibubi.create.content.palettes.AllPaletteBlocks;
import com.simibubi.create.content.palettes.PalettesItemGroup;
import com.simibubi.create.content.schematics.SchematicProcessor;
import com.simibubi.create.content.schematics.ServerSchematicLoader;
import com.simibubi.create.content.schematics.filtering.SchematicInstances;
import com.simibubi.create.events.CommonEvents;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.block.CopperRegistries;
import com.simibubi.create.foundation.command.ServerLagger;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.LangMerger;
import com.simibubi.create.foundation.data.recipe.MechanicalCraftingRecipeGen;
import com.simibubi.create.foundation.data.recipe.ProcessingRecipeGen;
import com.simibubi.create.foundation.data.recipe.SequencedAssemblyRecipeGen;
import com.simibubi.create.foundation.data.recipe.StandardRecipeGen;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.worldgen.AllWorldFeatures;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.common.data.ExistingFileHelper;

public class Create implements ModInitializer {

	public static final String ID = "create";
	public static final String NAME = "Create";
	public static final String VERSION = "0.5c";

	public static final Logger LOGGER = LogManager.getLogger();

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting()
		.disableHtmlEscaping()
		.create();

	public static final CreativeModeTab BASE_CREATIVE_TAB = new CreateItemGroup();
	public static final CreativeModeTab PALETTES_CREATIVE_TAB = new PalettesItemGroup();

	public static final ServerSchematicLoader SCHEMATIC_RECEIVER = new ServerSchematicLoader();
	public static final RedstoneLinkNetworkHandler REDSTONE_LINK_NETWORK_HANDLER = new RedstoneLinkNetworkHandler();
	public static final TorquePropagator TORQUE_PROPAGATOR = new TorquePropagator();
	public static final GlobalRailwayManager RAILWAYS = new GlobalRailwayManager();
	public static final ServerLagger LAGGER = new ServerLagger();
	public static final Random RANDOM = new Random();

	private static final NonNullSupplier<CreateRegistrate> REGISTRATE = CreateRegistrate.lazy(ID);

	@Override
	public void onInitialize() {
		onCtor();
	}

	public static void onCtor() {
		AllConfigs.register();
		AllSoundEvents.prepare();
		AllBlocks.register();
		AllItems.register();
		AllFluids.register();
		AllTags.register();
		AllPaletteBlocks.register();
		AllContainerTypes.register();
		AllEntityTypes.register();
		AllTileEntities.register();
		AllMovementBehaviours.registerDefaults();
		AllInteractionBehaviours.registerDefaults();
		AllDisplayBehaviours.registerDefaults();
		AllArmInteractionPointTypes.register();
		AllWorldFeatures.register();
		AllEnchantments.register();
		BlockSpoutingBehaviour.register();

		Milk.enableMilkFluid();

//		GatherDataEvent.EVENT.register(Create::gatherData);

		AllRecipeTypes.register();
		AllParticleTypes.register();
		AllSoundEvents.register();

//		forgeEventBus.register(CHUNK_UTIL);
//		CHUNK_UTIL.fabricInitEvents();

		// handled as ClientModInitializer
//		EnvExecutor.runWhenOn(EnvType.CLIENT,
//			() -> () -> CreateClient.onCtorClient(modEventBus, forgeEventBus));


		REGISTRATE.get().register();
		CopperRegistries.inject();
		init();
		CommonEvents.register();
		AllWorldFeatures.registerOreFeatures();

		AllEntityDataSerializers.register();

		AllPackets.channel.initServerListener();

		// causes class loading issues or something
		// noinspection Convert2MethodRef
		Mods.TRINKETS.executeIfInstalled(() -> () -> Trinkets.init());
	}

	public static void init() {
		AllPackets.registerPackets();
		SchematicInstances.register();
		BuiltinPotatoProjectileTypes.register();

//		event.enqueueWork(() -> {
			AllAdvancements.register();
			AllTriggers.register();
			SchematicProcessor.register();
			AllWorldFeatures.registerFeatures();
			AllWorldFeatures.registerPlacementTypes();
			BoilerHeaters.registerDefaults();
//		});
	}

	public static void gatherData(FabricDataGenerator gen, ExistingFileHelper helper) {
		gen.addProvider(new AllAdvancements(gen));
		gen.addProvider(new LangMerger(gen));
		gen.addProvider(AllSoundEvents.provider(gen));
		gen.addProvider(new StandardRecipeGen(gen));
		gen.addProvider(new MechanicalCraftingRecipeGen(gen));
		gen.addProvider(new SequencedAssemblyRecipeGen(gen));
		ProcessingRecipeGen.registerAll(gen);
	}

	public static CreateRegistrate registrate() {
		return REGISTRATE.get();
	}

	public static ResourceLocation asResource(String path) {
		return new ResourceLocation(ID, path);
	}

}

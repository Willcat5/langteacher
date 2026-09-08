package willits.langteacher;

import java.nio.file.Path;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;

public class LangteacherClient implements ClientModInitializer {
	public static final String MOD_ID = "langteacher";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final LangteacherConfig CONFIG = new LangteacherConfig();
	public static final ChatLoop CHAT_LOOP = new ChatLoop();

	private static final KeyMapping.Category KEY_CATEGORY =
			KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "langteacher"));

	private static Path configPath;

	@Override
	public void onInitializeClient() {
		configPath = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json");
		LangteacherConfig loaded = LangteacherConfig.load(configPath);
		CONFIG.enabled = loaded.enabled;
		CONFIG.showHud = loaded.showHud;
		CONFIG.intervalSeconds = loaded.intervalSeconds;
		CONFIG.command = loaded.command;
		CONFIG.messages = loaded.messages;

		KeyMapping openScreenKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key." + MOD_ID + ".open",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_H,
				KEY_CATEGORY));

		HudRenderer.register();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			CHAT_LOOP.tick();
			while (openScreenKey.consumeClick()) {
				Minecraft.getInstance().setScreen(openConfigScreen());
			}
		});
	}

	public static Screen openConfigScreen() {
		return openConfigScreen(Minecraft.getInstance().screen);
	}

	public static Screen openConfigScreen(Screen parent) {
		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Component.literal("Langteacher Settings"))
				.setSavingRunnable(() -> LangteacherConfig.save(configPath, CONFIG));

		ConfigEntryBuilder entryBuilder = builder.entryBuilder();
		ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));

		general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Enabled"), CONFIG.enabled)
				.setDefaultValue(false)
				.setTooltip(Component.literal("When enabled, the loop runs while connected to a world."))
				.setSaveConsumer(value -> CONFIG.enabled = value)
				.build());

		general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Show HUD"), CONFIG.showHud)
				.setDefaultValue(true)
				.setTooltip(Component.literal("Show cycle, next command and countdown on screen."))
				.setSaveConsumer(value -> CONFIG.showHud = value)
				.build());

		general.addEntry(entryBuilder.startIntField(Component.literal("Interval (seconds)"), CONFIG.intervalSeconds)
				.setDefaultValue(LangteacherConfig.DEFAULT_INTERVAL)
				.setMin(1)
				.setTooltip(Component.literal("Time between each command + message cycle."))
				.setSaveConsumer(value -> CONFIG.intervalSeconds = value)
				.build());

		general.addEntry(entryBuilder.startTextField(Component.literal("Command"), CONFIG.command)
				.setDefaultValue("/list")
				.setTooltip(Component.literal("Executed as a player command every cycle. Use multiple commands separated by commas; one is sent per cycle in rotation. Prefix with /"))
				.setSaveConsumer(value -> CONFIG.command = value)
				.build());

		for (int i = 0; i < 3; i++) {
			String value = i < CONFIG.messages.size() ? CONFIG.messages.get(i) : "";
			int index = i;
			general.addEntry(entryBuilder.startTextField(
					Component.literal("Message " + (index + 1)), value)
					.setDefaultValue("")
					.setSaveConsumer(v -> {
						while (CONFIG.messages.size() < 3) {
							CONFIG.messages.add("");
						}
						CONFIG.messages.set(index, v);
					})
					.build());
		}

		return builder.build();
	}
}

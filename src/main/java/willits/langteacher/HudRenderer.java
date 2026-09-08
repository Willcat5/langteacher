package willits.langteacher;

import java.util.Locale;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class HudRenderer {
	private static final int PADDING = 4;
	private static final int LINE_HEIGHT = 9;
	private static final int TEXT_COLOR = 0xFFFFFFFF;

	private HudRenderer() {
	}

	public static void register() {
		HudRenderCallback.EVENT.register(HudRenderer::render);
	}

	private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
		LangteacherConfig config = LangteacherClient.CONFIG;
		ChatLoop loop = LangteacherClient.CHAT_LOOP;

		if (!config.showHud || !loop.isActive()) {
			return;
		}

		Minecraft client = Minecraft.getInstance();
		int x = PADDING;
		int y = PADDING;

		y = drawLine(graphics, client, x, y, Component.literal("Cycle: " + loop.currentCycle()
				+ "  |  next in " + String.format(Locale.ROOT, "%.1fs", loop.secondsUntilNext())));

		String next = loop.nextCommand();
		if (next.isEmpty()) {
			next = "(none)";
		}
		drawLine(graphics, client, x, y, Component.literal("Next command: " + next));
	}

	private static int drawLine(GuiGraphics graphics, Minecraft client, int x, int y, Component text) {
		graphics.drawString(client.font, text, x, y, TEXT_COLOR);
		return y + LINE_HEIGHT + 1;
	}
}

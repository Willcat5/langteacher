package willits.langteacher;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

public class ChatLoop {
	private static final int STAGGER_TICKS = 10;
	private static final float TICKS_PER_SECOND = 20.0f;

	private boolean wasActive;
	private float elapsedSeconds;
	private int commandIndex;
	private int cycleNumber;
	private final Queue<ScheduledSend> pending = new ArrayDeque<>();

	private record ScheduledSend(int delayTicks, String text) {
	}

	public boolean isActive() {
		return wasActive;
	}

	public int currentCycle() {
		return cycleNumber;
	}

	public float secondsUntilNext() {
		return Math.max(0, LangteacherClient.CONFIG.intervalSeconds() - elapsedSeconds);
	}

	public String nextCommand() {
		List<String> commands = parseCommands(LangteacherClient.CONFIG.command());
		if (commands.isEmpty()) {
			return "";
		}
		return commands.get(commandIndex % commands.size());
	}

	public void tick() {
		Minecraft client = Minecraft.getInstance();
		LangteacherConfig config = LangteacherClient.CONFIG;

		boolean active = config.enabled
				&& client != null
				&& client.player != null
				&& client.getConnection() != null;

		if (active != wasActive) {
			wasActive = active;
			elapsedSeconds = 0;
			commandIndex = 0;
			cycleNumber = 0;
			pending.clear();
			if (active) {
				enqueueCycle(config);
			}
		}

		if (!active) {
			return;
		}

		processPending(client);

		elapsedSeconds += 1.0f / TICKS_PER_SECOND;
		if (elapsedSeconds >= config.intervalSeconds()) {
			elapsedSeconds = 0;
			enqueueCycle(config);
		}
	}

	private void enqueueCycle(LangteacherConfig config) {
		cycleNumber++;
		List<String> commands = parseCommands(config.command());
		if (!commands.isEmpty()) {
			String command = commands.get(commandIndex % commands.size());
			commandIndex = (commandIndex + 1) % commands.size();
			pending.add(new ScheduledSend(0, command));
		}

		int delay = STAGGER_TICKS;
		for (String message : config.messages()) {
			pending.add(new ScheduledSend(delay, message));
			delay += STAGGER_TICKS;
		}
	}

	private static List<String> parseCommands(String raw) {
		List<String> commands = new ArrayList<>();
		if (raw == null) {
			return commands;
		}
		for (String part : raw.split(",")) {
			String trimmed = part.trim();
			if (!trimmed.isEmpty()) {
				commands.add(trimmed);
			}
		}
		return commands;
	}

	private void processPending(Minecraft client) {
		if (pending.isEmpty()) {
			return;
		}

		List<ScheduledSend> due = new ArrayList<>();
		int count = pending.size();
		for (int i = 0; i < count; i++) {
			ScheduledSend send = pending.poll();
			if (send.delayTicks() <= 0) {
				due.add(send);
			} else {
				pending.add(new ScheduledSend(send.delayTicks() - 1, send.text()));
			}
		}

		for (ScheduledSend send : due) {
			sendNow(client, send.text());
		}
	}

	private void sendNow(Minecraft client, String text) {
		if (text.isEmpty()) {
			return;
		}

		ClientPacketListener connection = client.getConnection();
		if (connection == null) {
			return;
		}

		try {
			if (text.startsWith("/")) {
				connection.sendCommand(text.substring(1));
			} else {
				connection.sendChat(text);
			}
		} catch (Exception e) {
			LangteacherClient.LOGGER.warn("Failed to send chat text", e);
		}
	}
}

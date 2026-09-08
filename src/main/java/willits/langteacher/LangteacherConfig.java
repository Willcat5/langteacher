package willits.langteacher;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LangteacherConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static final int DEFAULT_INTERVAL = 60;

	public boolean enabled = false;
	public boolean showHud = true;
	public int intervalSeconds = DEFAULT_INTERVAL;
	public String command = "/list";
	public List<String> messages = new ArrayList<>();

	public LangteacherConfig() {
		for (int i = 0; i < 3; i++) {
			messages.add("");
		}
	}

	public int intervalSeconds() {
		return Math.max(1, intervalSeconds);
	}

	public List<String> messages() {
		List<String> out = new ArrayList<>();
		for (int i = 0; i < Math.min(3, messages.size()); i++) {
			String m = messages.get(i);
			if (m != null && !m.isBlank()) {
				out.add(m);
			}
		}
		return out;
	}

	public String command() {
		return command == null ? "" : command.trim();
	}

	public static LangteacherConfig load(Path path) {
		LangteacherConfig config = new LangteacherConfig();
		if (Files.isRegularFile(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				LangteacherConfig parsed = GSON.fromJson(reader, LangteacherConfig.class);
				if (parsed != null) {
					if (parsed.messages == null) {
						parsed.messages = new ArrayList<>();
					}
					while (parsed.messages.size() < 3) {
						parsed.messages.add("");
					}
					return parsed;
				}
			} catch (IOException e) {
				LangteacherClient.LOGGER.warn("Failed to read langteacher config at {}", path, e);
			}
		}
		return config;
	}

	public static void save(Path path, LangteacherConfig config) {
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path)) {
				GSON.toJson(config, writer);
			}
		} catch (IOException e) {
			LangteacherClient.LOGGER.warn("Failed to write langteacher config at {}", path, e);
		}
	}
}

package commands;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.text.StringSubstitutor;

import com.beust.jcommander.Parameter;

public class GenerateConfigCommand {

	@Parameter(names = "-t")
	private String templatePath;

	@Parameter(names = "-v")
	private String valuesPath;

	@Parameter(names = "-o")
	private String outputPath;

	private String template;

	private Properties values;

	private void loadTemplate() {

		try {
			template = new String(Files.readAllBytes(Paths.get(templatePath)), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadValues() {
		try (FileInputStream fileInputStream = new FileInputStream(valuesPath)) {
			values = new Properties();
			values.load(fileInputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String substitute() {
		Map<String, Object> valuesMap = new HashMap<>();

		for (String key : values.stringPropertyNames()) {
			valuesMap.put(key, values.getProperty(key));
		}

		return StringSubstitutor.replace(template, valuesMap);
	}

	private void output(String content) {
		try {
			Files.write(Paths.get(outputPath), content.getBytes(),
					new StandardOpenOption[] { StandardOpenOption.CREATE });
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void execute() {
		loadTemplate();
		loadValues();
		String content = substitute();
		output(content);
	}
}

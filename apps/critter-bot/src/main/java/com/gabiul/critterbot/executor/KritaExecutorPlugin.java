package com.gabiul.critterbot.executor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class KritaExecutorPlugin implements ExecutorPlugin {

	private final String critterFolder = "c:/critter";
	private final String critterTemp = "temp";

	@Override
	public boolean supports(ExecutorType type) {
		return type.equals(ExecutorType.KRITA);
	}

	@Override
	public File execute(String userId, String scriptName, String[] argsInput) {

		//execute and return userId.txt or userId.png
		//kritarunner scriptName userId argsInput

		if(!Pattern.matches("[0-9A-Za-z@.]+", scriptName))
		{
			throw new IllegalArgumentException();
		}

		for (String arg : argsInput) {
			if(!Pattern.matches("[0-9A-Za-z@.]+", arg))
			{
				throw new IllegalArgumentException();
			}
		}

		//TODO validate params with .ini
		//TODO copy .py to roaming/kritarunner/pykrita

		String argsstring = "";
		int i = 0;
		for(String arg : argsInput)
		{
			argsstring += (i != argsInput.length - 1?" ":"") + "\"" + arg + "\"";
			i++;
		}

		String kritarunnerstring = "kritarunner -s \"" + scriptName + "\" \"" + userId + "\" " + argsstring;

		Process p;
		try {
			p = Runtime.getRuntime().exec(kritarunnerstring);
		} catch (IOException e1) {
			System.out.println("Problem executing kritarunner.");
			return null;
		}

		System.out.println(kritarunnerstring);

		try {
			p.waitFor();
		} catch (InterruptedException e) {
			return new File(critterFolder + "/failedWait.txt");
		}

		//get first userId.gif/png/etc and return it
		try {
			Path pat = Files.walk(Paths.get(critterFolder + "/" + critterTemp + "/"))
				.filter(path -> Pattern.matches("^" + userId + "+\\.[a-z]+",  path.getFileName().toString()))
				.findFirst().orElse(null);

			if(pat != null)
			{
				File file = new File(pat.toString());
				if(file.exists())
					return file;
			}

		} catch (IOException e) {
			return new File(critterFolder + "/failedWait.txt");
		}
		return new File(critterFolder + "/failedWait.txt");
	}

	@Override
	public boolean delete(File outputFile) {
		//delete file after upload
		boolean worked = outputFile.delete();
		if(!worked)
		{
			System.out.println("Failed to delete file.");
			return false;
		}
		return true;
	}
}

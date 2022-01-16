package com.gabiul.critterbot.executor;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class KritaExecutorPlugin implements ExecutorPlugin {

	private final static String critterFolder = "c:/critter";
	private final static String critterTemp = "temp";

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
			argsstring += (i == argsInput.length - 1?" ":"") + "\"" + arg + "\"";
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

		File file = new File(critterFolder + "/" + critterTemp + "/" + userId + ".png");

		if(file.exists())
			return file;

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

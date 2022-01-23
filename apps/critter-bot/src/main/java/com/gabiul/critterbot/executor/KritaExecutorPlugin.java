package com.gabiul.critterbot.executor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class KritaExecutorPlugin implements ExecutorPlugin {

	private final String critterFolder = "~/critter";
	private final String critterTemp = "tmp";

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
		List<String> argsstringarray = new ArrayList<String>();

		argsstringarray.add("/bin/bash");
		argsstringarray.add("-c");

		int i = 0;
		for(String arg : argsInput)
		{
			argsstring += (i != argsInput.length - 1?" ":"") + "\"" + arg + "\"";
			i++;
		}

		argsstringarray.add("xvfb-run kritarunner -s \"" + scriptName + "\" \"" + userId + "\" " + argsstring);

		String kritarunnerstring = StringUtils.collectionToDelimitedString(argsstringarray, " ");
		System.out.println(kritarunnerstring);
		//System.out.println(kritarunnerstring);

		//ProcessBuilder pb = new ProcessBuilder("bash", "-c", "set");

		ProcessBuilder pb= new ProcessBuilder(argsstringarray);
		pb.environment().put("PWD", "/");

		try {
			Process p = pb.start();
			int exitcode = p.waitFor();
		} catch (IOException e1) {
			System.out.println("IO problem executing kritarunner.");
			e1.printStackTrace();
			return new File(critterFolder + "/failedWait.txt");
		} catch (InterruptedException e) {
			System.out.println("Interrupted kritarunner.");
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
				{
					System.out.print("Generation done: " + pat.getFileName() + "\n");
					return file;
				}
			}

		} catch (IOException e) {
			System.out.println("IOException when trying to get userId.extension file");
			return new File(critterFolder + "/failedWait.txt");
		}
		System.out.println("Never returned file");
		return new File(critterFolder + "/failedWait.txt");
	}

	@Override
	public boolean deleteCache(String userId) {
		//delete file after upload

		try {
			Path pat = Files.walk(Paths.get(critterFolder + "/" + critterTemp + "/"))
				.filter(path -> Pattern.matches("^" + userId + "+\\.[a-z]+",  path.getFileName().toString()))
				.findFirst().orElse(null);

			if(pat != null)
			{
				File file = new File(pat.toString());
				if(file.exists())
				{
					file.delete();
					return true;
				}
			}

		} catch (IOException e) {
			return false;
		}

		return false;
	}
}

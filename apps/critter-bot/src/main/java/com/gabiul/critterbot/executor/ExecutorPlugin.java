package com.gabiul.critterbot.executor;

import java.io.File;
import java.io.IOException;

import org.springframework.plugin.core.Plugin;

public interface ExecutorPlugin extends Plugin<ExecutorType> {

	public File execute(String userId, String scriptName, String[] args);
	public boolean deleteCache(String userId);

}

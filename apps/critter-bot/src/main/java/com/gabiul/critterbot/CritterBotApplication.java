package com.gabiul.critterbot;

import com.gabiul.critterbot.executor.ExecutorPlugin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.plugin.core.config.EnablePluginRegistries;

@SpringBootApplication
public class CritterBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(CritterBotApplication.class, args);
	}

}

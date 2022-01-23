package com.gabiul.critterbot.bot;

import java.io.File;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;

import com.gabiul.critterbot.executor.ExecutorType;
import com.gabiul.critterbot.executor.ExecutorPlugin;

import org.apache.commons.collections4.sequence.EditCommand;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.plugin.core.config.EnablePluginRegistries;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

@Service
@PropertySource("file:${user.home}/critter/secure.properties")
public class BotService implements InitializingBean {

	@Value("${discord.bottoken}")
	private String bottoken;

	private JDA botApi;

	@Autowired
	private BotListenerImpl coreListenerImpl;

	public void start() throws Exception
	{
		JDABuilder builder = JDABuilder.createDefault(bottoken);

		builder.addEventListeners(coreListenerImpl);

		botApi = builder.build();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		start();
	}

}

@Component
@EnablePluginRegistries(ExecutorPlugin.class)
class BotListenerImpl extends ListenerAdapter {

	private final char prefix = '*';

	@Autowired
	private PluginRegistry<ExecutorPlugin, ExecutorType> executorPluginReg;

	@Override
	public void onMessageReceived(@Nonnull MessageReceivedEvent event)
	{
		Message message = event.getMessage();
		User author = event.getAuthor();

		String messageRaw = message.getContentRaw();

		String[] commandSplit = parseCommandSplit(messageRaw);
		if(commandSplit.length > 0)
		{
			if(commandSplit[0].charAt(0) == prefix)
			{
				String command = parseCommandFromCommandSplit(commandSplit);
				String script = parseScriptFromCommandSplit(commandSplit);
				String[] args = parseArgsFromCommandSplit(commandSplit);

				try{
					final ExecutorPlugin ep = getPluginForCommandType(ExecutorType.valueOf(command.toUpperCase()));

					String argsstring = "";
					int i = 0;
					for(String arg : args)
					{
						argsstring += (i != args.length - 1?" ":"") + arg;
						i++;
					}

					MessageAction respond = event.getChannel().sendMessage( "[" + command.toUpperCase() + "] Script '" + script + "' currently processing with params '" + argsstring + "' requested by " + message.getAuthor().getAsMention());
					respond.queue();

					File file = ep.execute(author.getId(), script, args);

					event.getChannel().sendMessage("[" + command.toUpperCase() + "] Script '" + script + "' generated for " + author.getAsMention() + ". Here is your meme:").addFile(file).queue((msg2) -> {
						if(file.getPath().contains(".txt"))
							return;

						ep.deleteCache(author.getId());
					});


				}
				catch(IllegalArgumentException iae)
				{
					event.getChannel().sendMessage("[Wrong] " + author.getAsMention() + " invalid command: '" + messageRaw + "'").queue();
					return;
				}
			}
		}
	}

	private ExecutorPlugin getPluginForCommandType(ExecutorType executorType) throws UnsupportedOperationException
	{
		Optional<ExecutorPlugin> pluginFor = executorPluginReg.getPluginFor(executorType);
		if (pluginFor.isPresent()) {
			return pluginFor.get();
		}

		throw new UnsupportedOperationException("Unsupported executorType: "+ executorType.toString().toLowerCase() + ".");
	}

	private String[] parseCommandSplit(String command)
	{
		//TODO parse within quotes ""
		return command.split(" ");
	}

	private String parseCommandFromCommandSplit(String[] commandSplit)
	{
		return commandSplit[0].substring(1, commandSplit[0].length()).toLowerCase();
	}

	private String parseScriptFromCommandSplit(String[] commandSplit)
	{
		int scriptIndex = 1;
		return commandSplit[scriptIndex].toLowerCase();
	}

	private String[] parseArgsFromCommandSplit(String[] commandSplit)
	{
		int skipFirst = 2; //command, script, args
		String[] args = new String[commandSplit.length - skipFirst];
		for(int i = skipFirst; i < commandSplit.length; i++)
		{
			args[i - skipFirst] = commandSplit[i];
		}
		return args;
	}
}


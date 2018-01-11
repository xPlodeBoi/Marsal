package io.ph.bot;

import java.io.File;

import javax.security.auth.login.LoginException;

import io.ph.bot.commands.CommandHandler;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

/**
 * Main entry point
 * @author Paul
 *
 */
public class Launcher {
	public static void main(String[] args) throws LoginException, IllegalArgumentException, InterruptedException, RateLimitedException {
		CommandHandler.initCommands();
		Bot.getInstance().start(args);
	}
	
	static {
		new File("resources/feeds").mkdirs();
		new File("resources/cache").mkdirs();
		new File("resources/tempdownloads").mkdirs();
	}
	
const Discord = require('discord.js');
const client = new Discord.Client();

client.on('ready', () => {
    console.log('I am ready!');
});

client.on('message', message => {
    if (message.content === 'ping') {
    	message.reply('pong');
  	}
});

client.login(process.env.BOT_TOKEN);
}

package io.ph.bot.commands.owner;

import java.awt.Color;
import java.lang.management.ManagementFactory;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import io.ph.bot.Bot;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandCategory;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
/**
 * Diagnostics about the bot
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "diagnostics",
		aliases = {},
		category = CommandCategory.BOT_OWNER,
		permission = Permission.BOT_OWNER,
		description = "Diagnostic information on the bot",
		example = "(no parameters)"
		)
public class Diagnostics extends Command {
	public static Map<Integer, Integer> currentMusic = new HashMap<>();
	
	@Override
	public void executeCommand(Message msg) {
		AtomicInteger guildCounter = new AtomicInteger();
		AtomicInteger userCounter = new AtomicInteger();
		AtomicInteger botCounter = new AtomicInteger();
		AtomicInteger textCounter = new AtomicInteger();
		AtomicInteger voiceCounter = new AtomicInteger();
		AtomicLong responseCounter = new AtomicLong();
		Bot.getInstance().getBots().stream()
		.forEach(j -> {
			guildCounter.addAndGet(j.getGuilds().size());
			userCounter.addAndGet(j.getUsers().size());
			botCounter.addAndGet((int) j.getUsers().stream()
					.filter(u -> u.isBot())
					.count());
			textCounter.addAndGet(j.getTextChannels().size());
			voiceCounter.addAndGet(j.getVoiceChannels().size());
			responseCounter.addAndGet(j.getResponseTotal());
		});
		
		EmbedBuilder em = new EmbedBuilder();
		String title = msg.getGuild().getMember(msg.getJDA().getSelfUser()).getEffectiveName() + " diagnostics";
		if (msg.getJDA().getShardInfo() != null) {
			title += msg.getJDA().getShardInfo().getShardString();
		}
		em.setAuthor(title,	null, msg.getJDA().getSelfUser().getAvatarUrl());
		Runtime r = Runtime.getRuntime();
		NumberFormat format = NumberFormat.getInstance();
		em.addField("Connected guilds", guildCounter.intValue() + "", true);
		em.addField("Connected users", userCounter.intValue() + " (" + botCounter.intValue() + " bots)", true);
		em.addField("Total text channels", textCounter.intValue() + "", true);
		em.addField("Total voice channels", voiceCounter.intValue() + "", true);
		em.addField("Memory usage", format.format(r.totalMemory() / (1024 * 1024)) + "MB", true);
		em.addField("CPU usage", getCpuLoad() + "%", true);
		em.addField("Threads", Thread.activeCount() + "", true);
		em.addField("Playing music", String.format("%d/%d",
				playingMusic(), guildCounter.intValue()), true);
		AtomicLong responses = new AtomicLong();
		Bot.getInstance().getBots().stream()
		.forEach(j -> responses.addAndGet(j.getResponseTotal()));
		em.addField("Response count", msg.getJDA().getResponseTotal() 
				+ " | " + responses.longValue(), true);
		em.setColor(Color.CYAN);
		em.setFooter("Bot version: " + Bot.BOT_VERSION, null);
		msg.getChannel().sendMessage(em.build()).queue();
	}

	private static int playingMusic() {
		return currentMusic.values().stream()
				.mapToInt(Integer::intValue).sum();
	}
	private static double getCpuLoad() {
		// http://stackoverflow.com/questions/18489273/how-to-get-percentage-of-cpu-usage-of-os-from-java
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
			AttributeList list = mbs.getAttributes(name, new String[]{ "ProcessCpuLoad" });

			if (list.isEmpty())
				return Double.NaN;

			Attribute att = (Attribute)list.get(0);
			Double value  = (Double)att.getValue();

			// usually takes a couple of seconds before we get real values
			if (value == -1.0)    
				return Double.NaN;
			// returns a percentage value with 1 decimal point precision
			return ((int)(value * 1000) / 10.0);
		} catch(Exception e) {
			return Double.NaN;
		}
	}
}

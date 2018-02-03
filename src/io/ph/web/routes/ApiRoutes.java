package io.ph.web.routes;

import static io.ph.web.WebServer.getBotStats;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.post;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonValue;

import io.ph.bot.Bot;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.Permission;
import io.ph.util.Util;
import io.ph.web.WebServer;
import net.dean.jraw.http.NetworkException;
import net.dv8tion.jda.core.entities.Channel;
import twitter4j.TwitterException;
import twitter4j.User;

public class ApiRoutes {
	public static void initialize() {
		before("/api/*", (req, res) -> {
			if(WebServer.userToAuthGuilds.get(req.cookie("usession")) == null)
				halt(401, "Unauthorized access");
			if(!req.splat()[0].startsWith("commands/")) {
				if(req.queryParams("channelId") == null
						|| Bot.getInstance().shards.getTextChannelById(req.queryParams("channelId")) == null)
					halt(500, "Channel not found");
			}
			// Every request must include the guildId
			if(req.queryParams("guildId") == null || Bot.getInstance().shards.getGuildById(req.queryParams("guildId")) == null) {
				halt(500, "Guild not found");
			}
			if(!Util.memberHasPermission(Bot.getInstance().shards.getGuildById(req.queryParams("guildId"))
					.getMember(Bot.getInstance().shards.getUserById(WebServer.userToAuthGuilds
							.get(req.cookie("usession")).getUserId())), Permission.KICK)) {
				halt(401, "You are not authorized to view this page.");
			}
			// Kick permissions minimum here
		});

		post("/api/commands/enable", (req, res) -> {
			String guildId = req.queryParams("guildId");
			if(!Util.memberHasPermission(Bot.getInstance().shards.getGuildById(req.queryParams("guildId"))
					.getMember(Bot.getInstance().shards.getUserById(WebServer.userToAuthGuilds
							.get(req.cookie("usession")).getUserId())), Permission.MANAGE_ROLES)) {
				halt(401, "Not authorized");
			}
			String command = req.queryParams("command").toLowerCase();
			GuildObject g = GuildObject.guildMap.get(guildId);
			JsonArray ja = Json.parse(command).asArray();

			StringBuilder sb = new StringBuilder();
			try {
				for(JsonValue jv : ja) {
					String c = jv.asString();
					g.enableCommand(c);
					sb.append(c + ", ");
				}
			} catch(IllegalArgumentException | NullPointerException e) {
				halt(500, "Invalid command(s)");
			}
			return "Enabled " + sb.substring(0, sb.length() - 2);
		});
		post("/api/commands/disable", (req, res) -> {
			String guildId = req.queryParams("guildId");
			if(!Util.memberHasPermission(Bot.getInstance().shards.getGuildById(req.queryParams("guildId"))
					.getMember(Bot.getInstance().shards.getUserById(WebServer.userToAuthGuilds
							.get(req.cookie("usession")).getUserId())), Permission.MANAGE_ROLES)) {
				halt(401, "Not authorized");
			}
			String command = req.queryParams("command").toLowerCase();
			GuildObject g = GuildObject.guildMap.get(guildId);
			JsonArray ja = Json.parse(command).asArray();

			StringBuilder sb = new StringBuilder();
			try {
				for(JsonValue jv : ja) {
					String c = jv.asString();
					g.disableCommand(c);
					sb.append(c + ", ");
				}
			} catch(IllegalArgumentException e) {
				halt(500, "Invalid command(s)");
			}
			return "Disabled " + sb.substring(0, sb.length() - 2);
		});

		get("/public/counts", (req, res) -> {
			if (req.queryParams("type") != null && req.queryParams("type").equals("msg")) {
				return (new JsonArray()).add(getBotStats().getMessageCount())
						.add(getBotStats().getCommandCount())
						.toString();
			} else if (req.queryParams("type") != null && req.queryParams("type").equals("status")) {
				return (new JsonArray()).add(getBotStats().getUsers())
						.add(getBotStats().getMemoryUsage())
						.add(getBotStats().getGuilds())
						.add(getBotStats().getUptimeHours())
						.add(getBotStats().getUptimeMinutes())
						.toString();
			} else {
				halt(400);
			}
			return null;
		});
	}
}

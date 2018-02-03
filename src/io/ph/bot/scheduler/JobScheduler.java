package io.ph.bot.scheduler;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import io.ph.bot.jobs.ReminderJob;
import io.ph.bot.jobs.StatusChangeJob;
import io.ph.bot.jobs.TimedPunishJob;
import io.ph.bot.jobs.WebSyncJob;

public class JobScheduler {

	public static Scheduler scheduler;

	/**
	 * Load settings & start
	 */
	public static void initializeScheduler() {
		try {
			scheduler = new StdSchedulerFactory("resources/config/quartz.properties").getScheduler();
			scheduler.start();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Periodically check for reminders
	 */
	private static void remindCheck() {
		JobDetail job = JobBuilder.newJob(ReminderJob.class).withIdentity("reminderJob", "group1").build();
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity("reminderJob", "group1")
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(15).repeatForever()).build();
		try {
			scheduler.scheduleJob(job, trigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Periodically check to unban/mute offenders
	 */
	private static void punishCheck() {
		JobDetail job = JobBuilder.newJob(TimedPunishJob.class).withIdentity("punishJob", "group1").build();
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity("punishJob", "group1")
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(30).repeatForever()).build();
		try {
			scheduler.scheduleJob(job, trigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Change status on rotation
	 */
	private static void statusChange() {
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity("statusChangeJob", "group1")
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(60).repeatForever()).build();
		try {
			scheduler.scheduleJob(StatusChangeJob.job, trigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Sync stats to web info
	 */
	private static void webSync() {
		JobDetail job = JobBuilder.newJob(WebSyncJob.class).withIdentity("webSyncJob", "group1").build();
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity("webSyncJob", "group1")
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(5).repeatForever()).build();
		try {
			scheduler.scheduleJob(job, trigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}


	/*
	/ **
	 * Register scheduler
	 * /
	private static void startJobs() {
		try {
			Bot.getInstance().getApiKeys().get("twitch");
			twitchStreamCheck();
		} catch (NoAPIKeyException e1) { 
			LoggerFactory.getLogger(JobScheduler.class).warn("You do not have a Twitch.tv API "
					+ "key setup in Bot.properties - Your bot will not have support for Twitch.tv announcements.");
		}
		try {
			Bot.getInstance().getApiKeys().get("redditkey");
			redditFeed();
		} catch (NoAPIKeyException e1) { 
			LoggerFactory.getLogger(JobScheduler.class).warn("You do not have a "
					+ "reddit client/secret setup in Bot.properties - Your bot will not have support for Reddit feeds");
		}
		if(StatusChangeJob.statuses != null && StatusChangeJob.statuses.size() > 0
				&& !StatusChangeJob.statuses.get(0).isEmpty()) {
			statusChange();
		}
		try {
			Bot.getInstance().getApiKeys().get("dashboardid");
			webSync();
		} catch (NoAPIKeyException e1) { 
			LoggerFactory.getLogger(JobScheduler.class).warn("You do not have a "
					+ "dashbaord setup - Web sync will not proceed");
		}
			
		remindCheck();
		punishCheck();
	}
	*/
}

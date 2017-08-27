
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class Events {

	CalendarManager calendarManager;

	public Events(CalendarManager calendarManager) {
		this.calendarManager = calendarManager;
	}

/*
	public void countDown() {
		final long timeOrigin = System.currentTimeMillis();

		Thread t = new Thread(() -> {
			long timeLeft = 10000-(System.currentTimeMillis()-timeOrigin);
			while(timeLeft>0) {
				timeLeft =  10000-(System.currentTimeMillis()-timeOrigin);
				long toWait = (long) (timeLeft/1.1);
				System.out.println(timeLeft+" left");
				try {Thread.sleep(toWait);} catch (InterruptedException e) {};

			}

			System.out.println("finished");
		});
		t.start();
	}
*/
	
	public void startCalendarNotifier(MessageReceivedEvent event) {
		try {
			calendarManager.reload();
		} catch (Exception e) {}

		Thread calendarNotifier = new Thread(() -> {
			try {
				
				CalendarComponent nextEvent = calendarManager.getNextEvent();
				DateTime nextEventDateTime = Utils.normalizeDateTime(nextEvent);
				DateTime currentDateTime = new DateTime(LocalDate.now().format(DateTimeFormatter.ofPattern("YYYYMMdd"))+"T000000Z");

				while(!Thread.interrupted() && currentDateTime.before(nextEventDateTime)) {
					long timeToWait = (nextEventDateTime.toInstant().getEpochSecond()-86400)-currentDateTime.toInstant().getEpochSecond();
					System.out.println("starting wait for "+timeToWait+" seconds");
					Thread.sleep(timeToWait);
					BotUtils.sendMessage(event.getChannel(), "NOTICE: EVENT **"+nextEvent.getProperty(Property.NAME)+"** IS STARTING AT **"+nextEvent.getProperty(Property.DTSTART)+"**!");
					timeToWait = (nextEventDateTime.toInstant().getEpochSecond()-3600)-currentDateTime.toInstant().getEpochSecond();
					Thread.sleep(timeToWait);
					BotUtils.sendMessage(event.getChannel(), "NOTICE: EVENT **"+nextEvent.getProperty(Property.NAME)+"** IS STARTING AT **"+nextEvent.getProperty(Property.DTSTART)+"**!");
					
					//currentDateTime = new DateTime(LocalDate.now().format(DateTimeFormatter.ofPattern("YYYYMMdd"))+"T000000Z");
				}
				
				startCalendarNotifier(event);

			} catch (Exception e) {System.err.println("wait aborted");}
		});
		calendarNotifier.start();

	}

	public String reload() {
		try {
			calendarManager.reload();
			return "Calendar successfully redownloaded, reloaded, and parsed.";
		} catch (IOException | ParserException e) {return "Calendar reload error. See bot console for details.";}
	}

	public String getNextEvent(List<String> argsList) {
		CalendarComponent calendarEvent = calendarManager.getNextEvent();

		if(calendarEvent==null) 
			return "No future events.";


		if(argsList.size()>1) {
			String arg1 = argsList.get(1);

			switch(arg1) { //first argument exists, react to it
			case "desc" : //provide description for next event
				String eventDescription = calendarEvent.getProperty(Property.DESCRIPTION).getValue();
				if(eventDescription==null || eventDescription.isEmpty()) {
					return "No description provided. Advisors can add descriptions to events on the team calendar.";
				} else {
					return eventDescription;
				}
			case "help" : //provide help for nextevent command 
				return "possible parameters: [help - shows this message, desc - provides event description]";
			default : 
				return "Invalid parameter. Run `"+BotUtils.BOT_PREFIX+BotUtils.BOT_NAME+" nextevent help` for a list of parameters. ";
			}

		} else { //argument does not exist, run as normal
			DateTime dateTime = Utils.normalizeDateTime(calendarEvent);
			String formattedDateTime = new SimpleDateFormat("MM/dd/yyyy HH:mm").format(new Date(dateTime.toInstant().getEpochSecond()*1000L));

			return "The next event is **"+calendarEvent.getProperty(Property.SUMMARY).getValue()+
					"** and starts at **"+formattedDateTime+
					"**. Run `"+BotUtils.BOT_PREFIX+BotUtils.BOT_NAME+" nextevent desc` for more info.";
		}
	}

	public void handleFunStuff(MessageReceivedEvent event) {
		String message = event.getMessage().getContent();

		if(message.contains("calendar bot") && (message.contains("great") || message.contains("best") || message.contains("awesome") || message.contains("love") || message.contains("my hero"))) {
			BotUtils.sendMessage(event.getChannel(), Utils.getGladWord());
		} else if(message.contains("I'm lonely")) {
			event.getAuthor().getOrCreatePMChannel().sendMessage("hey there");
		} else if(message.contains("when will my husband return from the war")) {
			event.getAuthor().getOrCreatePMChannel().sendMessage("I have some bad news");
		}
	}

}
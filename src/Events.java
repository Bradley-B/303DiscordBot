
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import org.omg.CORBA.Environment;

import com.fasterxml.jackson.annotation.ObjectIdGenerators.StringIdGenerator;
import com.fasterxml.jackson.databind.annotation.JsonAppend.Prop;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Escapable;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class Events {

	CalendarManager calendarManager;

	public Events(CalendarManager calendarManager) {
		this.calendarManager = calendarManager;
	}
	
	public void startCalendarNotifier(MessageReceivedEvent event) {
		try {calendarManager.reload();} catch (Exception e) {}
		
		Utils.runInNewThreadForNotifier(true, () -> { //calendar notifier thread
			
				ArrayList<CalendarComponent> detectedEvents = new ArrayList<>();
				
				while(!Thread.interrupted()) { //check for new events in the next day, every hour
					ArrayList<CalendarComponent> nextDayEvents = calendarManager.getEventsForNext(86400000); //get all events in the next day
					
					for(CalendarComponent component : nextDayEvents) { //for every event
						if(!detectedEvents.contains(component) && component!=null) {
							detectedEvents.add(component);
							
							Utils.runInNewThreadForNotifier(true, () -> { //new thread to announce it and wait until an hour before
								String eventName = component.getProperty(Property.SUMMARY).getValue();
								BotUtils.sendMessage(event.getChannel(), "**"+eventName+"** is starting at **"+Utils.getFormattedDateTime(component)+"**!");
								
								long eventTimeInEpochMs = Utils.getDateTime(component).toInstant().getEpochSecond()*1000L;
								long timeToWaitInMs = eventTimeInEpochMs-System.currentTimeMillis()-3600000;
								try {Thread.sleep(timeToWaitInMs);} catch (InterruptedException ex) {} //wait until an hour before the event
								
								BotUtils.sendMessage(event.getChannel(), "**"+eventName+"** is starting in an hour!");
								
							});
						}
					}
					
					try {Thread.sleep(3600000);} catch (InterruptedException ex) {BotUtils.sendMessage(event.getChannel(), "Shutting down notifier.");} //wait an hour
					try {calendarManager.reload();} catch (Exception e) {} //reload the calendar to get any new events
				}
		});
		
	}

	public String reload() {
		try {
			calendarManager.reload();
			return "Calendar successfully redownloaded, reloaded, and parsed.";
		} catch (IOException | ParserException e) {return "Calendar reload error. See bot console for details.";}
	}

	public String getStringForNextEvents(List<String> argsList) {
	
		if(argsList.size()>1) {
			int days = 0;
			try {
				days = Integer.decode(argsList.get(1).trim());
			} catch (NumberFormatException e) {return "Parameter must be a number, in days.";}
			
			if(days<=0) {
				return "Parameter must be at least one.";
			}
			
			ArrayList<CalendarComponent> calendarEvents = calendarManager.getEventsForNext(days*86400000);
			String outputString = "";
			
			if(calendarEvents.size()>0) {
				int eventNumber = 1;
				for(CalendarComponent event : calendarEvents) {
					outputString+=(eventNumber+". **"+event.getProperty(Property.SUMMARY).getValue()+"** starts at **"+Utils.getFormattedDateTime(event)+"**. \n");
					System.out.println(outputString + "\n");
					eventNumber++;
				}
			}
			
			if(outputString.isEmpty()) {
				return "No events in range, or range is too big.";
			} else {
				return outputString;
			}
			
		}
		
		return "Invalid Parameter. Must be a number, in days.";
	}
	
	public String getNextEventString(List<String> argsList) {
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
			case "location" :
				String eventLocation = calendarEvent.getProperty(Property.LOCATION).getValue();
				if(eventLocation!=null || eventLocation.isEmpty()) {
					return "No location provided. Advisors can add locations to events on the team calendar.";
				} else {
					return eventLocation;
				}
			case "help" : //provide help for nextevent command 
				return "possible parameters: [help, desc]";
			default : 
				return "Invalid parameter. Run `"+BotUtils.BOT_PREFIX+BotUtils.BOT_NAME+" nextevent help` for a list of parameters. ";
			}

		} else { //argument does not exist, run as normal
			
			return "The next event is **"+calendarEvent.getProperty(Property.SUMMARY).getValue()+
					"** and starts at **"+Utils.getFormattedDateTime(calendarEvent)+
					"**. Run `"+BotUtils.BOT_PREFIX+BotUtils.BOT_NAME+" nextevent desc` for more info.";
		}
	}

	public void handleFunStuff(MessageReceivedEvent event) {
		String message = event.getMessage().getContent();

		boolean praise = message.contains("haha") || message.contains("lol") || message.contains("omg") || message.contains("great") || message.contains("best") || message.contains("awesome") || message.contains("love") || message.contains("my hero");
		boolean name = message.contains("calendar") || message.contains("CALENDAR") || message.contains("C.A.L.E.N.D.A.R.");
		
		if(praise && name) {
			BotUtils.sendMessage(event.getChannel(), Utils.getGladWord());
			event.getMessage().addReaction(":heart:");
		}
		
		if(message.contains("when will my husband return from the war")) {
			event.getGuild().banUser(event.getAuthor());
			
			
		}
	}

}
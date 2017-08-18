
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class MyEvents {
	
	CalendarManager calendarManager = new CalendarManager();
	
    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event) { //handles new messages
    	handleFunStuff(event);
    	handleParameterCommands(event);
    }
    
    public void handleParameterCommands(MessageReceivedEvent event) {
    	String message = event.getMessage().getContent();
    	String[] commands = message.split(" ", 4);
    	commands[0] = commands[0].substring(1);
    	boolean isCommand = message.startsWith(BotUtils.BOT_PREFIX);
    	String response = "";
    	
    	if(isCommand && commands[0].equals(BotUtils.BOT_NAME) && commands.length>1) {
    		if(commands[1].equals("reload")) {
    			response = commandReload();
    		} else if(commands[1].equals("ping")) {
    			response = Utils.getPongWord()+"! Did I do it right?";
    		} else if(commands[1].equals("nextevent")) {
    			response = commandNextEvent(commands);
    		} else {
    			response = "Invalid command. For a list of commands run `"+BotUtils.BOT_PREFIX+BotUtils.BOT_NAME+" help `.";
    		}
    		
    		BotUtils.sendMessage(event.getChannel(), response);
    	}
    }
    
    public String commandReload() {
    	try {
			calendarManager.reload();
			return "Calendar successfully redownloaded, reloaded, and parsed.";
		} catch (IOException | ParserException e) {return "Calendar reload error. See bot console for details.";}
    }
    
    public String commandNextEvent(String[] commands) {
		CalendarComponent calendarEvent = calendarManager.getNextEvent();
		try {
			if(commands[2].equals("desc")) {
				String eventDescription = calendarEvent.getProperty(Property.DESCRIPTION).getValue();
				if(eventDescription.isEmpty()) {
					return "No description provided. Advisors can add descriptions to events on the team calendar.";
				} else {
					return eventDescription;
				}
			} else {
				return "Invalid parameter. Run `"+BotUtils.BOT_PREFIX+BotUtils.BOT_NAME+" nextevent help` for a list of parameters. ";
			}
		} catch (NullPointerException e) {
			return "No future events.";
		} catch (IndexOutOfBoundsException e) { //no second command, run default
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
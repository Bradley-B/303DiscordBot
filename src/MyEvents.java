
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class MyEvents {

    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event) throws IOException, ParserException, ParseException{ //handles new messages
    	handleFunStuff(event);
    	handleParameterCommands(event);
    }
    
    public void handleParameterCommands(MessageReceivedEvent event) throws IOException, ParserException, ParseException {
    	String message = event.getMessage().getContent();
    	String[] commands = message.split(" ", 4);
    	commands[0] = commands[0].substring(1);
    	boolean isCommand = message.startsWith(BotUtils.BOT_PREFIX);
    	String response = "";
    	
    	if(isCommand && commands[0].equals("calbot")) {
    		if(commands[1].equals("refresh")) {
    			try {
					Utils.downloadFile("https://calendar.google.com/calendar/ical/firstteam303%40gmail.com/public/basic.ics");
					response = "success";
				} catch (IOException e) {response = "error";}
    		} else if(commands[1].equals("ping")) {
    			response = Utils.getPongWord()+"! Did I do it right?";
    		} else if(commands[1].equals("nextevent")) {
    			CalendarComponent calendarComponent = getNextEvent("basic.ics");
    			try {
    				DateTime dateTime = Utils.normalizeDateTime(calendarComponent);
    				String formattedDateTime = new SimpleDateFormat("MM/dd/yyyy HH:mm").format(new Date(dateTime.toInstant().getEpochSecond()*1000L));
    					
    				response = "The next event is **"+calendarComponent.getProperty(Property.SUMMARY).getValue()+
    						"** and starts at **"+formattedDateTime+
    						"**. Type '!calbot nextevent desc' for more info.";
    			} catch (NullPointerException e) {response = "No future events.";}
    		} else {
    			response = "command not found";
    		}
    		
    		BotUtils.sendMessage(event.getChannel(), response);
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
    
    public CalendarComponent getNextEvent(String file) throws IOException, ParserException, ParseException {
    	FileInputStream fin = new FileInputStream(file);
    	CalendarBuilder builder = new CalendarBuilder();
    	Calendar calendar = builder.build(fin);
    	
    	ComponentList<CalendarComponent> componentList = calendar.getComponents(Component.VEVENT);
    	componentList = Utils.sortEvents(componentList);
 
    	for(CalendarComponent component : componentList) {
    		DateTime eventDateTime = Utils.normalizeDateTime(component);
    		
    		if(eventDateTime.after(new DateTime(LocalDate.now().format(DateTimeFormatter.ofPattern("YYYYMMdd"))+"T000000Z"))) {
    			return component;
    		}	
    	} 
    	
    	return null;
    }
    

    
    
}
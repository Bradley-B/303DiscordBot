import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.CalendarComponent;

/**
 * Represents a calendar.
 * @author Bradley
 */
public class CalendarManager {
	
	ComponentList<CalendarComponent> eventList;
	Calendar calendar;
	public static final String calendarUrl = "https://calendar.google.com/calendar/ical/firstteam303%40gmail.com/public/basic.ics";
	public static final String calendarFilename = "basic.ics";
	
	public CalendarManager() {
		try {
			reload();
		} catch (IOException | ParserException e) {}
	}
	
	public static ComponentList<CalendarComponent> sortEvents(ComponentList<CalendarComponent> components) { //sorts so oldest events are first in array
		Collections.sort(components, new Comparator<CalendarComponent>() {
			@Override
			public int compare(CalendarComponent o1, CalendarComponent o2) {
				DateTime eventDateTime1 = Utils.getDateTime(o1);
				DateTime eventDateTime2 = Utils.getDateTime(o2);

				return eventDateTime1.compareTo(eventDateTime2);
			}
		});
		return components;
	}
	
	public void reload() throws IOException, ParserException {
		File file = new File(calendarFilename);
		if(file.exists()) {
			file.delete(); //delete the file to make space for the new one
		}
		
		URL website = new URL(calendarUrl); //download file
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream(calendarFilename);
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.flush();
		fos.close();
		rbc.close();
		
		FileInputStream fin = new FileInputStream(calendarFilename); //build file into calendar
    	CalendarBuilder builder = new CalendarBuilder();
    	this.calendar = builder.build(fin);
    	
    	ComponentList<CalendarComponent> eventList = calendar.getComponents(Component.VEVENT);
    	this.eventList = sortEvents(eventList);
	}
	
	public ArrayList<CalendarComponent> getEventsForNext(long scopeDepthInMs) {
		ArrayList<CalendarComponent> eventsInScope = new ArrayList<>();
		long minScopeInEpoch = System.currentTimeMillis(); //current time
		long maxScopeInEpoch = minScopeInEpoch+scopeDepthInMs;
		
		for(CalendarComponent component : eventList) {
			DateTime eventDateTime = Utils.getDateTime(component);
			
			if(eventDateTime.before(new Date(maxScopeInEpoch)) && eventDateTime.after(new Date(minScopeInEpoch))) {
				eventsInScope.add(component);
			}
		}
		
		return eventsInScope;
	}
	
    public CalendarComponent getNextEvent() {
   
    	for(CalendarComponent component : eventList) {
    		DateTime eventDateTime = Utils.getDateTime(component);
   	
    		try {
				if(eventDateTime.after(new DateTime(LocalDate.now().format(DateTimeFormatter.ofPattern("YYYYMMdd"))+"T000000Z"))) {
					return component;
				}
			} catch (ParseException e) {e.printStackTrace();}	
    	} 
    	
    	return null;
    }
	
}

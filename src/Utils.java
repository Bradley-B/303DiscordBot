import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;

public class Utils {

	public static boolean isEventAllDay(VEvent event){
		return event.getStartDate().toString().indexOf("VALUE=DATE") != -1;
	}

	public static ComponentList<CalendarComponent> sortEvents(ComponentList<CalendarComponent> components) {
		Collections.sort(components, new Comparator<CalendarComponent>() {
			@Override
			public int compare(CalendarComponent o1, CalendarComponent o2) {
				DateTime eventDateTime1 = normalizeDateTime(o1);
				DateTime eventDateTime2 = normalizeDateTime(o2);

				return eventDateTime1.compareTo(eventDateTime2);
			}
		});
		return components;
	}

	public static DateTime normalizeDateTime(Component component) {
		String eventDate = component.getProperty(Property.DTSTART).getValue();
		if(!eventDate.contains("T")) {
			eventDate = eventDate + "T000000Z";
		}
		DateTime eventDateTime = null;
		try {eventDateTime = new DateTime(eventDate);} catch (ParseException e) {}
		return eventDateTime;
	}

	public static void downloadFile(String url) throws IOException, MalformedURLException {
		URL website = new URL(url);
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream("basic.ics");
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.flush();

		fos.close();
		rbc.close();
	}

	public static String getPongWord() {
		String[] words = {"Potato", "Peach", "Pickup", "Puddle", "Pizza", "Puzzle", "Pumpkin", "Pacman", "Peacock", "Pretzel", "Puffin", "Pixie"};
		Random random = new Random();
		return words[random.nextInt(words.length)];

	}

	public static String getGladWord() {
		String[] words = {"love you too", "thanks", "you're pretty good yourself ;)", "ikr, best bot ever", "lol thanks", ":kazoo:", ":heart:"};
		Random random = new Random();
		return words[random.nextInt(words.length)];
	}
}

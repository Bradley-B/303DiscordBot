import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;

public class Utils {
	
	public static void shutdownCalendarNotifier() {
		ArrayList<Thread> toRemove = notifierThreadList;
		for(Thread thread : notifierThreadList) {
			thread.interrupt();
		}
		notifierThreadList.removeAll(toRemove);
	}
	
	public static ArrayList<Thread> notifierThreadList = new ArrayList<>();
	
	public static void runInNewThreadForNotifier(boolean daemon, Runnable runnable) {
		Thread thread = new Thread(runnable);
		thread.setDaemon(true);
		thread.start();
		notifierThreadList.add(thread);
	}
	
	public static String getFormattedDateTime(CalendarComponent component) {
		DateTime dateTime = Utils.getDateTime(component);
		String formattedDateTime = new SimpleDateFormat("MM/dd/yyyy HH:mm").format(new Date(dateTime.toInstant().getEpochSecond()*1000L));	
		return formattedDateTime;
	}
	
	public static boolean isEventAllDay(VEvent event){
		return event.getStartDate().toString().indexOf("VALUE=DATE") != -1;
	}

	public static LocalDateTime getLocalDateTime(DateTime dateTime) {
		return LocalDateTime.ofInstant(dateTime.toInstant(), ZoneId.systemDefault());
	}
	
	public static LocalDateTime getLocalDateTime(Component component) {
		return getLocalDateTime(getDateTime(component));
	}
	
	public static DateTime getDateTime(Component component) {
		String eventDate = component.getProperty(Property.DTSTART).getValue();
		if(!eventDate.contains("T")) {
			eventDate = eventDate + "T000000";
		}
		DateTime eventDateTime = null;
		try {eventDateTime = new DateTime(eventDate);} catch (ParseException e) {e.printStackTrace();}
		return eventDateTime;
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

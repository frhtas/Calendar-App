package ce.yildiz.edu.tr.mycalendar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

// A class for creating events or use them
public class Event implements Serializable {
    private long eventID;                 // Event ID
    private String eventType;             // Task, Meeting, Birthday
    private Calendar dateFrom;            // Start date of event
    private Calendar dateTo;              // End date of event
    private String eventName;             // Event title
    private String description;           // Event description, or use it as attendees if it is a meeting
    private String location;              // Event location: Where event will be?
    private int repeatTime;               // Event Repeat Time can be: One-time event, Daily, Weekly, Monthly, Yearly
    private int repeatCount;              // Example: How many days event will occur if it is Daily event?
    private ArrayList<Integer> reminders; // Which reminder times event will have

    // Constructors for Events
    public Event(long eventID, String eventType, Calendar dateFrom, Calendar dateTo, String eventName,
                 String description, String location, int repeatTime, int repeatCount, ArrayList<Integer> reminders) {
        this.eventID = eventID;
        this.eventType = eventType;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.eventName = eventName;
        this.description = description;
        this.location = location;
        this.repeatTime = repeatTime;
        this.repeatCount = repeatCount;
        this.reminders = reminders;
    }

    public Event(String eventType, Calendar dateFrom, Calendar dateTo, String eventName,
                 String description, String location, int repeatTime, int repeatCount, ArrayList<Integer> reminders) {
        this.eventType = eventType;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.eventName = eventName;
        this.description = description;
        this.location = location;
        this.repeatTime = repeatTime;
        this.repeatCount = repeatCount;
        this.reminders = reminders;
    }

    public Event() {
    }

    public long getEventID() {
        return eventID;
    }

    public void setEventID(long eventID) {
        this.eventID = eventID;
    }

    public String getEventType() {
        return eventType;
    }

    public Calendar getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Calendar dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Calendar getDateTo() {
        return dateTo;
    }

    public void setDateTo(Calendar dateTo) {
        this.dateTo = dateTo;
    }

    public String getEventName() {
        return eventName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getRepeatTime() {
        return repeatTime;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public ArrayList<Integer> getReminders() {
        return reminders;
    }

    public void setReminders(ArrayList<Integer> reminders) {
        this.reminders = reminders;
    }
}

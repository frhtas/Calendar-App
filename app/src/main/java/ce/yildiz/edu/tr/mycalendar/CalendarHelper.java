package ce.yildiz.edu.tr.mycalendar;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.format.DateUtils;
import android.util.Log;
import com.alamkanak.weekview.WeekViewEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


// A class which have some functions for add, get, delete events on Calendar
public class CalendarHelper {
    // Constants
    public final static int MIN_YEAR = 1970;
    public final static int MAX_YEAR = 2100;

    private static final String[] INSTANCE_PROJECTION = new String[] {
            CalendarContract.Instances.EVENT_ID,                // 0
            CalendarContract.Instances.BEGIN,                   // 1
            CalendarContract.Instances.END,                     // 2
            CalendarContract.Instances.TITLE,                   // 3
            CalendarContract.Instances.EVENT_LOCATION,          // 4
            CalendarContract.Instances.DESCRIPTION,             // 5
            CalendarContract.Instances.CALENDAR_DISPLAY_NAME,   // 6
            CalendarContract.Instances.RRULE                    // 7
    };

    private Context context;
    private DatabaseHelper databaseHelper;

    public CalendarHelper (Context context) {
        this.context = context;
        databaseHelper = new DatabaseHelper(context);
    }


    // Add a new Event to the Calendar
    public void addNewEvent(Event event) {
        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Instances.CALENDAR_ID, findCalendarID(event.getEventType()));

        Calendar beginDate = event.getDateFrom();
        long startMillis = beginDate.getTimeInMillis();
        Calendar endDate = event.getDateTo();
        long endMillis = endDate.getTimeInMillis();

        if (event.getEventType().equals("task") || event.getEventType().equals("meeting")) { // Task Event or Meeting Event
            values.put(CalendarContract.Events.DTSTART, startMillis);
            values.put(CalendarContract.Events.DTEND, endMillis);
            values.put(CalendarContract.Events.ALL_DAY, 0);
            values.put(CalendarContract.Events.TITLE, event.getEventName());
            values.put(CalendarContract.Events.DESCRIPTION, event.getDescription());
            values.put(CalendarContract.Events.EVENT_LOCATION, event.getLocation());
            values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
            if (event.getRepeatTime() != 0)
                values.put(CalendarContract.Events.RRULE, getRepeatAsFreq(event.getRepeatTime()) + ";COUNT=" + event.getRepeatCount());
            else
                values.put(CalendarContract.Events.RRULE, getRepeatAsFreq(event.getRepeatTime()));
        }

        else if (event.getEventType().equals("birthday") ) { // Birthday event
            values.put(CalendarContract.Events.DTSTART, startMillis);
            values.put(CalendarContract.Events.DTEND, endMillis);
            values.put(CalendarContract.Events.ALL_DAY, 1);
            values.put(CalendarContract.Events.TITLE, event.getEventName());
            values.put(CalendarContract.Events.RRULE, getRepeatAsFreq(event.getRepeatTime()));
            values.put(CalendarContract.Events.EVENT_TIMEZONE, "UTC");
        }

        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
        long eventID = Long.parseLong(Objects.requireNonNull(uri.getLastPathSegment()));
        event.setEventID(eventID);

        if (event.getReminders().size() != 0) {
            setAllReminders(event);
        }
    }


    // Get repeat freq as String
    public static String getRepeatAsFreq(int repeat) {
        String freq = "";
        switch (repeat) {
            case 0:
                freq = "";                       // One-Time Event
                break;
            case 1:
                freq = "FREQ=DAILY";             // Daily
                break;
            case 2:
                freq = "FREQ=WEEKLY";            // Weekly
                break;
            case 3:
                freq = "FREQ=MONTHLY";           // Monthly
                break;
            case 4:
                freq = "FREQ=YEARLY";            // Yearly
                break;
        }
        return freq;
    }


    // Get repeat freq as int
    public static int getRepeatAsInt(String repeat) {
        int freq = 0;
        if (repeat == null)
            return freq;

        repeat = repeat.split(";")[0];
        switch (repeat) {
            case "":
                freq = 0;    // One-Time Event
                break;
            case "FREQ=DAILY":
                freq = 1;    // Daily
                break;
            case "FREQ=WEEKLY":
                freq = 2;   // Weekly
                break;
            case "FREQ=MONTHLY":
                freq = 3;  // Monthly
                break;
            case "FREQ=YEARLY":
                freq = 4;   // Yearly
                break;
        }
        return freq;
    }


    // Get repeat count as int
    public static int getRepeatCountAsInt(String repeat) {
        if (repeat != null) {
            if (repeat.contains(";")) {
                repeat = repeat.split(";")[1];
                repeat = repeat.substring(repeat.indexOf("=") + 1);
                int repeatCount = Integer.parseInt(repeat);
                return repeatCount;
            }
        }
        return 1;
    }


    // Get selected reminders as minute ArrayList
    public static ArrayList<Integer> getRemindersAsMinute(ArrayList<Integer> selectedReminders) {
        ArrayList<Integer> reminders = new ArrayList<>();
        for (int i : selectedReminders) {
            if (i == 0)
                reminders.add(0);      // Time of the event
            else if (i == 1)
                reminders.add(30);     // 30 minutes before
            else if (i == 2)
                reminders.add(60);     // 1 hour before
            else if (i == 3)
                reminders.add(240);    // 4 hours before
            else if (i == 4)
                reminders.add(1440);   // 1 day before
        }
        return reminders;
    }


    // Set all reminders by event (If repeat is exist etc.)
    public void setAllReminders(Event event) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(event.getDateFrom().getTimeInMillis());
        switch (event.getRepeatTime()) {
            case 0:  // One-time event
                setReminders(event, 1);
                break;

            case 1:  // Daily event
                setReminders(event, AlarmManager.INTERVAL_DAY);
                break;

            case 2:  // Weekly event
                setReminders(event, AlarmManager.INTERVAL_DAY*7);
                break;

            case 3:  // Monthly event
                setReminders(event, AlarmManager.INTERVAL_DAY*30);
                break;

            case 4:  // Yearly event
                setReminders(event, DateUtils.YEAR_IN_MILLIS);
                break;
        }

        databaseHelper.addReminders(event.getEventID(), event.getReminders());
    }


    // Set reminders by Event
    public void setReminders(Event event, long repeatDistance) {
        ArrayList<Integer> selectedReminders = new ArrayList<>(getRemindersAsMinute(event.getReminders()));
        for (int minute : selectedReminders) {
            Intent intent = new Intent(context, ReminderBroadcast.class);
            intent.putExtra("eventID", event.getEventID());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) event.getEventID()*(minute+1), intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            long alarmTime = event.getDateFrom().getTimeInMillis() - minute*60000;
            if (repeatDistance == 1)
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            else
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, alarmTime, repeatDistance, pendingIntent);
        }
    }


    // Delete reminder from database by its EventID
    public void deleteReminder(long eventID, int timeBefore) {
        Intent intent = new Intent(context, ReminderBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) eventID*(timeBefore+1), intent, timeBefore);
        pendingIntent.cancel();
    }


    // Get events from Calendar
    public ArrayList<Event> getAllEvents(Calendar calendarViewDate, String comeActivity) {
        ArrayList<Event> allEvents = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(context ,Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR}, 1000);
            return allEvents;
        }

        // Specify the date range you want to search for recurring event instances
        Calendar beginTime = Calendar.getInstance();
        beginTime.setTimeInMillis(calendarViewDate.getTimeInMillis());
        if (comeActivity.equals("CalendarActivity"))
            beginTime.set(Calendar.DAY_OF_MONTH, beginTime.getMinimum(Calendar.DAY_OF_MONTH));
        else if (comeActivity.equals("ListEventsActivity"))
            beginTime.add(Calendar.MONTH, -1);
        long startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.setTimeInMillis(beginTime.getTimeInMillis());
        if (comeActivity.equals("CalendarActivity"))
            endTime.add(Calendar.MONTH, 1);
        else if (comeActivity.equals("ListEventsActivity"))
            endTime.set(Calendar.YEAR, MAX_YEAR);
        long endMillis = endTime.getTimeInMillis();

        // Construct the query with the desired date range.
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);

        // The DISPLAY_NAME of the recurring calendar whose instances you are searching for in the Instances table
        String selection = CalendarContract.Instances.CALENDAR_DISPLAY_NAME + " = ? OR " + CalendarContract.Instances.CALENDAR_DISPLAY_NAME + " = ? OR "
                + CalendarContract.Instances.CALENDAR_DISPLAY_NAME + " = ? OR " + CalendarContract.Instances.CALENDAR_DISPLAY_NAME + " = ?";
        String[] selectionArgs = new String[]{"task", "meeting", "birthday", "Holidays in Turkey"};

        // Submit the query
        Cursor cur = context.getContentResolver().query(builder.build(), INSTANCE_PROJECTION, selection, selectionArgs, CalendarContract.Instances.BEGIN + " ASC");

        if (cur != null) {
            while (cur.moveToNext()) {
                // Get the field values
                long eventID = cur.getLong(0);
                long beginVal = cur.getLong(1);
                long endVal = cur.getLong(2);
                String eventName = cur.getString(3);
                String location = cur.getString(4);
                String description = cur.getString(5);
                String eventType = cur.getString(6);
                String repeatTime = cur.getString(7);

                Calendar dateFrom = Calendar.getInstance();
                dateFrom.setTimeInMillis(beginVal);
                Calendar dateTo = Calendar.getInstance();
                dateTo.setTimeInMillis(endVal);

                Event event = new Event(eventID, eventType, dateFrom, dateTo, eventName, description, location,
                        getRepeatAsInt(repeatTime), getRepeatCountAsInt(repeatTime), databaseHelper.getRemindersByID(eventID));
                allEvents.add(event);
            }
            cur.close();
        }

        return allEvents;
    }


    // Get events from Calendar by day
    public ArrayList<Event> getEventsByDay(Calendar selectedDay) {
        ArrayList<Event> dayEvents = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(context ,Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR}, 1000);
            return dayEvents;
        }

        // Specify the date range you want to search for recurring event instances
        Calendar beginTimeDate = Calendar.getInstance();
        beginTimeDate.set(selectedDay.get(Calendar.YEAR), selectedDay.get(Calendar.MONTH), selectedDay.get(Calendar.DAY_OF_MONTH), 00, 00);
        long startMillisDate = beginTimeDate.getTimeInMillis();

        Calendar endTime1 = Calendar.getInstance();
        endTime1.set(selectedDay.get(Calendar.YEAR), selectedDay.get(Calendar.MONTH), selectedDay.get(Calendar.DAY_OF_MONTH), 23, 59);
        long endMillisDate = endTime1.getTimeInMillis();

        // Specify the date range you want to search for recurring event instances
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(MIN_YEAR, 0, 0, 0, 0);
        long startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.set(MAX_YEAR, 0, 0, 0, 0);
        long endMillis = endTime.getTimeInMillis();

        // Construct the query with the desired date range.
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);

        // The DISPLAY_NAME of the recurring calendar whose instances you are searching for in the Instances table
        String selection = "(" + CalendarContract.Instances.CALENDAR_DISPLAY_NAME + " = ? OR " + CalendarContract.Instances.CALENDAR_DISPLAY_NAME + " = ? OR "
                + CalendarContract.Instances.CALENDAR_DISPLAY_NAME + " = ? OR " + CalendarContract.Instances.CALENDAR_DISPLAY_NAME + " = ?) AND " + "(" + CalendarContract.Instances.BEGIN +  " BETWEEN " + startMillisDate + " AND " + endMillisDate + ")";
        String[] selectionArgs = new String[]{"task", "meeting", "birthday", "Holidays in Turkey"};

        // Submit the query
        Cursor cur = context.getContentResolver().query(builder.build(), INSTANCE_PROJECTION, selection, selectionArgs, null);

        if (cur != null) {
            while (cur.moveToNext()) {
                // Get the field values
                long eventID = cur.getLong(0);
                long beginVal = cur.getLong(1);
                long endVal = cur.getLong(2);
                String eventName = cur.getString(3);
                String location = cur.getString(4);
                String description = cur.getString(5);
                String eventType = cur.getString(6);
                String repeatTime = cur.getString(7);

                Calendar dateFrom = Calendar.getInstance();
                dateFrom.setTimeInMillis(beginVal);
                Calendar dateTo = Calendar.getInstance();
                dateTo.setTimeInMillis(endVal);

                Event event = new Event(eventID, eventType, dateFrom, dateTo, eventName, description, location,
                        getRepeatAsInt(repeatTime), getRepeatCountAsInt(repeatTime), databaseHelper.getRemindersByID(eventID));
                dayEvents.add(event);
            }
            cur.close();
        }

        return dayEvents;
    }


    // Get events from Calendar by eventID
    public Event getEventsByID(long ID) {
        // Specify the date range you want to search for recurring event instances
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(MIN_YEAR, 0, 0, 0, 0);
        long startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.set(MAX_YEAR, 0, 0, 0, 0);
        long endMillis = endTime.getTimeInMillis();

        // Construct the query with the desired date range.
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);

        // The DISPLAY_NAME of the recurring calendar whose instances you are searching for in the Instances table
        String selection = CalendarContract.Instances.CALENDAR_DISPLAY_NAME + " = ? OR " + CalendarContract.Instances.CALENDAR_DISPLAY_NAME + " = ? OR "
                + CalendarContract.Instances.CALENDAR_DISPLAY_NAME + " = ? OR " + CalendarContract.Instances.CALENDAR_DISPLAY_NAME + " = ?";
        String[] selectionArgs = new String[]{"task", "meeting", "birthday", "Holidays in Turkey"};

        // Submit the query
        Cursor cur = context.getContentResolver().query(builder.build(), INSTANCE_PROJECTION, selection, selectionArgs, null);

        Event event = new Event();
        if (cur != null) {
            while (cur.moveToNext()) {
                // Get the field values
                long eventID = cur.getLong(0);
                if (eventID == ID) {
                    long beginVal = cur.getLong(1);
                    long endVal = cur.getLong(2);
                    String eventName = cur.getString(3);
                    String location = cur.getString(4);
                    String description = cur.getString(5);
                    String eventType = cur.getString(6);
                    String repeatTime = cur.getString(7);

                    long currentMillis = System.currentTimeMillis();
                    Calendar dateFrom = Calendar.getInstance();
                    dateFrom.setTimeInMillis(beginVal);
                    Calendar dateTo = Calendar.getInstance();
                    dateTo.setTimeInMillis(endVal);

                    event = new Event(eventID, eventType, dateFrom, dateTo, eventName, description, location,
                            getRepeatAsInt(repeatTime), getRepeatCountAsInt(repeatTime), databaseHelper.getRemindersByID(eventID));
                    if ((!repeatTime.equals("")) && (beginVal - currentMillis <= 1440*60000) && (beginVal - currentMillis >= -100*60000))
                        return event;
                }
            }
            cur.close();
        }

        return event;
    }


    // Get events from Calendar by eventID and date
    public Event getEventsByID(long ID, Calendar date) {
        // Specify the date range you want to search for recurring event instances
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(MIN_YEAR, 0, 0, 0, 0);
        long startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.set(MAX_YEAR, 0, 0, 0, 0);
        long endMillis = endTime.getTimeInMillis();

        // Construct the query with the desired date range.
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);

        // The DISPLAY_NAME of the recurring calendar whose instances you are searching for in the Instances table
        String selection = CalendarContract.Instances.CALENDAR_DISPLAY_NAME + " = ? OR " + CalendarContract.Instances.CALENDAR_DISPLAY_NAME + " = ? OR "
                + CalendarContract.Instances.CALENDAR_DISPLAY_NAME + " = ? OR " + CalendarContract.Instances.CALENDAR_DISPLAY_NAME + " = ?";
        String[] selectionArgs = new String[]{"task", "meeting", "birthday", "Holidays in Turkey"};

        // Submit the query
        Cursor cur = context.getContentResolver().query(builder.build(), INSTANCE_PROJECTION, selection, selectionArgs, null);

        Event event = new Event();
        if (cur != null) {
            while (cur.moveToNext()) {
                // Get the field values
                long eventID = cur.getLong(0);
                if (eventID == ID) {
                    long beginVal = cur.getLong(1);
                    long endVal = cur.getLong(2);
                    String eventName = cur.getString(3);
                    String location = cur.getString(4);
                    String description = cur.getString(5);
                    String eventType = cur.getString(6);
                    String repeatTime = cur.getString(7);

                    Calendar dateFrom = Calendar.getInstance();
                    dateFrom.setTimeInMillis(beginVal);
                    Calendar dateTo = Calendar.getInstance();
                    dateTo.setTimeInMillis(endVal);

                    event = new Event(eventID, eventType, dateFrom, dateTo, eventName, description, location,
                            getRepeatAsInt(repeatTime), getRepeatCountAsInt(repeatTime), databaseHelper.getRemindersByID(eventID));
                    if (date.getTimeInMillis() == dateFrom.getTimeInMillis())
                        return event;
                }
            }
            cur.close();
        }

        return event;
    }


    // Get events from Calendar for a month
    public ArrayList<WeekViewEvent> getAllEventsMonthly(int newYear, int newMonth) {
        ArrayList<WeekViewEvent> weekViewEvents = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(context ,Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR}, 1000);
            return weekViewEvents;
        }


        // Specify the date range you want to search for recurring event instances
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(Calendar.YEAR, newYear);
        beginTime.set(Calendar.MONTH, newMonth);
        beginTime.add(Calendar.MONTH, -1);
        long startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.set(Calendar.YEAR, newYear);
        endTime.set(Calendar.MONTH, newMonth);
        endTime.add(Calendar.MONTH, 1);
        long endMillis = endTime.getTimeInMillis();

        // Construct the query with the desired date range.
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);

        // The DISPLAY_NAME of the recurring calendar whose instances you are searching for in the Instances table
        String selection = CalendarContract.Instances.CALENDAR_DISPLAY_NAME + " = ? OR " + CalendarContract.Instances.CALENDAR_DISPLAY_NAME + " = ? OR "
                + CalendarContract.Instances.CALENDAR_DISPLAY_NAME + " = ? OR " + CalendarContract.Instances.CALENDAR_DISPLAY_NAME + " = ?";
        String[] selectionArgs = new String[]{"task", "meeting", "birthday", "Holidays in Turkey"};

        // Submit the query
        Cursor cur = context.getContentResolver().query(builder.build(), INSTANCE_PROJECTION, selection, selectionArgs, null);

        if (cur != null) {
            while (cur.moveToNext()) {
                // Get the field values
                long eventID = cur.getLong(0);
                long beginVal = cur.getLong(1);
                long endVal = cur.getLong(2);
                String eventName = cur.getString(3);
                String location = cur.getString(4);
                String eventType = cur.getString(6);

                Calendar dateFrom = Calendar.getInstance();
                dateFrom.setTimeInMillis(beginVal);
                Calendar dateTo = Calendar.getInstance();
                dateTo.setTimeInMillis(endVal);

                if ((dateFrom.get(Calendar.YEAR) == newYear) && (dateFrom.get(Calendar.MONTH) == newMonth)) {
                    WeekViewEvent weekViewEvent = new WeekViewEvent(eventID, eventName, location, dateFrom, dateTo);
                    switch (eventType) {
                        case "task":
                            weekViewEvent.setColor(Color.parseColor("#AD1457"));
                            break;

                        case "meeting":
                            weekViewEvent.setColor(Color.parseColor("#6A1B9A"));
                            break;

                        case "birthday":
                            weekViewEvent.setColor(Color.parseColor("#0277BD"));
                            weekViewEvent.setAllDay(true);
                            break;

                        case "Holidays in Turkey":
                            weekViewEvent.setColor(Color.parseColor("#FF8F00"));
                            weekViewEvent.setAllDay(true);
                            break;
                    }

                    weekViewEvents.add(weekViewEvent);
                }
            }
        }

        if (cur != null) {
            cur.close();
        }
        return weekViewEvents;
    }


    // Delete the event
    public void deleteEvent(Event event) {
        Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.getEventID());
        int rows = context.getContentResolver().delete(deleteUri, null, null);
        databaseHelper.deleteReminders(event.getEventID());

        if (event.getReminders().size() != 0) {
            ArrayList<Integer> selectedReminders = new ArrayList<>(getRemindersAsMinute(event.getReminders()));
            for (int minute : selectedReminders) {
                deleteReminder(event.getEventID(), minute);
            }
        }
    }


    // Find CalendarID by its DisplayName
    public long findCalendarID(String displayName) {
        // Projection array. Creating indices for this array instead of doing dynamic lookups improves performance.
        final String[] EVENT_PROJECTION = new String[]{
                CalendarContract.Calendars._ID,                           // 0
        };

        // The DISPLAY_NAME of the recurring calendar whose instances you are searching for in the Instances table
        String selection = CalendarContract.Instances.CALENDAR_DISPLAY_NAME + " = ?";
        String[] selectionArgs = new String[]{displayName};

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cur = contentResolver.query(CalendarContract.Calendars.CONTENT_URI, EVENT_PROJECTION, selection, selectionArgs, null);

        long calID = 0;
        if (cur != null) {
            while (cur.moveToNext()) {
                // Get the field values
                calID = cur.getLong(0);
            }
            cur.close();
        }

        return calID;
    }


    // Get Calendars on phone
    public void getCalendars(CalendarActivity calendarActivity) {
        if (ContextCompat.checkSelfPermission(calendarActivity,Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(calendarActivity, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(calendarActivity, new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR}, 1000);
            return;
        }

        // Projection array. Creating indices for this array instead of doing dynamic lookups improves performance.
        final String[] EVENT_PROJECTION = new String[]{
                CalendarContract.Calendars._ID,                           // 0
                CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
                CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
        };

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cur = contentResolver.query(CalendarContract.Calendars.CONTENT_URI, EVENT_PROJECTION, null, null, null);

        if (cur != null) {
            while (cur.moveToNext()) {
                long calID = 0;
                String displayName = null;
                String accountName = null;
                String ownerName = null;

                // Get the field values
                calID = cur.getLong(0);
                accountName = cur.getString(1);
                displayName = cur.getString(2);
                ownerName = cur.getString(3);

                String calendarInfo = String.format("Calendar ID: %s\nDisplay Name: %s\nAccount Name: %s\nOwner Name: %s", calID, displayName, accountName, ownerName);
                Log.i("CALENDARS", "CalendarInfo: " + calendarInfo);
            }
            cur.close();
        }
    }


    // Get activity for usage
    public Activity getActivity() {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }


    // Adding a new calendar
    public void addNewCalendar(String name, String accountName, int color) {
        Uri target = Uri.parse(CalendarContract.Calendars.CONTENT_URI.toString());
        target = target.buildUpon().appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, "com.google").build();

        ContentValues values = new ContentValues();
        values.put(CalendarContract.Calendars.ACCOUNT_NAME, accountName);
        values.put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
        values.put(CalendarContract.Calendars.NAME, name);
        values.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, name);
        values.put(CalendarContract.Calendars.CALENDAR_COLOR, color);
        values.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_READ);
        values.put(CalendarContract.Calendars.OWNER_ACCOUNT, accountName);
        values.put(CalendarContract.Calendars.VISIBLE, 1);
        values.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        values.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, "Europe/Rome");
        values.put(CalendarContract.Calendars.CAN_PARTIALLY_UPDATE, 1);
        values.put(CalendarContract.Calendars.CAL_SYNC1, "https://www.google.com/calendar/feeds/" + accountName + "/private/full");
        values.put(CalendarContract.Calendars.CAL_SYNC2, "https://www.google.com/calendar/feeds/default/allcalendars/full/" + accountName);
        values.put(CalendarContract.Calendars.CAL_SYNC3, "https://www.google.com/calendar/feeds/default/allcalendars/full/" + accountName);
        values.put(CalendarContract.Calendars.CAL_SYNC4, 1);
        values.put(CalendarContract.Calendars.CAL_SYNC5, 0);
        values.put(CalendarContract.Calendars.CAL_SYNC8, System.currentTimeMillis());

        Uri newCalendar = context.getContentResolver().insert(target, values);

    }


    // Find CalendarID by its DisplayName
    public boolean isCalendarExist(String displayName) {
        // Projection array. Creating indices for this array instead of doing dynamic lookups improves performance.
        final String[] EVENT_PROJECTION = new String[]{
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,                           // 0
        };

        // The DISPLAY_NAME of the recurring calendar whose instances you are searching for in the Instances table
        String selection = CalendarContract.Instances.CALENDAR_DISPLAY_NAME + " = ?";
        String[] selectionArgs = new String[]{displayName};

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cur = contentResolver.query(CalendarContract.Calendars.CONTENT_URI, EVENT_PROJECTION, selection, selectionArgs, null);

        int count = cur.getCount();
        boolean isExist = false;
        if (count > 0)
            isExist = true;

        cur.close();
        return isExist;
    }

}

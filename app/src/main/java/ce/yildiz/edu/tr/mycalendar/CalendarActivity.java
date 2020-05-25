package ce.yildiz.edu.tr.mycalendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.exceptions.OutOfDateRangeException;
import com.applandeo.materialcalendarview.listeners.OnCalendarPageChangeListener;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static ce.yildiz.edu.tr.mycalendar.BirthdayFragment.getTimeAsString;


// Main activity of Calendar App, we can see a calendar and events and we can see an AddEvent fab button.
public class CalendarActivity extends AppCompatActivity {
    Toolbar myToolbar;
    Menu menu;
    CalendarView calendarView;
    RecyclerView recyclerView_events;
    FloatingActionButton floatingActionButton_addEvent, floatingActionButton_today;

    CalendarEventAdapter calendarEventAdapter;  // An adapter which show the events below the Calendar
    String today = "";
    Calendar selectedCalendarDay;
    SimpleDateFormat dateFormat;
    CalendarHelper calendarHelper;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        Boolean mode = sharedPreferences.getBoolean("mode", false);
        SettingsActivity.setMode(mode);

        myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        floatingActionButton_today = (FloatingActionButton) findViewById(R.id.floatingActionButton_today);
        floatingActionButton_today.hide();

        calendarHelper = new CalendarHelper(this);

        calendarView = (CalendarView) findViewById(R.id.calendarView);
        setToolbarTitleAsMonth(); // Set toolbar title as Year and Month
        setCalendarDistance();    // Set calendar min and max date
        setEventDays();           // Set icons on CalendarView if there are events

        dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        selectedCalendarDay = calendarView.getFirstSelectedDate();
        today = dateFormat.format(selectedCalendarDay.getTime());

        recyclerView_events = (RecyclerView) findViewById(R.id.recyclerView_events);
        calendarEventAdapter = new CalendarEventAdapter(this, calendarHelper.getEventsByDay(selectedCalendarDay));

        setDateAfterChange("lastSelectedDay"); // Set date after view change (Monthly, Weekly, Daily)
        setDateAfterChange("deletedEventDay"); // Set date after an event deleted
        setDateAfterChange("updatedDay");      // Set date after an event updated

        recyclerView_events.setAdapter(calendarEventAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView_events.setLayoutManager(linearLayoutManager);

        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                floatingActionButton_today.hide();
                selectedCalendarDay = eventDay.getCalendar();
                String selectedDate = dateFormat.format(selectedCalendarDay.getTime());
                if (!selectedDate.equals(today))
                    floatingActionButton_today.show();

                calendarEventAdapter.updateEvents(calendarHelper.getEventsByDay(selectedCalendarDay)); // Update recycler view after clicking a day
            }
        });


        floatingActionButton_addEvent = (FloatingActionButton) findViewById(R.id.floatingActionButton_addEvent);
        floatingActionButton_addEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedCalendarDay.set(Calendar.HOUR_OF_DAY, Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
                selectedCalendarDay.set(Calendar.MINUTE, Calendar.getInstance().get(Calendar.MINUTE));

                Intent intent = new Intent(CalendarActivity.this, BottomNavigationActivity.class);
                intent.putExtra("selectedCalendarDay", selectedCalendarDay);
                startActivityForResult(intent , 1);
            }
        });


        // Go back to today with button
        floatingActionButton_today.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    calendarView.setDate(Calendar.getInstance().getTime());
                    floatingActionButton_today.hide();
                    selectedCalendarDay.setTimeInMillis(Calendar.getInstance().getTimeInMillis());

                    calendarEventAdapter.updateEvents(calendarHelper.getEventsByDay(Calendar.getInstance())); // Update recycler view after clicking a day
                    setEventDays();
                    setToolbarTitleAsMonth();
                } catch (OutOfDateRangeException e) {
                    e.printStackTrace();
                }
            }
        });


        // If month change
        calendarView.setOnPreviousPageChangeListener(new OnCalendarPageChangeListener() {
            @Override
            public void onChange() {
                new MonthChangeAsyncTask().execute(10);
                setToolbarTitleAsMonth();
                setEventDays();
            }
        });

        // If month change
        calendarView.setOnForwardPageChangeListener(new OnCalendarPageChangeListener() {
            @Override
            public void onChange() {
                new MonthChangeAsyncTask().execute(10);
                setToolbarTitleAsMonth();
                setEventDays();
            }
        });

    }


    // Set date again after an event deleted or updated
    public void setDateAfterChange(String key) {
        Calendar eventDay = (Calendar) getIntent().getSerializableExtra(key);
        if (eventDay != null) {
            try {
                calendarView.setDate(eventDay);
                calendarEventAdapter.updateEvents(calendarHelper.getEventsByDay(eventDay));
                selectedCalendarDay.setTimeInMillis(eventDay.getTimeInMillis());
                setEventDays();
                setToolbarTitleAsMonth();
                if (dateFormat.format(eventDay.getTime()).equals(today))
                    floatingActionButton_today.hide();
                else
                    floatingActionButton_today.show();
            } catch (OutOfDateRangeException e) {
                e.printStackTrace();
            }
        }
    }


    // Toolbar title will be current year and month name
    public void setToolbarTitleAsMonth() {
        Calendar currentMonth = calendarView.getCurrentPageDate();
        myToolbar.setTitle(new SimpleDateFormat("yyyy MMMM", Locale.getDefault()).format(currentMonth.getTime()));
    }


    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        this.menu = menu;
        menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_toolbar_monthly));
        return true;
    }

    // Toolbar options
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Intent intent;
        ActionMenuItemView chosenItem = (ActionMenuItemView) findViewById(R.id.toolbar_go_to_view);
        switch (item.getItemId()) {
            case R.id.toolbar_go_to_monthly: // Already in this activity
                return true;

            case R.id.toolbar_go_to_weekly:
                intent = new Intent(this, WeeklyDailyActivity.class);
                intent.putExtra("lastSelectedDay", selectedCalendarDay);
                intent.putExtra("selection", "weekly");
                editor.putString("view", "weekly").apply();
                startActivity(intent);
                finish();
                break;

            case R.id.toolbar_go_to_daily:
                intent = new Intent(this, WeeklyDailyActivity.class);
                intent.putExtra("lastSelectedDay", selectedCalendarDay);
                intent.putExtra("selection", "daily");
                editor.putString("view", "daily").apply();
                startActivity(intent);
                finish();
                break;

            case R.id.toolbar_eventlist:
                intent = new Intent(this, ListEventsActivity.class);
                intent.putExtra("activityComeFrom", "CalendarActivity");
                startActivity(intent);
                break;

            case R.id.toolbar_go_to_date:
                chooseDate();
                break;

            case R.id.toolbar_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }


    // Setting calendar distance at start
    public void setCalendarDistance() {
        Calendar minDate = Calendar.getInstance();
        minDate.set(CalendarHelper.MIN_YEAR, 0, 0);
        Calendar maxDate = Calendar.getInstance();
        maxDate.set(CalendarHelper.MAX_YEAR, 0, 0);
        calendarView.setMinimumDate(minDate);
        calendarView.setMaximumDate(maxDate);
    }


    // A function which setEventDays for showing events as icon on Calendar
    public void setEventDays() {
        new EventDaysAsyncTask().execute(1);
    }


    // A function which choose date on Calendar View
    public void chooseDate() {
        Calendar selectedDateTemp = Calendar.getInstance();
        selectedDateTemp.setTimeInMillis(selectedCalendarDay.getTimeInMillis());
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View parentView = getLayoutInflater().inflate(R.layout.bottom_time_picker, null);
        bottomSheetDialog.setContentView(parentView);
        bottomSheetDialog.show();

        TimePicker timePicker = parentView.findViewById(R.id.timePicker);
        timePicker.setVisibility(View.GONE);

        TextView textView_pickerTimeText = parentView.findViewById(R.id.textView_pickerTimeText);
        textView_pickerTimeText.setText(R.string.choose_date);
        final TextView textView_pickerTime = parentView.findViewById(R.id.textView_pickerTime);
        textView_pickerTime.setText(getTimeAsString(selectedDateTemp));

        DatePicker datePicker = parentView.findViewById(R.id.datePicker);
        datePicker.init(selectedDateTemp.get(Calendar.YEAR), selectedDateTemp.get(Calendar.MONTH), selectedDateTemp.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int year, int month, int dayOfMonth) {
                selectedDateTemp.set(Calendar.YEAR, year);
                selectedDateTemp.set(Calendar.MONTH, month);
                selectedDateTemp.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                textView_pickerTime.setText(getTimeAsString(selectedDateTemp));
            }
        } );


        Button button_ok = (Button) parentView.findViewById(R.id.button_ok);
        button_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    selectedCalendarDay.setTimeInMillis(selectedDateTemp.getTimeInMillis());
                    calendarView.setDate(selectedCalendarDay);
                    String selectedDate = dateFormat.format(selectedCalendarDay.getTime());
                    if (selectedDate.equals(today))
                        floatingActionButton_today.hide();
                    else
                        floatingActionButton_today.show();
                    calendarEventAdapter.updateEvents(calendarHelper.getEventsByDay(selectedCalendarDay)); // Update recycler view after choosing date
                    setToolbarTitleAsMonth();
                    setEventDays();
                } catch (OutOfDateRangeException e) {
                    e.printStackTrace();
                }
                bottomSheetDialog.dismiss();
            }
        });

        Button button_cancel = (Button) parentView.findViewById(R.id.button_cancel);
        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Calendar selectedDay = null;
            if (data != null)
                selectedDay = (Calendar) data.getSerializableExtra("selectedDay");

            try {
                if (selectedDay != null) {
                    calendarView.setDate(selectedDay);  // Set selected date again after user add an event
                    calendarEventAdapter.updateEvents(calendarHelper.getEventsByDay(selectedDay));
                    setEventDays();
                    selectedCalendarDay.setTimeInMillis(selectedDay.getTimeInMillis());
                }
            }
            catch (OutOfDateRangeException e) {
                e.printStackTrace();
            }
        }
    }


    // An AsyncTask class for doing something on background to see faster foreground
    private class MonthChangeAsyncTask extends AsyncTask<Integer, Integer, String> {
        Calendar currentCalendar = calendarView.getCurrentPageDate();
        Calendar calendar = calendarView.getFirstSelectedDate();
        ArrayList<Event> dayEvents = new ArrayList<>();

        @Override
        protected String doInBackground(Integer[] objects) {
            for (int i = 0; i < objects[0]; i++){
                try {
                    Thread.sleep(objects[0]);
                    currentCalendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
                    dayEvents = calendarHelper.getEventsByDay(currentCalendar);
                    selectedCalendarDay.setTimeInMillis(currentCalendar.getTimeInMillis());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            floatingActionButton_today.show();
            String curDate = dateFormat.format(currentCalendar.getTime());
            String date = dateFormat.format(Calendar.getInstance().getTime());
            if (curDate.equals(date))
                floatingActionButton_today.hide();

            try {
                calendarView.setDate(currentCalendar.getTime());
                calendarEventAdapter.updateEvents(dayEvents);
            } catch (OutOfDateRangeException e) {
                e.printStackTrace();
            }
            super.onPostExecute(s);
        }
    }


    // An AsyncTask class for doing something on background to see faster foreground
    private class EventDaysAsyncTask extends AsyncTask<Integer, Integer, String> {
        ArrayList<EventDay> myEventDays = new ArrayList<>();
        @Override
        protected String doInBackground(Integer[] objects) {
            for (int i = 0; i < objects[0]; i++){
                try {
                    Thread.sleep(objects[0]);
                    ArrayList<Event> events = calendarHelper.getAllEvents(selectedCalendarDay, "CalendarActivity");
                    for (Event event : events) {
                        EventDay eventDay = new EventDay(event.getDateFrom(), R.drawable.ic_event_icon);
                        myEventDays.add(eventDay);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            calendarView.setEvents(myEventDays);
            super.onPostExecute(s);
        }
    }


    // On result method for check Calendar Permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1000) { // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (!calendarHelper.isCalendarExist("task")) {  // Add new calendars for using them in app if they are not exist
                    calendarHelper.addNewCalendar("task", "Calendar", Color.RED);
                    calendarHelper.addNewCalendar("meeting", "Calendar", Color.GREEN);
                    calendarHelper.addNewCalendar("birthday", "Calendar", Color.BLUE);
                }
                setEventDays();
            }
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}

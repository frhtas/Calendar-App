package ce.yildiz.edu.tr.mycalendar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static ce.yildiz.edu.tr.mycalendar.BirthdayFragment.getTimeAsString;


// An activity which show us Weekly and Daily views of calendar
public class WeeklyDailyActivity extends AppCompatActivity {
    Toolbar myToolbar;
    Menu menu;
    WeekView mWeekView;
    FloatingActionButton floatingActionButton_addEvent, floatingActionButton_today;

    Calendar selectedDay;
    String selection;
    Calendar today;
    CalendarHelper calendarHelper;
    SharedPreferences sharedPreferences;
    SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_daily);

        floatingActionButton_addEvent = (FloatingActionButton) findViewById(R.id.floatingActionButton_addEvent);
        floatingActionButton_today = (FloatingActionButton) findViewById(R.id.floatingActionButton_today);
        myToolbar = (Toolbar) findViewById(R.id.toolbarWeek);
        setSupportActionBar(myToolbar);

        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        Boolean mode = sharedPreferences.getBoolean("mode", false);
        SettingsActivity.setMode(mode);

        calendarHelper = new CalendarHelper(this);
        setWeekViewSettings();
        setToolbarTitleAsMonth();

        mWeekView.setMonthChangeListener(mMonthChangeListener);               // Set events when month is changed
        mWeekView.setScrollListener(mScrollListener);                         // Change month name etc. after scrolling
        mWeekView.setOnEventClickListener(mEventClickListener);               // Set an action when any event is clicked
        mWeekView.setEmptyViewClickListener(mEmptyViewClickListener);         // Set an action when any empty day is clicked
        mWeekView.setEmptyViewLongPressListener(mEmptyViewLongPressListener); // Set long click listener for adding event
        mWeekView.setAddEventClickListener(mAddEventClickListener);           // Set listener for adding event


        // Go to BottomNavigationActivity to add event
        floatingActionButton_addEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WeeklyDailyActivity.this, BottomNavigationActivity.class);
                intent.putExtra("selectedCalendarDay", selectedDay);
                startActivityForResult(intent , 2);
            }
        });


        // Go back to today with button
        floatingActionButton_today.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                floatingActionButton_today.hide();
                mWeekView.goToToday();
                mWeekView.goToHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
                selectedDay.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
                setToolbarTitleAsMonth();
            }
        });

    }


    // Setting week view settings when activity starts: Weekly or Daily, and get lastSelectedDay
    public void setWeekViewSettings() {
        dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        today = Calendar.getInstance();
        selectedDay = Calendar.getInstance();
        mWeekView = (WeekView) findViewById(R.id.weekView);
        if (selection == null)
            selection = getIntent().getStringExtra("selection");

        setDateAfterChange("lastSelectedDay"); // Set date after view change (Monthly, Weekly, Daily)
        setDateAfterChange("deletedEventDay"); // Set date after an event deleted
        setDateAfterChange("updatedDay");      // Set date after an event updated

        if (selection.equals("weekly")) {
            mWeekView.setNumberOfVisibleDays(7);
            mWeekView.setEventTextSize(30);
            mWeekView.setTextSize(28);
        }
        else if (selection.equals("daily")) {
            mWeekView.setNumberOfVisibleDays(1);
            mWeekView.setEventTextSize(40);
            mWeekView.setTextSize(30);
        }

        mWeekView.goToDate(selectedDay);
        mWeekView.goToHour(today.get(Calendar.HOUR_OF_DAY));
        interpretDate();
    }


    // Set date again after an event deleted or updated
    public void setDateAfterChange(String key) {
        Calendar eventDay = (Calendar) getIntent().getSerializableExtra(key);
        if (eventDay != null) {
            selectedDay.setTimeInMillis(eventDay.getTimeInMillis());
            setToolbarTitleAsMonth();
        }
    }


    // Listener for watching Month change to get events from CalendarProvider
    MonthLoader.MonthChangeListener mMonthChangeListener = new MonthLoader.MonthChangeListener() {
        @Override
        public List<WeekViewEvent> onMonthChange(int newYear, int newMonth) {
            // Populate the week view with some events
            ArrayList<WeekViewEvent> events = calendarHelper.getAllEventsMonthly(newYear, newMonth-1);
            return events;
        }
    };


    // Listener for watch the scrolling and change the month name
    WeekView.ScrollListener mScrollListener = new WeekView.ScrollListener() {
        @Override
        public void onFirstVisibleDayChanged(Calendar newFirstVisibleDay, Calendar oldFirstVisibleDay) {
            if (selection.equals("weekly")) {
                if (newFirstVisibleDay.getTimeInMillis() > today.getTimeInMillis() || (today.getTimeInMillis() - newFirstVisibleDay.getTimeInMillis() > 7*1440*60000))
                    floatingActionButton_today.show();
                else
                    floatingActionButton_today.hide();
            }
            else if (selection.equals("daily")) {
                if (!dateFormat.format(newFirstVisibleDay.getTime()).equals(dateFormat.format(today.getTime())))
                    floatingActionButton_today.show();
                else
                    floatingActionButton_today.hide();
            }

            selectedDay.setTimeInMillis(newFirstVisibleDay.getTimeInMillis());
            setToolbarTitleAsMonth();
        }
    };


    // Listener for going to ShowEventActivity to show event details
    WeekView.EventClickListener mEventClickListener = new WeekView.EventClickListener() {
        @Override
        public void onEventClick(WeekViewEvent event, RectF eventRect) {
            long eventID = event.getId();
            Event myEvent = calendarHelper.getEventsByID(eventID, event.getStartTime());
            Intent showActivityEvent = new Intent(WeeklyDailyActivity.this, ShowEventActivity.class);
            showActivityEvent.putExtra("selectedEvent", myEvent);
            showActivityEvent.putExtra("activityComeFrom", "WeeklyDailyActivity");
            showActivityEvent.putExtra("selection", selection);
            startActivity(showActivityEvent);
        }
    };


    // Listener for change empty view background to make it adding new event button
    WeekView.EmptyViewClickListener mEmptyViewClickListener = new WeekView.EmptyViewClickListener() {
        @Override
        public void onEmptyViewClicked(Calendar date) {
            selectedDay.setTimeInMillis(date.getTimeInMillis());
            selectedDay.set(Calendar.MINUTE, 00);
            mWeekView.setNewEventColor(R.color.colorAccent);
        }
    };


    // Listener for adding new event
    WeekView.EmptyViewLongPressListener mEmptyViewLongPressListener = new WeekView.EmptyViewLongPressListener() {
        @Override
        public void onEmptyViewLongPress(Calendar time) {
            selectedDay.setTimeInMillis(time.getTimeInMillis());
            selectedDay.set(Calendar.MINUTE, 00);

            Intent intent = new Intent(WeeklyDailyActivity.this, BottomNavigationActivity.class);
            intent.putExtra("selectedCalendarDay", selectedDay);
            startActivityForResult(intent , 2);
        }
    };


    // Listener for adding new event
    WeekView.AddEventClickListener mAddEventClickListener = new WeekView.AddEventClickListener() {
        @Override
        public void onAddEventClicked(Calendar startTime, Calendar endTime) {
            selectedDay.setTimeInMillis(startTime.getTimeInMillis());
            selectedDay.set(Calendar.MINUTE, 00);

            Intent intent = new Intent(WeeklyDailyActivity.this, BottomNavigationActivity.class);
            intent.putExtra("selectedCalendarDay", selectedDay);
            startActivityForResult(intent , 2);
        }
    };


    // Interpretting Header Day Names Style, also set MIN and MAX date for Calendar
    public void interpretDate() {
        mWeekView.setDateTimeInterpreter(new DateTimeInterpreter() {
            @Override
            public String interpretDate(Calendar date) {
                SimpleDateFormat sdfWeekly = new SimpleDateFormat("EEE" + "\n" + "M/dd", Locale.getDefault());
                SimpleDateFormat sdfDaily = new SimpleDateFormat("EEEE" + "\n" + "M/dd", Locale.getDefault());
                try {
                    if (selection.equals("weekly"))
                        return sdfWeekly.format(date.getTime()).toUpperCase();
                    else if (selection.equals("daily"))
                        return sdfDaily.format(date.getTime()).toUpperCase();
                } catch (Exception e) {
                    e.printStackTrace();
                    return "";
                }
                return sdfWeekly.format(date.getTime()).toUpperCase();
            }

            @Override
            public String interpretTime(int hour, int minutes) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minutes);
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                return sdf.format(calendar.getTime());
            }
        });
    }


    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        this.menu = menu;
        if (selection.equals("weekly"))
            menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_toolbar_weekly));
        else if (selection.equals("daily"))
            menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_toolbar_daily));
        return true;
    }

    // Toolbar options
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Intent intent;
        switch (item.getItemId()) {
            case R.id.toolbar_go_to_monthly:
                intent = new Intent(this, CalendarActivity.class);
                intent.putExtra("lastSelectedDay", selectedDay);
                editor.putString("view", "monthly").apply();
                startActivity(intent);
                finish();
                break;

            case R.id.toolbar_go_to_weekly:
                menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_toolbar_weekly));
                mWeekView.setNumberOfVisibleDays(7);
                mWeekView.setEventTextSize(30);
                mWeekView.setTextSize(28);
                mWeekView.goToDate(selectedDay);
                mWeekView.goToHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
                selection = "weekly";
                editor.putString("view", "weekly").apply();
                if (selectedDay.getTimeInMillis() > today.getTimeInMillis() || (today.getTimeInMillis() - selectedDay.getTimeInMillis() > 7*1440*60000))
                    floatingActionButton_today.show();
                else
                    floatingActionButton_today.hide();
                break;

            case R.id.toolbar_go_to_daily:
                menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_toolbar_daily));
                mWeekView.setNumberOfVisibleDays(1);
                mWeekView.setEventTextSize(45);
                mWeekView.setTextSize(30);
                mWeekView.goToDate(selectedDay);
                mWeekView.goToHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
                selection = "daily";
                editor.putString("view", "daily").apply();
                if (dateFormat.format(selectedDay.getTime()).equals(dateFormat.format(today.getTime())))
                    floatingActionButton_today.hide();
                else
                    floatingActionButton_today.show();
                break;

            case R.id.toolbar_eventlist:
                intent = new Intent(this, ListEventsActivity.class);
                intent.putExtra("activityComeFrom", "WeeklyDailyActivity");
                intent.putExtra("selection", selection);
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


    // A function which choose date on Calendar View
    public void chooseDate() {
        Calendar selectedDateTemp = Calendar.getInstance();
        selectedDateTemp.setTimeInMillis(selectedDay.getTimeInMillis());
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
                selectedDay.setTimeInMillis(selectedDateTemp.getTimeInMillis());
                mWeekView.goToDate(selectedDay);
                if (selectedDay.getTimeInMillis() > today.getTimeInMillis() || (today.getTimeInMillis() - selectedDay.getTimeInMillis() > 7*1440*60000))
                    floatingActionButton_today.show();
                else
                    floatingActionButton_today.hide();

                setToolbarTitleAsMonth();
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


    // Toolbar title will be current year and month name
    public void setToolbarTitleAsMonth() {
        myToolbar.setTitle(new SimpleDateFormat("yyyy MMMM", Locale.getDefault()).format(selectedDay.getTime()));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK) {
            Calendar selectedDate = null;
            if (data != null)
                selectedDate = (Calendar) data.getSerializableExtra("selectedDay");
            if (selectedDate != null)
                selectedDate.setTimeInMillis(selectedDate.getTimeInMillis());
            selectedDay.setTimeInMillis(selectedDate.getTimeInMillis());
            mWeekView.goToDate(selectedDay);
            mWeekView.notifyDatasetChanged();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}

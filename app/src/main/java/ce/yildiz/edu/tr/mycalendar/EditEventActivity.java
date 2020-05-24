package ce.yildiz.edu.tr.mycalendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;


// An activity which we can edit Event, Birthday etc. with layout change by EventType
public class EditEventActivity extends AppCompatActivity {
    private EditText editText_eventName, editText_description, editText_location, editText_attendees, editText_repeatCount;
    private TextView textView_timeFrom, textView_timeTo, textView_repeat, textView_reminder;
    private LinearLayout linearLayout_timeFrom, linearLayout_timeTo, linearLayout_repeat, linearLayout_reminder;

    private EditText editText_birthdayName;
    private TextView textView_when, textView_reminderBirthday;
    private LinearLayout linearLayout_when, linearLayout_reminderBirthday;

    private CoordinatorLayout coordinatorLayout_edit;
    private FloatingActionButton floatingActionButton_updateEvent;

    private Event event;
    private String[] reminderItems;
    private ArrayList<Integer> selectedReminders = new ArrayList<>();
    private String[] repeatItems;
    private int selectedRepeat;

    CalendarHelper calendarHelper;
    String activityComeFrom; // Which activity we came from, for going there after changes
    String selection;        // If the activity we came from is WeeklyDailyActivity, choose which one with this variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        calendarHelper = new CalendarHelper(this);

        activityComeFrom = getIntent().getStringExtra("activityComeFrom");
        selection = getIntent().getStringExtra("selection");
        event = (Event) getIntent().getSerializableExtra("editableEvent");
        coordinatorLayout_edit = (CoordinatorLayout) findViewById(R.id.coordinatorLayout_edit);
        View taskFragment;
        View meetingFragment;
        View birthdayFragment;

        switch (event.getEventType()) {
            case "task":
                taskFragment = getLayoutInflater().inflate(R.layout.fragment_event, coordinatorLayout_edit, false);
                chooseEventFragment(taskFragment);
                break;
            case "meeting":
                meetingFragment = getLayoutInflater().inflate(R.layout.fragment_event, coordinatorLayout_edit, false);
                chooseEventFragment(meetingFragment);
                break;
            case "birthday":
                birthdayFragment = getLayoutInflater().inflate(R.layout.fragment_birthday, coordinatorLayout_edit, false);
                chooseBirthdayFragment(birthdayFragment);
                break;
        }


        floatingActionButton_updateEvent = (FloatingActionButton) findViewById(R.id.floatingActionButton_updateEvent);
        floatingActionButton_updateEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarHelper.deleteEvent(event);
                switch (event.getEventType()) {
                    case "task":
                        addNewEvent();
                        Toast.makeText(EditEventActivity.this, R.string.task_updated_successfully, Toast.LENGTH_SHORT).show();
                        break;
                    case "meeting":
                        addNewEvent();
                        Toast.makeText(EditEventActivity.this, R.string.meeting_updated_successfully, Toast.LENGTH_SHORT).show();
                        break;
                    case "birthday":
                        addNewBirthdayEvent();
                        Toast.makeText(EditEventActivity.this, R.string.birthday_updated_successfully, Toast.LENGTH_SHORT).show();
                        break;
                }

                Intent intent = new Intent();
                if (activityComeFrom.equals("WeeklyDailyActivity")) {
                    intent = new Intent(EditEventActivity.this, WeeklyDailyActivity.class);
                    intent.putExtra("selection", selection);
                }
                else if (activityComeFrom.equals("CalendarActivity"))
                    intent = new Intent(EditEventActivity.this, CalendarActivity.class);

                intent.putExtra("updatedDay", event.getDateFrom());
                startActivity(intent);
                finish();
            }
        });

    }


    // If event is a Task or Meeting, inflate layout
    public void chooseEventFragment (View fragment) {
        coordinatorLayout_edit.addView(fragment);
        editText_eventName = fragment.findViewById(R.id.editText_eventName);
        editText_eventName.setText(event.getEventName());
        editText_description = fragment.findViewById(R.id.editText_description);

        if (event.getEventType().equals("meeting")) {
            editText_eventName.setHint("Meeting");
            editText_description.setVisibility(View.GONE);
            editText_attendees = (EditText) fragment.findViewById(R.id.editText_attendees);
            editText_attendees.setVisibility(View.VISIBLE);
            editText_attendees.setText(event.getDescription());
        }
        else if (event.getEventType().equals("task")) {
            editText_description.setText(event.getDescription());
        }

        editText_location = fragment.findViewById(R.id.editText_location);
        editText_location.setText(event.getLocation());
        textView_timeFrom = (TextView) fragment.findViewById(R.id.textView_timeFrom);
        textView_timeFrom.setText(TaskFragment.getTimeAsString(event.getDateFrom()));
        textView_timeTo = (TextView) fragment.findViewById(R.id.textView_timeTo);
        textView_timeTo.setText(TaskFragment.getTimeAsString(event.getDateTo()));

        linearLayout_timeFrom = (LinearLayout) fragment.findViewById(R.id.linearLayout_timeFrom);
        linearLayout_timeFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createBottomTimePicker(fragment.getContext(), event.getDateFrom(), "From");
            }
        });

        linearLayout_timeTo = (LinearLayout) fragment.findViewById(R.id.linearLayout_timeTo);
        linearLayout_timeTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createBottomTimePicker(fragment.getContext(), event.getDateTo(), "To");
            }
        });


        editText_repeatCount = (EditText) fragment.findViewById(R.id.editText_repeatCount);
        selectedRepeat = event.getRepeatTime();

        if (selectedRepeat != 0) {
            editText_repeatCount.setVisibility(View.VISIBLE);
            editText_repeatCount.setText(String.valueOf(event.getRepeatCount()));
        }

        repeatItems = getResources().getStringArray(R.array.repeat_items);
        textView_repeat = (TextView) fragment.findViewById(R.id.textView_repeat);
        textView_repeat.setText(repeatItems[selectedRepeat]);
        linearLayout_repeat = (LinearLayout) fragment.findViewById(R.id.linearLayout_repeat);
        linearLayout_repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAlertDialogRepeat();
            }
        });

        reminderItems = getResources().getStringArray(R.array.reminder_items);
        textView_reminder = (TextView) fragment.findViewById(R.id.textView_reminder);
        textView_reminder.setText(getItemsAsString());
        linearLayout_reminder = (LinearLayout) fragment.findViewById(R.id.linearLayout_reminder);
        linearLayout_reminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAlertDialogReminder();
            }
        });

    }

    // A function which add an event after user confirmed
    public void addNewEvent() {
        String descriptionOrAttendee = "";

        if (event.getEventType().equals("task"))
            descriptionOrAttendee = editText_description.getText().toString();
        else if (event.getEventType().equals("meeting"))
            descriptionOrAttendee = editText_attendees.getText().toString();

        String eventName = editText_eventName.getText().toString();
        String location = editText_location.getText().toString();

        int repeatCount = 5;  // Default value of repeat count, user can customize with input
        if (!editText_repeatCount.getText().toString().equals(""))
            repeatCount = Integer.parseInt(editText_repeatCount.getText().toString());

        Event newTaskEvent = new Event(event.getEventType(), event.getDateFrom(), event.getDateTo(),
                eventName, descriptionOrAttendee, location, selectedRepeat, repeatCount, selectedReminders);

        calendarHelper.addNewEvent(newTaskEvent);
    }


    // If event is a birthday, inflate layout as BirthdayFragment
    public void chooseBirthdayFragment(View birthdayFragment) {
        coordinatorLayout_edit.addView(birthdayFragment);
        editText_birthdayName = birthdayFragment.findViewById(R.id.editText_birthdayName);
        editText_birthdayName.setText(event.getEventName().split("'")[0]);
        textView_when = (TextView) birthdayFragment.findViewById(R.id.textView_when);
        textView_when.setText(BirthdayFragment.getTimeAsString(event.getDateFrom()));

        linearLayout_when = (LinearLayout) birthdayFragment.findViewById(R.id.linearLayout_when);
        linearLayout_when.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createBottomDatePicker(birthdayFragment.getContext());
            }
        });

        reminderItems = getResources().getStringArray(R.array.reminder_items);
        textView_reminderBirthday = (TextView) birthdayFragment.findViewById(R.id.textView_reminderBirthday);
        textView_reminderBirthday.setText(getItemsAsString());
        linearLayout_reminderBirthday = (LinearLayout) birthdayFragment.findViewById(R.id.linearLayout_reminderBirthday);
        linearLayout_reminderBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAlertDialogReminder();
            }
        });

    }

    // A function which add a birthday after user confirmed
    public void addNewBirthdayEvent() {
        String eventType = "birthday";
        String eventName = editText_birthdayName.getText().toString() + "" + getResources().getString(R.string.sbirthday);

        event.getDateFrom().set(Calendar.HOUR_OF_DAY, 03);
        event.getDateFrom().set(Calendar.MINUTE, 00);

        Event newBirthdayEvent = new Event(eventType, event.getDateFrom(), event.getDateFrom(), eventName, "", "",
                4, CalendarHelper.MAX_YEAR - CalendarHelper.MIN_YEAR, selectedReminders);
        calendarHelper.addNewEvent(newBirthdayEvent);
    }


    // Get selected items as string
    public String getItemsAsString() {
        selectedReminders = new ArrayList<>(event.getReminders());

        StringBuilder items = new StringBuilder();
        if (selectedReminders.size() != 0) {
            for (int j = 0; j<selectedReminders.size(); j++) {
                if (j == 0)
                    items.append(reminderItems[selectedReminders.get(j)]);
                else
                    items.append(", ").append(reminderItems[selectedReminders.get(j)]);
            }
        }

        return items.toString();
    }


    // A function which create a bottom sheet dialog to pick the date and time
    public void createBottomTimePicker(Context context, final Calendar selectedDate, final String FromTo) {
        Calendar selectedDayTemp = Calendar.getInstance();
        selectedDayTemp.setTimeInMillis(selectedDate.getTimeInMillis());
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View parentView = getLayoutInflater().inflate(R.layout.bottom_time_picker, null);
        bottomSheetDialog.setContentView(parentView);
        bottomSheetDialog.show();

        TextView textView_pickerTimeText = parentView.findViewById(R.id.textView_pickerTimeText);

        if (FromTo.equals("From"))
            textView_pickerTimeText.setText(R.string.from);
        else if (FromTo.equals("To"))
            textView_pickerTimeText.setText(R.string.to);

        final TextView textView_pickerTime = parentView.findViewById(R.id.textView_pickerTime);
        textView_pickerTime.setText(TaskFragment.getTimeAsString(selectedDayTemp));

        DatePicker datePicker = parentView.findViewById(R.id.datePicker);
        datePicker.init(selectedDayTemp.get(Calendar.YEAR), selectedDayTemp.get(Calendar.MONTH), selectedDayTemp.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int year, int month, int dayOfMonth) {
                selectedDayTemp.set(Calendar.MONTH, month);
                selectedDayTemp.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                textView_pickerTime.setText(TaskFragment.getTimeAsString(selectedDayTemp));
            }
        } );

        TimePicker timePicker = parentView.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        timePicker.setHour(selectedDayTemp.get(Calendar.HOUR_OF_DAY));
        timePicker.setMinute(selectedDayTemp.get(Calendar.MINUTE));
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hour, int min) {
                selectedDayTemp.set(Calendar.HOUR_OF_DAY, hour);
                selectedDayTemp.set(Calendar.MINUTE, min);
                textView_pickerTime.setText(TaskFragment.getTimeAsString(selectedDayTemp));
            }
        });

        Calendar dateFrom = event.getDateFrom();
        Calendar dateTo = event.getDateTo();
        Button button_ok = (Button) parentView.findViewById(R.id.button_ok);
        button_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (FromTo.equals("From")) {
                    dateFrom.setTimeInMillis(selectedDayTemp.getTimeInMillis());
                    if (dateFrom.after(dateTo)) {
                        dateTo.setTimeInMillis(dateFrom.getTimeInMillis());
                        dateTo.add(Calendar.HOUR_OF_DAY, 1);
                    }
                    textView_timeFrom.setText(TaskFragment.getTimeAsString(dateFrom));
                    textView_timeTo.setText(TaskFragment.getTimeAsString(dateTo));
                }

                else if (FromTo.equals("To")) {
                    dateTo.setTimeInMillis(selectedDayTemp.getTimeInMillis());
                    if (dateTo.before(dateFrom)) {
                        dateTo.setTimeInMillis(dateFrom.getTimeInMillis());
                    }
                    textView_timeFrom.setText(TaskFragment.getTimeAsString(dateFrom));
                    textView_timeTo.setText(TaskFragment.getTimeAsString(dateTo));
                }

                event.setDateFrom(dateFrom);
                event.setDateTo(dateTo);
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


    // A function which create a bottom sheet dialog to pick the birthday date
    public void createBottomDatePicker(Context context) {
        final Calendar birthdayDate = Calendar.getInstance();
        birthdayDate.set(event.getDateFrom().get(Calendar.YEAR), event.getDateFrom().get(Calendar.MONTH), event.getDateFrom().get(Calendar.DAY_OF_MONTH));

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View parentView = getLayoutInflater().inflate(R.layout.bottom_time_picker, null);
        bottomSheetDialog.setContentView(parentView);
        bottomSheetDialog.show();

        TimePicker timePicker = parentView.findViewById(R.id.timePicker);
        timePicker.setVisibility(View.GONE);

        TextView textView_pickerTimeText = parentView.findViewById(R.id.textView_pickerTimeText);
        textView_pickerTimeText.setText(R.string.birthday_date);
        final TextView textView_pickerTime = parentView.findViewById(R.id.textView_pickerTime);
        textView_pickerTime.setText(BirthdayFragment.getTimeAsString(birthdayDate));

        DatePicker datePicker = parentView.findViewById(R.id.datePicker);
        datePicker.init(birthdayDate.get(Calendar.YEAR), birthdayDate.get(Calendar.MONTH), birthdayDate.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int year, int month, int dayOfMonth) {
                birthdayDate.set(Calendar.YEAR, year);
                birthdayDate.set(Calendar.MONTH, month);
                birthdayDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                textView_pickerTime.setText(BirthdayFragment.getTimeAsString(birthdayDate));
            }
        } );


        Button button_ok = (Button) parentView.findViewById(R.id.button_ok);
        button_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                event.setDateFrom(birthdayDate);
                textView_when.setText(BirthdayFragment.getTimeAsString(birthdayDate));
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


    // A function which create a alert dialog to pick the default repeat for events
    public void createAlertDialogRepeat() {
        int tempRepeat = selectedRepeat;
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this, R.style.AlertDialog);
        mBuilder.setSingleChoiceItems(repeatItems, selectedRepeat, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int selected) {
                selectedRepeat = selected;
            }
        });

        mBuilder.setCancelable(false);
        mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                textView_repeat.setText(repeatItems[selectedRepeat]);
                if (selectedRepeat != 0) {
                    String[] repeatCountHints = getResources().getStringArray(R.array.repeat_count_hints);
                    editText_repeatCount.setHint(repeatCountHints[selectedRepeat]);
                    editText_repeatCount.setVisibility(View.VISIBLE);
                }
                else {
                    editText_repeatCount.setVisibility(View.GONE);
                    editText_repeatCount.setText("");
                }

            }
        });

        mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                selectedRepeat = tempRepeat;
                dialogInterface.dismiss();
            }
        });

        TextView alertHeader = new TextView(this);
        alertHeader.setTextColor(Color.WHITE); alertHeader.setText(R.string.repeat); alertHeader.setTextSize(22);
        alertHeader.setBackgroundColor(this.getColor(R.color.colorAccent));
        alertHeader.setPadding(74, 25, 20, 25);
        mBuilder.setCustomTitle(alertHeader);
        AlertDialog mDialog = mBuilder.create();
        mDialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.rounded_alertdialog));

        mDialog.show();
    }


    // A function which create a alert dialog to pick the reminders for event
    public void createAlertDialogReminder() {
        ArrayList<Integer> tempReminders = new ArrayList<>(selectedReminders);
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this, R.style.AlertDialog);
        mBuilder.setMultiChoiceItems(reminderItems, new boolean[reminderItems.length], new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
                if(isChecked) {
                    if (!tempReminders.contains(position))
                        tempReminders.add(position);
                }
                else if (tempReminders.contains(position))
                    tempReminders.remove((Integer) position);
            }
        });

        mBuilder.setCancelable(false);
        mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                selectedReminders = new ArrayList<>(tempReminders);
                StringBuilder selectedRem = new StringBuilder();
                if (selectedReminders.size() != 0) {
                    for (int j = 0; j<selectedReminders.size(); j++) {
                        if (j == 0)
                            selectedRem.append(reminderItems[selectedReminders.get(j)]);
                        else
                            selectedRem.append(", ").append(reminderItems[selectedReminders.get(j)]);
                    }
                }

                if (event.getEventType().equals("birthday"))
                    textView_reminderBirthday.setText(selectedRem.toString());
                else
                    textView_reminder.setText(selectedRem.toString());
            }
        });

        mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        TextView alertHeader = new TextView(this);
        alertHeader.setTextColor(Color.WHITE); alertHeader.setText(R.string.reminders); alertHeader.setTextSize(22);
        alertHeader.setBackgroundColor(this.getColor(R.color.colorAccent));
        alertHeader.setPadding(74, 25, 20, 25);
        mBuilder.setCustomTitle(alertHeader);
        AlertDialog mDialog = mBuilder.create();
        mDialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.rounded_alertdialog));

        mDialog.show();
        for (int k = 0; k<tempReminders.size(); k++) {
            mDialog.getListView().setItemChecked(tempReminders.get(k), true);
        }
    }

}

package ce.yildiz.edu.tr.mycalendar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


// A fragment which we can add event with its name, from, to, description, location, repeat, reminders etc.
public class TaskFragment extends Fragment {
    private EditText editText_eventName, editText_description, editText_location, editText_repeatCount;
    private TextView textView_timeFrom, textView_timeTo, textView_repeat, textView_reminder;
    private LinearLayout linearLayout_timeFrom, linearLayout_timeTo, linearLayout_repeat, linearLayout_reminder;

    private Calendar selectedCalendarDay;
    private Calendar dateFrom;
    private Calendar dateTo;

    private String[] repeatItems;
    private String[] repeatCountHints;
    private String[] reminderItems;
    private ArrayList<Integer> selectedReminders = new ArrayList<>();
    private int selectedRepeat;
    private int defaultReminder;

    public TaskFragment(Calendar selectedCalendarDay) {
        this.selectedCalendarDay = selectedCalendarDay;

        dateFrom = Calendar.getInstance();
        dateTo = Calendar.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event, container, false);
        getDefSettings();

        editText_eventName = (EditText) view.findViewById(R.id.editText_eventName);
        editText_eventName.requestFocus();
        editText_description = (EditText) view.findViewById(R.id.editText_description);
        editText_location = (EditText) view.findViewById(R.id.editText_location);

        dateFrom.setTimeInMillis(selectedCalendarDay.getTimeInMillis());
        textView_timeFrom = (TextView) view.findViewById(R.id.textView_timeFrom);
        textView_timeFrom.setText(getTimeAsString(dateFrom));
        linearLayout_timeFrom = (LinearLayout) view.findViewById(R.id.linearLayout_timeFrom);
        linearLayout_timeFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createBottomTimePicker(getContext(), dateFrom, "From");
            }
        });

        dateTo.setTimeInMillis(selectedCalendarDay.getTimeInMillis());
        dateTo.add(Calendar.HOUR_OF_DAY, 1); // One hour after right now, initial

        textView_timeTo = (TextView) view.findViewById(R.id.textView_timeTo);
        textView_timeTo.setText(getTimeAsString(dateTo));
        linearLayout_timeTo = (LinearLayout) view.findViewById(R.id.linearLayout_timeTo);
        linearLayout_timeTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               createBottomTimePicker(getContext(), dateTo, "To");
            }
        });


        editText_repeatCount = (EditText) view.findViewById(R.id.editText_repeatCount);
        repeatCountHints = getResources().getStringArray(R.array.repeat_count_hints);
        if (selectedRepeat != 0) {
            editText_repeatCount.setVisibility(View.VISIBLE);
            editText_repeatCount.setHint(repeatCountHints[selectedRepeat]);
        }

        repeatItems = getResources().getStringArray(R.array.repeat_items);
        textView_repeat = (TextView) view.findViewById(R.id.textView_repeat);
        textView_repeat.setText(repeatItems[selectedRepeat]);
        linearLayout_repeat = (LinearLayout) view.findViewById(R.id.linearLayout_repeat);
        linearLayout_repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAlertDialogRepeat();
            }
        });


        reminderItems = getResources().getStringArray(R.array.reminder_items);
        textView_reminder = (TextView) view.findViewById(R.id.textView_reminder);
        selectedReminders.add(defaultReminder);
        textView_reminder.setText(reminderItems[defaultReminder]);
        linearLayout_reminder = (LinearLayout) view.findViewById(R.id.linearLayout_reminder);
        linearLayout_reminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAlertDialogReminder();
            }
        });

        return view;
    }


    // Get default settings with Shared Preferences

    public void getDefSettings() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("AppSettings", getContext().MODE_PRIVATE);
        selectedRepeat = sharedPreferences.getInt("defaultRepeat", 0);
        defaultReminder = sharedPreferences.getInt("defaultReminder", 0);
        repeatItems = getResources().getStringArray(R.array.repeat_items);
        reminderItems = getResources().getStringArray(R.array.reminder_items);
    }


    // A function which add an event after user confirmed
    public Calendar addNewTask() {
        String eventType = "task";
        String eventName = editText_eventName.getText().toString();
        String description =  editText_description.getText().toString();
        String location = editText_location.getText().toString();

        int repeatCount = 5;  // Default value of repeat count, user can customize with input
        if (!editText_repeatCount.getText().toString().equals(""))
            repeatCount = Integer.parseInt(editText_repeatCount.getText().toString());

        Event newTaskEvent = new Event(eventType, dateFrom, dateTo, eventName, description, location, selectedRepeat, repeatCount, selectedReminders);

        CalendarHelper calendarHelper = new CalendarHelper(getContext());
        calendarHelper.addNewEvent(newTaskEvent);
        return dateFrom;
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
        textView_pickerTime.setText(getTimeAsString(selectedDayTemp));

        DatePicker datePicker = parentView.findViewById(R.id.datePicker);
        datePicker.init(selectedDayTemp.get(Calendar.YEAR), selectedDayTemp.get(Calendar.MONTH), selectedDayTemp.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int year, int month, int dayOfMonth) {
                selectedDayTemp.set(Calendar.MONTH, month);
                selectedDayTemp.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                textView_pickerTime.setText(getTimeAsString(selectedDayTemp));
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
                textView_pickerTime.setText(getTimeAsString(selectedDayTemp));
            }
        });


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
                    textView_timeFrom.setText(getTimeAsString(dateFrom));
                    textView_timeTo.setText(getTimeAsString(dateTo));
                }
                else if (FromTo.equals("To")) {
                    dateTo.setTimeInMillis(selectedDayTemp.getTimeInMillis());
                    if (dateTo.before(dateFrom)) {
                        dateTo.setTimeInMillis(dateFrom.getTimeInMillis());
                    }
                    textView_timeFrom.setText(getTimeAsString(dateFrom));
                    textView_timeTo.setText(getTimeAsString(dateTo));
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


    // A function which create a alert dialog to pick the default repeat for events
    public void createAlertDialogRepeat() {
        int tempRepeat = selectedRepeat;
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getContext(), R.style.AlertDialog);
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

        TextView alertHeader = new TextView(getContext());
        alertHeader.setTextColor(Color.WHITE); alertHeader.setText(R.string.repeat); alertHeader.setTextSize(22);
        alertHeader.setBackgroundColor(getContext().getColor(R.color.colorAccent));
        alertHeader.setPadding(74, 25, 20, 25);
        mBuilder.setCustomTitle(alertHeader);
        AlertDialog mDialog = mBuilder.create();
        mDialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.rounded_alertdialog));

        mDialog.show();
    }


    // A function which create a alert dialog to pick the reminders for event
    public void createAlertDialogReminder() {
        ArrayList<Integer> tempReminders = new ArrayList<>(selectedReminders);
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getContext(), R.style.AlertDialog);
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
                textView_reminder.setText(selectedRem.toString());
            }
        });

        mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        TextView alertHeader = new TextView(getContext());
        alertHeader.setTextColor(Color.WHITE); alertHeader.setText(R.string.reminders); alertHeader.setTextSize(22);
        alertHeader.setBackgroundColor(getContext().getColor(R.color.colorAccent));
        alertHeader.setPadding(74, 25, 20, 25);
        mBuilder.setCustomTitle(alertHeader);
        AlertDialog mDialog = mBuilder.create();
        mDialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.rounded_alertdialog));

        mDialog.show();
        for (int k = 0; k<tempReminders.size(); k++) {
            mDialog.getListView().setItemChecked(tempReminders.get(k), true);
        }
    }


    // Get time as string by calendar
    public static String getTimeAsString(Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", Locale.getDefault());
        String selectedTime = dateFormat.format(calendar.getTime());

        return selectedTime;
    }

}

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


// A fragment which we can add birthday event with name, when, reminders etc.
public class BirthdayFragment extends Fragment {
    private EditText editText_birthdayName;
    private TextView textView_when, textView_reminderBirthday;
    private LinearLayout linearLayout_when, linearLayout_reminderBirthday;

    private Calendar selectedCalendarDay;

    private String[] reminderItems;
    private ArrayList<Integer> selectedReminders = new ArrayList<>();
    private int defaultReminder;

    public BirthdayFragment(Calendar selectedCalendarDay) {
        this.selectedCalendarDay = selectedCalendarDay;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_birthday, container, false);

        editText_birthdayName = (EditText) view.findViewById(R.id.editText_birthdayName);
        editText_birthdayName.requestFocus();

        textView_when = (TextView) view.findViewById(R.id.textView_when);
        textView_when.setText(getTimeAsString(selectedCalendarDay));

        linearLayout_when = (LinearLayout) view.findViewById(R.id.linearLayout_when);
        linearLayout_when.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createBottomDatePicker(getContext());
            }
        });


        SharedPreferences sharedPreferences = getContext().getSharedPreferences("AppSettings", getContext().MODE_PRIVATE);
        defaultReminder = sharedPreferences.getInt("defaultReminder", 0);
        reminderItems = getResources().getStringArray(R.array.reminder_items);
        textView_reminderBirthday = (TextView) view.findViewById(R.id.textView_reminderBirthday);
        selectedReminders.add(defaultReminder);
        textView_reminderBirthday.setText(reminderItems[defaultReminder]);
        linearLayout_reminderBirthday = (LinearLayout) view.findViewById(R.id.linearLayout_reminderBirthday);
        linearLayout_reminderBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAlertDialogReminder();
            }
        });
        
        return view;
    }


    // A function which add a birthday after user confirmed
    public Calendar addNewBirthdayEvent() {
        String eventType = "birthday";
        String eventName = editText_birthdayName.getText().toString() + "" + getResources().getString(R.string.sbirthday);

        selectedCalendarDay.set(Calendar.HOUR_OF_DAY, 03);
        selectedCalendarDay.set(Calendar.MINUTE, 00);

        Event newBirthdayEvent = new Event(eventType, selectedCalendarDay, selectedCalendarDay, eventName, "", "",
                4, CalendarHelper.MAX_YEAR - CalendarHelper.MIN_YEAR, selectedReminders);
        CalendarHelper calendarHelper = new CalendarHelper(getContext());
        calendarHelper.addNewEvent(newBirthdayEvent);
        return selectedCalendarDay;
    }


    // A function which create a bottom sheet dialog to pick the birthday date
    public void createBottomDatePicker(Context context) {
        final Calendar birthdayDate = Calendar.getInstance();
        birthdayDate.set(selectedCalendarDay.get(Calendar.YEAR), selectedCalendarDay.get(Calendar.MONTH), selectedCalendarDay.get(Calendar.DAY_OF_MONTH));

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View parentView = getLayoutInflater().inflate(R.layout.bottom_time_picker, null);
        bottomSheetDialog.setContentView(parentView);
        bottomSheetDialog.show();

        TimePicker timePicker = parentView.findViewById(R.id.timePicker);
        timePicker.setVisibility(View.GONE);

        TextView textView_pickerTimeText = parentView.findViewById(R.id.textView_pickerTimeText);
        textView_pickerTimeText.setText(R.string.birthday_date);
        final TextView textView_pickerTime = parentView.findViewById(R.id.textView_pickerTime);
        textView_pickerTime.setText(getTimeAsString(birthdayDate));

        DatePicker datePicker = parentView.findViewById(R.id.datePicker);
        datePicker.init(birthdayDate.get(Calendar.YEAR), birthdayDate.get(Calendar.MONTH), birthdayDate.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int year, int month, int dayOfMonth) {
                birthdayDate.set(Calendar.YEAR, year);
                birthdayDate.set(Calendar.MONTH, month);
                birthdayDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                textView_pickerTime.setText(getTimeAsString(birthdayDate));
            }
        } );


        Button button_ok = (Button) parentView.findViewById(R.id.button_ok);
        button_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedCalendarDay.setTimeInMillis(birthdayDate.getTimeInMillis());
                textView_when.setText(getTimeAsString(selectedCalendarDay));
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
                textView_reminderBirthday.setText(selectedRem.toString());
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
        String selectedTime = dateFormat.format(calendar.getTime());

        return selectedTime;
    }

}

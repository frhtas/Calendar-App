package ce.yildiz.edu.tr.mycalendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;


// An activity which we can show the event details, and we can edit and delete the event
public class ShowEventActivity extends AppCompatActivity implements View.OnClickListener {
    ImageView imageView_showIcon;
    Button button_edit, button_delete, button_share;
    TextView textView_showTitle, textView_showDate, textView_showDescriptionText, textView_showDescription,
            textView_showLocation, textView_showRepeat, textView_showReminders;
    CardView cardview_eventDetails, cardView_location, cardView_description, cardView_repeat, cardView_reminders;
    LinearLayout linearLayout_desc_loc, linearLayout_rep_rem;

    CalendarHelper calendarHelper;
    Event event;
    String[] reminderItems;
    String[] repeatItems;

    String activityComeFrom; // Which activity we came from, for going there after changes
    String selection;        // If the activity we came from is WeeklyDailyActivity, choose which one with this variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_event);

        SharedPreferences sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        Boolean mode = sharedPreferences.getBoolean("mode", false);
        SettingsActivity.setMode(mode);

        calendarHelper = new CalendarHelper(this);
        event = (Event) getIntent().getSerializableExtra("selectedEvent");  // Come from Calendar Adapter Items, WeeklyDailyActivity or Notifications
        activityComeFrom = getIntent().getStringExtra("activityComeFrom");
        selection = getIntent().getStringExtra("selection");

        imageView_showIcon = (ImageView) findViewById(R.id.imageView_showIcon);
        textView_showTitle = (TextView) findViewById(R.id.textView_showTitle);
        textView_showTitle.setText(event.getEventName());

        textView_showDate = (TextView) findViewById(R.id.textView_showDate);

        textView_showDescriptionText = (TextView) findViewById(R.id.textView_showDescriptionText);
        textView_showDescription = (TextView) findViewById(R.id.textView_showDescription);
        textView_showDescription.setText(event.getDescription());

        textView_showLocation = (TextView) findViewById(R.id.textView_showLocation);
        textView_showLocation.setText(event.getLocation());


        repeatItems = getResources().getStringArray(R.array.repeat_items);
        textView_showRepeat = (TextView) findViewById(R.id.textView_showRepeat);
        textView_showRepeat.setText(repeatItems[event.getRepeatTime()]);


        reminderItems = getResources().getStringArray(R.array.reminder_items);
        ArrayList<Integer> selectedReminders = new ArrayList<>(event.getReminders());
        StringBuilder reminders = new StringBuilder();
        if (selectedReminders.size() != 0) {
            for (int j = 0; j<selectedReminders.size(); j++) {
                if (j < selectedReminders.size()-1)
                    reminders.append(reminderItems[selectedReminders.get(j)]).append("\n");
                else
                    reminders.append(reminderItems[selectedReminders.get(j)]);
            }
        }
        textView_showReminders = (TextView) findViewById(R.id.textView_showReminders);
        textView_showReminders.setText(reminders.toString());

        button_edit = (Button) findViewById(R.id.button_edit);
        button_delete = (Button) findViewById(R.id.button_delete);
        button_share = (Button) findViewById(R.id.button_share);
        cardview_eventDetails = (CardView) findViewById(R.id.cardview_eventDetails);
        cardView_description = (CardView) findViewById(R.id.cardView_description);
        cardView_location = (CardView) findViewById(R.id.cardView_location);
        cardView_repeat = (CardView) findViewById(R.id.cardView_repeat);
        cardView_reminders = (CardView) findViewById(R.id.cardView_reminders);
        linearLayout_desc_loc = (LinearLayout) findViewById(R.id.linearLayout_desc_loc);
        linearLayout_rep_rem = (LinearLayout) findViewById(R.id.linearLayout_rep_rem);

        switch (event.getEventType()) {
            case "task":
                imageView_showIcon.setImageResource(R.drawable.ic_menu_event_black);
                textView_showDate.setText(CalendarEventAdapter.getTimeAsString(this, event));
                break;

            case "meeting":
                imageView_showIcon.setImageResource(R.drawable.ic_meeting_black_24dp);
                textView_showDescriptionText.setText(R.string.attendees);
                textView_showDate.setText(CalendarEventAdapter.getTimeAsString(this, event));
                break;

            case "birthday":
                imageView_showIcon.setImageResource(R.drawable.ic_menu_birthday_black);
                linearLayout_desc_loc.setVisibility(View.GONE);
                cardView_repeat.setVisibility(View.GONE);
                textView_showDate.setText(CalendarEventAdapter.getTimeAsStringBirthday(this, event));
                break;

            case "Holidays in Turkey":
                button_edit.setVisibility(View.GONE);
                button_delete.setVisibility(View.GONE);
                imageView_showIcon.setVisibility(View.INVISIBLE);
                cardView_description.setVisibility(View.GONE);
                cardView_location.setVisibility(View.GONE);
                cardView_repeat.setVisibility(View.GONE);
                cardView_reminders.setVisibility(View.GONE);
                textView_showDate.setText(CalendarEventAdapter.getTimeAsStringBirthday(this, event));
                break;
        }

        button_edit.setOnClickListener(this);
        button_delete.setOnClickListener(this);
        button_share.setOnClickListener(this);
        cardView_location.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_edit:
                Intent editEventActivityIntent = new Intent(ShowEventActivity.this, EditEventActivity.class);
                if (activityComeFrom == null) {
                    SharedPreferences sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
                    String lastView = sharedPreferences.getString("view", "monthly");
                    if (lastView != null) {
                        switch (lastView) {
                            case "monthly":
                                editEventActivityIntent.putExtra("activityComeFrom", "CalendarActivity");
                                break;
                            case "weekly":
                                editEventActivityIntent.putExtra("activityComeFrom", "WeeklyDailyActivity");
                                editEventActivityIntent.putExtra("selection", "weekly");
                                break;
                            case "daily":
                                editEventActivityIntent.putExtra("activityComeFrom", "WeeklyDailyActivity");
                                editEventActivityIntent.putExtra("selection", "daily");
                                break;
                        }
                    }
                }
                else if (activityComeFrom.equals("WeeklyDailyActivity")) {
                    editEventActivityIntent.putExtra("activityComeFrom", "WeeklyDailyActivity");
                    editEventActivityIntent.putExtra("selection", selection);
                }
                else if (activityComeFrom.equals("CalendarActivity"))
                    editEventActivityIntent.putExtra("activityComeFrom", "CalendarActivity");

                editEventActivityIntent.putExtra("editableEvent", event);
                startActivity(editEventActivityIntent);
                break;

            case R.id.button_delete:
                createBottomDeletePicker(this);
                break;

            case R.id.button_share:
                shareEvent();
                break;

            case R.id.cardView_location:
                Uri uri = Uri.parse("http://maps.google.com/maps?q=" + event.getLocation());
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
        }
    }


    // Sharing event as an intent as an image; we need to get Storage Permission to share screenshots of event with saving it in external storage
    public void shareEvent() {
        if (ContextCompat.checkSelfPermission(this , Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ShowEventActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001);
            return;
        }

        int height = cardview_eventDetails.getHeight();
        int width = cardview_eventDetails.getWidth();
        Bitmap shareBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(shareBitmap);
        cardview_eventDetails.draw(canvas);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/png");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        shareBitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), shareBitmap, event.getEventName() + ".png", event.getDescription());
        Uri imageUri =  Uri.parse(path);

        String eventDetails = "";
        if (event.getEventType().equals("birthday")) {
            eventDetails = this.getString(R.string.name) + ": " + event.getEventName() + "\n" +
                    this.getString(R.string.choose_date) + ": " + BirthdayFragment.getTimeAsString(event.getDateFrom());
        }
        else if (event.getEventType().equals("task")) {
            eventDetails = this.getString(R.string.task) + ": " + event.getEventName() + "\n" +
                    this.getString(R.string.choose_date) + ": " + TaskFragment.getTimeAsString(event.getDateFrom()) + " - " + TaskFragment.getTimeAsString(event.getDateTo()) + "\n" +
                    this.getString(R.string.description) + ": " + event.getDescription() + "\n" +
                    this.getString(R.string.location) + ": " + event.getLocation();
        }
        else if (event.getEventType().equals("meeting")) {
            eventDetails = this.getString(R.string.meeting) + ": " + event.getEventName() + "\n" +
                    this.getString(R.string.choose_date) + ": " + TaskFragment.getTimeAsString(event.getDateFrom()) + " - " + TaskFragment.getTimeAsString(event.getDateTo()) + "\n" +
                    this.getString(R.string.attendees) + ": " + event.getDescription() + "\n" +
                    this.getString(R.string.location) + ": " + event.getLocation();
        }

        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, this.getString(R.string.event));
        shareIntent.putExtra(Intent.EXTRA_TEXT, eventDetails);
        startActivity(Intent.createChooser(shareIntent, "Share via"));

    }


    // A function which create a bottom sheet dialog to delete event or not
    public void createBottomDeletePicker(Context context) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View parentView = getLayoutInflater().inflate(R.layout.bottom_time_picker, null);
        bottomSheetDialog.setContentView(parentView);
        bottomSheetDialog.show();

        LinearLayout linearLayout_pickers = parentView.findViewById(R.id.linearLayout_pickers);
        linearLayout_pickers.setVisibility(View.GONE);

        TextView textView_pickerTimeText = parentView.findViewById(R.id.textView_pickerTimeText);
        textView_pickerTimeText.setText(R.string.delete_this_event);

        Button button_ok = (Button) parentView.findViewById(R.id.button_ok);
        button_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendarHelper.deleteEvent(event);
                switch (event.getEventType()) {
                    case "task":
                        Toast.makeText(context, R.string.task_deleted_successfully, Toast.LENGTH_SHORT).show();
                        break;
                    case "meeting":
                        Toast.makeText(context, R.string.meeting_deleted_successfully, Toast.LENGTH_SHORT).show();
                        break;
                    case "birthday":
                        Toast.makeText(context, R.string.birthday_deleted_successfully, Toast.LENGTH_SHORT).show();
                        break;
                }

                Intent intent = new Intent();
                if (activityComeFrom == null) {
                    SharedPreferences sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
                    String lastView = sharedPreferences.getString("view", "monthly");
                    if (lastView != null) {
                        switch (lastView) {
                            case "monthly":
                                intent = new Intent(ShowEventActivity.this, CalendarActivity.class);
                                intent.putExtra("activityComeFrom", "CalendarActivity");
                                break;
                            case "weekly":
                                intent = new Intent(ShowEventActivity.this, WeeklyDailyActivity.class);
                                intent.putExtra("activityComeFrom", "WeeklyDailyActivity");
                                intent.putExtra("selection", "weekly");
                                break;
                            case "daily":
                                intent = new Intent(ShowEventActivity.this, WeeklyDailyActivity.class);
                                intent.putExtra("activityComeFrom", "WeeklyDailyActivity");
                                intent.putExtra("selection", "daily");
                                break;
                        }
                    }
                }
                else if (activityComeFrom.equals("WeeklyDailyActivity")) {
                    intent = new Intent(ShowEventActivity.this, WeeklyDailyActivity.class);
                    intent.putExtra("selection", selection);
                }
                else if (activityComeFrom.equals("CalendarActivity"))
                    intent = new Intent(ShowEventActivity.this, CalendarActivity.class);

                intent.putExtra("deletedEventDay", event.getDateFrom());
                onBackPressed();
                startActivity(intent);
                finish();
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


    // On result method for check Storage Permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1001) { // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                shareEvent();
            }
        }
    }

}

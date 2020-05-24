package ce.yildiz.edu.tr.mycalendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


// An activity which user can set default ringtone, default repeat and reminder times, and user can switch between Dark Mode and Light Mode
public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    private LinearLayout linearLayout_defSound, linearLayout_defRepeat, linearLayout_defReminder;
    private TextView textView_defRepeat, textView_defReminder;
    private Switch switch_vibration;
    private Switch switch_mode;
    private Spinner spinner_defSound;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String[] repeatItems;
    private String[] reminderItems;
    private String defaultSoundName;
    private int defaultRepeat;
    private int defaultReminder;

    Map<String, String> ringtonesMap;
    ArrayList<String> ringtoneNames;
    ArrayList<String> ringtoneUris;

    MediaPlayer mp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ringtonesMap = getRingtones();
        ringtoneNames = new ArrayList<>(ringtonesMap.keySet());
        ringtoneUris = new ArrayList<>(ringtonesMap.values());
        getDefSettings();

        spinner_defSound = (Spinner) findViewById(R.id.spinner_defSound);
        ArrayAdapter<String> ringtoneAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, ringtoneNames);
        ringtoneAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner_defSound.setAdapter(ringtoneAdapter);
        int index = ringtoneNames.indexOf(defaultSoundName);
        spinner_defSound.setSelection(index);

        textView_defRepeat = (TextView) findViewById(R.id.textView_defRepeat);
        textView_defRepeat.setText(repeatItems[defaultRepeat]);
        textView_defReminder = (TextView) findViewById(R.id.textView_defReminder);
        textView_defReminder.setText(reminderItems[defaultReminder]);

        switch_vibration = (Switch) findViewById(R.id.switch_vibration);
        controlSwitchVibration();

        switch_mode = (Switch) findViewById(R.id.switch_mode);
        controlSwitchMode();

        linearLayout_defSound = (LinearLayout) findViewById(R.id.linearLayout_defSound);
        linearLayout_defSound.setOnClickListener(this);
        linearLayout_defRepeat = (LinearLayout) findViewById(R.id.linearLayout_defRepeat);
        linearLayout_defRepeat.setOnClickListener(this);
        linearLayout_defReminder = (LinearLayout) findViewById(R.id.linearLayout_defReminder);
        linearLayout_defReminder.setOnClickListener(this);
    }


    // Get default settings with Shared Preferences
    public void getDefSettings() {
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        defaultSoundName = sharedPreferences.getString("defaultSoundName", ringtoneNames.get(0));
        defaultRepeat = sharedPreferences.getInt("defaultRepeat", 0);
        defaultReminder = sharedPreferences.getInt("defaultReminder", 0);
        repeatItems = getResources().getStringArray(R.array.repeat_items);
        reminderItems = getResources().getStringArray(R.array.reminder_items);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.linearLayout_defSound:
                if (mp != null)
                    mp.stop();
                spinner_defSound.performClick();
                spinner_defSound.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String name = ringtoneNames.get(position);
                        String uri = ringtonesMap.get(name);
                        Log.i("RINGTONE", "Sound Uri: " + uri);
                        editor.putString("defaultSoundName", name).apply();
                        editor.putString("defaultSoundUri", uri).apply();
                        mp = MediaPlayer.create(getApplicationContext(), Uri.parse(uri));
                        mp.start();
                        Toast.makeText(SettingsActivity.this, R.string.default_sound_updated, Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                break;

            case R.id.linearLayout_vibration:
                break;

            case R.id.linearLayout_defRepeat:
                if (mp != null)
                    mp.stop();
                createAlertDialogRepeat();
                break;

            case R.id.linearLayout_defReminder:
                if (mp != null)
                    mp.stop();
                createAlertDialogReminder();
                break;
        }
    }


    // Get ringtones which are on device
    public Map<String, String> getRingtones() {
        RingtoneManager manager = new RingtoneManager(this);
        manager.setType(RingtoneManager.TYPE_ALL);
        Cursor cursor = manager.getCursor();

        Map<String, String> list = new HashMap<>();
        while (cursor.moveToNext()) {
            String notificationTitle = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            String notificationUri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX) + "/" + cursor.getString(RingtoneManager.ID_COLUMN_INDEX);

            Log.i("RINGTONES", "Name: " + notificationTitle + " | " +
                    "Uri: " + notificationUri);

            list.put(notificationTitle, notificationUri);
        }

        return list;
    }


    // A function which create a alert dialog to pick the default repeat for events
    public void createAlertDialogRepeat() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this, R.style.AlertDialog);
        mBuilder.setSingleChoiceItems(repeatItems, defaultRepeat, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int selected) {
                defaultRepeat = selected;
            }
        });

        mBuilder.setCancelable(false);
        mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                editor.putInt("defaultRepeat", defaultRepeat).apply();
                textView_defRepeat.setText(repeatItems[defaultRepeat]);
                Toast.makeText(SettingsActivity.this, R.string.default_repeat_selected_successfully, Toast.LENGTH_SHORT).show();
            }
        });

        mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
                defaultRepeat = sharedPreferences.getInt("defaultRepeat", 0);
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


    // A function which create a alert dialog to pick the default repeat for events
    public void createAlertDialogReminder() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this, R.style.AlertDialog);
        mBuilder.setSingleChoiceItems(reminderItems, defaultReminder, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int selected) {
                defaultReminder = selected;
            }
        });

        mBuilder.setCancelable(false);
        mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                editor.putInt("defaultReminder", defaultReminder).apply();
                textView_defReminder.setText(reminderItems[defaultReminder]);
                Toast.makeText(SettingsActivity.this, R.string.default_reminder_selected_successfully, Toast.LENGTH_SHORT).show();
            }
        });

        mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
                defaultReminder = sharedPreferences.getInt("defaultReminder", 0);
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
    }


    // A function which control the switch state to change vibration state
    public void controlSwitchVibration() {
        boolean mode = sharedPreferences.getBoolean("vibration", false);
        switch_vibration.setChecked(mode);
        switch_vibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (mp != null)
                    mp.stop();
                if (b) {
                    editor.putBoolean("vibration", true).apply();
                    Toast.makeText(SettingsActivity.this, R.string.vibration_on, Toast.LENGTH_SHORT).show();
                } else {
                    editor.putBoolean("vibration", false).apply();
                    Toast.makeText(SettingsActivity.this, R.string.vibration_off, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    // A function which control the switch state to change mode
    public void controlSwitchMode() {
        boolean mode = sharedPreferences.getBoolean("mode", false);
        switch_mode.setChecked(mode);
        switch_mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (mp != null)
                    mp.stop();
                if (b) {
                    setMode(true);
                    editor.putBoolean("mode", true).apply();
                    Toast.makeText(SettingsActivity.this, R.string.dark_mode_on, Toast.LENGTH_SHORT).show();
                } else {
                    setMode(false);
                    editor.putBoolean("mode", false).apply();
                    Toast.makeText(SettingsActivity.this, R.string.dark_mode_off, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    // Set Mode (dark or night)
    public static void setMode(Boolean mode) {
        if (mode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mp != null)
            mp.stop();
    }
}

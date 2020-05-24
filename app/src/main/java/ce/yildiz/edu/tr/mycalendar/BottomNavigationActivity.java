package ce.yildiz.edu.tr.mycalendar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


// An activity which is root of TaskFragment, BirthdayFragment...
public class BottomNavigationActivity extends AppCompatActivity {
    FloatingActionButton floatingActionButton_addEvent;
    BottomNavigationView bottomNavigation;

    String time;
    Calendar selectedCalendarDay;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);

        selectedCalendarDay = (Calendar) getIntent().getSerializableExtra("selectedCalendarDay");

        final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        time = dateFormat.format(Calendar.getInstance().getTime());

        bottomNavigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new TaskFragment(selectedCalendarDay), "TaskFragment").commit();


        floatingActionButton_addEvent = (FloatingActionButton) findViewById(R.id.floatingActionButton_addEvent);
        floatingActionButton_addEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewEvent();
            }
        });


    }


    // Go to fragments with BottomNavigation
    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_task:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new TaskFragment(selectedCalendarDay), "TaskFragment").commit();
                    break;

                case R.id.nav_meeting:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MeetingFragment(selectedCalendarDay), "MeetingFragment").commit();
                    break;

                case R.id.nav_birthday:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new BirthdayFragment(selectedCalendarDay), "BirthdayFragment").commit();
                    break;
            }

            return true;
        }
    };


    // Add a new event (event, birthday etc.) if user confirm
    public void addNewEvent() {
        Calendar selectedDay = Calendar.getInstance();
        switch (bottomNavigation.getSelectedItemId()) {
            case R.id.nav_task:
                TaskFragment taskFragment = (TaskFragment) getSupportFragmentManager().findFragmentByTag("TaskFragment");
                if (taskFragment != null) {
                    selectedDay = taskFragment.addNewTask();
                    Toast.makeText(this, R.string.task_added_successfully, Toast.LENGTH_SHORT).show();
                }

            case R.id.nav_meeting:
                MeetingFragment meetingFragment = (MeetingFragment) getSupportFragmentManager().findFragmentByTag("MeetingFragment");
                if (meetingFragment != null) {
                    selectedDay = meetingFragment.addNewMeeting();
                    Toast.makeText(this, R.string.meeting_added_successfully, Toast.LENGTH_SHORT).show();
                }

            case R.id.nav_birthday:
                BirthdayFragment birthdayFragment = (BirthdayFragment) getSupportFragmentManager().findFragmentByTag("BirthdayFragment");
                if (birthdayFragment != null) {
                    selectedDay = birthdayFragment.addNewBirthdayEvent();
                    Toast.makeText(this, R.string.birthday_added_successfully, Toast.LENGTH_SHORT).show();
                }
        }

        Intent intent = getIntent();
        intent.putExtra("selectedDay", selectedDay);
        setResult(RESULT_OK, intent);
        onBackPressed();
        finish();
    }

}

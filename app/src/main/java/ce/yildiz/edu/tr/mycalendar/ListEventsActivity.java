package ce.yildiz.edu.tr.mycalendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


// An activity which we can see all events ve wave
public class ListEventsActivity extends AppCompatActivity {
    Toolbar myToolbar;
    RecyclerView recyclerView_listEvents;

    EventListAdapter eventListAdapter;  // An adapter which show the events in an activity
    CalendarHelper calendarHelper;
    Calendar today;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_events);

        myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.hideOverflowMenu();

        calendarHelper = new CalendarHelper(this);
        today = Calendar.getInstance();

        String activityComeFrom = getIntent().getStringExtra("activityComeFrom");
        String selection = getIntent().getStringExtra("selection");

        ArrayList<Event> allEvents = calendarHelper.getAllEvents(today, "ListEventsActivity");

        recyclerView_listEvents = (RecyclerView) findViewById(R.id.recyclerView_listEvents);
        eventListAdapter = new EventListAdapter(ListEventsActivity.this, allEvents, activityComeFrom, selection);
        recyclerView_listEvents.setAdapter(eventListAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView_listEvents.setLayoutManager(linearLayoutManager);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy MMMM", Locale.getDefault());
        String date = dateFormat.format(allEvents.get(0).getDateFrom().getTime());
        myToolbar.setTitle(date);

        recyclerView_listEvents.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstItemPosition = linearLayoutManager.findFirstVisibleItemPosition();

                String dateScroll = dateFormat.format(allEvents.get(firstItemPosition).getDateFrom().getTime());
                myToolbar.setTitle(dateScroll);
            }
        });

    }
}

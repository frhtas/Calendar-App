package ce.yildiz.edu.tr.mycalendar;

import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


// An adapter which we can see event or birthday details on ListEventsActivity
public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.EventHolder> {
    private ArrayList<Event> allEvents;
    private LayoutInflater inflater;

    private ListEventsActivity listEventsActivity;
    String activityComeFrom;
    String selection;

    public EventListAdapter(ListEventsActivity listEventsActivity, ArrayList<Event> events, String activityComeFrom, String selection) {
        this.inflater = LayoutInflater.from(listEventsActivity);
        this.listEventsActivity = listEventsActivity;
        this.allEvents = events;
        if (events.size() == 0) {
            allEvents.add(new Event("no_events", Calendar.getInstance(), Calendar.getInstance(),
                    listEventsActivity.getString(R.string.no_events), "", "", 0, 0,null));
        }

        this.activityComeFrom = activityComeFrom;
        this.selection = selection;
    }

    @NonNull
    @Override
    public EventHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_eventlist_card, parent, false);
        EventHolder holder = new EventHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull EventHolder holder, int position) {
        final Event selectedEvent = allEvents.get(position);
        holder.setData(selectedEvent);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!selectedEvent.getEventType().equals("no_events")) {
                    Intent intent = new Intent(listEventsActivity, ShowEventActivity.class);
                    intent.putExtra("selectedEvent", selectedEvent);
                    intent.putExtra("activityComeFrom", activityComeFrom);
                    if (activityComeFrom.equals("WeeklyDailyActivity"))
                        intent.putExtra("selection", selection);
                    listEventsActivity.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return allEvents.size();
    }



    class EventHolder extends RecyclerView.ViewHolder {
        ImageView imageView_event;
        TextView textView_eventName, textView_eventTime, textView_eventDay;
        LinearLayout linearLayout_eventDay;

        public EventHolder(View itemView) {
            super(itemView);
            textView_eventDay = (TextView) itemView.findViewById(R.id.textView_eventDay);
            imageView_event = (ImageView) itemView.findViewById(R.id.imageView_event);
            textView_eventName = (TextView) itemView.findViewById(R.id.textView_eventName);
            textView_eventTime = (TextView) itemView.findViewById(R.id.textView_eventTime);
            linearLayout_eventDay = (LinearLayout) itemView.findViewById(R.id.linearLayout_eventDay);
    }

        public void setData(final Event selectedEvent) {
            linearLayout_eventDay.setVisibility(View.VISIBLE);
            switch (selectedEvent.getEventType()) {
                case "task":
                    imageView_event.setImageResource(R.drawable.ic_menu_event_black);
                    linearLayout_eventDay.setBackgroundColor(Color.parseColor("#AD1457"));
                    textView_eventTime.setText(getTimeAsString(selectedEvent.getDateFrom(), selectedEvent.getDateTo()));
                    break;

                case "meeting":
                    imageView_event.setImageResource(R.drawable.ic_meeting_black_24dp);
                    linearLayout_eventDay.setBackgroundColor(Color.parseColor("#6A1B9A"));
                    textView_eventTime.setText(getTimeAsString(selectedEvent.getDateFrom(), selectedEvent.getDateTo()));
                    break;

                case "birthday":
                    imageView_event.setImageResource(R.drawable.ic_menu_birthday_black);
                    linearLayout_eventDay.setBackgroundColor(Color.parseColor("#0277BD"));
                    textView_eventTime.setText(R.string.all_day);
                    break;

                case "Holidays in Turkey":
                    //imageView_event.setImageResource(R.drawable.ic_event_icon);
                    imageView_event.setVisibility(View.GONE);
                    linearLayout_eventDay.setBackgroundColor(Color.parseColor("#FF8F00"));
                    textView_eventTime.setText(R.string.all_day);
                    break;

                case "no_events":
                    textView_eventName.setGravity(Gravity.CENTER);
                    textView_eventName.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    lp.setMargins(0, 0, 0, 0);
                    textView_eventName.setLayoutParams(lp);

                    linearLayout_eventDay.setVisibility(View.GONE);
                    textView_eventTime.setVisibility(View.GONE);
                    imageView_event.setVisibility(View.GONE);
            }
            textView_eventDay.setText(getTimeAsString(selectedEvent.getDateFrom()));
            textView_eventName.setText(selectedEvent.getEventName());
        }

    }


    // Get time as string by calendar
    public static String getTimeAsString(Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd\nMMMM\nyyyy", Locale.getDefault());
        String selectedTime = dateFormat.format(calendar.getTime());
        return selectedTime;
    }


    // Get time as string by two calendar
    private static String getTimeAsString(Calendar dateFrom, Calendar dateTo) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String timeDistance = dateFormat.format(dateFrom.getTime()) + " - " + dateFormat.format(dateTo.getTime());
        return timeDistance;
    }
}

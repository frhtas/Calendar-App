package ce.yildiz.edu.tr.mycalendar;

import android.content.Context;
import android.content.Intent;
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
import java.util.Collections;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


// An adapter which we can see event or birthday details on CalendarActivity
public class CalendarEventAdapter extends RecyclerView.Adapter<CalendarEventAdapter.EventHolder> {
    private ArrayList<Event> allEvents;
    private LayoutInflater inflater;

    private CalendarActivity calendarActivity;

    public CalendarEventAdapter(CalendarActivity calendarActivity, ArrayList<Event> events) {
        this.inflater = LayoutInflater.from(calendarActivity);
        this.calendarActivity = calendarActivity;
        Collections.reverse(events);
        this.allEvents = events;
        if (events.size() == 0) {
            allEvents.add(new Event("no_events", Calendar.getInstance(), Calendar.getInstance(),
                    calendarActivity.getString(R.string.no_events), "", "", 0, 0,null));
        }
    }

    @NonNull
    @Override
    public EventHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_event_card, parent, false);
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
                    Intent intent = new Intent(calendarActivity, ShowEventActivity.class);
                    intent.putExtra("selectedEvent", selectedEvent);
                    intent.putExtra("activityComeFrom", "CalendarActivity");
                    calendarActivity.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return allEvents.size();
    }


    // Update the recycler view after selected another day
    public void updateEvents(ArrayList<Event> filteredList) {
        Collections.reverse(filteredList);
        allEvents = filteredList;
        if (filteredList.size() == 0) {
            allEvents.add(new Event("no_events", Calendar.getInstance(), Calendar.getInstance(),
                    calendarActivity.getString(R.string.no_events), "", "", 0, 0,null));
        }
        notifyDataSetChanged();
    }



    class EventHolder extends RecyclerView.ViewHolder {
        ImageView imageView_event;
        TextView textView_eventName, textView_time;
        LinearLayout linearLayout_icon_time;

        public EventHolder(View itemView) {
            super(itemView);
            imageView_event = (ImageView) itemView.findViewById(R.id.imageView_event);
            textView_eventName = (TextView) itemView.findViewById(R.id.textView_eventName);
            textView_time = (TextView) itemView.findViewById(R.id.textView_timeFrom);
            linearLayout_icon_time = (LinearLayout) itemView.findViewById(R.id.linearLayout_icon_time);
        }

        public void setData(final Event selectedEvent) {
            linearLayout_icon_time.setVisibility(View.VISIBLE);
            textView_eventName.setGravity(Gravity.BOTTOM);
            switch (selectedEvent.getEventType()) {
                case "task":
                    imageView_event.setImageResource(R.drawable.ic_menu_event_black);
                    textView_time.setText(getTimeAsString(itemView.getContext(), selectedEvent));
                    break;

                case "meeting":
                    imageView_event.setImageResource(R.drawable.ic_meeting_black_24dp);
                    textView_time.setText(getTimeAsString(itemView.getContext(), selectedEvent));
                    break;

                case "birthday":
                    imageView_event.setImageResource(R.drawable.ic_menu_birthday_black);
                    textView_time.setText(getTimeAsStringBirthday(itemView.getContext(), selectedEvent));
                    break;

                case "Holidays in Turkey":
                    imageView_event.setImageResource(R.drawable.ic_event_icon);
                    textView_time.setText(getTimeAsStringBirthday(itemView.getContext(), selectedEvent));
                    break;

                case "no_events":
                    linearLayout_icon_time.setVisibility(View.GONE);
                    textView_eventName.setGravity(Gravity.CENTER);
                    textView_eventName.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                    break;
            }
            textView_eventName.setText(selectedEvent.getEventName());

        }

    }


    // Get time as string by event
    public static String getTimeAsString(Context context, Event selectedEvent) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Calendar today = Calendar.getInstance();
        Calendar eventDay = Calendar.getInstance();
        eventDay.setTimeInMillis(selectedEvent.getDateFrom().getTimeInMillis());

        String beginTime = dateFormat.format(selectedEvent.getDateFrom().getTime());
        String endTime = dateFormat.format(selectedEvent.getDateTo().getTime());

        if (eventDay.getTimeInMillis() - today.getTimeInMillis() > 2880*60000) {
            if (selectedEvent.getLocation().trim().equals(""))
                return getTimeAsString(selectedEvent.getDateFrom()) + " - " + getTimeAsString(selectedEvent.getDateTo());
            return getTimeAsString(selectedEvent.getDateFrom()) + " - " + getTimeAsString(selectedEvent.getDateTo()) + " | " + selectedEvent.getLocation();
        }

        else if ((eventDay.get(Calendar.DAY_OF_MONTH) - today.get(Calendar.DAY_OF_MONTH) == 1)
                && (eventDay.get(Calendar.MONTH) == today.get(Calendar.MONTH))
                && (eventDay.get(Calendar.YEAR) == today.get(Calendar.YEAR))) {
            if (selectedEvent.getLocation().trim().equals(""))
                return context.getString(R.string.tomorrow) + " " + beginTime + " - " + endTime;
            return context.getString(R.string.tomorrow) + " " + beginTime + " - " + endTime + " | " + selectedEvent.getLocation();
        }

        else if ((eventDay.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH))
                && (eventDay.get(Calendar.MONTH) == today.get(Calendar.MONTH))
                && (eventDay.get(Calendar.YEAR) == today.get(Calendar.YEAR))) {
            if (selectedEvent.getLocation().trim().equals(""))
                return context.getString(R.string.today) + " " + beginTime + " - " + endTime;
            return context.getString(R.string.today) + " " + beginTime + " - " + endTime + " | " + selectedEvent.getLocation();
        }

        if (selectedEvent.getLocation().trim().equals(""))
            return getTimeAsString(selectedEvent.getDateFrom()) + " - " + getTimeAsString(selectedEvent.getDateTo());
        return getTimeAsString(selectedEvent.getDateFrom()) + " - " + getTimeAsString(selectedEvent.getDateTo()) + " | " + selectedEvent.getLocation();
    }


    // Get time as string by event
    public static String getTimeAsStringBirthday(Context context, Event selectedEvent) {
        Calendar today = Calendar.getInstance();
        Calendar eventDay = Calendar.getInstance();
        eventDay.setTimeInMillis(selectedEvent.getDateFrom().getTimeInMillis());

        if (eventDay.getTimeInMillis() - today.getTimeInMillis() > 2880*60000) {
            return BirthdayFragment.getTimeAsString(selectedEvent.getDateFrom());
        }

        else if ((eventDay.get(Calendar.DAY_OF_MONTH) - today.get(Calendar.DAY_OF_MONTH) == 1)
                  && (eventDay.get(Calendar.MONTH) == today.get(Calendar.MONTH))
                  && (eventDay.get(Calendar.YEAR) == today.get(Calendar.YEAR))) {
            return context.getString(R.string.tomorrow);
        }

        else if ((eventDay.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH))
                && (eventDay.get(Calendar.MONTH) == today.get(Calendar.MONTH))
                && (eventDay.get(Calendar.YEAR) == today.get(Calendar.YEAR))) {
            return context.getString(R.string.today);
        }

        return BirthdayFragment.getTimeAsString(selectedEvent.getDateFrom());
    }


    // Get time as string by calendar
    public static String getTimeAsString(Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.getDefault());
        String selectedTime = dateFormat.format(calendar.getTime());

        return selectedTime;
    }
}

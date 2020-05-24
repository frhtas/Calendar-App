package ce.yildiz.edu.tr.mycalendar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

// Application icon made by;
// Icons made by <a href="https://www.flaticon.com/authors/pixel-perfect" title="Pixel perfect">Pixel perfect</a> from <a href="https://www.flaticon.com/" title="Flaticon"> www.flaticon.com</a>

// An activity which is welcome screen of application
public class SplashScreen extends AppCompatActivity {
    private final static int TIME = 800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        SharedPreferences sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        Boolean mode = sharedPreferences.getBoolean("mode", false);
        String view = sharedPreferences.getString("view", "monthly");
        SettingsActivity.setMode(mode);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd", Locale.getDefault());
        String today = dateFormat.format(Calendar.getInstance().getTime());
        TextView textView_todayDate = (TextView)findViewById(R.id.textView_todayDate);
        textView_todayDate.setText(today);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                if (view != null) {
                    switch (view) {
                        case "monthly":
                            intent = new Intent(SplashScreen.this, CalendarActivity.class);
                            break;
                        case "weekly":
                            intent = new Intent(SplashScreen.this, WeeklyDailyActivity.class);
                            intent.putExtra("selection", "weekly");
                            break;
                        case "daily":
                            intent = new Intent(SplashScreen.this, WeeklyDailyActivity.class);
                            intent.putExtra("selection", "daily");
                            break;

                    }
                }
                startActivity(intent);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                finish();
            }
        }, TIME);

    }
}

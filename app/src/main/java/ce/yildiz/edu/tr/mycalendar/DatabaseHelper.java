package ce.yildiz.edu.tr.mycalendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;


// Database for reminders
public class DatabaseHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Calendar.db";
    private static final String TABLE_REMINDER= "reminder";


    private String CREATE_REMINDER_TABLE = "CREATE TABLE " + TABLE_REMINDER + "("
            + "_id" + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "event_id" + " TEXT,"
            + "reminders" + " BLOB" + ")";

    private String DROP_REMINDER_TABLE = "DROP TABLE IF EXISTS " + TABLE_REMINDER;

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_REMINDER_TABLE);
    }

    @Override
    public  void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL(DROP_REMINDER_TABLE);
        onCreate(db);
    }


    // A function which add reminders to the database
    public void addReminders(long eventID, ArrayList<Integer> selectedReminders) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        Gson gson = new Gson();
        values.put("event_id", eventID);
        values.put("reminders", gson.toJson(selectedReminders).getBytes());

        db.insert(TABLE_REMINDER, null, values);
    }

    // A function which delete reminders to the database
    public void deleteReminders(long eventID) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM reminder WHERE event_id = '" + eventID + "' ";

        db.execSQL(query);
    }

    // A function which get reminders from database
    public ArrayList<Integer> getRemindersByID(long eventID) {
        ArrayList<Integer> reminders = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT reminders FROM reminder WHERE event_id = '" + eventID + "' ";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                byte[] blob = cursor.getBlob(0);
                String json = new String(blob);
                Gson gson = new Gson();
                reminders = gson.fromJson(json, new TypeToken<ArrayList<Integer>>() {}.getType());
            }
        }
        cursor.close();
        return reminders;
    }
}

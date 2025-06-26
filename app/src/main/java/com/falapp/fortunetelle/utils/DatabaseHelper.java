package com.falapp.fortunetelle.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.falapp.fortunetelle.models.FortuneReading;
import com.falapp.fortunetelle.models.FortuneTeller;
import com.falapp.fortunetelle.models.User;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "FalApp.db";

    // Table Names
    private static final String TABLE_USER = "user";
    private static final String TABLE_FORTUNE_TELLERS = "fortune_tellers";
    private static final String TABLE_READINGS = "readings";

    // Common column names
    private static final String KEY_ID = "id";

    // USER Table - column names
    private static final String KEY_USER_NAME = "name";
    private static final String KEY_USER_COINS = "coins";
    private static final String KEY_USER_IS_PREMIUM = "is_premium";

    // FORTUNE_TELLERS Table - column names
    private static final String KEY_FT_NAME = "name";
    private static final String KEY_FT_ONLINE = "online";
    private static final String KEY_FT_RATING = "rating";
    private static final String KEY_FT_SPECIALTIES = "specialties";
    private static final String KEY_FT_PRICE = "price";
    private static final String KEY_FT_ICON = "icon";

    // READINGS Table - column names
    private static final String KEY_READING_TYPE = "type";
    private static final String KEY_READING_FORTUNE_TELLER_ID = "fortune_teller_id";
    private static final String KEY_READING_USER_ID = "user_id";
    private static final String KEY_READING_TOPICS = "topics";
    private static final String KEY_READING_USER_NAME = "user_name";
    private static final String KEY_READING_BIRTH_DATE = "birth_date";
    private static final String KEY_READING_RELATIONSHIP_STATUS = "relationship_status";
    private static final String KEY_READING_JOB_STATUS = "job_status";
    private static final String KEY_READING_PHOTO_PATH = "photo_path";
    private static final String KEY_READING_RESULT = "result";
    private static final String KEY_READING_TIMESTAMP = "timestamp";
    private static final String KEY_READING_AVAILABLE_TIME = "available_time";
    private static final String KEY_READING_IS_AVAILABLE = "is_available";

    // Create Table Statements
    // User table create statement
    private static final String CREATE_TABLE_USER = "CREATE TABLE " + TABLE_USER + "("
            + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_USER_NAME + " TEXT,"
            + KEY_USER_COINS + " INTEGER,"
            + KEY_USER_IS_PREMIUM + " INTEGER DEFAULT 0" + ")";


    private static final String CREATE_TABLE_FORTUNE_TELLERS = "CREATE TABLE " + TABLE_FORTUNE_TELLERS + "("
            + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_FT_NAME + " TEXT,"
            + KEY_FT_ONLINE + " INTEGER,"
            + KEY_FT_RATING + " REAL,"
            + KEY_FT_SPECIALTIES + " TEXT,"
            + KEY_FT_PRICE + " INTEGER,"
            + KEY_FT_ICON + " TEXT" + ")";


    private static final String CREATE_TABLE_READINGS = "CREATE TABLE " + TABLE_READINGS + "("
            + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_READING_TYPE + " TEXT,"
            + KEY_READING_FORTUNE_TELLER_ID + " INTEGER,"
            + KEY_READING_USER_ID + " INTEGER,"
            + KEY_READING_TOPICS + " TEXT,"
            + KEY_READING_USER_NAME + " TEXT,"
            + KEY_READING_BIRTH_DATE + " TEXT,"
            + KEY_READING_RELATIONSHIP_STATUS + " TEXT,"
            + KEY_READING_JOB_STATUS + " TEXT,"
            + KEY_READING_PHOTO_PATH + " TEXT,"
            + KEY_READING_RESULT + " TEXT,"
            + KEY_READING_TIMESTAMP + " INTEGER,"
            + KEY_READING_AVAILABLE_TIME + " INTEGER,"
            + KEY_READING_IS_AVAILABLE + " INTEGER DEFAULT 0" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_TABLE_USER);
        db.execSQL(CREATE_TABLE_FORTUNE_TELLERS);
        db.execSQL(CREATE_TABLE_READINGS);


        initializeFortuneTellers(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FORTUNE_TELLERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_READINGS);


        onCreate(db);
    }


    private void initializeFortuneTellers(SQLiteDatabase db) {
        String[] names = {"Sinem", "Gamze", "Ayşe", "Merve", "Fatma", "Gizem", "Zeynep", "Dilara"};
        String[] specialties = {
                "aşk,para,sağlık",
                "kariyer,aile,arkadaş",
                "aşk,sağlık,aile",
                "para,kariyer,arkadaş",
                "aşk,para,kariyer",
                "sağlık,aile,para",
                "aşk,arkadaş,kariyer",
                "aile,sağlık,para"
        };
        String[] icons = {
                "falci", "fortune_teller_2", "fortune_teller_3", "fortune_teller_4",
                "fortune_teller_5", "fortune_teller_6", "fortune_teller_7", "fortune_teller_8"
        };

        for (int i = 0; i < names.length; i++) {
            ContentValues values = new ContentValues();
            values.put(KEY_FT_NAME, names[i]);
            values.put(KEY_FT_ONLINE, 1);
            values.put(KEY_FT_RATING, 5.0);
            values.put(KEY_FT_SPECIALTIES, specialties[i]);
            values.put(KEY_FT_PRICE, 10);
            values.put(KEY_FT_ICON, icons[i]);
            db.insert(TABLE_FORTUNE_TELLERS, null, values);
        }
    }


    public long addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USER_NAME, user.getName());
        values.put(KEY_USER_COINS, user.getCoins());
        values.put(KEY_USER_IS_PREMIUM, user.isPremium() ? 1 : 0);


        long id = db.insert(TABLE_USER, null, values);

        db.close();
        return id;
    }

    public User getUser() {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_USER + " LIMIT 1";
        Cursor cursor = db.rawQuery(selectQuery, null);

        User user = null;
        if (cursor.moveToFirst()) {
            user = new User(
                    cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                    cursor.getString(cursor.getColumnIndex(KEY_USER_NAME)),
                    cursor.getInt(cursor.getColumnIndex(KEY_USER_COINS)),
                    cursor.getInt(cursor.getColumnIndex(KEY_USER_IS_PREMIUM)) == 1
            );
        }

        cursor.close();
        db.close();
        return user;
    }

    public int updateUserCoins(int userId, int newCoins) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USER_COINS, newCoins);

        // Updating row
        int result = db.update(TABLE_USER, values, KEY_ID + " = ?",
                new String[]{String.valueOf(userId)});

        db.close();
        return result;
    }

    public int updateUserPremium(int userId, boolean isPremium) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USER_IS_PREMIUM, isPremium ? 1 : 0);

        // Updating row
        int result = db.update(TABLE_USER, values, KEY_ID + " = ?",
                new String[]{String.valueOf(userId)});

        db.close();
        return result;
    }

    // FORTUNE_TELLERS table methods
    public List<FortuneTeller> getAllFortuneTellers() {
        List<FortuneTeller> fortuneTellers = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_FORTUNE_TELLERS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                FortuneTeller fortuneTeller = new FortuneTeller(
                        cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                        cursor.getString(cursor.getColumnIndex(KEY_FT_NAME)),
                        cursor.getInt(cursor.getColumnIndex(KEY_FT_ONLINE)) == 1,
                        cursor.getFloat(cursor.getColumnIndex(KEY_FT_RATING)),
                        cursor.getString(cursor.getColumnIndex(KEY_FT_SPECIALTIES)),
                        cursor.getInt(cursor.getColumnIndex(KEY_FT_PRICE)),
                        cursor.getString(cursor.getColumnIndex(KEY_FT_ICON))
                );

                fortuneTellers.add(fortuneTeller);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return fortuneTellers;
    }

    public FortuneTeller getFortuneTeller(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_FORTUNE_TELLERS, null, KEY_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        FortuneTeller fortuneTeller = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                fortuneTeller = new FortuneTeller(
                        cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                        cursor.getString(cursor.getColumnIndex(KEY_FT_NAME)),
                        cursor.getInt(cursor.getColumnIndex(KEY_FT_ONLINE)) == 1,
                        cursor.getFloat(cursor.getColumnIndex(KEY_FT_RATING)),
                        cursor.getString(cursor.getColumnIndex(KEY_FT_SPECIALTIES)),
                        cursor.getInt(cursor.getColumnIndex(KEY_FT_PRICE)),
                        cursor.getString(cursor.getColumnIndex(KEY_FT_ICON))
                );
            }
            cursor.close();
        }

        db.close();
        return fortuneTeller;
    }

    // READINGS table methods
    public long addReading(FortuneReading reading) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_READING_TYPE, reading.getType());
        values.put(KEY_READING_FORTUNE_TELLER_ID, reading.getFortuneTellerId());
        values.put(KEY_READING_USER_ID, reading.getUserId());
        values.put(KEY_READING_TOPICS, reading.getTopics());
        values.put(KEY_READING_USER_NAME, reading.getUserName());
        values.put(KEY_READING_BIRTH_DATE, reading.getBirthDate());
        values.put(KEY_READING_RELATIONSHIP_STATUS, reading.getRelationshipStatus());
        values.put(KEY_READING_JOB_STATUS, reading.getJobStatus());
        values.put(KEY_READING_PHOTO_PATH, reading.getPhotoPath());
        values.put(KEY_READING_RESULT, reading.getResult());
        values.put(KEY_READING_TIMESTAMP, reading.getTimestamp());
        values.put(KEY_READING_AVAILABLE_TIME, reading.getAvailableTime());
        values.put(KEY_READING_IS_AVAILABLE, reading.isAvailable() ? 1 : 0);

        // Insert row
        long id = db.insert(TABLE_READINGS, null, values);

        db.close();
        return id;
    }

    public List<FortuneReading> getAllReadingsForUser(int userId) {
        List<FortuneReading> readings = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_READINGS +
                " WHERE " + KEY_READING_USER_ID + " = ? ORDER BY " +
                KEY_READING_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                FortuneReading reading = new FortuneReading(
                        cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                        cursor.getString(cursor.getColumnIndex(KEY_READING_TYPE)),
                        cursor.getInt(cursor.getColumnIndex(KEY_READING_FORTUNE_TELLER_ID)),
                        cursor.getInt(cursor.getColumnIndex(KEY_READING_USER_ID)),
                        cursor.getString(cursor.getColumnIndex(KEY_READING_TOPICS)),
                        cursor.getString(cursor.getColumnIndex(KEY_READING_USER_NAME)),
                        cursor.getString(cursor.getColumnIndex(KEY_READING_BIRTH_DATE)),
                        cursor.getString(cursor.getColumnIndex(KEY_READING_RELATIONSHIP_STATUS)),
                        cursor.getString(cursor.getColumnIndex(KEY_READING_JOB_STATUS)),
                        cursor.getString(cursor.getColumnIndex(KEY_READING_PHOTO_PATH)),
                        cursor.getString(cursor.getColumnIndex(KEY_READING_RESULT)),
                        cursor.getLong(cursor.getColumnIndex(KEY_READING_TIMESTAMP)),
                        cursor.getLong(cursor.getColumnIndex(KEY_READING_AVAILABLE_TIME)),
                        cursor.getInt(cursor.getColumnIndex(KEY_READING_IS_AVAILABLE)) == 1
                );

                readings.add(reading);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return readings;
    }

    public int updateReadingResult(int readingId, String result, boolean isAvailable) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_READING_RESULT, result);
        values.put(KEY_READING_IS_AVAILABLE, isAvailable ? 1 : 0);


        int updateResult = db.update(TABLE_READINGS, values, KEY_ID + " = ?",
                new String[]{String.valueOf(readingId)});

        db.close();
        return updateResult;
    }

    public FortuneReading getReading(int readingId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_READINGS, null, KEY_ID + " = ?",
                new String[]{String.valueOf(readingId)}, null, null, null, null);

        FortuneReading reading = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                reading = new FortuneReading(
                        cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                        cursor.getString(cursor.getColumnIndex(KEY_READING_TYPE)),
                        cursor.getInt(cursor.getColumnIndex(KEY_READING_FORTUNE_TELLER_ID)),
                        cursor.getInt(cursor.getColumnIndex(KEY_READING_USER_ID)),
                        cursor.getString(cursor.getColumnIndex(KEY_READING_TOPICS)),
                        cursor.getString(cursor.getColumnIndex(KEY_READING_USER_NAME)),
                        cursor.getString(cursor.getColumnIndex(KEY_READING_BIRTH_DATE)),
                        cursor.getString(cursor.getColumnIndex(KEY_READING_RELATIONSHIP_STATUS)),
                        cursor.getString(cursor.getColumnIndex(KEY_READING_JOB_STATUS)),
                        cursor.getString(cursor.getColumnIndex(KEY_READING_PHOTO_PATH)),
                        cursor.getString(cursor.getColumnIndex(KEY_READING_RESULT)),
                        cursor.getLong(cursor.getColumnIndex(KEY_READING_TIMESTAMP)),
                        cursor.getLong(cursor.getColumnIndex(KEY_READING_AVAILABLE_TIME)),
                        cursor.getInt(cursor.getColumnIndex(KEY_READING_IS_AVAILABLE)) == 1
                );
            }
            cursor.close();
        }

        db.close();
        return reading;
    }
}
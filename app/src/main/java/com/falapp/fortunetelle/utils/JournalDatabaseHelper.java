package com.falapp.fortunetelle.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.falapp.fortunetelle.models.JournalEntry;

import java.util.ArrayList;
import java.util.List;

public class JournalDatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "JournalDB.db";

    // Table Name
    private static final String TABLE_JOURNAL = "journal";

    // Column names
    private static final String KEY_ID = "id";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_DATE = "date";
    private static final String KEY_CONTENT = "content";
    private static final String KEY_LAST_MODIFIED = "last_modified";

    // Create Table Statement
    private static final String CREATE_TABLE_JOURNAL = "CREATE TABLE " + TABLE_JOURNAL + "("
            + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_USER_ID + " INTEGER,"
            + KEY_DATE + " INTEGER,"
            + KEY_CONTENT + " TEXT,"
            + KEY_LAST_MODIFIED + " INTEGER" + ")";

    public JournalDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creating required table
        db.execSQL(CREATE_TABLE_JOURNAL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_JOURNAL);

        // Create table again
        onCreate(db);
    }

    // Add new journal entry
    public long addJournalEntry(JournalEntry entry) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USER_ID, entry.getUserId());
        values.put(KEY_DATE, entry.getDate());
        values.put(KEY_CONTENT, entry.getContent());
        values.put(KEY_LAST_MODIFIED, entry.getLastModified());

        // Insert row
        long id = db.insert(TABLE_JOURNAL, null, values);

        db.close();
        return id;
    }

    // Get journal entry for a specific user and date
    public JournalEntry getJournalEntry(int userId, long date) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Get the beginning and end of the day
        long startOfDay = getStartOfDay(date);
        long endOfDay = getEndOfDay(date);

        Cursor cursor = db.query(TABLE_JOURNAL, null,
                KEY_USER_ID + " = ? AND " + KEY_DATE + " BETWEEN ? AND ?",
                new String[]{String.valueOf(userId), String.valueOf(startOfDay), String.valueOf(endOfDay)},
                null, null, KEY_LAST_MODIFIED + " DESC", "1");

        JournalEntry entry = null;
        if (cursor != null && cursor.moveToFirst()) {
            entry = new JournalEntry(
                    cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                    cursor.getInt(cursor.getColumnIndex(KEY_USER_ID)),
                    cursor.getLong(cursor.getColumnIndex(KEY_DATE)),
                    cursor.getString(cursor.getColumnIndex(KEY_CONTENT)),
                    cursor.getLong(cursor.getColumnIndex(KEY_LAST_MODIFIED))
            );
            cursor.close();
        }

        db.close();
        return entry;
    }

    // Update journal entry
    public int updateJournalEntry(JournalEntry entry) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CONTENT, entry.getContent());
        values.put(KEY_LAST_MODIFIED, entry.getLastModified());

        // Updating row
        int result = db.update(TABLE_JOURNAL, values, KEY_ID + " = ?",
                new String[]{String.valueOf(entry.getId())});

        db.close();
        return result;
    }

    // Delete journal entry
    public void deleteJournalEntry(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_JOURNAL, KEY_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Get all journal entries for a user
    public List<JournalEntry> getAllJournalEntries(int userId) {
        List<JournalEntry> entries = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_JOURNAL +
                " WHERE " + KEY_USER_ID + " = ? ORDER BY " + KEY_DATE + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                JournalEntry entry = new JournalEntry(
                        cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                        cursor.getInt(cursor.getColumnIndex(KEY_USER_ID)),
                        cursor.getLong(cursor.getColumnIndex(KEY_DATE)),
                        cursor.getString(cursor.getColumnIndex(KEY_CONTENT)),
                        cursor.getLong(cursor.getColumnIndex(KEY_LAST_MODIFIED))
                );

                entries.add(entry);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return entries;
    }

    // Helper method to get start of day (midnight)
    private long getStartOfDay(long timestamp) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    // Helper method to get end of day (23:59:59.999)
    private long getEndOfDay(long timestamp) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 23);
        calendar.set(java.util.Calendar.MINUTE, 59);
        calendar.set(java.util.Calendar.SECOND, 59);
        calendar.set(java.util.Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }
}
package com.tasks;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TaskDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "task_manager.db";
    private static final int DATABASE_VERSION = 1;

    // Istruzione SQL per creare la tabella delle attività
    private static final String CREATE_TABLE_TASKS =
            "CREATE TABLE " + TaskContract.TaskEntry.TABLE_NAME + "(" +
                    TaskContract.TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    TaskContract.TaskEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                    TaskContract.TaskEntry.COLUMN_DESCRIPTION + " TEXT, " +
                    TaskContract.TaskEntry.COLUMN_COMPLETED + " INTEGER DEFAULT 0)";

    public TaskDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crea la tabella delle attività
        db.execSQL(CREATE_TABLE_TASKS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Gestisce gli aggiornamenti del database
    }
}

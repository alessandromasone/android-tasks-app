package com.tasks;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class Task {
    private long id;
    private final String name;
    private final String description;
    private boolean completed;

    public Task(String name, String description, boolean completed) {
        this.name = name;
        this.description = description;
        this.completed = completed;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    // Salva o aggiorna una task nel database
    public void saveToDatabase(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_NAME, name);
        values.put(TaskContract.TaskEntry.COLUMN_DESCRIPTION, description);
        values.put(TaskContract.TaskEntry.COLUMN_COMPLETED, completed ? 1 : 0);

        if (id == 0) {
            // Se l'ID è 0, significa che la task non esiste nel database, quindi viene inserita
            id = db.insert(TaskContract.TaskEntry.TABLE_NAME, null, values);
        } else {
            // Altrimenti, la task esiste già nel database e viene aggiornata
            String selection = TaskContract.TaskEntry._ID + " = ?";
            String[] selectionArgs = { String.valueOf(id) };
            db.update(TaskContract.TaskEntry.TABLE_NAME, values, selection, selectionArgs);
        }
    }

    // Ottiene tutte le tasks dal database
    public static ArrayList<Task> getAllTasksFromDatabase(SQLiteDatabase db) {
        ArrayList<Task> taskList = new ArrayList<>();
        String sortOrder = TaskContract.TaskEntry._ID + " ASC";
        Cursor cursor = db.query(
                TaskContract.TaskEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                sortOrder
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Estrae le informazioni della task dal cursore del database
                @SuppressLint("Range") long taskId = cursor.getLong(cursor.getColumnIndex(TaskContract.TaskEntry._ID));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_NAME));
                @SuppressLint("Range") String description = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_DESCRIPTION));
                @SuppressLint("Range") int completedValue = cursor.getInt(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_COMPLETED));
                boolean completed = completedValue == 1;

                // Crea un oggetto Task con le informazioni estratte e lo aggiunge alla lista
                Task task = new Task(name, description, completed);
                task.setId(taskId);
                taskList.add(task);
            } while (cursor.moveToNext());

            cursor.close();
        }

        return taskList;
    }

    // Aggiorna una task nel database
    public static void updateTaskInDatabase(SQLiteDatabase db, Task task) {
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_NAME, task.getName());
        values.put(TaskContract.TaskEntry.COLUMN_DESCRIPTION, task.getDescription());
        values.put(TaskContract.TaskEntry.COLUMN_COMPLETED, task.isCompleted() ? 1 : 0);

        String selection = TaskContract.TaskEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(task.getId()) };

        db.update(
                TaskContract.TaskEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );
    }

    // Elimina una task dal database
    public static void deleteTaskFromDatabase(SQLiteDatabase db, long taskId) {
        String selection = TaskContract.TaskEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(taskId) };

        db.delete(
                TaskContract.TaskEntry.TABLE_NAME,
                selection,
                selectionArgs
        );
    }
}

package com.tasks;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // Costanti per preferenze condivise e chiavi di preferenza
    private static final String PREFS_NAME = "TaskPrefs";
    private static final String PREF_SHOW_COMPLETED_TASKS = "ShowCompletedTasks";
    private static final String PREF_THEME_MODE = "ThemeMode";

    // Elementi e variabili dell'interfaccia utente
    private ArrayList<Task> taskList;
    private TaskListAdapter taskListAdapter;
    private ListView taskListView;
    private CheckBox completedCheckBox;
    private TaskDBHelper dbHelper;
    private SQLiteDatabase db;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inizializza le preferenze condivise, l'helper del database e il database
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        dbHelper = new TaskDBHelper(this);
        db = dbHelper.getWritableDatabase();

        // Carica le attività dal database
        taskList = Task.getAllTasksFromDatabase(db);

        // Inizializza l'adattatore dell'elenco attività e impostalo sulla visualizzazione elenco
        taskListAdapter = new TaskListAdapter(this, taskList, db);
        taskListView = findViewById(R.id.taskListView);
        taskListView.setAdapter(taskListAdapter);

        // Imposta il listener di clic del pulsante "Aggiungi attività".
        Button addTaskButton = findViewById(R.id.addTaskButton);
        addTaskButton.setOnClickListener(v -> showAddTaskDialog());

        // Imposta la casella di controllo "Mostra attività completate" e il relativo click listener
        completedCheckBox = findViewById(R.id.completedCheckBox);
        completedCheckBox.setChecked(prefs.getBoolean(PREF_SHOW_COMPLETED_TASKS, true));
        completedCheckBox.setOnClickListener(v -> {
            boolean showCompletedTasks = completedCheckBox.isChecked();
            taskListAdapter.setShowCompletedTasks(showCompletedTasks);
            taskListAdapter.notifyDataSetChanged();
            taskListView.setSelection(0);
            saveShowCompletedTasksPref(showCompletedTasks);
        });

        // Imposta il listener di clic del pulsante "Impostazioni".
        Button settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> showSettingsDialog());

        // Carica la modalità del tema corrente dalle preferenze e applicala
        int themeMode = prefs.getInt(PREF_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        setThemeMode(themeMode);

        // Carica la lista in base alle preferenze
        boolean showCompletedTasks = completedCheckBox.isChecked();
        taskListAdapter.setShowCompletedTasks(showCompletedTasks);
        taskListAdapter.notifyDataSetChanged();
    }

    // Salva la preferenza "Mostra attività completate".
    private void saveShowCompletedTasksPref(boolean showCompletedTasks) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREF_SHOW_COMPLETED_TASKS, showCompletedTasks).apply();
    }

    // Salva la preferenza della modalità del tema
    private void saveThemeModePref(int themeMode) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PREF_THEME_MODE, themeMode).apply();
    }

    // Mostra la finestra di dialogo delle impostazioni
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.settings_title)
                .setItems(R.array.settings_options, (dialog, which) -> handleSettingsOption(which))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // Gestisci l'opzione delle impostazioni selezionate
    private void handleSettingsOption(int option) {
        switch (option) {
            case 0:
                showThemeDialog();
                break;
            case 1:
                showDeleteAllTasksDialog();
                break;
            default:
                break;
        }
    }

    // Mostra la finestra di dialogo della modalità tema
    private void showThemeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.theme_mode)
                .setItems(R.array.theme_options, (dialog, which) -> handleThemeOption(which))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // Gestisce l'opzione del tema selezionato
    private void handleThemeOption(int option) {
        switch (option) {
            case 0:
                setThemeMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 1:
                setThemeMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                break;
        }
    }

    // Imposta la modalità del tema e ricrea l'attività
    private void setThemeMode(int themeMode) {
        int currentThemeMode = AppCompatDelegate.getDefaultNightMode();
        if (currentThemeMode != themeMode) {
            AppCompatDelegate.setDefaultNightMode(themeMode);
            saveThemeModePref(themeMode);
            recreate();
        }
    }

    // Mostra la finestra di dialogo per confermare l'eliminazione di tutte le attività
    private void showDeleteAllTasksDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_all_tasks)
                .setMessage(R.string.confirm_delete_all_tasks)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    deleteAllTasks();
                    taskListAdapter.setShowCompletedTasks(completedCheckBox.isChecked());
                    taskListAdapter.notifyDataSetChanged();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // Elimina tutte le attività dal database
    private void deleteAllTasks() {
        taskList.clear();
        taskListAdapter.notifyDataSetChanged();
        db.delete(TaskContract.TaskEntry.TABLE_NAME, null, null);
    }

    // Mostra la finestra di dialogo per aggiungere una nuova attività
    private void showAddTaskDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_task, null);

        final EditText taskNameEditText = dialogView.findViewById(R.id.taskNameEditText);
        final EditText taskDescriptionEditText = dialogView.findViewById(R.id.taskDescriptionEditText);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setTitle(R.string.add_task)
                .setPositiveButton(R.string.add, (dialog, which) -> {
                    String taskName = taskNameEditText.getText().toString().trim();
                    String taskDescription = taskDescriptionEditText.getText().toString().trim();
                    addTask(taskName, taskDescription);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel())
                .create()
                .show();
    }

    // Aggiunge una nuova attività all'elenco e al database
    private void addTask(String taskName, String taskDescription) {
        if (!taskName.isEmpty()) {
            Task task = new Task(taskName, taskDescription, false);
            task.saveToDatabase(db);
            taskList.add(task);
            taskListAdapter.setShowCompletedTasks(completedCheckBox.isChecked());
            taskListAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(MainActivity.this, R.string.empty_task_name, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
        dbHelper.close();
    }
}

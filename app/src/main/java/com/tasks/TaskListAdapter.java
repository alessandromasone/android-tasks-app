package com.tasks;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class TaskListAdapter extends ArrayAdapter<Task> {

    private final Context context;
    private final ArrayList<Task> taskList;
    private final ArrayList<Task> filteredTaskList;
    private boolean showCompletedTasks;
    private final SQLiteDatabase db;

    public TaskListAdapter(Context context, ArrayList<Task> taskList, SQLiteDatabase db) {
        super(context, 0, taskList);
        this.context = context;
        this.taskList = taskList;
        this.filteredTaskList = new ArrayList<>(taskList);
        this.showCompletedTasks = true; // Mostra tutte le task all'inizio
        this.db = db;
        filterTasks();
    }

    public void setShowCompletedTasks(boolean showCompletedTasks) {
        this.showCompletedTasks = showCompletedTasks;
        filterTasks();
    }

    private void filterTasks() {
        filteredTaskList.clear();
        // Itera attraverso la lista delle task in ordine inverso
        for (int i = taskList.size() - 1; i >= 0; i--) {
            Task task = taskList.get(i);
            // Aggiungi la task alla lista filtrata solo se showCompletedTasks è true o se la task non è completata
            if (showCompletedTasks || !task.isCompleted()) {
                filteredTaskList.add(task);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            // Se convertView è null, crea una nuova view tramite l'inflater
            listItemView = LayoutInflater.from(context).inflate(R.layout.list_item_task, parent, false);
        }

        // Ottieni la task corrente dalla lista filtrata in base alla posizione
        Task currentTask = filteredTaskList.get(position);

        // Ottieni i riferimenti alle view all'interno del layout del singolo elemento della lista
        TextView taskNameTextView = listItemView.findViewById(R.id.taskNameTextView);
        TextView taskDescriptionTextView = listItemView.findViewById(R.id.taskDescriptionTextView);
        CheckBox completedCheckBoxItem = listItemView.findViewById(R.id.completedCheckBoxItem);
        ImageView deleteTaskButton = listItemView.findViewById(R.id.deleteTaskButton);

        // Imposta i valori corrispondenti per la task corrente
        taskNameTextView.setText(currentTask.getName());
        taskDescriptionTextView.setText(currentTask.getDescription());
        completedCheckBoxItem.setChecked(currentTask.isCompleted());

        // Aggiungi un listener di click per il checkbox di completamento
        completedCheckBoxItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inverti lo stato di completamento della task corrente
                currentTask.setCompleted(!currentTask.isCompleted());
                if (!showCompletedTasks && currentTask.isCompleted()) {
                    deleteCompletedTasks();
                }

                // Aggiorna lo stato di completamento nel database
                Task.updateTaskInDatabase(db, currentTask);
            }
        });

        // Aggiungi un listener di click per il pulsante di eliminazione della task
        deleteTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mostra un dialogo di conferma per l'eliminazione della task
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Eliminazione Task");
                builder.setMessage("Sei sicuro di voler eliminare questa task?");
                builder.setPositiveButton("Elimina", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Elimina la task
                        deleteTask(currentTask);
                    }
                });
                builder.setNegativeButton("Annulla", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        return listItemView;
    }

    @Override
    public int getCount() {
        return filteredTaskList.size();
    }

    public void deleteCompletedTasks() {
        ArrayList<Task> tasksToRemove = new ArrayList<>();
        // Itera attraverso la lista delle task filtrate
        for (Task task : filteredTaskList) {
            if (task.isCompleted()) {
                tasksToRemove.add(task);
            }
        }
        // Rimuovi le task completate dalla lista filtrata
        filteredTaskList.removeAll(tasksToRemove);
        notifyDataSetChanged();

        // Elimina le task completate dal database
        for (Task task : tasksToRemove) {
            Task.deleteTaskFromDatabase(db, task.getId());
        }
    }

    private void deleteTask(Task task) {
        // Rimuovi la task dal database
        Task.deleteTaskFromDatabase(db, task.getId());

        // Rimuovi la task dalla lista filtrata
        filteredTaskList.remove(task);
        // Rimuovi la task anche dalla lista originale taskList
        taskList.remove(task);
        notifyDataSetChanged();
    }
}

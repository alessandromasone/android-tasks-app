package com.tasks;

import android.provider.BaseColumns;

public class TaskContract {
    private TaskContract() {
        // Impedisce l'istanza
    }

    public static class TaskEntry implements BaseColumns {
        // Definisce il nome della tabella per le attività
        public static final String TABLE_NAME = "tasks";

        // Definisce i nomi delle colonne per gli attributi delle attività
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_COMPLETED = "completed";
    }
}

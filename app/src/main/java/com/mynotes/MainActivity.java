package com.mynotes;

import android.app.ListActivity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

import com.mynotes.db.NotesDB;

public class MainActivity extends ListActivity {

    private SimpleCursorAdapter cursorAdapter;
    private NotesDB db;
    private SQLiteDatabase dbRead;

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_EDIT_NOTE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cursorAdapter = new SimpleCursorAdapter(this, R.layout.list_item,
                null, new String[]{NotesDB.COLUMN_NAME_NOTE_NAME, NotesDB.COLUMN_NAME_NOTE_DATE},
                new int[]{R.id.tvName, R.id.tvDate});

        setListAdapter(cursorAdapter);

        refreshNotesListView();


    }

    private void refreshNotesListView() {

        cursorAdapter.changeCursor(dbRead.query(NotesDB.TABLE_NAME_NOTES,
                null, null, null, null, null, null));
    }


}
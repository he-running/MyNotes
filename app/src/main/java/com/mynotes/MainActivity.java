package com.mynotes;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.mynotes.db.NotesDB;

public class MainActivity extends ListActivity implements View.OnClickListener {

    private SimpleCursorAdapter cursorAdapter;
    private NotesDB db;
    private SQLiteDatabase dbRead;

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_EDIT_NOTE = 2;

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_addNote:
                Intent intent1 = new Intent(MainActivity.this, AtyEditNote.class);
                startActivityForResult(intent1, REQUEST_CODE_ADD_NOTE);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new NotesDB(this);
        dbRead = db.getReadableDatabase();

        cursorAdapter = new SimpleCursorAdapter(this, R.layout.list_item,
                null, new String[]{NotesDB.COLUMN_NAME_NOTE_NAME, NotesDB.COLUMN_NAME_NOTE_DATE},
                new int[]{R.id.tvName, R.id.tvDate});

        setListAdapter(cursorAdapter);

        refreshNotesListView();

        findViewById(R.id.btn_addNote).setOnClickListener(this);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        Cursor cursor = cursorAdapter.getCursor();
        cursor.moveToPosition(position);

        Intent intent2 = new Intent(MainActivity.this, AtyEditNote.class);
        intent2.putExtra(AtyEditNote.EXTRA_NOTE_ID, cursor.getInt(cursor.getColumnIndex(NotesDB.COLUMN_NAME_ID)));
        intent2.putExtra(AtyEditNote.EXTRA_NOTE_NAME, cursor.getString(cursor.getColumnIndex(NotesDB.COLUMN_NAME_NOTE_NAME)));
        intent2.putExtra(AtyEditNote.EXTRA_NOTE_CONTENT, cursor.getString(cursor.getColumnIndex(NotesDB.COLUMN_NAME_NOTE_CONTENT)));
        startActivityForResult(intent2, REQUEST_CODE_EDIT_NOTE);

        super.onListItemClick(l, v, position, id);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case REQUEST_CODE_ADD_NOTE:
            case REQUEST_CODE_EDIT_NOTE:

                if (resultCode == Activity.RESULT_OK) {
                    refreshNotesListView();
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void refreshNotesListView() {

        cursorAdapter.changeCursor(dbRead.query(NotesDB.TABLE_NAME_NOTES,
                null, null, null, null, null, null));
    }
}
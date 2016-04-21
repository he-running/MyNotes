package com.mynotes;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.mynotes.db.NotesDB;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AtyEditNote extends ListActivity implements View.OnClickListener {

    private int noteId = -1;
    private MediaAdapter adapter;
    private NotesDB db;
    private SQLiteDatabase dbRead, dbWrite;
    private EditText etName, etContent;
    private  String currentPath;

    public  static final String EXTRA_NOTE_ID = "noteId";
    public static final String EXTRA_NOTE_NAME = "noteName";
    public static final String EXTRA_NOTE_CONTENT = "noteContent";
    public static final int REQUEST_CODE_GET_PHOTO=1;
    public static final int REQUEST_CODE_GET_VIDEO=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_edit_note);

        adapter = new MediaAdapter(this);
        setListAdapter(adapter);

        db = new NotesDB(this);
        dbRead = db.getReadableDatabase();
        dbWrite = db.getWritableDatabase();

        etName = (EditText) findViewById(R.id.et_name);
        etContent = (EditText) findViewById(R.id.et_content);

        findViewById(R.id.btn_save).setOnClickListener(this);
        findViewById(R.id.btn_cancle).setOnClickListener(this);
        findViewById(R.id.btn_video).setOnClickListener(this);
        findViewById(R.id.btn_photo).setOnClickListener(this);

        noteId = getIntent().getIntExtra(EXTRA_NOTE_ID, -1);

        if (noteId > -1) {

            etName.setText(getIntent().getStringExtra(EXTRA_NOTE_NAME));
            etContent.setText(getIntent().getStringExtra(EXTRA_NOTE_CONTENT));

            Cursor cursor = dbRead.query(NotesDB.TABLE_NAME_MEDIA, null,
                    NotesDB.COLUMN_NAME_MEDIA_OWNER_NOTE_ID + "=?", new String[]{noteId + ""},
                    null, null, null);

            while (cursor.moveToNext()) {

                adapter.add(new MediaListCellData(cursor.getString(
                        cursor.getColumnIndex(NotesDB.COLUMN_NAME_MEDIA_PATH)), cursor.getInt(
                        cursor.getColumnIndex(NotesDB.COLUMN_NAME_ID))));
            }
            adapter.notifyDataSetChanged();
        }
    }

    public File getMediaDir(){

        File dir=new File(Environment.getExternalStorageDirectory(),"NotesMedia");
        if (!dir.exists()){
            dir.mkdirs();
        }
        return dir;
    }

    public void saveMedia(int noteId) {

        MediaListCellData data;
        ContentValues cv;

        for (int i = 0; i < adapter.getCount(); i++) {

            data = adapter.getItem(i);

            if (data.id <= -1) {
                cv = new ContentValues();
                cv.put(NotesDB.COLUMN_NAME_MEDIA_PATH, data.path);
                cv.put(NotesDB.COLUMN_NAME_MEDIA_OWNER_NOTE_ID, noteId);
                dbWrite.insert(NotesDB.TABLE_NAME_MEDIA, null, cv);
            }
        }
    }

    private int saveNote() {

        ContentValues values = new ContentValues();
        values.put(NotesDB.COLUMN_NAME_NOTE_NAME, etName.getText().toString());
        values.put(NotesDB.COLUMN_NAME_NOTE_CONTENT, etContent.getText().toString());
        values.put(NotesDB.COLUMN_NAME_NOTE_DATE, new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));

        if (noteId > -1) {

            dbWrite.update(NotesDB.TABLE_NAME_NOTES, values,
                    NotesDB.COLUMN_NAME_ID + "=?", new String[]{noteId + ""});
            return noteId;
        } else {
            return (int) dbWrite.insert(NotesDB.TABLE_NAME_NOTES, null, values);
        }
    }

    @Override
    protected void onDestroy() {
        dbRead.close();
        dbWrite.close();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        Intent i;
        File file;

        switch (v.getId()) {

            case R.id.btn_photo:
                i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                file=new File(getMediaDir(),System.currentTimeMillis()+".jpg");
                if (!file.exists()){
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                currentPath=file.getAbsolutePath();
                i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));

                startActivityForResult(i,REQUEST_CODE_GET_PHOTO);
                break;

            case R.id.btn_video:
                i=new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                file=new File(getMediaDir(),System.currentTimeMillis()+".mp4");

                if (!file.exists()){
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                currentPath=file.getAbsolutePath();
                i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));

                startActivityForResult(i,REQUEST_CODE_GET_VIDEO);
                break;

            case R.id.btn_save:

                saveMedia(saveNote());
                setResult(RESULT_OK);
                finish();
                break;

            case R.id.btn_cancle:

                setResult(RESULT_CANCELED);
                finish();
                break;

            default:
                break;

        }
    }

    static class MediaListCellData {

        int type=0;
        int id = -1;
        String path = "";
        int iconId = R.drawable.notes;

        public MediaListCellData(String path) {
            this.path = path;
            if (path.endsWith(".jpg")) {
                iconId = R.drawable.icon_photo;
                type=MediaType.PHOTO;
            } else if (path.endsWith(".mp4")) {
                iconId = R.drawable.icon_video;
                type=MediaType.VIDEO;
            }
        }

        public MediaListCellData(String path, int id) {
            this(path);
            this.id = id;
        }
    }

   public static class MediaAdapter extends BaseAdapter {

        private Context context;
        private List<MediaListCellData> list=new ArrayList<>();

        public MediaAdapter(Context context) {
            this.context = context;
        }

        public void add(MediaListCellData data) {
            list.add(data);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public MediaListCellData getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.media_list_cell, null);
            }

            MediaListCellData data =  getItem(position);

            ImageView ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
            TextView tvPath = (TextView) convertView.findViewById(R.id.tvPath);

            ivIcon.setImageResource(data.iconId);
            tvPath.setText(data.path);
            return convertView;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch(requestCode){
            case REQUEST_CODE_GET_PHOTO:
            case REQUEST_CODE_GET_VIDEO:

                if (resultCode==RESULT_OK){

                    adapter.add(new MediaListCellData(currentPath));
                    adapter.notifyDataSetChanged();
                }
                break;
            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

   public static class MediaType{
        static final int PHOTO=1;
        static final int VIDEO=2;
    }

}

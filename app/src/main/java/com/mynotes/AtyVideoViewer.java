package com.mynotes;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class AtyVideoViewer extends AppCompatActivity {

    private VideoView vv;
    public static final String EXTRA_PATH="path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vv=new VideoView(this);
        setContentView(vv);

        String path=getIntent().getStringExtra(EXTRA_PATH);
        if (path!=null){
            vv.getHolder();
            vv.setMediaController(new MediaController(this));
            vv.setVideoPath(path);
        }else{
            finish();
        }
    }
}

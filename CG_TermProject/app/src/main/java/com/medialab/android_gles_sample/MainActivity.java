package com.medialab.android_gles_sample;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends ListActivity {

    private ArrayList<ListItem> mSamples;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView iv = (ImageView)findViewById(R.id.imageView);
        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.main);
        iv.setImageBitmap(image);
        iv.setScaleType(ImageView.ScaleType.FIT_XY); // 레이아웃 크기에 이미지를 맞춘다
        mSamples = new ArrayList<>();
        mSamples.add(new ListItem("START", ViewType.VIEW_FRAG_LIGHT));
        mSamples.add(new ListItem("ABOUT", ViewType.VIEW_FRAG_LIGHT));
        mSamples.add(new ListItem("EXIT", ViewType.VIEW_FRAG_LIGHT));

        ArrayAdapter<ListItem> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, mSamples);

        setListAdapter(adapter);

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Toast.makeText(this, mSamples.get(position).getName(), Toast.LENGTH_SHORT).show();
        mSamples.get(position).changeActivity();
    }

    private class ListItem {
        private String mName;
        private ViewType mType;

        public ListItem(String mName, ViewType type) {
            this.mName = mName;
            this.mType = type;
        }

        public String getName() {
            return mName;
        }

        public void changeActivity() {
            Activity activity = SampleLauncher.getInstance().InitSampleView(mType);
            final Intent newActivity = new Intent(getApplication(), activity.getClass());

            newActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(newActivity);
                }
            }, 100);
        }

        @Override
        public String toString() {
            return this.mName;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}

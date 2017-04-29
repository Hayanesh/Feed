package com.hayanesh.feeder;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Cache;
import com.example.fontometrics.Fontometrics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hayanesh.feeder.adapter.FeedListAdapter;
import com.hayanesh.feeder.app.AppController;
import com.hayanesh.feeder.data.FeedItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FeedMain extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ListView listView;
    private FeedListAdapter listAdapter;
    private List<FeedItem> feedItems;
    String user = "user-one";
    DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView feed_title = (TextView) findViewById(R.id.title_feed);
        feed_title.setTypeface(Fontometrics.back_black(this));
        listView = (ListView) findViewById(R.id.list);

        feedItems = new ArrayList<FeedItem>();

        listAdapter = new FeedListAdapter(this, feedItems);
        listView.setAdapter(listAdapter);

        // These two lines not needed,
        // just to get the look of facebook (changing background color & hiding the icon)
//        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3b5998")));
//        getActionBar().setIcon(
                // new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        // We first check for cached request
//        Cache cache = AppController.getInstance().getRequestQueue().getCache();


        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();

// Attach a listener to read the data at our posts reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot userSnap = dataSnapshot.child(user);
                Log.d(TAG,userSnap.getKey().toString());
                Iterable<DataSnapshot> photos = userSnap.getChildren();
                int i =1;
                for(DataSnapshot photo:photos)
                {
                    try {
                        Log.d(TAG, photo.getKey().toString());
                        String description = photo.child("Description").getValue().toString();
                        Log.d("Des", description + " ");
                        String url = photo.child("URL").getValue().toString();
                        Log.d("URL", url + " ");
                        FeedItem feed = new FeedItem();
                        feed.setId(i);
                        feed.setImge(url);
                        feed.setName("Hayanesh");
                        feed.setUrl(url);
                        feed.setTimeStamp("1403375851930");
                        feed.setStatus(description);
                        i++;
                        feedItems.add(feed);
                    }catch (Exception ex)
                    {
                        break;
                    }
                }
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }
}

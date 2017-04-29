package com.hayanesh.feeder;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.input.image.ClarifaiImage;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Color;
import clarifai2.dto.prediction.Concept;
import me.gujun.android.taggroup.TagGroup;

public class Post extends AppCompatActivity {
    String user = "user-one";
    private ImageView photo;
    LinearLayout color_palette;
    FirebaseStorage storage;
    DatabaseReference databaseReference;
    Uri imageUri;
    Uri downloadUrl;
    TagGroup mTagGroup;
    ViewSwitcher color_spectrum,imagetags;
    String[] tags;
    EditText descrip;
    ArrayAdapter<String> arrayAdapter;
    ArrayList<String> arrayList;
    View[] color_drops;
    Switch tag_switch;
    public Snackbar snackbar;
    public Button cancel,save;
    public RelativeLayout tagCard;
    final String FbURL = "gs://feeder-1cc65.appspot.com/";
    final String appID = "dAcPv8PJqMTqTkUxWj0er3MYH_zpVi0H60zN0LIC";
    final String appSecret = "jgswQliGaYMKfQS5tjmDOrmUY0WU1t-kCknkpkhR";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initViews();

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendImage(intent);
            }
        }

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(DeleteFromFirebase())
                {
                    //Launch intent
                    Intent toFeed = new Intent(Post.this,FeedMain.class);
                    startActivity(toFeed);
                }
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.child(user).child(imageUri.getLastPathSegment()).child("URL").setValue(downloadUrl.toString());
                databaseReference.child(user).child(imageUri.getLastPathSegment()).child("Description").setValue(descrip.getText().toString());
                int i=0;
                for(String t:tags)
                {
                    databaseReference.child(user).child(imageUri.getLastPathSegment()).child("Tags").child("Tag"+i).setValue(t);
                    i++;
                }
                i=0;
                for(String c:arrayList)
                {
                    databaseReference.child(user).child(imageUri.getLastPathSegment()).child("Colors").child("Color"+i).setValue(c);
                    i++;
                }

                Intent toFeed = new Intent(Post.this,FeedMain.class);
                startActivity(toFeed);
            }
        });
        tag_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    tagCard.setVisibility(View.VISIBLE);
                    new tagSync().execute();
                }
                else
                {
                    tagCard.setVisibility(View.GONE);
                }
            }
        });
        /*   mTagGroup.setOnTagClickListener(new TagGroup.OnTagClickListener() {
            @Override
            public void onTagClick(String tag) {
                Spannable word = new SpannableString("#"+tag+" ");
                word.setSpan(new ForegroundColorSpan(Color.parseColor("#3B4465")), 0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                word.setSpan(new UnderlineSpan(), 0,word.length()-1, 0);
                descrip.append(word);
            }
        });*/


    }
    void initViews()
    {
        mTagGroup = (TagGroup) findViewById(R.id.tag_group);
        photo = (ImageView) findViewById(R.id.photo_post);
        color_spectrum = (ViewSwitcher) findViewById(R.id.color_switcher);
        imagetags = (ViewSwitcher)findViewById(R.id.tag_switcher);
        descrip = (EditText) findViewById(R.id.description);
        color_palette = (LinearLayout) findViewById(R.id.color_palette);
        color_drops = new View[]{(View)findViewById(R.id.view1),(View)findViewById(R.id.view2),(View)findViewById(R.id.view3),
                (View)findViewById(R.id.view4),(View)findViewById(R.id.view5),(View)findViewById(R.id.view6),(View)findViewById(R.id.view7)};
        tag_switch = (Switch)findViewById(R.id.switch1);
        snackbar = Snackbar.make((RelativeLayout)findViewById(R.id.content_post),"",Snackbar.LENGTH_SHORT);
        cancel = (Button)findViewById(R.id.cancel);
        save = (Button)findViewById(R.id.save);
        tagCard = (RelativeLayout)findViewById(R.id.TagView);
    }


    void handleSendImage(Intent intent) {
        imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);

        //Loading image to imageView
        if (imageUri != null) {
            Glide.with(this).load(imageUri).fitCenter().into(photo);
        }
        if (isNetworkConnected()) {
            try {
                FireBaseStore();
            } catch (Exception e) {
                snackbar.setText("Error while uploading..");
                snackbar.show();
            }
        } else {
            snackbar.setText("Connect to internet");
            snackbar.show();
        }

    }

    private void FireBaseStore() {
        storage = FirebaseStorage.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        final StorageReference storageRef = storage.getReferenceFromUrl(FbURL);
        StorageReference photoRef = storageRef.child("photos").child(imageUri.getLastPathSegment());
        Log.d("TAG", "Upload path" + photoRef.getPath());

        UploadTask uploadTask = photoRef.putFile(imageUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                snackbar.setText("Upload failed");snackbar.show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                downloadUrl = taskSnapshot.getDownloadUrl();
                Log.i("URL", downloadUrl.toString());

                new ColorSync().execute();
                //Toast.makeText(MainActivity.this, downloadUrl.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public Boolean DeleteFromFirebase()
    {
        Boolean flag = false;
        storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(FbURL);
        StorageReference photoRef = storageRef.child("photos").child(imageUri.getLastPathSegment());
        photoRef.delete().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        });
        return true;
    }


    class ColorSync extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... mApi) {
            try {
                final ClarifaiClient client =
                        new ClarifaiBuilder(appID,appSecret).buildSync();
                final List<ClarifaiOutput<Color>> predictionResults =
                        client.getDefaultModels().colorModel() // You can also do client.getModelByID("id") to get custom models
                                .predict()
                                .withInputs(
                                        ClarifaiInput.forImage(ClarifaiImage.of(downloadUrl.toString()))
                                )
                                .executeSync()
                                .get();
                List<clarifai2.dto.prediction.Color> al = predictionResults.get(0).data();

                Log.d("Colors", predictionResults.get(0).data().toString());
                arrayList = new ArrayList<>();


                for (int i = 0; i < al.size(); i++) {
                    Log.d("color " + i, al.get(i).webSafeHex());
                    arrayList.add(al.get(i).hex());
                }

            } catch (Exception ex) {
                Log.e("Message Failed", ex.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            CreatePalette();
        }
    }
    public void CreatePalette()
    {
        for(int i =0;i<arrayList.size()&&i<7;i++)
        {
            color_drops[i].setBackgroundColor(android.graphics.Color.parseColor(arrayList.get(i)));
        }
        color_spectrum.showNext();
    }

    public class tagSync extends AsyncTask<Void,Void,Void>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mTagGroup.setTags(tags);
            imagetags.showNext();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                final ClarifaiClient client =
                        new ClarifaiBuilder(appID,appSecret).buildSync();

                final List<ClarifaiOutput<Concept>> predictionResults =
                        client.getDefaultModels().generalModel() // You can also do client.getModelByID("id") to get custom models
                                .predict()
                                .withInputs(
                                        ClarifaiInput.forImage(ClarifaiImage.of(downloadUrl.toString()))
                                )
                                .executeSync()
                                .get();
                List<Concept> al = predictionResults.get(0).data();
                // mTagGroup.setTags(new String[]{"Tag1", "Tag2", "Tag3", "Tag4"});
                tags = new String[al.size()];
                for(int i=0;i<al.size();i++)
                {
                    tags[i] = al.get(i).name();
                    Log.d("TAGS",tags[i]);
                }


            } catch (Exception ex) {
                Log.e("Message Failed", ex.toString());
            }
            return null;
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}


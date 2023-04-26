package com.example.buildingdetector;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

public class InformationActivity extends AppCompatActivity {
    public static Context context;
    ImageView buildingImage;
    TextView buildingName;
    TextView description;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        getSupportActionBar().hide();
        context = getApplicationContext();
        buildingImage = (ImageView) findViewById(R.id.buildingImage);
        buildingName = (TextView) findViewById(R.id.buildingName);
        description = (TextView) findViewById(R.id.description);
        Resources res = getApplicationContext().getResources();
        Drawable image = ResourcesCompat.getDrawable(res, getIntent().getExtras().getInt("image"), null);
        buildingImage.setImageDrawable(image);
        description.setText(getIntent().getExtras().getString("description"));
        buildingName.setText(getIntent().getExtras().getString("name"));
        Toast.makeText(InformationActivity.context, getIntent().getExtras().getString("confidence")+"%", Toast.LENGTH_SHORT).show();




    }
}

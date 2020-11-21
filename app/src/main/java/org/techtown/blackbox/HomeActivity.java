package org.techtown.blackbox;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {

    String userId, parentNum;
    TextView UserIdView;
    Button btn_gallery, btn_record, btn_end;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //home에 데이터값 저장
        if(userId == null || parentNum == null) {
            userId = getIntent().getStringExtra("UserId");
            parentNum = getIntent().getStringExtra("UserPPhone");
        }

        UserIdView = findViewById(R.id.user_id);

        if(userId != null) {
            UserIdView.setText(userId);
        }

        // 갤러리 버튼
        btn_gallery = findViewById(R.id.btn_gallery);
        btn_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), VideoGallery.class);
                intent.putExtra("UserPPhone", userId);
                intent.putExtra("UserId", parentNum);
                startActivity(intent);
            }
        });

        // 녹화버
        btn_record = findViewById(R.id.btn_record);
        btn_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                intent.putExtra("UserPPhone", userId);
                intent.putExtra("UserId", parentNum);
                startActivity(intent);
            }
        });

        btn_end = findViewById(R.id.btn_end);
        btn_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent exit_intent = new Intent(getApplicationContext(), LoginActivity.class);
                clearPref();
                startActivity(exit_intent);

            }
        });
    }

    protected void clearPref(){
        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        userId = null;
        parentNum = null;
        editor.commit();
    }

}

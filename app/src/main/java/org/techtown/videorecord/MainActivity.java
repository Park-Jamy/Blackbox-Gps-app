package org.techtown.videorecord;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {


//    private StorageReference mStorgeRef;
    private Camera camera;
    private MediaRecorder mediaRecorder;
    private Button btn_record, btn_upload;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private boolean recording = false;
    private String filename = null;
    private String dirPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        //startService(new Intent(this, UnCatchTask.class));
        setContentView(R.layout.activity_main);

        TedPermission.with(this)
                .setPermissionListener(permission)
                .setRationaleMessage("녹화를 위하여 권한을 허용해주세요.")
                .setDeniedMessage("권한이 거부되었습니다.")
                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO )
                .check();

// 디렉토리 생성
        String str = Environment.getExternalStorageState();
        if ( str.equals(Environment.MEDIA_MOUNTED)) {

            dirPath = "/sdcard/black_box";
            File file = new File(dirPath);
            if( !file.exists() )  // 원하는 경로에 폴더가 있는지 확인
            {
                Log.e("TAG : ", "디렉토리 생성");
               file.mkdirs();
        }
            else Log.e("TAG : ", "디렉토리 이미존재");

        }
        else
            Toast.makeText(this, "SD Card 인식 실패", Toast.LENGTH_SHORT).show();




        //recording button
        btn_record = findViewById(R.id.btn_record);
        btn_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recording){
                    mediaRecorder.stop();
                    mediaRecorder.release();
                    camera.lock();
                    recording  = false;

                    Toast.makeText(MainActivity.this, "파이어베이스에 자동업로드", Toast.LENGTH_SHORT).show();

                    btn_upload.callOnClick();
                }
                else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //과부화도 덜되고 동영상 처리는 여기서 하는게 좋다
                            Toast.makeText(MainActivity.this, "녹화가 시작되었습니다.", Toast.LENGTH_SHORT).show();
                            try {


                                mediaRecorder = new MediaRecorder();
                                camera.unlock();
                                mediaRecorder.setCamera(camera);
                                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                                mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                                mediaRecorder. setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));
                                mediaRecorder.setOrientationHint(90);

                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_HH_mmss");
                                Date now = new Date();
                                filename = formatter.format(now) + ".mp4";



                                mediaRecorder.setOutputFile(dirPath +"/"+ filename);
                                mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
                                mediaRecorder.prepare();
                                mediaRecorder.start();
                                recording = true;


                            }catch (Exception e){
                                e.printStackTrace();
                                mediaRecorder.release();
                            }
                        }
                    });
                }

            }
        });




        // uploadbutton
        btn_upload = findViewById(R.id.btn_upload);

        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(filename != null) {
                    Log.e("파일명 확인: ", filename);

                    FirebaseStorage storage = FirebaseStorage.getInstance();

                    Uri file = Uri.fromFile(new File(dirPath +"/"+ filename));
                    StorageReference storageRef = storage.getReferenceFromUrl("gs://videorecording-e653f.appspot.com/").child(dirPath +"/"+ filename);
                    //storage url 적는란


                    Log.e("URi 확인: ", String.valueOf(file));
                    storageRef.putFile(file)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Toast.makeText(MainActivity.this, "업로드 성공", Toast.LENGTH_SHORT).show();
                                    filename = null;
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, "업로드 실패", Toast.LENGTH_SHORT).show();
                                }
                            });


                }
                else{
                    Toast.makeText(MainActivity.this, "파일 없음", Toast.LENGTH_SHORT).show();
                }
            }
        });





    }

    @Override
    protected void onStop() {  //백그라운드로 이동할때
        super.onStop();
        if(recording){
            mediaRecorder.stop();
            mediaRecorder.release();
            camera.lock();
            recording  = false;

            Toast.makeText(MainActivity.this, "파이어베이스에 자동업로드", Toast.LENGTH_SHORT).show();

            btn_upload.callOnClick();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("TAG", "onDestroy 호출");
//        if(recording){
//            mediaRecorder.stop();
//            mediaRecorder.release();
//            camera.lock();
//            recording  = false;
//
//            Toast.makeText(MainActivity.this, "파이어베이스에 자동업로드", Toast.LENGTH_SHORT).show();
//
//            btn_upload.callOnClick();
//        }
    }



    // permission
    PermissionListener permission = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            Toast.makeText(MainActivity.this, "권한 허가", Toast.LENGTH_SHORT).show();

            camera = Camera.open();
            camera.setDisplayOrientation(90);
            surfaceView = findViewById(R.id.surfaceView);
            surfaceHolder = surfaceView.getHolder();
            surfaceHolder.addCallback(MainActivity.this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(MainActivity.this, "권한 거부", Toast.LENGTH_SHORT).show();
        }
    };
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        refreshCamera(camera);
    }

    private void refreshCamera(Camera camera) {
    if(surfaceHolder.getSurface() == null){
        return ;
    }
    try {
        camera.stopPreview();
    }
    catch (Exception e){
        e.printStackTrace();
    }

    setCamera(camera);


    }

    private void setCamera(Camera cam) {
        camera = cam;

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }


//    public class  UnCatchTaskService extends Service {
//        @Nullable
//        @Override
//        public IBinder onBind(Intent intent) {
//            return null;
//        }
//
//        @Override
//        public void onTaskRemoved(Intent rootIntent) {
//
//
//            Log.e("Error","onTaskRemoved - " + rootIntent);
//            if(recording){
//                mediaRecorder.stop();
//                mediaRecorder.release();
//                camera.lock();
//                recording  = false;
//
//                Toast.makeText(MainActivity.this, "파이어베이스에 자동업로드", Toast.LENGTH_SHORT).show();
//
//                btn_upload.callOnClick();
//            }
//
//
//            stopSelf(); //서비스도 같이 종료
//
//        }
    }



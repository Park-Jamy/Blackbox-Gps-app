package org.techtown.videorecord;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;

import android.os.Bundle;
import android.os.Environment;

import android.util.Log;

import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.amazonaws.auth.AWSCredentialsProvider;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener {


    private AmazonS3 s3;
    private TransferUtility transferUtility;
    private File f;
    private Camera camera;
    private MediaRecorder mediaRecorder;
    private Button btn_record, btn_upload;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private boolean recording = false;
    private String filename = null;
    private String dirPath;
    private String TAG = "TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        //startService(new Intent(this, UnCatchTask.class));
        setContentView(R.layout.activity_main);
        allowPermission();  //Ted permission으로 권한 얻어오기
        makeDir();

        // Amazon Cognito 인증 공급자를 초기화합니다
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "ap-northeast-2:ec6efe79-ddb8-4400-b58b-28386eb57c3e", // 자격 증명 풀 ID
                Regions.AP_NORTHEAST_2 // 리전
        );
        s3 = new AmazonS3Client(credentialsProvider);
        // S3 버킷의 Region 이 서울일 경우 아래와 같습니다.
        s3.setRegion(Region.getRegion(Regions.AP_NORTHEAST_2));
        s3.setEndpoint("s3.ap-northeast-2.amazonaws.com");
        transferUtility = new TransferUtility(s3, getApplicationContext());


        btn_record = findViewById(R.id.btn_record);
        btn_upload = findViewById(R.id.btn_upload);
        btn_record.setOnClickListener(this);
        btn_upload.setOnClickListener(this);




    }

    private void makeDir() {
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
    }

    private void allowPermission() {

        TedPermission.with(this)
                .setPermissionListener(permission)
                .setRationaleMessage("녹화를 위하여 권한을 허용해주세요.")
                .setDeniedMessage("권한이 거부되었습니다.")
                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO )
                .check();
    }

    @Override
    protected void onStop() {  //백그라운드로 이동할때
        super.onStop();
        if(recording){
            mediaRecorder.stop();
            mediaRecorder.release();
            camera.lock();
            recording  = false;

            Toast.makeText(MainActivity.this, "aws에 업로드중 기다려주세요", Toast.LENGTH_SHORT).show();

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
            //Toast.makeText(MainActivity.this, "권한 허가", Toast.LENGTH_SHORT).show();
            Log.e("TAG", "권한 허가");
            camera = Camera.open();
            camera.setDisplayOrientation(90);
            surfaceView = findViewById(R.id.surfaceView);
            surfaceHolder = surfaceView.getHolder();
            surfaceHolder.addCallback(MainActivity.this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(MainActivity.this, "권한 거부이 거부되었습니다. 설정 -> 권한 허용", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onClick(View view) {
        switch(view.getId()){

            case R.id.btn_record:
                if(recording){
                    mediaRecorder.stop();
                    mediaRecorder.release();
                    camera.lock();
                    recording  = false;

                    Toast.makeText(MainActivity.this, "aws에 업로드중 기다려주세요", Toast.LENGTH_SHORT).show();

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

                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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

                break;
            case R.id.btn_upload:
                if(filename != null) {
                    Log.e("파일명 확인: ", filename);

                    f = new File(dirPath +"/"+ filename);
                    TransferObserver observer = transferUtility.upload("blackbox-aws", f.getName(), f);

                    Toast.makeText(MainActivity.this, "aws로 전송", Toast.LENGTH_SHORT).show();

                    observer.setTransferListener(new TransferListener() {
                        @Override
                        public void onStateChanged(int id, TransferState state) {
                            Log.e(TAG, "onStateChanged: " + id + ", " + state.toString());
                        }

                        @Override
                        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                            float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                            int percentDone = (int)percentDonef;
                            Log.e(TAG, "ID:" + id + " bytesCurrent: " + bytesCurrent + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
                        }
                        @Override
                        public void onError(int id, Exception ex) {
                            Log.e(TAG, ex.getMessage());
                        }
                    });
                }
                else{
                    Toast.makeText(MainActivity.this, "파일 없음", Toast.LENGTH_SHORT).show();
                }
                break;
        }

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



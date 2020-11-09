package org.techtown.videorecord;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.amazonaws.services.s3.AmazonS3;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener {


    private AmazonS3 s3;
    private Camera camera;
    private MediaRecorder mediaRecorder;
    private Button btn_record, btn_upload;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private boolean recording = false;
    private String filename = null;
    private String dirPath;
    private String TAG = "TAG";
    private Button btn_send;
    private EditText textPhoneNo;
    private String phoneNum = null;

    private GpsTracker gpsTracker;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        //startService(new Intent(this, UnCatchTask.class));
        setContentView(R.layout.activity_main);
        allowPermission();  //Ted permission으로 권한 얻어오기
        makeDir();



        btn_record = findViewById(R.id.btn_record);
        btn_upload = findViewById(R.id.btn_upload);
        btn_send = findViewById(R.id.btn_send);
        btn_record.setOnClickListener(this);
        btn_upload.setOnClickListener(this);
        btn_send.setOnClickListener(this);
        textPhoneNo = findViewById(R.id.edit_text_phone);



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
                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION )
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

            Toast.makeText(MainActivity.this, "firebase에 업로드중 기다려주세요", Toast.LENGTH_SHORT).show();

            btn_upload.callOnClick();
            btn_send.callOnClick();
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

                    Toast.makeText(MainActivity.this, "firebase에 업로드중 기다려주세요", Toast.LENGTH_SHORT).show();

                    btn_upload.callOnClick();
                    btn_send.callOnClick();



                }
                else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if(textPhoneNo.length() == 11){
                                phoneNum = textPhoneNo.getText().toString();
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
                            }}
                            else{
                                Toast.makeText(MainActivity.this, "전화번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                break;
            case R.id.btn_upload:
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
                break;


            case R.id.btn_send:
                gpsTracker = new GpsTracker(this);
                double latitude = gpsTracker.getLatitude();
                double longtitude = gpsTracker.getLongitude();
                String address = getCurrentAddress(latitude, longtitude);
                String location = "\n위도: " + latitude + "\n경도: " + longtitude;
                    sendSms(phoneNum, address+location);
                    phoneNum = null;
                    Toast.makeText(this, "문자 메시지 전송", Toast.LENGTH_SHORT).show();

                break;
        }

  }

    private void sendSms(String phoneNum, String msg) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNum, null, msg, null, null);


    }


    public String getCurrentAddress( double latitude, double longitude) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }



        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
//                        checkRunTimePermission();
                        return;
                    }
                }

                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
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









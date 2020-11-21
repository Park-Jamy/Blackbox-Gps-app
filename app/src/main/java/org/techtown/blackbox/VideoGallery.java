package org.techtown.blackbox;
import androidx.appcompat.app.AppCompatActivity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.MediaController;
import android.widget.VideoView;
import java.io.File;


public class VideoGallery extends AppCompatActivity {
    private VideoView videoView;
    private String videoList[];
    private String videoDirPath;
    private String videoFilePath;
    @Override
    protected void onPause() {
        if(videoView!=null && videoView.isPlaying()) videoView.pause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(videoView!=null)
            videoView.stopPlayback();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_gallery);
        videoDirPath = this.getFilesDir().getAbsolutePath() + "/blackbox";

        File file = new File(videoDirPath);
        videoList = file.list();
        String videoListReverse[] = new String[videoList.length];
        for(int i = 0; i< videoList.length; i++)
            videoListReverse[i] = videoList[videoList.length - i - 1];


        for(int i = 0; i< videoList.length;i ++)
            Log.e("file: ", videoList[i]);

        MyGalleryAdapter adapter = new MyGalleryAdapter(
                getApplicationContext(), // 현재 화면의 제어권자
                R.layout.activity_row,
                videoListReverse);

        // adapterView
        Gallery gallery = (Gallery)findViewById(R.id.gallery1);
        gallery.setAdapter(adapter);


        final MediaController controller = new MediaController(this);
        gallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) { // 선택되었을 때 콜백메서드
               videoFilePath = videoDirPath + "/" + videoListReverse[position];
               Log.e("VideoFilePath: ", videoFilePath);
                videoView =findViewById(R.id.videoVideo);
                videoView.setVideoPath(videoFilePath);
                videoView.requestFocus();
                videoView.setMediaController(controller);
                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {

                        videoView.start();

                        // 첫화면 보이게
                        videoView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                controller.show(0);
                                videoView.pause();
                            }
                        },100);


                    }
                });

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

}
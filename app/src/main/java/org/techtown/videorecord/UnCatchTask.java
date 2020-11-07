package org.techtown.videorecord;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class  UnCatchTask extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("Error","onTaskRemoved - 강제 종료 ");
        Toast.makeText(this, "onTaskRemoved ", Toast.LENGTH_SHORT).show();
        stopSelf();
    }
}



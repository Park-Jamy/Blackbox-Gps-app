package org.techtown.blackbox;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class GPSLocationRequest extends StringRequest{
    // 서버 URL 설정 ( PHP 파일 연동 )
    final static private String URL = "http://34.64.132.117/gpsupdate.php";
    private Map<String, String> map;

    public GPSLocationRequest( String StartLat, String StartLong, String DestinationLat, String DestinationLong, String UserId,
                              Response.Listener<String> listener) {
        super(Request.Method.POST, URL, listener, null);

        map = new HashMap<>();

        map.put("StartLat",StartLat);
        map.put("StartLong",StartLong);
        map.put("DestinationLat",DestinationLat);
        map.put("DestinationLong",DestinationLong);
        map.put("UserId", UserId);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}

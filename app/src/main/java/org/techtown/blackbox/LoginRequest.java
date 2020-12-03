package org.techtown.blackbox;

import android.service.autofill.UserData;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class LoginRequest extends StringRequest {

    // 서버 URL 설정 ( PHP 파일 연동 )
    final static private String URL = "http://192.168.0.8:8091/Login";
    private Map<String, String> map;


    public LoginRequest(String UserId, String UserPwd, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        map = new HashMap<>();
        map.put("UserId",UserId);
        map.put("UserPwd", UserPwd);

    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}
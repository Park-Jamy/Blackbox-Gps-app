package org.techtown.blackbox;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class RegisterRequest extends StringRequest {

    // 서버 URL 설정 ( PHP 파일 연동 )
//    final static private String URL = "http://34.64.132.117/Register.php";
    final static private String URL = "http://192.168.0.8:8091/Register";

//          34.64.229.192
    private Map<String, String> map;


    public RegisterRequest(String UserDate ,String UserId, String UserPwd, String UserName, String UserPhone, String UserPName, String UserPPhone, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        map = new HashMap<>();
        map.put("UserDate",UserDate);
        map.put("UserId",UserId);
        map.put("UserPwd", UserPwd);
        map.put("UserName", UserName);
        map.put("UserPhone", UserPhone);
        map.put("UserPName", UserPName);
        map.put("UserPPhone", UserPPhone);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}
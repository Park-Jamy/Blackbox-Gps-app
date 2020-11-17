package org.techtown.blackbox;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import com.kakao.network.ApiErrorCode;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RegisterActivity extends AppCompatActivity {

    private EditText join_id, join_password, join_name, join_pwck;
    private EditText join_phone, join_pname, join_pphone;
    private Button join_button, check_button;
    private AlertDialog dialog;
    private boolean validate = false;
    long now = System.currentTimeMillis();


    String strAgeRange;

    @Override
    protected void onCreate(Bundle savedInstanceState) { // 액티비티 시작시 처음으로 실행되는 생명주기!
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 아이디 값 찾아주기
        join_id = findViewById( R.id.join_id );
        join_password = findViewById( R.id.join_password );
        join_name = findViewById( R.id.join_name );
        join_pwck = findViewById(R.id.join_pwck);
        join_phone = findViewById(R.id.join_phone);
        join_pname = findViewById(R.id.join_pname);
        join_pphone = findViewById(R.id.join_pphone);

        //데이터 받기
        Intent intent = getIntent();
        strAgeRange = intent.getStringExtra("ageRange");

        if (strAgeRange.equals("0~9") && strAgeRange.equals("10~19")) {
            UserManagement.getInstance().requestUnlink(new UnLinkResponseCallback() {
                @Override
                public void onFailure(ErrorResult errorResult) {
                    int result = errorResult.getErrorCode();

                    if(result == ApiErrorCode.CLIENT_ERROR_CODE) {
                        Toast.makeText(getApplicationContext(), "네트워크 연결이 불안정합니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "회원탈퇴에 실패했습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    Toast.makeText(getApplicationContext(), "로그인 세션이 닫혔습니다. 다시 로그인해 주세요.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, CertiActivity.class);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onNotSignedUp() {
                    Toast.makeText(getApplicationContext(), "가입되지 않은 계정입니다. 다시 로그인해 주세요.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, CertiActivity.class);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onSuccess(Long result) {
                    Toast.makeText(getApplicationContext(), "20살 미만 본인인증을 했습니다.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, CertiActivity.class);
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("20살 미만 본인인증을 했습니다.").setPositiveButton("확인", null).create();
                    dialog.show();
                    startActivity(intent);
                    finish();
                }
            });

        } else {
            UserManagement.getInstance().requestUnlink(new UnLinkResponseCallback() {
                @Override
                public void onFailure(ErrorResult errorResult) {
                    int result = errorResult.getErrorCode();

                    if (result == ApiErrorCode.CLIENT_ERROR_CODE) {
                        Toast.makeText(getApplicationContext(), "네트워크 연결이 불안정합니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "회원탈퇴에 실패했습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    Toast.makeText(getApplicationContext(), "로그인 세션이 닫혔습니다. 다시 로그인해 주세요.", Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onNotSignedUp() {
                    Toast.makeText(getApplicationContext(), "가입되지 않은 계정입니다. 다시 로그인해 주세요.", Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onSuccess(Long result) {
                    Toast.makeText(getApplicationContext(), "20살 미만 본인인증을 했습니다.", Toast.LENGTH_SHORT).show();

                }
            });
        }

        //아이디 중복 체크
        check_button = findViewById(R.id.check_button);
        check_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String UserId = join_id.getText().toString();
                if (validate) {
                    return; //검증 완료
                }

                if (UserId.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("아이디를 입력하세요.").setPositiveButton("확인", null).create();
                    dialog.show();
                    return;
                }

                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");

                            if (success) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                                dialog = builder.setMessage("사용할 수 있는 아이디입니다.").setPositiveButton("확인", null).create();
                                dialog.show();
                                join_id.setEnabled(false); //아이디값 고정
                                validate = true; //검증 완료
                                check_button.setBackgroundColor(getResources().getColor(R.color.colorGray));
                            }
                            else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                                dialog = builder.setMessage("이미 존재하는 아이디입니다.").setNegativeButton("확인", null).create();
                                dialog.show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                ValidateRequest validateRequest = new ValidateRequest(UserId, responseListener);
                RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
                queue.add(validateRequest);
            }
        });

        //회원가입 버튼 클릭 시 수행
        join_button = findViewById( R.id.join_button );
        join_button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String UserId = join_id.getText().toString();
                final String UserPwd = join_password.getText().toString();
                final String UserName = join_name.getText().toString();
                final String PassCk = join_pwck.getText().toString();
                final String UserPhone = join_phone.getText().toString();
                final String UserPName = join_pname.getText().toString();
                final String UserPPhone = join_pphone.getText().toString();

                // 현재시간을 date 변수에 저장한다.
                Date date = new Date(now);
                // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
                SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                // nowDate 변수에 값을 저장한다.
                String UserDate = sdfNow.format(date);

                //아이디 중복체크 했는지 확인
                if (!validate) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("중복된 아이디가 있는지 확인하세요.").setNegativeButton("확인", null).create();
                    dialog.show();
                    return;
                }

                //한 칸이라도 입력 안했을 경우
                if (UserId.equals("") || UserPwd.equals("") || UserName.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("모두 입력해주세요.").setNegativeButton("확인", null).create();
                    dialog.show();
                    return;
                }

                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject( response );
                            boolean success = jsonObject.getBoolean( "success" );

                            //회원가입 성공시
                            if(UserPwd.equals(PassCk)) {
                                if (success) {

                                    Toast.makeText(getApplicationContext(), String.format("%s님 가입을 환영합니다.", UserName), Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                    startActivity(intent);

                                    //회원가입 실패시
                                } else {
                                    Toast.makeText(getApplicationContext(), "회원가입에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                                dialog = builder.setMessage("비밀번호가 동일하지 않습니다.").setNegativeButton("확인", null).create();
                                dialog.show();
                                return;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                };

                //서버로 Volley를 이용해서 요청
                RegisterRequest registerRequest = new RegisterRequest(UserDate ,UserId, UserPwd, UserName, UserPhone, UserPName, UserPPhone, responseListener);
                GpsIdRequest gpsidRequest = new GpsIdRequest(UserId, responseListener);
                RequestQueue queue = Volley.newRequestQueue( RegisterActivity.this );
                queue.add( registerRequest );
                queue.add( gpsidRequest );

//                RequestQueue queue1 = Volley.newRequestQueue( RegisterActivity.this );
//                queue1.add( gpsidRequest );
            }
        });

    }
}
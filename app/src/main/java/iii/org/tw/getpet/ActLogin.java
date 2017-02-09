package iii.org.tw.getpet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.facebook.FacebookSdk;
import com.facebook.CallbackManager;
import com.facebook.AccessToken;
import com.facebook.*;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.accountkit.AccountKit;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import common.CDictionary;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ActLogin extends AppCompatActivity {

    CallbackManager callbackManager;
    AccessToken accessToken;

    OkHttpClient Iv_OkHttp_client = new OkHttpClient();
    public static final MediaType Iv_MTyp_JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_login);
        initComponent();

        //宣告callback Manager
        callbackManager = CallbackManager.Factory.create();

        //找到login button (facebook套件裡的登入按鈕元件)
        LoginButton btn_FBlogin = (LoginButton) findViewById(R.id.btn_FBlogin);
        btn_FBlogin.setReadPermissions(Arrays.asList("email"));   //要求存取使用者的email

        //LoginButton增加callback function
        btn_FBlogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {//成功登入
                //accessToken之後或許還會用到 先存起來
                accessToken = loginResult.getAccessToken();
                Log.d(CDictionary.Debug_TAG,"FB access token got.");
                Log.d(CDictionary.Debug_TAG,"Access token: "+accessToken.toString());

                //send request and call graph api
                GraphRequest request = GraphRequest.newMeRequest(
                        accessToken,
                        new GraphRequest.GraphJSONObjectCallback() {
                            //當RESPONSE回來的時候會取得JSON物件
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                //讀出姓名 ID FB個人頁面連結
                                Log.d(CDictionary.Debug_TAG,"FB get"+response);
                                Log.d(CDictionary.Debug_TAG,object.optString("name"));
                                Log.d(CDictionary.Debug_TAG,object.optString("link"));
                                Log.d(CDictionary.Debug_TAG,object.optString("id"));
                                Log.d(CDictionary.Debug_TAG,object.optString("email"));
                            }
                        });
                //包入想要得到的資料 送出request
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,link");
                request.setParameters(parameters);
                request.executeAsync();

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("ExternalAccessToken", accessToken.getToken());
                    Log.d(CDictionary.Debug_TAG,"GET JSON OBJ : "+jsonObject.optString("ExternalAccessToken"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d(CDictionary.Debug_TAG,"Get strJSONString: "+jsonObject.toString());

                RequestBody requestBody =  RequestBody.create(Iv_MTyp_JSON,jsonObject.toString());
                Request postRrequest = new Request.Builder()
                        .url("http://twpetanimal.ddns.net:9487/api/v1/Account/AddExternalLogin")
                        .addHeader("Content-Type","application/x-www-form-urlencoded")
                        .post(requestBody)
                        .build();

                Call call = Iv_OkHttp_client.newCall(postRrequest);
                call.enqueue(new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.d(CDictionary.Debug_TAG,"GET RESPONSE: "+response.body().toString());
                    }
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d(CDictionary.Debug_TAG,"POST FAIL......");
                    }
                });
                goMainScreen();
            }

            @Override
            public void onCancel() {//取消登入
                Log.d("FB","CANCEL");
            }

            @Override
            public void onError(FacebookException exception) {//登入失敗
                Log.d("FB",exception.toString());
            }
        });

        //Check if user is currently logged in
        if (AccessToken.getCurrentAccessToken() != null && com.facebook.Profile.getCurrentProfile() != null){
            //登入狀態下不給登出
            //btn_FBlogin.setVisibility(View.INVISIBLE);
        }
    }

    private void goMainScreen() {
        Intent intent = new Intent(ActLogin.this, ActHomePage.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode,resultCode,data);
    }

    View.OnClickListener btn_register_Click=new View.OnClickListener(){
        public void onClick(View arg0) {
            Intent intent = new Intent(ActLogin.this, ActRegister.class);
            startActivity(intent);
        }
    };

    View.OnClickListener btn_login_Click=new View.OnClickListener(){
        public void onClick(View arg0) {


        }
    };

    public void initComponent(){
        btn_register = (Button)findViewById(R.id.btn_register);
        btn_register.setOnClickListener(btn_register_Click);

        btn_login = (Button)findViewById(R.id.btn_login);
        btn_login.setOnClickListener(btn_login_Click);
    }

    Button btn_login, btn_register;

}

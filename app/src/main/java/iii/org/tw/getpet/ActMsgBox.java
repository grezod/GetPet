package iii.org.tw.getpet;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.ParsedRequestListener;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import adapter.MsgListAdapter;
import common.CDictionary;
import model.Category;
import model.CMessage;
import model.UserMsg;

public class ActMsgBox extends AppCompatActivity {
    static ActMsgBox iv_ActMsgBox;
    ArrayList<UserMsg> myDataset = new ArrayList<UserMsg>();
    private String access_token, Email, UserName,UserId, HasRegistered, LoginProvider;
    MsgListAdapter adapter;
    RecyclerView recyclerList;
    private ProgressDialog progressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_msg_box);

        //初始化元件
        iv_ActMsgBox = this;
        recyclerList = (RecyclerView) findViewById(R.id.msgboxlist_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerList.setLayoutManager(layoutManager);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });

        UserName = getSharedPreferences("userInfo",MODE_PRIVATE).getString(CDictionary.SK_username,"");
        Log.d(CDictionary.Debug_TAG,"GET USER NAME："+UserName);
        UserId = getSharedPreferences("userInfo",MODE_PRIVATE).getString(CDictionary.SK_userid,"");
        Log.d(CDictionary.Debug_TAG,"GET USER ID："+UserId);
        access_token = getSharedPreferences("userInfo",MODE_PRIVATE).getString(CDictionary.SK_token,"");
        Log.d(CDictionary.Debug_TAG,"GET USER TOKEN："+access_token);

        getDatafromServer();

    }

    public void getDatafromServer(){
        progressDialog = ProgressDialog.show(ActMsgBox.this, Html.fromHtml("<font color='#2d4b44'>資料讀取中, 請稍後...</font>"), "", true);
        String url = "http://twpetanimal.ddns.net:9487/api/v1/MsgUsers";
        url += "/"+UserId;
        url += "?$orderby=msgTime desc";
        Log.d(CDictionary.Debug_TAG,"GET URL："+url);
        //取回MSG資料存入集合
        AndroidNetworking.initialize(getApplicationContext());
        AndroidNetworking.get(url)
                .setTag(this)
                .setPriority(Priority.HIGH)
                .build()
                .getAsParsed(new TypeToken<ArrayList<UserMsg>>() {
                             },
                        new ParsedRequestListener<ArrayList<UserMsg>>() {
                            @Override
                            public void onResponse(ArrayList<UserMsg> response) {
                                String size = String.format("%d", response.size());
                                Log.d(CDictionary.Debug_TAG, size);
                                if (response.size() > 0) {
                                    for (UserMsg rs : response) {
                                        myDataset.add(rs);
                                        Log.d(CDictionary.Debug_TAG, "GET MSG: "+rs.getMsgID());
                                    }
                                    adapter = new MsgListAdapter(myDataset);
                                    recyclerList.setAdapter(adapter);
                                    progressDialog.dismiss();
                                } else {
                                    progressDialog.dismiss();
                                    AlertDialog.Builder dialog = new AlertDialog.Builder(ActMsgBox.this);
                                    dialog.setTitle(Html.fromHtml("<font color='#2d4b44'>目前尚無訊息</font>"));
                                    dialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            goMemberScreen();
                                        }
                                    });
                                    dialog.create().show();
                                }
                            }

                            @Override
                            public void onError(ANError anError) {
                                progressDialog.dismiss();
                                AlertDialog.Builder dialog = new AlertDialog.Builder(ActMsgBox.this);
                                dialog.setTitle(Html.fromHtml("<font color='#2d4b44'>連線錯誤, 請稍後再試</font>"));
                                dialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        goMemberScreen();
                                    }
                                });
                                dialog.create().show();
                            }
                        });
    }

    public void goMemberScreen(){
        Intent intent = new Intent(this,ActMember.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(CDictionary.Debug_TAG,"TEST ON PAUSE");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(CDictionary.Debug_TAG,"TEST ON RESUME");
        //getDatafromServer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_default, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_backtohome) {
            Intent intent = new Intent(this, ActHomePage.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}

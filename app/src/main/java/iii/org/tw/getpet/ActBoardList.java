package iii.org.tw.getpet;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.ParsedRequestListener;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

import adapter.BoardListAdapter;
import adapter.MsgListAdapter;
import common.CDictionary;
import model.Board;
import model.UserMsg;

public class ActBoardList extends AppCompatActivity {
    ArrayList<Board> myDataset = new ArrayList<Board>();
    private String access_token, Email, UserName,UserId, HasRegistered, LoginProvider;
    BoardListAdapter adapter;
    RecyclerView recyclerList;
    Intent intent;
    String animalid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_board_list);
        setTitle("留言板");
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        //初始化元件
        recyclerList = (RecyclerView) findViewById(R.id.boardlist_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerList.setLayoutManager(layoutManager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActBoardList.this,ActBoardInput.class);
                Bundle bundle = new Bundle();
                bundle.putString(CDictionary.BK_animalID,animalid);

                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        UserName = getSharedPreferences("userInfo",MODE_PRIVATE).getString(CDictionary.SK_username,"");
        Log.d(CDictionary.Debug_TAG,"GET USER NAME："+UserName);
        UserId = getSharedPreferences("userInfo",MODE_PRIVATE).getString(CDictionary.SK_userid,"");
        Log.d(CDictionary.Debug_TAG,"GET USER ID："+UserId);
        access_token = getSharedPreferences("userInfo",MODE_PRIVATE).getString(CDictionary.SK_token,"");
        Log.d(CDictionary.Debug_TAG,"GET USER TOKEN："+access_token);

        getDatafromServer();

    }

    public void getDatafromServer(){
        String url = "http://twpetanimal.ddns.net:9487/api/v1/boards?";
        intent = getIntent();
        animalid = intent.getExtras().getString(CDictionary.BK_animalID);
        url += "$filter=board_animalID eq "+animalid;
        Log.d(CDictionary.Debug_TAG,"GET URL："+url);
        //取回MSG資料存入集合
        AndroidNetworking.initialize(getApplicationContext());
        AndroidNetworking.get(url)
                .setTag(this)
                .setPriority(Priority.HIGH)
                .build()
                .getAsParsed(new TypeToken<ArrayList<Board>>() {
                             },
                        new ParsedRequestListener<ArrayList<Board>>() {
                            @Override
                            public void onResponse(ArrayList<Board> response) {
                                String size = String.format("%d", response.size());
                                Log.d(CDictionary.Debug_TAG, size);
                                if (response.size() > 0) {
                                    for (Board rs : response) {
                                        myDataset.add(rs);
                                    }
                                    Log.d(CDictionary.Debug_TAG, "SIZE: "+String.format("%d", myDataset.size()));
                                    adapter = new BoardListAdapter(myDataset);
                                    recyclerList.setAdapter(adapter);
                                } else {
                                    AlertDialog.Builder dialog = new AlertDialog.Builder(ActBoardList.this);
                                    dialog.setTitle("目前尚無留言");
                                    dialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                                    dialog.create().show();
                                }
                            }

                            @Override
                            public void onError(ANError anError) {

                            }
                        });
    }

}
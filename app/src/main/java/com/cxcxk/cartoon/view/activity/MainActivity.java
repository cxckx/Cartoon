package com.cxcxk.cartoon.view.activity;

import android.animation.AnimatorSet;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cxcxk.cartoon.R;
import com.example.cxcxk.android_my_library.utils.ConnectUtils;
import com.example.cxcxk.android_my_library.utils.JsonNetDataOperator;
import com.example.cxcxk.android_my_library.utils.JsonPostNetDataOperator;
import com.example.cxcxk.android_my_library.utils.NetDataOperater;
import com.example.cxcxk.android_my_library.view.BaseActivity;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cxcxk on 2016/9/20.
 */
public class MainActivity extends BaseActivity{

    private NetDataOperater<String> operater;
    private  ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activiy_main);
        operater = new JsonPostNetDataOperator();

        ImageView imageView = (ImageView) findViewById(R.id.image_launcher);

        Animation set = AnimationUtils.loadAnimation(this, R.anim.img_launcher_anim);

        imageView.startAnimation(set);



        set.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                final SharedPreferences preferences = getSharedPreferences("config",MODE_PRIVATE);
                dialog = new ProgressDialog(MainActivity.this);
                dialog.setMessage("程序正在获取授权...");

                String access_token = preferences.getString("access_token",null);
                Map<String,String> map = new HashMap<String, String>();
                long expire_in = preferences.getLong("expires_in",0);

                if(access_token == null) {
                    /**
                     * 第一次请求token
                     *
                     */
                    map.put("grant_type","client_credentials");

                    request(map);

                }else {
                    long mills = Calendar.getInstance().getTimeInMillis();
                    if(mills == expire_in){
                        map.put("grant_type","refresh_token");
                        map.put("refresh_token",access_token);
                        request(map);

                    }else {
                        /**
                         * 进入漫画主界面
                         */
                       /* if(!ConnectUtils.isConnect()) {
                            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                                    .setMessage("获取授权失败，请检查网络连接")
                                    .create();
                            alertDialog.show();
                        }*/
                    }
                }


                operater.setNetWorkListener(new NetDataOperater.INetWork<String>() {
                    @Override
                    public void OnCompleted(String s) {
                        dialog.dismiss();
                        JSONObject object = JSON.parseObject(s);
                        String access_token = object.getString("access_token");
                        long expires_in = object.getLong("expires_in");
                        Log.i("TAGGG", s);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("access_token", access_token);
                        editor.putLong("expires_in", expires_in + Calendar.getInstance().getTimeInMillis());
                        editor.commit();

                        /**
                         *   进入漫画主界面
                         */
                    }

                    @Override
                    public void OnError(String s) {
                        dialog.setMessage(s);
                    }

                    @Override
                    public void OnProgress(int i) {

                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }
    private void request(Map<String ,String> map){
        map.put("app_id","134008760000257069");
        map.put("app_secret", "58052c6f87f348d5f870f8d87969c6b1");
        NetDataOperater.Attribute attribute = new NetDataOperater.Attribute("https://oauth.api.189.cn/emp/oauth2/v3/access_token",
                map );

        if(ConnectUtils.isConnect()) {
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            operater.request(attribute, "1");
        }else {
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                    .setMessage("获取授权失败，请检查网络连接")
                    .create();
            alertDialog.show();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        operater.cancleRequest("1");
    }
}

package com.yt.kangaroo.libs.net.okhttp.listener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;

/**
 *@content: okhttp的Get/Post请求数据的接口回调,负责将解析完成的数据回调出去组装
 *@time:2018-12-12
 *@build:zhouqiang
 */

public interface HttpListener {
    void onReady(Call call);//准备
    void onFailure(Call call, Exception e);//失败
    void onResponse(Call call, int code, JSONObject data) throws JSONException;//成功回调
}

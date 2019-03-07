package com.yt.kangaroo.libs.net.okhttp.listener;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

/**
 *@content: okhttp的上传数据监听接口
 *@time:2018-12-12
 *@build:zhouqiang
 */

public interface HttpUpListener {
    void onFailure(Call call, IOException e);
    void onUpFile(long total, long current);
    void onResponse(Call call, Response response);
}

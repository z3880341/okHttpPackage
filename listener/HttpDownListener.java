package com.yt.kangaroo.libs.net.okhttp.listener;



import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

/**
 *@content: okhttp的下载数据监听接口
 *@time:2018-12-12
 *@build:zhouqiang
 */

public interface HttpDownListener {
    void onFailure(Call call, IOException e);
    void onResponse(Call call, Response response, long total, long current);
}

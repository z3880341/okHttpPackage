package com.yt.kangaroo.libs.net.okhttp.listener;

import com.yt.kangaroo.libs.net.okhttp.RequestBase;

import java.io.IOException;

/**
 *@content: 自定义POJO类的向外接口类（数据组装后的回调一个POJO）
 *@time:2018-12-12
 *@build:zhouqiang
 */

public interface ResponseDataListener<T> {
     void onReady();
     void onResponse(int code, T data);
     void onError(Exception e);
}

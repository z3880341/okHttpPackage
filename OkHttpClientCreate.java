package com.yt.kangaroo.libs.net.okhttp;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 *@content: okhttp配置客户端的参数，并且实现客户端的单例模式
 *@time:2018-12-12
 *@build:zhouqiang
 */

public class OkHttpClientCreate {
    private static final boolean IS_RETRY = false;//失败是否重连
    private static final int CONNECT_TIME = 10;//设置连接超时时间 单位:秒
    private static final int READ_TIME = 10;//设置读取超时时间
    private static final int WRITE_TIME = 10;//设置写入超时间
    private static OkHttpClient mOkHttpClient;
    public static OkHttpClient CreateClient(){
        if (mOkHttpClient == null){
            return mOkHttpClient = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(IS_RETRY)
                    .connectTimeout(CONNECT_TIME,TimeUnit.SECONDS)//连接超时
                    .readTimeout(READ_TIME,TimeUnit.SECONDS)//读取超时
                    .writeTimeout(WRITE_TIME,TimeUnit.SECONDS)//写入超时
//                    .callTimeout()//呼叫超时,设置此参数为整体流程请求的超时时间
//                    .addInterceptor() //设置拦截器
//                    .authenticator() //设置认证器
//                    .proxy()//设置代理
                    .build();
        }
        return mOkHttpClient;
    }

    public static void destroy(){
        mOkHttpClient = null;
    }

}

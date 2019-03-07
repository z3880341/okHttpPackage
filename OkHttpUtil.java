package com.yt.kangaroo.libs.net.okhttp;

import android.os.Handler;
import android.os.Looper;

import com.yt.kangaroo.data.SPDataSession;
import com.yt.kangaroo.data.TSPDataSession;
import com.yt.kangaroo.libs.net.okhttp.listener.HttpListener;
import com.yt.kangaroo.utils.L;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * @content: okhttp的get和post功能实现类
 * @time:2018-12-12
 * @build:zhouqiang
 */

public class OkHttpUtil {
    private static final String TAG = "OkHttpUtil";
    private RequestBase mRequestBase;
    private Call mCall;
    private Handler mHandler = new Handler(Looper.getMainLooper());


    public OkHttpUtil(RequestBase requestBase) throws Exception {
        mRequestBase = requestBase;//将传入的请求配置类缓存
        switch (requestBase.getRequestMethod()){
            case GET:
                getRequest(requestBase.getUrl(),requestBase.getMap(),requestBase.getListener());
                break;
            case POST:
                postRequest(requestBase.getUrl(),requestBase.getJsonObject(),requestBase.getListener());
                break;
            default:
                break;
        }
    }

    /**
     * get请求方法
     * @param url 网络地址
     * @param params get参数 没有参数可以为null
     * @param listener 接口回调
     */
    private void getRequest(final String url, final Map<String,String> params, final HttpListener listener){
        Request request = new Request.Builder()
                .get()
                .url(params == null || params.isEmpty() ? url : getUrlWithParams(url, params))//判断get请求的参数是否为null,如果不为null则组合参数
                .build();
        mCall = OkHttpClientCreate.CreateClient().newCall(request);//构建了一个完整的http请求
        mRequestBase.setCall(mCall);//将call传入给请求配置类,让配置类中的stop方法调用
        if (listener!=null){
            listener.onReady(mCall);
        }
        mCall.enqueue(new Callback() {//发送请求
            @Override
            public void onFailure(final Call call, final IOException e) {
                e.printStackTrace();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onFailure(call, e);
                        }

                    }
                });
            }
            @Override
            public void onResponse(final Call call, final Response response) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener == null) {
                            return;
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            final int code = jsonObject.getInt("code");
                            String message = jsonObject.getString("message");
                            if (jsonObject.isNull("data")){
                                listener.onResponse(call, code, null);
                                return;
                            }
                            final JSONObject data = jsonObject.getJSONObject("data");
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        listener.onResponse(call, code, data);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });
                        } catch (final JSONException e) {
                            e.printStackTrace();
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onFailure(call, e);
                                }
                            });

                        } catch (final IOException e) {
                            e.printStackTrace();
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onFailure(call, e);
                                }
                            });
                        } catch (final Exception e){
                            e.printStackTrace();
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onFailure(call, e);
                                }
                            });
                        }

                    }
                });
            }
        });
    }

    /**
     * post请求方法
     * @param url 网络地址
     * @param params post请求参数
     * @param listener 接口回调
     */
    private void postRequest(final String url, final JSONObject params, final HttpListener listener){
        final Request request = new Request.Builder()
                .url(url)
                .header("token", SPDataSession.I().getToken())
                .post(changeJSON(params))//使用change方法将JSON参数转成RequestBody
                .build();
        mCall = OkHttpClientCreate.CreateClient().newCall(request);
        mRequestBase.setCall(mCall);
        if (listener!=null){
            listener.onReady(mCall);
        }
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                e.printStackTrace();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onFailure(call, e);
                        }

                    }
                });
            }

            @Override
            public void onResponse(final Call call, final Response response) {
                if (listener == null) {
                    return;
                }
                try {
                    L.ee(TAG,"response="+request.toString());
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    L.ee(TAG,"回调数据="+jsonObject.toString());
                    final int code = jsonObject.getInt("code");
                    String message = jsonObject.getString("message");
                    if (jsonObject.isNull("data")){
                        listener.onResponse(call, code, null);
                        return;
                    }
                    final JSONObject data = jsonObject.getJSONObject("data");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                listener.onResponse(call, code, data);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });

                } catch (final JSONException e) {
                    e.printStackTrace();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFailure(call, e);
                        }
                    });

                } catch (final IOException e) {
                    e.printStackTrace();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFailure(call, e);
                        }
                    });
                } catch (final Exception e){
                    e.printStackTrace();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFailure(call, e);
                        }
                    });
                }

            }
        });

    }

    protected void destroy(){
        if (mCall != null){
            mCall.cancel();
        }
        if (mRequestBase != null){
            mRequestBase = null;
        }
    }

    /**
     * 转换Json参数为RequestBody
     * @param jsonParam json对象
     * @return RequestBody
     */
    private RequestBody changeJSON(JSONObject jsonParam){
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                ,String.valueOf(jsonParam));
        return requestBody;
    }


    /**
     * 講參數进行URLEncode编码转换
     * @param params
     * @return
     */
    private static Map<String, String> getURLEncodeParams(Map<String, String> params) {
        Map<String, String> map = new HashMap<String, String>();
        for (String key : params.keySet()) {
            String value = params.get(key);
            try {
                value = URLEncoder.encode(value, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            map.put(key, value);
        }
        return map;
    }

    /**
     * 将参数转换为字符串
     */
    private static String convertMapToString(Map<String, String> params) {
        StringBuffer content = new StringBuffer();
        List<String> keys = new ArrayList<>(params.keySet());
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);
            if (value != null) {
                content.append((i == 0 ? "" : "&") + key + "=" + value);
            } else {
                content.append((i == 0 ? "" : "&") + key + "=");
            }
        }
        return content.toString();
    }

    /**
     * 拼接url和参数，用于Get请求
     * @param url
     * @param params
     * @return
     */
    private static String getUrlWithParams(String url, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return url;
        }
        params = getURLEncodeParams(params);
        String paramsStr = convertMapToString(params);
        if (!url.endsWith("?")) {
            url += "?";
        }
        url += paramsStr;
        return url;
    }

}

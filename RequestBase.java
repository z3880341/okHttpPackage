package com.yt.kangaroo.libs.net.okhttp;

import android.text.TextUtils;

import com.yt.kangaroo.libs.net.okhttp.listener.HttpListener;
import com.yt.kangaroo.libs.net.okhttp.listener.ResponseDataListener;

import org.json.JSONObject;

import java.util.Map;

import androidx.annotation.NonNull;
import okhttp3.Call;


/**
 *@content: 请求的基类,所有请求都需要继承这个类
 *@time:2018-12-12
 *@build:zhouqiang
 */
public abstract class RequestBase {
    private String mUrl;
    private Call mCall;
    private HttpListener mListener;
    private OkHttpUtil mOkHttpUtil;
    private JSONObject mJsonObject;
    private ResponseDataListener mDatalistener;
    private Map<String,String> mMap;

    public RequestBase(@NonNull ResponseDataListener listener){
        mDatalistener = listener;
    }

    public abstract @NonNull String setUrl();
    public abstract @NonNull RequestMethod setRequestMethod();
    public abstract @NonNull HttpListener setListener();

    protected ResponseDataListener getDataListener(){
        return mDatalistener;
    }

    protected HttpListener getListener() {
        return mListener = setListener();
    }

    protected String getUrl() throws Exception {
        mUrl = setUrl();
        if (TextUtils.isEmpty(mUrl)){
            throw new Exception("异常 error Url is null");
        }
        return mUrl;
    }

    protected RequestMethod getRequestMethod(){
        return setRequestMethod();

    }

    protected void setCall(Call call){
        this.mCall = call;

    }

    protected Call getCall() throws Exception{
        if (mCall == null) {
            throw new Exception("异常 error Call is null");
        }
        return mCall;

    }

    public JSONObject getJsonObject() {
        return mJsonObject;
    }

    public Map<String,String> getMap(){
        return mMap;
    }

    public void startPost(JSONObject params) throws Exception {
        mJsonObject = params;
        mOkHttpUtil = new OkHttpUtil(this);

    }

    public void startGet(Map<String,String> params) throws Exception {
        mMap = params;
        mOkHttpUtil = new OkHttpUtil(this);

    }

    /**
     *  停止请求
     */
    public void stop(){
        try {
            if (getCall() != null) {
                getCall().cancel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 重新请求
     */
    public void againRequest(){
        try {
            if (getCall()!=null){
                getCall().execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 销毁请求
     */
    public void desroy(){
        mListener = null;
        if (mOkHttpUtil != null) {
            mOkHttpUtil.destroy();
        }
        mOkHttpUtil = null;
        mJsonObject = null;

    }

}

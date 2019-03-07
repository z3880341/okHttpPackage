package com.yt.kangaroo.libs.net.okhttp;

import android.util.Log;

import com.yt.kangaroo.data.SPDataSession;
import com.yt.kangaroo.data.TSPDataSession;
import com.yt.kangaroo.libs.net.okhttp.listener.HttpUpListener;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class OkHttpUpUtil {
    private static final String TAG = "OkHttpUpUtil";
    private String mUpUrl;
    private File mPath;
    private Call mCall;
    private Map<String,String> mParams;
    private long mAlreadyUpLength = 0;//已经上传长度
    private long mTotalLength = 0;//整体文件大小
    private int mSign = 0;
    private HttpUpListener mHttpUpListener;

    /**
     * post上传
     * @param upUrl
     * @param upFilePathAndName
     * @param params
     * @param listener
     */
    public void postUpRequest(final String upUrl, final File upFilePathAndName, final Map<String,String> params, final HttpUpListener listener){
        synchronized (this) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mSign = 1;
                    mUpUrl = upUrl;
                    mPath = upFilePathAndName;
                    mParams = params;
                    mHttpUpListener = listener;
                    mAlreadyUpLength = 0;
                    RequestBody requestBody = new RequestBody() {
                        @Override
                        public MediaType contentType() {
                            return null;
                        }

                        @Override
                        public void writeTo(BufferedSink sink) throws IOException {
                            RandomAccessFile randomAccessFile = new RandomAccessFile(mPath, "rw");
                            if (mTotalLength == 0) {
                                mTotalLength = randomAccessFile.length();
                            }
                            byte[] bytes = new byte[2048];
                            int len = 0;
                            try {
                                while ((len = randomAccessFile.read(bytes)) != -1) {
                                    sink.write(bytes, 0, len);
                                    mAlreadyUpLength = mAlreadyUpLength + len;
                                    if (mHttpUpListener != null) {
                                        mHttpUpListener.onUpFile(mTotalLength, mAlreadyUpLength);
                                    }
                                }
                            }catch (Exception e){
                                Log.e(TAG, "上传中断");
                            }finally {
                                randomAccessFile.close();//关闭流
                                Log.e(TAG, "流关闭");
                            }

                        }
                    };
//                MultipartBody multipartBody = new MultipartBody.Builder()
//                        .addPart(changeJSON(mJson))
//                        .addFormDataPart("file",mPath.getName(),requestBody)
//                        .build();
                    MultipartBody.Builder builder = new MultipartBody.Builder();
                    if (mParams!=null) {
                        Set<String> keys = mParams.keySet();
                        for (String key : keys) {
                            builder.addFormDataPart(key, mParams.get(key));
                        }
                    }
                    builder.addFormDataPart("file", mPath.getName(), requestBody);
                    MultipartBody multipartBody = builder.build();

                    Request request = new Request.Builder()
                            .url(mUpUrl)
                            .header("token", SPDataSession.I().getToken())
                            .post(multipartBody)
                            .build();
                    mCall = OkHttpClientCreate.CreateClient().newCall(request);
                    mCall.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            if (mHttpUpListener != null) {
                                mHttpUpListener.onFailure(call, e);
                            }
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (mHttpUpListener != null) {
                                mHttpUpListener.onResponse(call, response);
                            }

                        }
                    });

                }
            }).start();
        }

    }

    /**
     * post断点上传
     * @param upUrl
     * @param upFilePathAndName
     * @param params
     * @param listener
     */
    public void postRenewalUpRequest(final String upUrl, final File upFilePathAndName, final Map<String,String> params, final HttpUpListener listener){
        synchronized (this){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mSign = 2;
                    mUpUrl = upUrl;
                    mPath = upFilePathAndName;
                    mParams = params;
                    mHttpUpListener = listener;
                    RequestBody requestBody = new RequestBody() {
                        @Override
                        public MediaType contentType() {
                            return null;
                        }

                        @Override
                        public void writeTo(BufferedSink sink) throws IOException {
                            RandomAccessFile randomAccessFile = new RandomAccessFile(mPath, "rw");
                            if (mTotalLength == 0) {
                                mTotalLength = randomAccessFile.length();
                            }
                            if (mAlreadyUpLength != 0){
                                randomAccessFile.seek(mAlreadyUpLength);
                            }
                            byte[] bytes = new byte[2048];
                            int len = 0;
                            try {
                                while ((len = randomAccessFile.read(bytes)) != -1) {
                                    sink.write(bytes, 0, len);
                                    mAlreadyUpLength = mAlreadyUpLength + len;
                                    if (mHttpUpListener != null) {
                                        mHttpUpListener.onUpFile(mTotalLength, mAlreadyUpLength);
                                    }
                                }
                            }catch (Exception e){
                                Log.e(TAG, "上传中断");
                            }finally {
                                mAlreadyUpLength = randomAccessFile.getFilePointer();
                                randomAccessFile.close();//关闭流
                                Log.e(TAG, "流关闭");
                            }

                        }
                    };

                    MultipartBody.Builder builder = new MultipartBody.Builder();
                    if (mParams!=null) {
                        Set<String> keys = mParams.keySet();
                        for (String key : keys) {
                            builder.addFormDataPart(key, mParams.get(key));
                        }
                    }
                    builder.addFormDataPart("file", mPath.getName(), requestBody);
                    MultipartBody multipartBody = builder.build();

                    Request request = new Request.Builder()
                            .url(mUpUrl)
                            .header("token", SPDataSession.I().getToken())
                            .header("RANGE","bytes="+mAlreadyUpLength+"-"+mTotalLength)
                            .post(multipartBody)
                            .build();
                    mCall = OkHttpClientCreate.CreateClient().newCall(request);
                    mCall.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            if (mHttpUpListener != null) {
                                mHttpUpListener.onFailure(call, e);
                            }
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (mHttpUpListener != null) {
                                mHttpUpListener.onResponse(call, response);
                            }
                            mAlreadyUpLength = 0;
                            mTotalLength = 0;

                        }
                    });

                }
            }).start();
        }


    }

    /**
     * 恢复上传
     */
    public void resume(){
        if (mSign==0){
            return;
        }
        switch (mSign){
            case 1:
                postUpRequest(mUpUrl,mPath,mParams,mHttpUpListener);
                break;
            case 2:
                postRenewalUpRequest(mUpUrl,mPath,mParams,mHttpUpListener);
                break;
            default:
                break;
        }

    }

    /**
     * 暂停上传
     */
    public void stop(){
        if (mCall!=null){
            mCall.cancel();
        }

    }

    /**
     * 删除上传路径文件
     */
    public void deleteCurrentFile(){
        if (mPath == null){
            Log.e(TAG, "deleteCurrentFile error : 没有路径");
            return;
        }
        if (!mPath.exists()){
            Log.e(TAG, "deleteCurrentFile error: 文件不存在");
            return;
        }
        mPath.delete();
        mAlreadyUpLength = 0;
        mTotalLength = 0;
        mSign = 0;
    }

    /**
     * 销毁
     */
    public void destroy(){
        if (mCall!=null){
            mCall.cancel();
            mCall = null;
        }
        mSign = 0;
        mHttpUpListener = null;
        mPath = null;
        mHttpUpListener = null;
        mAlreadyUpLength = 0;
        mTotalLength = 0;
    }

    /**
     * 转换Json参数为RequestBody
     * @param jsonParam json对象
     * @return RequestBody
     */
    private RequestBody changeJSON(JSONObject jsonParam){
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , String.valueOf(jsonParam));
        return requestBody;
    }





}

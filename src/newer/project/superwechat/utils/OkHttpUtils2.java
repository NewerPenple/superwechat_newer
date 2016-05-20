package newer.project.superwechat.utils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yao on 2016/4/6.
 * OkHttp框架的二次封装
 * 具有以下功能：
 * 1、下载解析json数据，可根据指定的类型自动转换为相应的实体类或数组
 * 2、上传文件
 * 3、下载文件，当下载文件直接由服务端读写，则可以实现更新下载进度的效果。
 * 没有图片下载、二级缓存的功能。
 */
public class OkHttpUtils2<T> {
    static final String UTF_8 = "utf-8";
    //声明OkHttpClient
    private OkHttpClient mokHttpClient;
    OnCompleteListener<T> mListener;
    Request mRequest;
    Callback mCallback;
    /**
     * 服务端的地址
     */
    private StringBuilder mUrl;
    /**
     * 上传文件用的请求实体
     */
    private RequestBody mFileBody;
    private Class<T> mClzz;

    /**
     * 构造器
     */
    public OkHttpUtils2() {
        mUrl = new StringBuilder();
        mokHttpClient = new OkHttpClient();
        //处理服务端响应成功和失败的消息
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case RESULT_ERROR://响应失败
                        mListener.onError(msg.obj.toString());
                        break;
                    case RESULT_SUCCESS: {//响应成功
                        T result = (T) msg.obj;
                        mListener.onSuccess(result);
                        break;
                    }
                    case DOWNLOADING_START://文件下载的开始的事件
                    case DOWNLOADING_PERCENT://文件下载的进度更新的事件
                    case DOWNLOADING_FINISH://文件下载完成的事件
                        T result = (T) msg;
                        mListener.onSuccess(result);
                        break;
                }
            }
        };
    }

    /**
     * 添加服务端地址
     *
     * @param url:服务端根地址
     * @return OkHttpUtils2.this
     */
    public OkHttpUtils2<T> url(String url) {
        mUrl.append(url);
        return this;
    }

    /**
     * 添加get请求的参数
     *
     * @param key:请求的键
     * @param value：请求的值
     * @return OkHttpUtils2.this
     */
    public OkHttpUtils2<T> addParam(String key, String value) {
        try {
            if (mUrl.indexOf("?") == -1) {
                mUrl.append("?").append(key).append("=")
                        .append(URLEncoder.encode(value, UTF_8));
            } else {
                mUrl.append("&").append(key).append("=")
                        .append(URLEncoder.encode(value, UTF_8));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * 上传文件用：向表单中添加文件
     *
     * @param file：上传的文件
     * @return OkHttpUtils2.this
     */
    public OkHttpUtils2<T> addFile(File file) {
        mFileBody = RequestBody.create(null, file);
        return this;
    }

    /**
     * 设置服务端响应事件在工作线程的回调
     * 用于下载文件和其它非json解析的场景
     *
     * @param callback
     * @return
     */
    public OkHttpUtils2<T> doInBackground(Callback callback) {
        if (callback != null) {
            mCallback = callback;
        }
        return this;
    }

    /**
     * 在工作线程之前，在主线程中执行的代码
     *
     * @param runnable：在主线程中执行的代码块
     * @return
     */
    public OkHttpUtils2<T> onPreExecute(Runnable runnable) {
        if (runnable != null) {
            runnable.run();
        }
        return this;
    }

    /**
     * 设置服务端响应在主线程的回调
     * doinBackground之后调用
     *
     * @param listener
     * @return
     */
    public OkHttpUtils2<T> onPostExecute(OnCompleteListener<T> listener) {
        mListener = listener;
        return this;
    }

    /**
     * 设置json解析的目标类对象
     *
     * @param clzz:json解析的目标类对象
     * @return
     */
    public OkHttpUtils2<T> targetClass(Class<T> clzz) {
        mClzz = clzz;
        return this;
    }

    /**
     * 向服务端发送请求，用于解析json数据或上传文件
     */
    public void execute(OnCompleteListener<T> listener) {
        if (mListener == null && listener != null) {
            mListener = listener;
        }
        //创建OkHttp请求
        Request.Builder builder = new Request.Builder().url(this.mUrl.toString());
        Log.i("main", mUrl.toString());
        if (mFileBody != null) {//若文件表单非空，则创建上传文件的请求
            mRequest = builder.post(mFileBody).build();
        } else {//否则创建不带上传文件的请求
            mRequest = builder.build();
        }
        //创建包含请求的任务
        final Call call = mokHttpClient.newCall(mRequest);
        //将任务添加至队列，向服务端发送请求
        if (mCallback != null) { //若由调用处设置了服务端返回结果的处理代码
            call.enqueue(mCallback);
        } else { //否则，在框架内部使用缺省的处理代码，解析json数据
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    Message msg = Message.obtain();
                    msg.what = RESULT_ERROR;
                    msg.obj = e.getMessage();
                    mHandler.sendMessage(msg);
                    if (!call.isCanceled()) {
                        call.cancel();
                    }
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    try {
                        T obj;
                        if (mClzz != null) {//若有目标类对象，则解析
                            obj = parseJson(response.body().string(), mClzz);
                        } else {//下载文件场景，直接保存response实例
                            if (mFileBody == null) {//若上传文件为null，则是下载文件的场景
                                obj = (T) response;
                            } else {
                                //否则是上传文件，但使用者忘记获取服务端返回数据了
                                obj=null;
                            }
                        }
                        if (obj != null) {
                            Message msg = Message.obtain();
                            msg.what = RESULT_SUCCESS;
                            msg.obj = obj;
                            mHandler.sendMessage(msg);
                        } else {
                            Log.e("main", "上传文件请设置服务端返回json数据的解析类型");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }finally {
                        if (call != null) {
                            if (!call.isCanceled()) {
                                call.cancel();
                            }
                        }
                    }
                }
            });
        }
    }

    private T parseJson(String json, Class<T> clzz) {
        T t = new Gson().fromJson(json, clzz);
        return t;
    }

    /**
     * 事件处理接口
     *
     * @param <T>
     */
    public interface OnCompleteListener<T> {
        void onSuccess(T result);

        void onError(String error);
    }

    /**
     * 解析解析成功的消息
     */
    private static final int RESULT_ERROR = 0;
    /**
     * 解析解析失败的消息
     */
    private static final int RESULT_SUCCESS = 1;
    //下载进度的消息
    public static final int DOWNLOADING_PERCENT = 2;
    public static final int DOWNLOADING_START = 3;
    public static final int DOWNLOADING_FINISH = 4;
    /**
     * 处理主线程消息的Handler
     */
    static Handler mHandler;

    /**
     * 工作线程向主线程发送下载进度的消息
     *
     * @param percent:下载进度的百分比
     */
    public static void publish(int what,int percent) {
        final Message message = Message.obtain();
        message.what = what;
        message.arg1 = percent;
        mHandler.sendMessage(message);
    }

    /**
     * 发送Message消息
     *
     * @param msg
     */
    public static void sendMessage(Message msg) {
        mHandler.sendMessage(msg);
    }


    /**
     * 数组转换为ArrayList集合
     *
     * @param array
     * @param <T>
     * @return
     */
    public static <T> ArrayList<T> array2List(T[] array) {
        final List<T> list = Arrays.asList(array);
        return new ArrayList(list);
    }
}


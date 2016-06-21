package newer.project.fulicenter.task;

import android.content.Context;
import android.content.Intent;

import com.android.volley.Response;

import newer.project.fulicenter.FuliCenterApplication;
import newer.project.fulicenter.I;
import newer.project.fulicenter.activity.BaseActivity;
import newer.project.fulicenter.bean.MessageBean;
import newer.project.fulicenter.data.ApiParams;
import newer.project.fulicenter.data.GsonRequest;

public class DownloadCollectCountTask extends BaseActivity {
    private static final String TAG = DownloadCollectCountTask.class.toString();
    private Context context;
    private String username;
    private String path;

    public DownloadCollectCountTask(Context context) {
        this.context = context;
        this.username = FuliCenterApplication.getInstance().getUser().getMUserName();
        initPath();
    }

    public void initPath() {
        try {
            path = new ApiParams()
                    .with(I.User.USER_NAME, username)
                    .getRequestUrl(I.REQUEST_FIND_COLLECT_COUNT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute() {
        executeRequest(new GsonRequest<MessageBean>(path, MessageBean.class, responseDownloadCollectCountTask(), errorListener()));
    }

    private Response.Listener<MessageBean> responseDownloadCollectCountTask() {
        return new Response.Listener<MessageBean>() {
            @Override
            public void onResponse(MessageBean message) {
                if (message != null && message.isSuccess()) {
                    int count = Integer.parseInt(message.getMsg());
                    FuliCenterApplication.getInstance().setCollectCount(count);
                    context.sendStickyBroadcast(new Intent("update_collect_count"));
                }
            }
        };
    }
}
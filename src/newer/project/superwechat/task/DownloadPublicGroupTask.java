package newer.project.superwechat.task;

import android.content.Context;
import android.content.Intent;

import com.android.volley.Response;

import java.util.ArrayList;

import newer.project.superwechat.I;
import newer.project.superwechat.SuperWeChatApplication;
import newer.project.superwechat.activity.BaseActivity;
import newer.project.superwechat.bean.Group;
import newer.project.superwechat.data.ApiParams;
import newer.project.superwechat.data.GsonRequest;
import newer.project.superwechat.utils.Utils;

public class DownloadPublicGroupTask extends BaseActivity {
    private static final String TAG = DownloadPublicGroupTask.class.toString();
    private Context context;
    private String username;
    private int pageId;
    private int pageSize;
    private String path;

    public DownloadPublicGroupTask(Context context, String username, int pageId, int pageSize) {
        this.context = context;
        this.username = username;
        this.pageId = pageId;
        this.pageSize = pageSize;
    }

    public void initPath() {
        try {
            path = new ApiParams()
                    .with(I.User.USER_NAME, username)
                    .with(I.PAGE_ID, String.valueOf(pageId))
                    .with(I.PAGE_SIZE, String.valueOf(pageSize))
                    .getRequestUrl(I.REQUEST_FIND_PUBLIC_GROUPS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute() {
        executeRequest(new GsonRequest<Group[]>(path,Group[].class,responseDownloadPublicGroupTask(),errorListener()));
    }

    private Response.Listener<Group[]> responseDownloadPublicGroupTask() {
        return new Response.Listener<Group[]>() {
            @Override
            public void onResponse(Group[] contacts) {
                if (contacts != null) {
                    ArrayList<Group> publicGroupList = SuperWeChatApplication.getInstance().getPublicGroupList();
                    ArrayList<Group> list = Utils.array2List(contacts);
                    publicGroupList.clear();
                    publicGroupList.addAll(list);
                    sendStickyBroadcast(new Intent("update_public_group_list"));
                }
            }
        };
    }
}
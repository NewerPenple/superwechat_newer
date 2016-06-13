package newer.project.fulicenter.task;

import android.content.Context;
import android.content.Intent;

import com.android.volley.Response;

import java.util.ArrayList;

import newer.project.fulicenter.I;
import newer.project.fulicenter.SuperWeChatApplication;
import newer.project.fulicenter.activity.BaseActivity;
import newer.project.fulicenter.bean.Group;
import newer.project.fulicenter.data.ApiParams;
import newer.project.fulicenter.data.GsonRequest;
import newer.project.fulicenter.utils.Utils;

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
        initPath();
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
            public void onResponse(Group[] groups) {
                if (groups != null) {
                    ArrayList<Group> publicGroupList = SuperWeChatApplication.getInstance().getPublicGroupList();
                    ArrayList<Group> list = Utils.array2List(groups);
                    publicGroupList.clear();
                    publicGroupList.addAll(list);
                    context.sendStickyBroadcast(new Intent("update_public_group_list"));
                }
            }
        };
    }
}
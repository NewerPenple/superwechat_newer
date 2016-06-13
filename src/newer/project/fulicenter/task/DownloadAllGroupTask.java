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

public class DownloadAllGroupTask extends BaseActivity {
    private static final String TAG = DownloadAllGroupTask.class.toString();
    private Context context;
    private String username;
    private String path;

    public DownloadAllGroupTask(Context context, String username) {
        this.context = context;
        this.username = username;
        initPath();
    }

    public void initPath() {
        try {
            path = new ApiParams()
                    .with(I.User.USER_NAME, username)
                    .getRequestUrl(I.REQUEST_DOWNLOAD_GROUPS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute() {
        executeRequest(new GsonRequest<Group[]>(path,Group[].class,responseDownloadAllGroupTask(),errorListener()));
    }

    private Response.Listener<Group[]> responseDownloadAllGroupTask() {
        return new Response.Listener<Group[]>() {
            @Override
            public void onResponse(Group[] groups) {
                if (groups != null) {
                    ArrayList<Group> groupList = SuperWeChatApplication.getInstance().getGroupList();
                    ArrayList<Group> list = Utils.array2List(groups);
                    groupList.clear();
                    groupList.addAll(list);
                    context.sendStickyBroadcast(new Intent("update_group_list"));
                }
            }
        };
    }
}

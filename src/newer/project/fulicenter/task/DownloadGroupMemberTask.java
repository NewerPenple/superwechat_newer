package newer.project.fulicenter.task;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Response;

import java.util.ArrayList;
import java.util.HashMap;

import newer.project.fulicenter.I;
import newer.project.fulicenter.SuperWeChatApplication;
import newer.project.fulicenter.activity.BaseActivity;
import newer.project.fulicenter.bean.Member;
import newer.project.fulicenter.data.ApiParams;
import newer.project.fulicenter.data.GsonRequest;
import newer.project.fulicenter.utils.Utils;

public class DownloadGroupMemberTask extends BaseActivity {
    private static final String TAG = DownloadAllGroupTask.class.toString();
    private Context context;
    private String hxid;
    private String path;

    public DownloadGroupMemberTask(Context context, String hxid) {
        this.context = context;
        this.hxid = hxid;
        initPath();
    }

    public void initPath() {
        try {
            path = new ApiParams()
                    .with(I.Member.GROUP_ID, hxid)
                    .getRequestUrl(I.REQUEST_DOWNLOAD_GROUP_MEMBERS_BY_HXID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute() {
        executeRequest(new GsonRequest<Member[]>(path,Member[].class,responseDownloadAllGroupTask(),errorListener()));
    }

    private Response.Listener<Member[]> responseDownloadAllGroupTask() {
        return new Response.Listener<Member[]>() {
            @Override
            public void onResponse(Member[] members) {
                if (members != null) {
                    ArrayList<Member> list = Utils.array2List(members);
                    HashMap<String, ArrayList<Member>> groupMembers = SuperWeChatApplication.getInstance().getGroupMembers();
                    ArrayList<Member> memberList = groupMembers.get(hxid);
                    if (memberList != null) {
                        memberList.clear();
                        memberList.addAll(list);
                        Log.i("my", TAG + memberList.size());
                    } else {
                        Log.i("my", TAG + " put memberList");
                        groupMembers.put(hxid, list);
                    }
                    context.sendStickyBroadcast(new Intent("update_group_member_list"));
                } else {
                    Log.i("my", TAG + "members is null");
                }
            }
        };
    }
}